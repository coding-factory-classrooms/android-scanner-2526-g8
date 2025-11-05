package com.example.scanner.camera

import android.graphics.Bitmap
import android.util.Base64
import androidx.lifecycle.ViewModel
import com.example.scanner.Feature
import com.example.scanner.GoogleVisionAPI
import com.example.scanner.Image
import com.example.scanner.RequestItem
import com.example.scanner.VisionRequest
import com.example.scanner.VisionResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.ByteArrayOutputStream
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException


sealed class CameraUiState {
    data object Initial : CameraUiState()
    data object Loading : CameraUiState()
    data class Success(val message: String?) : CameraUiState()
    data class Error(val error: String) : CameraUiState()
}


class CameraViewModel : ViewModel() {
    val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl("https://vision.googleapis.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val api: GoogleVisionAPI = retrofit.create(GoogleVisionAPI::class.java)

    val uiStateFlow = MutableStateFlow<CameraUiState>(CameraUiState.Initial)

    fun getEncodedStringFromBitmap(bitmap: Bitmap): String {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
        val imageBytes = outputStream.toByteArray()
        return Base64.encodeToString(imageBytes, Base64.NO_WRAP)
    }

    suspend fun sendImageToAPI(bitmap: Bitmap) {
        val imageString = getEncodedStringFromBitmap(bitmap)

        val call: Call<VisionResponse> = api.detectText(
            "AIzaSyAoSwwDOVrguBX1NqH3N8ebzUkXr_gMamU",
            VisionRequest(
                requests = listOf(
                    RequestItem(
                        image = Image(content = imageString),
                        features = listOf(Feature(type = "TEXT_DETECTION"))
                    )
                )
            )
        )

        uiStateFlow.value = CameraUiState.Loading

        return suspendCancellableCoroutine { cont ->
            call.enqueue(object : Callback<VisionResponse> {
                override fun onResponse(call: Call<VisionResponse>, response: Response<VisionResponse>) {
                    if (response.isSuccessful) {
                        val msg = response.body()
                            ?.responses
                            ?.firstOrNull()
                            ?.textAnnotations
                            ?.firstOrNull()
                            ?.description ?: ""
                        uiStateFlow.value = CameraUiState.Success(msg)
                        cont.resume(msg)
                    } else {
                        val err = "HTTP ${response.code()}"
                        uiStateFlow.value = CameraUiState.Error(err)
                        cont.resumeWithException(Exception(err))
                    }
                }

                override fun onFailure(call: Call<VisionResponse>, t: Throwable) {
                    val errMsg = t.message ?: "Unknown error"
                    uiStateFlow.value = CameraUiState.Error("API call failed: $errMsg")
                    cont.resumeWithException(t)
                }
            })

            cont.invokeOnCancellation {
                call.cancel()
            }
        }
    }
}

package com.example.scanner.list

import android.graphics.Bitmap
import android.util.Base64
import androidx.lifecycle.ViewModel
import com.example.scanner.Feature
import com.example.scanner.GoogleVisionAPI
import com.example.scanner.Image
import com.example.scanner.Paper.PhotoRepository
import com.example.scanner.RequestItem
import com.example.scanner.VisionRequest
import com.example.scanner.VisionResponse
import kotlinx.coroutines.flow.MutableStateFlow
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.ByteArrayOutputStream

sealed class ListUiState {
    data object Initial : ListUiState()
    data object Loading : ListUiState()
    data class Success(val message: String?) : ListUiState()
    data class Error(val error: String) : ListUiState()
}

class ListViewModel : ViewModel() {

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl("https://vision.googleapis.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val api: GoogleVisionAPI = retrofit.create(GoogleVisionAPI::class.java)

    val uiStateFlow = MutableStateFlow<ListUiState>(ListUiState.Initial)

    // fonction pour sauvegarder le record dans le paper
    // ListViewModel.kt
    fun savePhotoRecord(
        imagePath: String,
        ocrText: String
    ): String {
        val rec = com.example.scanner.Paper.PhotoRepository.createFrom(
            imagePath = imagePath,
            ocrText = ocrText
        )
        return rec.id
    }


    private fun getEncodedStringFromBitmap(bitmap: Bitmap): String {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
        val imageBytes = outputStream.toByteArray()
        return Base64.encodeToString(imageBytes, Base64.NO_WRAP)
    }

    fun sendImageToAPI(bitmap: Bitmap) {
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

        uiStateFlow.value = ListUiState.Loading

        call.enqueue(object : Callback<VisionResponse> {
            override fun onResponse(call: Call<VisionResponse>, response: Response<VisionResponse>) {
                if (response.isSuccessful) {
                    val msg = response.body()
                        ?.responses
                        ?.firstOrNull()
                        ?.textAnnotations
                        ?.firstOrNull()
                        ?.description
                    uiStateFlow.value = ListUiState.Success(msg)
                } else {
                    uiStateFlow.value = ListUiState.Error("HTTP ${response.code()}")
                }
            }

            override fun onFailure(call: Call<VisionResponse>, t: Throwable) {
                uiStateFlow.value = ListUiState.Error("API call failed: ${t.message ?: "Unknown error"}")
            }
        })
    }
}

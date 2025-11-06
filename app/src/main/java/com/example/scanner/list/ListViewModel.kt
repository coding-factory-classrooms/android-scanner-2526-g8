package com.example.scanner.list

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import com.example.scanner.common.Feature
import com.example.scanner.common.GoogleVisionAPI
import com.example.scanner.common.Image
import com.example.scanner.common.RequestItem
import com.example.scanner.common.VisionRequest
import com.example.scanner.common.VisionResponse
import kotlinx.coroutines.flow.MutableStateFlow
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream
import java.util.Base64

data class SuccessContent(
    val message: String?, val bitmap: Bitmap
)

sealed class ListUiState {
    data object Initial : ListUiState()
    data object Loading : ListUiState()
    data class Success(val content: SuccessContent) : ListUiState()
    data class Error(val error: String) : ListUiState()
}

class ListViewModel : ViewModel() {
    lateinit var api: GoogleVisionAPI

    val uiStateFlow = MutableStateFlow<ListUiState>(ListUiState.Initial)

    private fun getEncodedStringFromBitmap(bitmap: Bitmap): String {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
        val imageBytes = outputStream.toByteArray()

        return Base64.getEncoder().encodeToString(imageBytes)
    }

    fun sendImageToAPI(bitmap: Bitmap) {
        val imageString = getEncodedStringFromBitmap(bitmap)

        val call = api.detectText(
            "AIzaSyAoSwwDOVrguBX1NqH3N8ebzUkXr_gMamU", VisionRequest(
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
            override fun onResponse(
                call: Call<VisionResponse>,
                response: Response<VisionResponse>
            ) {
                if (!response.isSuccessful) {
                    uiStateFlow.value = ListUiState.Error("HTTP ${response.code()}")
                    return
                }

                val msg =
                    response.body()?.responses?.firstOrNull()?.textAnnotations?.firstOrNull()?.description

                uiStateFlow.value =
                    ListUiState.Success(SuccessContent(bitmap = bitmap, message = msg))
            }

            override fun onFailure(call: Call<VisionResponse>, t: Throwable) {
                uiStateFlow.value = ListUiState.Error(t.message ?: "Unknown error")
            }
        })
    }
}

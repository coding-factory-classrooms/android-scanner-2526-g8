package com.example.scanner

import android.graphics.Bitmap
import com.example.scanner.common.GoogleVisionAPI
import com.example.scanner.common.OcrResult
import com.example.scanner.common.TextAnnotation
import com.example.scanner.common.VisionRequest
import com.example.scanner.common.VisionResponse
import com.example.scanner.list.ListUiState
import com.example.scanner.list.ListViewModel
import org.junit.Assert
import org.junit.Test
import org.mockito.Mockito
import retrofit2.Call
import retrofit2.mock.Calls

val sampleResponse: VisionResponse = VisionResponse(
    listOf(
        OcrResult(
            listOf(TextAnnotation("sample response text"))
        )
    )
)


class FakeGoogleVisionAPI : GoogleVisionAPI {
    override fun detectText(
        apiKey: String, body: VisionRequest
    ): Call<VisionResponse> {
        return Calls.response(sampleResponse)
    }
}

class ListViewModelTest {
    @Test
    fun `Valid response produces Success`() {
        // Arrange
        val viewModel = ListViewModel()
        viewModel.api = FakeGoogleVisionAPI()

        val bitmap = Mockito.mock(Bitmap::class.java)

        //Act
        viewModel.sendImageToAPI(bitmap)

        // Assert
        Assert.assertEquals(
            ListUiState.Success(sampleResponse.responses
                .first()
                .textAnnotations
                ?.first()
                ?.description),
            viewModel.uiStateFlow.value
        )
    }
}
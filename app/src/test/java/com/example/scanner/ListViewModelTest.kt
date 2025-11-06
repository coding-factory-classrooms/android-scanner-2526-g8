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
import org.mockito.Mockito.mock
import retrofit2.Call
import retrofit2.mock.Calls
import java.io.IOException

val sampleSuccessResponse: VisionResponse = VisionResponse(
    listOf(
        OcrResult(
            listOf(TextAnnotation("sample response text"))
        )
    )
)

val sampleFailureResponse = IOException("Error 500")


class FakeSuccessGoogleVisionAPI : GoogleVisionAPI {
    override fun detectText(
        apiKey: String, body: VisionRequest
    ): Call<VisionResponse> {
        return Calls.response(sampleSuccessResponse)
    }
}


class FakeFailureGoogleVisionAPI : GoogleVisionAPI {
    override fun detectText(
        apiKey: String, body: VisionRequest
    ): Call<VisionResponse> {
        return Calls.failure(sampleFailureResponse)
    }
}

class ListViewModelTest {
    @Test
    fun `Valid response produces Success state`() {
        // Arrange
        val viewModel = ListViewModel()
        val mockApi = mock<FakeSuccessGoogleVisionAPI>()

        viewModel.api = FakeSuccessGoogleVisionAPI()

        val bitmap = mock(Bitmap::class.java)

        //Act
        viewModel.sendImageToAPI(bitmap)

        // Assert
        Assert.assertEquals(
            ListUiState.Success(sampleSuccessResponse.responses
                .first()
                .textAnnotations
                ?.first()
                ?.description),
            viewModel.uiStateFlow.value
        )
    }

    @Test
    fun `Error response produces Error state`() {
        // Arrange
        val viewModel = ListViewModel()
        val mockApi = mock<FakeFailureGoogleVisionAPI>()

        viewModel.api = FakeFailureGoogleVisionAPI()

        val bitmap = mock(Bitmap::class.java)

        //Act
        viewModel.sendImageToAPI(bitmap)

        // Assert
        Assert.assertEquals(
            ListUiState.Error(sampleFailureResponse.message!!),
            viewModel.uiStateFlow.value
        )
    }
}
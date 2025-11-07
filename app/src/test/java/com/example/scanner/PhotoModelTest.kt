package com.example.scanner

import com.example.scanner.common.GoogleVisionAPI
import com.example.scanner.common.VisionRequest
import com.example.scanner.common.VisionResponse
import com.example.scanner.photo.PhotoModel
import com.example.scanner.photo.PhotoTest
import org.junit.Test
import retrofit2.Call
import retrofit2.mock.Calls

val samplePhoto = PhotoModel(
    id = "1",
    imagePath = "/path/to/image.jpg",
    text = "Sample extracted text",
    createdAtEpochMs = 1625077800000,
    isFavorite = false,
    targetLanguage = "fr",
    translatedText = "Texte extrait d'exemple"
)

class FakePaper: PhotoTest{
    override fun readIndex(): MutableList<String> {
        TODO("Not yet implemented")
    }

    override fun writeIndex(ids: List<String>) {
        TODO("Not yet implemented")
    }

    override fun createFrom(
        imagePath: String,
        ocrText: String
    ): PhotoModel {
        TODO("Not yet implemented")
    }

    override fun getAll(): List<PhotoModel> {
        TODO("Not yet implemented")
    }

    override fun get(id: String?): PhotoModel? {
        TODO("Not yet implemented")
    }

    override fun toggleFavorite(id: String) {
        TODO("Not yet implemented")
    }

    override fun updateTranslation(
        id: String,
        targetLanguage: String,
        translatedText: String
    ): Boolean {
        TODO("Not yet implemented")
    }

    override fun delete(id: String) {
        TODO("Not yet implemented")
    }

    override fun query(
        text: String?,
        onlyFavorites: Boolean
    ): List<PhotoModel> {
        TODO("Not yet implemented")
    }

}


class PhotoModelTest {
    @Test
    fun `can insert and get record after insert`() {
        val fakeRepo = FakePaper()
        // Insert the sample photo
        val createdPhoto = fakeRepo.createFrom(
            imagePath = samplePhoto.imagePath,
            ocrText = samplePhoto.text
        )

        // Retrieve the photo by ID
        val retrievedPhoto = fakeRepo.get(createdPhoto.id)

        // Assertions
        assert(retrievedPhoto != null)
        assert(retrievedPhoto?.id == createdPhoto.id)
        assert(retrievedPhoto?.imagePath == createdPhoto.imagePath)
        assert(retrievedPhoto?.text == createdPhoto.text)
    }
}
package com.example.scanner

import com.example.scanner.photo.PhotoModel
import com.example.scanner.photo.PhotoTest
import org.junit.Assert
import org.junit.Test
import java.util.UUID

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
    val storage = mutableListOf<PhotoModel>()

    override fun createFrom(
        imagePath: String,
        ocrText: String,
        targetLanguage: String,
        translatedText: String
    ): PhotoModel {
        val id = UUID.randomUUID().toString()
        val rec = PhotoModel(
            id = id,
            imagePath = imagePath,
            text = ocrText,
            createdAtEpochMs = System.currentTimeMillis(),
            targetLanguage = targetLanguage,
            translatedText = translatedText,
            isFavorite = false,
        )

        storage.add(rec)

        return rec
    }

    override fun getAll(): List<PhotoModel> {
        return storage
    }

    override fun get(id: String?): PhotoModel? {
        return storage.find { it.id == id }
    }

    override fun toggleFavorite(id: String) {
        val photo = storage.find { id == it.id } ?: return

        storage.remove(photo)

        photo.isFavorite = !photo.isFavorite

        storage.add(photo)
    }

    override fun delete(id: String) {
        storage.remove(storage.find { id == it.id })
    }

    override fun query(
        text: String?,
        onlyFavorites: Boolean
    ): List<PhotoModel> {
        val q = text?.trim().orEmpty()
        val hasText = q.isNotEmpty()

        return storage
            .filter { rec -> !onlyFavorites || rec.isFavorite }
            .filter { rec -> if (!hasText) true else rec.text.contains(q, ignoreCase = true) }
            .toList()
    }
}


class PhotoModelTest {
    @Test
    fun `can insert and get record after insert`() {
        // Arrange
        val fakeRepo = FakePaper()

        //Act
        val createdPhoto = fakeRepo.createFrom(
            imagePath = samplePhoto.imagePath,
            ocrText = samplePhoto.text,
            targetLanguage = samplePhoto.targetLanguage,
            translatedText = samplePhoto.translatedText
        )

        val retrievedPhoto = fakeRepo.get(createdPhoto.id)

        // Assert
        Assert.assertNotEquals(null, retrievedPhoto)
    }

    @Test
    fun `can insert and get record after insert then remove the record and get an empty list`() {
        // Arrange
        val fakeRepo = FakePaper()

        //Act
        val createdPhoto = fakeRepo.createFrom(
            imagePath = samplePhoto.imagePath,
            ocrText = samplePhoto.text,
            targetLanguage = samplePhoto.targetLanguage,
            translatedText = samplePhoto.translatedText
        )

        val retrievedPhoto = fakeRepo.get(createdPhoto.id)

        // Assert
        Assert.assertNotEquals(null, retrievedPhoto)

        // Act
        fakeRepo.delete(createdPhoto.id)

        // Assert
        Assert.assertEquals(emptyList<PhotoModel>(), fakeRepo.getAll())
    }

    @Test
    fun `toggle favorite make a record favorite`() {
        // Arrange
        val fakeRepo = FakePaper()

        //Act
        val createdPhoto = fakeRepo.createFrom(
            imagePath = samplePhoto.imagePath,
            ocrText = samplePhoto.text,
            targetLanguage = samplePhoto.targetLanguage,
            translatedText = samplePhoto.translatedText
        )

        fakeRepo.toggleFavorite(createdPhoto.id)

        val record = fakeRepo.get(createdPhoto.id)

        // Assert
        Assert.assertEquals(true, record?.isFavorite)
    }


    @Test
    fun `adding isFavorite or a text to query filters the records`() {
        // Arrange
        val fakeRepo = FakePaper()

        //Act
        val createdPhoto = fakeRepo.createFrom(
            imagePath = samplePhoto.imagePath,
            ocrText = samplePhoto.text,
            targetLanguage = samplePhoto.targetLanguage,
            translatedText = samplePhoto.translatedText
        )

        var data = fakeRepo.query()

        // Assert
        Assert.assertEquals(listOf(createdPhoto), data)

        // Act
        data = fakeRepo.query(onlyFavorites = true)

        // Assert
        Assert.assertEquals(emptyList<PhotoModel>(), data)


        // Act
        fakeRepo.toggleFavorite(createdPhoto.id)
        data = fakeRepo.query(onlyFavorites = true)

        createdPhoto.isFavorite = true

        // Assert
        Assert.assertEquals(listOf(createdPhoto), data)


        // Act
        fakeRepo.toggleFavorite(createdPhoto.id)
        data = fakeRepo.query(text = createdPhoto.text + "random text")

        // Assert
        Assert.assertEquals(emptyList<PhotoModel>(), data)


        // Act
        data = fakeRepo.query(text = createdPhoto.text.take(6))

        // Assert
        Assert.assertEquals(listOf(createdPhoto), data)

    }
}
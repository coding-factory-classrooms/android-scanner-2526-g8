package com.example.scanner

import com.example.scanner.common.PhotoModel
import com.example.scanner.photo.PaperBookInterface
import com.example.scanner.photo.PhotoRepository
import org.junit.Assert
import org.junit.Test

val samplePhoto = PhotoModel(
    id = "1",
    imagePath = "/path/to/image.jpg",
    text = "Sample extracted text",
    createdAtEpochMs = 1625077800000,
    isFavorite = false,
    targetLanguage = "fr",
    translatedText = "Texte extrait d'exemple"
)

class FakePaperBook : PaperBookInterface {
    val book = mutableMapOf<String, Any>()

    override fun <T> write(key: String, value: T) {
        book[key] = value as Any
    }

    override fun <T> read(key: String, defaultValue: T): T {
        @Suppress("UNCHECKED_CAST")
        return book[key] as T ?: defaultValue
    }
}


class PhotoModelTest {
    @Test
    fun `can insert and get record after insert`() {
        // Arrange
        val fakeBook = FakePaperBook()
        val repo = PhotoRepository(fakeBook)

        //Act
        val createdPhoto = repo.createFrom(
            imagePath = samplePhoto.imagePath,
            ocrText = samplePhoto.text,
            targetLanguage = samplePhoto.targetLanguage,
            translatedText = samplePhoto.translatedText
        )

        val retrievedPhoto = repo.get(createdPhoto.id)

        // Assert
        Assert.assertNotEquals(null, retrievedPhoto)
    }

    @Test
    fun `can insert and get record after insert then remove the record and get an empty list`() {
        // Arrange
        val fakeBook = FakePaperBook()
        val repo = PhotoRepository(fakeBook)

        //Act
        val createdPhoto = repo.createFrom(
            imagePath = samplePhoto.imagePath,
            ocrText = samplePhoto.text,
            targetLanguage = samplePhoto.targetLanguage,
            translatedText = samplePhoto.translatedText
        )

        val retrievedPhoto = repo.get(createdPhoto.id)

        // Assert
        Assert.assertNotEquals(null, retrievedPhoto)

        // Act
        repo.delete(createdPhoto.id)

        // Assert
        Assert.assertEquals(emptyList<PhotoModel>(), repo.getAll())
    }

    @Test
    fun `toggle favorite make a record favorite`() {
        // Arrange
        val fakeBook = FakePaperBook()
        val repo = PhotoRepository(fakeBook)

        //Act
        val createdPhoto = repo.createFrom(
            imagePath = samplePhoto.imagePath,
            ocrText = samplePhoto.text,
            targetLanguage = samplePhoto.targetLanguage,
            translatedText = samplePhoto.translatedText
        )

        repo.toggleFavorite(createdPhoto.id)

        val record = repo.get(createdPhoto.id)

        // Assert
        Assert.assertEquals(true, record?.isFavorite)
    }


    @Test
    fun `adding isFavorite or a text to query filters the records`() {
        // Arrange
        val fakeBook = FakePaperBook()
        val repo = PhotoRepository(fakeBook)

        //Act
        val createdPhoto = repo.createFrom(
            imagePath = samplePhoto.imagePath,
            ocrText = samplePhoto.text,
            targetLanguage = samplePhoto.targetLanguage,
            translatedText = samplePhoto.translatedText
        )

        var data = repo.query(text = null, onlyFavorites = false)

        // Assert
        Assert.assertEquals(listOf(createdPhoto), data)

        // Act
        data = repo.query(text = null, onlyFavorites = true)

        // Assert
        Assert.assertEquals(emptyList<PhotoModel>(), data)


        // Act
        repo.toggleFavorite(createdPhoto.id)
        data = repo.query(text = null, onlyFavorites = true)

        createdPhoto.isFavorite = true

        // Assert
        Assert.assertEquals(listOf(createdPhoto), data)


        // Act
        repo.toggleFavorite(createdPhoto.id)
        data = repo.query(text = createdPhoto.text + "random text", onlyFavorites = false)

        // Assert
        Assert.assertEquals(emptyList<PhotoModel>(), data)


        // Act
        data = repo.query(text = createdPhoto.text.take(6), onlyFavorites = false)

        // Assert
        Assert.assertEquals(listOf(createdPhoto), data)

    }
}
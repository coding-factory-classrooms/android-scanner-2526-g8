package com.example.scanner.photo

import io.paperdb.Paper
import java.util.UUID

class PhotoRepository(
    val book: PaperBookInterface
) {

    // sert à créer une nouvelle photo
    fun createFrom(
        imagePath: String, ocrText: String, targetLanguage: String, translatedText: String
    ): PhotoModel {
        val allPhotos = book.read("photos", emptyList<PhotoModel>()).toMutableList()

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

        allPhotos.add(rec)

        book.write("photos", allPhotos)

        return rec
    }

    // sert à récupérer toutes les photos triées par date décroissante
    fun getAll(): List<PhotoModel> {
        return book.read("photos", emptyList<PhotoModel>())
            .sortedByDescending { it.createdAtEpochMs }
    }

    // sert à récupérer une photo par son ID
    fun get(id: String?): PhotoModel? {
        if (id == null) return null

        return getAll().find { id == it.id }
    }

    // sert à basculer le statut favori d'une photo
    fun toggleFavorite(id: String) {
        val allPhotos = getAll().toMutableList()
        val photo = allPhotos.find { id == it.id } ?: return

        allPhotos.remove(photo)

        photo.isFavorite = !photo.isFavorite

        allPhotos.add(photo)

        book.write("photos", allPhotos)
    }


    // sert a supprimer une photo
    fun delete(id: String) {
        val allPhotos = getAll().toMutableList()
        allPhotos.remove(allPhotos.find { id == it.id })

        book.write("photos", allPhotos)
    }

    fun query(
        text: String?,
        onlyFavorites: Boolean,
    ): List<PhotoModel> {
        val q = text?.trim().orEmpty()
        val hasText = q.isNotEmpty()

        return getAll().asSequence()
            .filter { rec -> !onlyFavorites || rec.isFavorite }
            .filter { rec -> if (!hasText) true else rec.text.contains(q, ignoreCase = true) }
            .toList()
    }
}

data object PhotoObject {
    val repo = PhotoRepository(book = Paper.book() as PaperBookInterface)
}
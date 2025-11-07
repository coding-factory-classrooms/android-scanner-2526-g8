package com.example.scanner.photo

import io.paperdb.Paper
import java.util.UUID

class PhotoClassTest (): PhotoTest {

    // sert à créer une nouvelle photo
    override fun createFrom(
        imagePath: String,
        ocrText: String,
        targetLanguage: String,
        translatedText: String
    ): PhotoModel {
        val allPhotos = Paper.book()
            .read("photos", emptyList<PhotoModel>())!!
            .toMutableList()

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

        Paper.book().write("photos", allPhotos)

        return rec
    }

    // sert à récupérer toutes les photos triées par date décroissante
    override fun getAll(): List<PhotoModel> {
        return Paper.book()
            .read("photos", emptyList<PhotoModel>())!!
            .sortedByDescending { it.createdAtEpochMs }
    }

    // sert à récupérer une photo par son ID
    override fun get(id: String?): PhotoModel? {
        if (id == null) return null

        return getAll().find { id == it.id }
    }

    // sert à basculer le statut favori d'une photo
    override fun toggleFavorite(id: String) {
        val allPhotos = getAll().toMutableList()
        val photo = allPhotos.find { id == it.id } ?: return

        allPhotos.remove(photo)

        photo.isFavorite = !photo.isFavorite

        allPhotos.add(photo)

        Paper.book().write("photos", allPhotos)
    }


    // sert a supprimer une photo
    override fun delete(id: String) {
        val allPhotos = getAll().toMutableList()
        allPhotos.remove(allPhotos.find { id == it.id })

        Paper.book().write("photos", allPhotos)
    }

    override fun query(
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
    val repo = PhotoClassTest()
}
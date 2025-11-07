package com.example.scanner.photo

import io.paperdb.Paper
import java.io.File
import java.util.UUID

class PhotoClassTest: PhotoTest {
    private val BOOK = "photos"
    private val INDEX_KEY = "index"

    override fun readIndex(): MutableList<String> =
        (Paper.book(BOOK).read(INDEX_KEY, emptyList<String>()) ?: emptyList()).toMutableList()

    override fun writeIndex(ids: List<String>) {
        Paper.book(BOOK).write(INDEX_KEY, ids)
    }

    // sert à créer une nouvelle photo
    override fun createFrom(imagePath: String, ocrText: String): PhotoModel {
        val id = UUID.randomUUID().toString()
        val rec = PhotoModel(
            id = id,
            imagePath = imagePath,
            text = ocrText,
            createdAtEpochMs = System.currentTimeMillis(),
            isFavorite = false
        )
        val book = Paper.book(BOOK)
        book.write("photo:$id", rec)
        val idx = readIndex()
        idx.add(0, id)
        writeIndex(idx)

        return rec
    }

    // sert à récupérer toutes les photos triées par date décroissante
    override fun getAll(): List<PhotoModel> {
        return readIndex().mapNotNull { Paper.book(BOOK).read<PhotoModel>("photo:$it") }
            .sortedByDescending { it.createdAtEpochMs }
    }

    // sert à récupérer une photo par son ID
    override fun get(id: String?): PhotoModel? {
        if (id == null) return null

        return Paper.book(BOOK).read("photo:$id", null)
    }

    // sert à basculer le statut favori d'une photo
    override fun toggleFavorite(id: String) {
        val book = Paper.book(BOOK)
        val cur = book.read<PhotoModel>("photo:$id", null) ?: return
        book.write("photo:$id", cur.copy(isFavorite = !cur.isFavorite))
    }

    // sert à mettre à jour la traduction d'une photo
    override fun updateTranslation(
        id: String, targetLanguage: String, translatedText: String
    ): Boolean {
        val book = Paper.book(BOOK)
        val cur = book.read<PhotoModel>("photo:$id", null) ?: return false

        val lang = targetLanguage.lowercase() // normalise le code du language
        val updated = cur.copy(
            targetLanguage = lang, translatedText = translatedText

        )

        book.write("photo:$id", updated)
        return true
    }


    // sert a supprimer une photo
    override fun delete(id: String) {
        val book = Paper.book(BOOK)
        val cur = book.read<PhotoModel>("photo:$id", null) ?: return
        runCatching { File(cur.imagePath).takeIf { it.exists() }?.delete() }
        book.delete("photo:$id")
        val idx = readIndex()
        idx.remove(id)
        writeIndex(idx)
    }

    override fun query(
        text: String?,
        onlyFavorites: Boolean,
    ): List<PhotoModel> {
        val q = text?.trim().orEmpty()
        val hasText = q.isNotEmpty()

        return getAll().asSequence().filter { rec -> !onlyFavorites || rec.isFavorite }
            .filter { rec -> if (!hasText) true else rec.text.contains(q, ignoreCase = true) }
            .toList()
    }
}

data object PhotoObject {
    val repo = PhotoClassTest()
}
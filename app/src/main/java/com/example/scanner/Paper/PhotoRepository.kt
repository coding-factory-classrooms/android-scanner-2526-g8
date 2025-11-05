package com.example.scanner.Paper

import io.paperdb.Paper
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

object PhotoRepository {
    private const val BOOK = "photos"
    private const val INDEX_KEY = "index"
    private const val TIME_ZONE_EU = "Europe/Berlin"
    private val displayFmt = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())

    private fun readIndex(): MutableList<String> =
        (Paper.book(BOOK).read(INDEX_KEY, emptyList<String>()) ?: emptyList()).toMutableList()

    private fun writeIndex(ids: List<String>) {
        Paper.book(BOOK).write(INDEX_KEY, ids)
    }

    // sert à créer une nouvelle photo
    fun createFrom(imagePath: String, ocrText: String): PhotoRecord {
        val id = UUID.randomUUID().toString()
        val now = Calendar.getInstance(TimeZone.getTimeZone(TIME_ZONE_EU))
        val rec = PhotoRecord(
            id = id,
            imagePath = imagePath,
            text = ocrText,
            createdAtEpochMs = now.timeInMillis,
            createdAtDisplay = displayFmt.format(now.time),
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
    fun getAll(): List<PhotoRecord> =
        readIndex().mapNotNull { Paper.book(BOOK).read<PhotoRecord>("photo:$it", null) }
            .sortedByDescending { it.createdAtEpochMs }

    // sert à récupérer une photo par son ID
    fun get(id: String): PhotoRecord? = Paper.book(BOOK).read("photo:$id", null)

    // sert à basculer le statut favori d'une photo
    fun toggleFavorite(id: String) {
        val book = Paper.book(BOOK)
        val cur = book.read<PhotoRecord>("photo:$id", null) ?: return
        book.write("photo:$id", cur.copy(isFavorite = !cur.isFavorite))
    }

    // sert à mettre à jour la traduction d'une photo
    fun updateTranslation(
        id: String,
        targetLanguage: String,
        translatedText: String
    ): Boolean {
        val book = Paper.book(BOOK)
        val cur = book.read<PhotoRecord>("photo:$id", null) ?: return false

        val lang = targetLanguage.lowercase() // en mode si t'écris EN sa écris en car j'ai peurs que avec des api ou autre sa fait n'importe quoi
        val updated = cur.copy(
            targetLanguage = lang,
            translatedText = translatedText

        )

        book.write("photo:$id", updated)
        return true
    }


    // sert a supprimer une photo
    fun delete(id: String) {
        val book = Paper.book(BOOK)
        val cur = book.read<PhotoRecord>("photo:$id", null) ?: return
        runCatching { File(cur.imagePath).takeIf { it.exists() }?.delete() }
        book.delete("photo:$id")
        val idx = readIndex()
        idx.remove(id)
        writeIndex(idx)
    }
}

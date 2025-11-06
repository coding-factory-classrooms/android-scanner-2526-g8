package com.example.scanner.photo

// la classe pour les photos
data class PhotoModel(
    val id: String,
    val imagePath: String,
    val text: String,
    val createdAtEpochMs: Long,
    val isFavorite: Boolean = false,
    val targetLanguage: String? = null,
    var translatedText: String? = null
)
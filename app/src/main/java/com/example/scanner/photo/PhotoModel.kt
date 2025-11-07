package com.example.scanner.photo



// la classe pour les photos
data class PhotoModel(
    val id: String,
    val imagePath: String,
    val text: String,
    var createdAtEpochMs: Long,
    val targetLanguage: String,
    val translatedText: String,
    var isFavorite: Boolean = false
)
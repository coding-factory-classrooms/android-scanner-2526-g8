package com.example.scanner.Paper

// la classe pour paper
data class PhotoRecord(
    val id: String,
    val imagePath: String,
    val text: String,
    val createdAtEpochMs: Long,
    val createdAtDisplay: String,
    val isFavorite: Boolean = false,
    val targetLanguage: String? = null,
    val translatedText: String? = null
)
package com.example.scanner.common

data class VisionRequest(
    val requests: List<RequestItem>
)

data class RequestItem(
    val image: Image,
    val features: List<Feature>
)

data class Image(
    val content: String
)

data class Feature(
    val type: String = "TEXT_DETECTION"
)

data class VisionResponse(
    val responses: List<OcrResult>
)

data class OcrResult(
    val textAnnotations: List<TextAnnotation>?
)

data class TextAnnotation(
    val description: String
)


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
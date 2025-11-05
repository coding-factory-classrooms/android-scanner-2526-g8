package com.example.scanner

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
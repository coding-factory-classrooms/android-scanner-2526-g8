package com.example.scanner.photo


import io.paperdb.Paper
import java.io.File
import java.util.UUID

interface PhotoTest {

    fun readIndex(): MutableList<String>

    fun writeIndex(ids: List<String>)

    // sert à créer une nouvelle photo
    fun createFrom(imagePath: String, ocrText: String): PhotoModel

    // sert à récupérer toutes les photos triées par date décroissante
    fun getAll(): List<PhotoModel>

    // sert à récupérer une photo par son ID
    fun get(id: String?): PhotoModel?

    // sert à basculer le statut favori d'une photo
    fun toggleFavorite(id: String)

    // sert à mettre à jour la traduction d'une photo
    fun updateTranslation(
        id: String, targetLanguage: String, translatedText: String
    ): Boolean


    // sert a supprimer une photo
    fun delete(id: String)

    fun query(
        text: String? = null,
        onlyFavorites: Boolean = false,
    ): List<PhotoModel>
}
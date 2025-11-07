package com.example.scanner.photo


interface PaperBookInterface {
    fun <T> write(key: String, value: T)

    fun <T> read(key: String, defaultValue: T): T
}
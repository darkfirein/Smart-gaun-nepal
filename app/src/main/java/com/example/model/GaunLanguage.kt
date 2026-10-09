package com.example.model

enum class GaunLanguage(val code: String, val displayName: String, val nativeName: String) {
    NEPALI("ne", "Nepali", "नेपाली"),
    ENGLISH("en", "English", "English");

    companion object {
        fun fromCode(code: String): GaunLanguage {
            return entries.firstOrNull { it.code.equals(code, ignoreCase = true) } ?: NEPALI
        }
    }
}

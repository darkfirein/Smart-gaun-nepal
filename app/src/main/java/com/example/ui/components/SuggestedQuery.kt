package com.example.ui.components

import com.example.model.GaunLanguage

data class SuggestedQuery(
    val titleNe: String,
    val titleEn: String,
    val promptNe: String,
    val promptEn: String
) {
    fun getTitle(language: GaunLanguage): String {
        return if (language == GaunLanguage.NEPALI) titleNe else titleEn
    }

    fun getPrompt(language: GaunLanguage): String {
        return if (language == GaunLanguage.NEPALI) promptNe else promptEn
    }
}

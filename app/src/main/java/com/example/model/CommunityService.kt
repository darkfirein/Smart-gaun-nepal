package com.example.model

data class CommunityService(
    val id: String,
    val titleNe: String,
    val titleEn: String,
    val categoryNe: String,
    val categoryEn: String,
    val descriptionNe: String,
    val descriptionEn: String,
    val requiredDocumentsNe: List<String>,
    val requiredDocumentsEn: List<String>,
    val feeNe: String,
    val feeEn: String,
    val processingTimeNe: String,
    val processingTimeEn: String,
    val responsiblePersonNe: String = "वडा सचिव / अधिकृत",
    val responsiblePersonEn: String = "Ward Secretary / Officer",
    val downloadableFormUrl: String? = null
)

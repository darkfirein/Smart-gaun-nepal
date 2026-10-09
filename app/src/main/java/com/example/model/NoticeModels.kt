package com.example.model

enum class NoticeCategory(val id: String, val titleNe: String, val titleEn: String) {
    ALL("all", "सबै सूचना", "All Notices"),
    AGRICULTURE("agriculture", "कृषि तथा पशु", "Agriculture & Livestock"),
    HEALTH("health", "स्वास्थ्य तथा खोप", "Health & Vaccines"),
    CIVIC("civic", "प्रशासन तथा सिफारिस", "Civic & Governance"),
    DISASTER("disaster", "विपद् तथा मौसम", "Disaster & Weather"),
    EDUCATION("education", "शिक्षा तथा युवा", "Education & Youth"),
    COMMUNITY("community", "सामुदायिक कार्यक्रम", "Community Events");

    companion object {
        fun fromString(value: String): NoticeCategory {
            return entries.firstOrNull { it.id.equals(value, ignoreCase = true) || it.name.equals(value, ignoreCase = true) } ?: ALL
        }
    }
}

enum class NoticePriority(val value: String, val labelNe: String, val labelEn: String) {
    URGENT("urgent", "अत्यन्त जरुरी", "Urgent"),
    HIGH("high", "महत्वपूर्ण", "High Priority"),
    NORMAL("normal", "सामान्य सूचना", "Normal");

    companion object {
        fun fromString(value: String): NoticePriority {
            return entries.firstOrNull { it.value.equals(value, ignoreCase = true) || it.name.equals(value, ignoreCase = true) } ?: NORMAL
        }
    }
}

data class LocalNotice(
    val id: String,
    val title: String,
    val titleEn: String = "",
    val description: String,
    val descriptionEn: String = "",
    val dateIso: String,
    val timestamp: Long = System.currentTimeMillis(),
    val category: NoticeCategory,
    val locationId: String = "",
    val locationName: String = "",
    val wardNumber: Int? = null,
    val priority: NoticePriority = NoticePriority.NORMAL,
    val publisher: String = "गाउँपालिका कार्यालय",
    val isDemoTemplate: Boolean = false,
    val sourceName: String? = null,
    val authority: String? = null,
    val updatedAt: Long? = null,
    val verifiedAt: Long? = null,
    val verifiedBy: String? = null
) {
    fun getDisplayTitle(language: GaunLanguage): String {
        return if (language == GaunLanguage.ENGLISH && titleEn.isNotBlank()) titleEn else title
    }

    fun getDisplayDescription(language: GaunLanguage): String {
        return if (language == GaunLanguage.ENGLISH && descriptionEn.isNotBlank()) descriptionEn else description
    }

    fun getDisplayLocation(language: GaunLanguage): String {
        val wardText = if (wardNumber != null && wardNumber > 0) {
            if (language == GaunLanguage.NEPALI) " (वडा नं $wardNumber)" else " (Ward $wardNumber)"
        } else ""
        return "$locationName$wardText"
    }

    fun toFirestoreMap(): Map<String, Any?> {
        return mapOf(
            "title" to title,
            "titleEn" to titleEn,
            "description" to description,
            "descriptionEn" to descriptionEn,
            "dateIso" to dateIso,
            "timestamp" to timestamp,
            "category" to category.id,
            "locationId" to locationId,
            "locationName" to locationName,
            "wardNumber" to wardNumber,
            "priority" to priority.value,
            "publisher" to publisher,
            "sourceName" to sourceName,
            "authority" to authority,
            "updatedAt" to updatedAt,
            "verifiedAt" to verifiedAt,
            "verifiedBy" to verifiedBy
        )
    }

    companion object {
        fun fromFirestoreMap(id: String, map: Map<String, Any?>): LocalNotice {
            return LocalNotice(
                id = id,
                title = map["title"] as? String ?: "",
                titleEn = map["titleEn"] as? String ?: "",
                description = map["description"] as? String ?: "",
                descriptionEn = map["descriptionEn"] as? String ?: "",
                dateIso = map["dateIso"] as? String ?: "",
                timestamp = (map["timestamp"] as? Number)?.toLong() ?: System.currentTimeMillis(),
                category = NoticeCategory.fromString(map["category"] as? String ?: ""),
                locationId = map["locationId"] as? String ?: "",
                locationName = map["locationName"] as? String ?: "",
                wardNumber = (map["wardNumber"] as? Number)?.toInt(),
                priority = NoticePriority.fromString(map["priority"] as? String ?: ""),
                publisher = map["publisher"] as? String ?: "गाउँपालिका",
                sourceName = map["sourceName"] as? String,
                authority = map["authority"] as? String,
                updatedAt = (map["updatedAt"] as? Number)?.toLong(),
                verifiedAt = (map["verifiedAt"] as? Number)?.toLong(),
                verifiedBy = map["verifiedBy"] as? String
            )
        }
    }
}

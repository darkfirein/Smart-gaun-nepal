package com.example.model

data class EmergencyContact(
    val id: String,
    val nameNe: String,
    val nameEn: String,
    val designationNe: String,
    val designationEn: String,
    val contactNumber: String,
    val secondaryContact: String? = null,
    val locationName: String = "",
    val wardNumber: Int? = null,
    val isPrimaryHelpline: Boolean = false,
    val sourceName: String? = null,
    val authority: String? = null,
    val updatedAt: Long? = null,
    val verifiedAt: Long? = null,
    val verifiedBy: String? = null
) {
    fun toFirestoreMap(): Map<String, Any?> {
        return mapOf(
            "nameNe" to nameNe,
            "nameEn" to nameEn,
            "designationNe" to designationNe,
            "designationEn" to designationEn,
            "contactNumber" to contactNumber,
            "secondaryContact" to secondaryContact,
            "locationName" to locationName,
            "wardNumber" to wardNumber,
            "isPrimaryHelpline" to isPrimaryHelpline,
            "sourceName" to sourceName,
            "authority" to authority,
            "updatedAt" to updatedAt,
            "verifiedAt" to verifiedAt,
            "verifiedBy" to verifiedBy
        )
    }

    companion object {
        fun fromFirestoreMap(id: String, map: Map<String, Any?>): EmergencyContact {
            return EmergencyContact(
                id = id,
                nameNe = map["nameNe"] as? String ?: "",
                nameEn = map["nameEn"] as? String ?: "",
                designationNe = map["designationNe"] as? String ?: "",
                designationEn = map["designationEn"] as? String ?: "",
                contactNumber = map["contactNumber"] as? String ?: "",
                secondaryContact = map["secondaryContact"] as? String,
                locationName = map["locationName"] as? String ?: "",
                wardNumber = (map["wardNumber"] as? Number)?.toInt(),
                isPrimaryHelpline = map["isPrimaryHelpline"] as? Boolean ?: false,
                sourceName = map["sourceName"] as? String,
                authority = map["authority"] as? String,
                updatedAt = (map["updatedAt"] as? Number)?.toLong(),
                verifiedAt = (map["verifiedAt"] as? Number)?.toLong(),
                verifiedBy = map["verifiedBy"] as? String
            )
        }
    }
}

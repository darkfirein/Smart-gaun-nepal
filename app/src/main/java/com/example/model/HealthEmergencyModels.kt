package com.example.model

data class HealthFacility(
    val id: String,
    val nameNe: String,
    val nameEn: String,
    val typeNe: String,
    val typeEn: String,
    val locationName: String,
    val wardNumber: Int? = null,
    val contactNumber: String,
    val is24Hours: Boolean = false,
    val distanceKm: Double? = null,
    val doctorAvailable: Boolean = false,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val servicesAvailableNe: List<String> = emptyList(),
    val servicesAvailableEn: List<String> = emptyList(),
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
            "typeNe" to typeNe,
            "typeEn" to typeEn,
            "locationName" to locationName,
            "wardNumber" to wardNumber,
            "contactNumber" to contactNumber,
            "is24Hours" to is24Hours,
            "doctorAvailable" to doctorAvailable,
            "latitude" to latitude,
            "longitude" to longitude,
            "sourceName" to sourceName,
            "authority" to authority,
            "updatedAt" to updatedAt,
            "verifiedAt" to verifiedAt,
            "verifiedBy" to verifiedBy
        )
    }

    companion object {
        fun fromFirestoreMap(id: String, map: Map<String, Any?>): HealthFacility {
            return HealthFacility(
                id = id,
                nameNe = map["nameNe"] as? String ?: "",
                nameEn = map["nameEn"] as? String ?: "",
                typeNe = map["typeNe"] as? String ?: "स्वास्थ्य चौकी",
                typeEn = map["typeEn"] as? String ?: "Health Post",
                locationName = map["locationName"] as? String ?: "",
                wardNumber = (map["wardNumber"] as? Number)?.toInt(),
                contactNumber = map["contactNumber"] as? String ?: "",
                is24Hours = map["is24Hours"] as? Boolean ?: false,
                doctorAvailable = map["doctorAvailable"] as? Boolean ?: false,
                latitude = (map["latitude"] as? Number)?.toDouble(),
                longitude = (map["longitude"] as? Number)?.toDouble(),
                sourceName = map["sourceName"] as? String,
                authority = map["authority"] as? String,
                updatedAt = (map["updatedAt"] as? Number)?.toLong(),
                verifiedAt = (map["verifiedAt"] as? Number)?.toLong(),
                verifiedBy = map["verifiedBy"] as? String
            )
        }
    }
}

data class AmbulanceServiceInfo(
    val id: String,
    val providerNameNe: String,
    val providerNameEn: String,
    val contactNumber: String,
    val secondaryContact: String? = null,
    val baseStationNe: String,
    val baseStationEn: String,
    val isAvailableNow: Boolean = true
)

data class EmergencyServiceNumber(
    val id: String,
    val titleNe: String,
    val titleEn: String,
    val number: String,
    val isUrgentSos: Boolean = false,
    val descriptionNe: String = "",
    val descriptionEn: String = ""
)

data class BasicHealthGuideline(
    val id: String,
    val titleNe: String,
    val titleEn: String,
    val summaryNe: String,
    val summaryEn: String,
    val stepsNe: List<String>,
    val stepsEn: List<String>,
    val warningNe: String = "",
    val warningEn: String = ""
)

sealed interface HealthFacilitiesUiState {
    data object Loading : HealthFacilitiesUiState
    data class Success(
        val facilities: List<HealthFacility>,
        val ambulances: List<AmbulanceServiceInfo> = emptyList(),
        val isFromOfflineCache: Boolean = false
    ) : HealthFacilitiesUiState
    data class Error(
        val messageNe: String,
        val messageEn: String,
        val cachedFacilities: List<HealthFacility> = emptyList()
    ) : HealthFacilitiesUiState
}

package com.example.model

enum class LocalLevelType(val code: String, val titleNe: String, val titleEn: String) {
    METROPOLITAN("METROPOLITAN", "महानगरपालिका", "Metropolitan City"),
    SUB_METROPOLITAN("SUB_METROPOLITAN", "उपमहानगरपालिका", "Sub-Metropolitan City"),
    MUNICIPALITY("MUNICIPALITY", "नगरपालिका", "Municipality"),
    RURAL_MUNICIPALITY("RURAL_MUNICIPALITY", "गाउँपालिका", "Rural Municipality");

    companion object {
        fun fromString(str: String): LocalLevelType {
            return entries.firstOrNull { it.code.equals(str, ignoreCase = true) || it.name.equals(str, ignoreCase = true) }
                ?: RURAL_MUNICIPALITY
        }
    }
}

data class GaunLocation(
    val id: String,
    val provinceId: String,
    val provinceNe: String,
    val provinceEn: String,
    val districtId: String,
    val districtNe: String,
    val districtEn: String,
    val municipalityId: String,
    val nameNe: String,
    val nameEn: String,
    val wardNumber: Int? = null,
    val localLevelType: LocalLevelType = LocalLevelType.RURAL_MUNICIPALITY,
    val isGpsDetected: Boolean = false,
    val latitude: Double? = null,
    val longitude: Double? = null
) {
    fun getDisplayName(language: GaunLanguage): String {
        val wardSuffix = if (wardNumber != null && wardNumber > 0) {
            if (language == GaunLanguage.NEPALI) " - वडा $wardNumber" else " - Ward $wardNumber"
        } else ""
        return if (language == GaunLanguage.NEPALI) {
            "$nameNe$wardSuffix, $districtNe"
        } else {
            "$nameEn$wardSuffix, $districtEn"
        }
    }

    fun getFullRegion(language: GaunLanguage): String {
        val gpsBadge = if (isGpsDetected) {
            if (language == GaunLanguage.NEPALI) " • GPS स्थान" else " • GPS Location"
        } else ""
        return if (language == GaunLanguage.NEPALI) {
            "$provinceNe प्रदेश$gpsBadge"
        } else {
            "$provinceEn Province$gpsBadge"
        }
    }
}

data class ProvinceData(
    val id: String,
    val code: Int = 0,
    val nameNe: String,
    val nameEn: String,
    val sortOrder: Int = 0,
    val districts: List<DistrictData> = emptyList(),
    val aliases: List<String> = emptyList()
)

data class DistrictData(
    val id: String,
    val provinceId: String = "",
    val code: Int = 0,
    val nameNe: String,
    val nameEn: String,
    val sortOrder: Int = 0,
    val municipalities: List<MunicipalityData> = emptyList(),
    val aliases: List<String> = emptyList()
)

data class MunicipalityData(
    val id: String,
    val nameNe: String,
    val nameEn: String,
    val isRural: Boolean = true,
    val maxWards: Int = 9,
    val districtId: String = "",
    val provinceId: String = "",
    val code: Int = 0,
    val type: LocalLevelType = if (isRural) LocalLevelType.RURAL_MUNICIPALITY else LocalLevelType.MUNICIPALITY,
    val wardCount: Int = maxWards,
    val sortOrder: Int = 0,
    val aliases: List<String> = emptyList()
)

data class WardData(
    val id: String,
    val localLevelId: String,
    val districtId: String,
    val provinceId: String,
    val wardNumber: Int,
    val nameNe: String = "वडा नं. $wardNumber",
    val nameEn: String = "Ward No. $wardNumber"
)

data class AdministrativeRecordCounts(
    val provinces: Int,
    val districts: Int,
    val localLevels: Int,
    val wards: Int,
    val metropolitan: Int,
    val subMetropolitan: Int,
    val municipality: Int,
    val ruralMunicipality: Int
)

data class AdministrativeDataVersion(
    val datasetVersion: String,
    val source: String,
    val sourceDate: String,
    val importedAt: String,
    val recordCounts: AdministrativeRecordCounts
)

data class AdministrativeDataMetadata(
    val datasetVersion: String,
    val sourceName: String,
    val sourceUrls: List<String>,
    val verificationDate: String,
    val verifiedBySystem: String,
    val provinceCount: Int = 7,
    val districtCount: Int = 77,
    val localLevelCount: Int = 753,
    val wardCount: Int = 6743,
    val metropolitanCount: Int = 6,
    val subMetropolitanCount: Int = 11,
    val municipalityCount: Int = 276,
    val ruralMunicipalityCount: Int = 460
)

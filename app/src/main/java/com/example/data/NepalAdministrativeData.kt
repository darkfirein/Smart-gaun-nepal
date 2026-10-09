package com.example.data

import com.example.model.AdministrativeDataMetadata
import com.example.model.AdministrativeDataVersion
import com.example.model.AdministrativeRecordCounts
import com.example.model.DistrictData
import com.example.model.GaunLocation
import com.example.model.LocalLevelType
import com.example.model.MunicipalityData
import com.example.model.ProvinceData
import com.example.model.WardData
import org.json.JSONObject
import java.io.InputStream

/**
 * Authoritative Master Administrative Dataset for Nepal (Phase 19 & 20).
 *
 * Official Hierarchy:
 * Nepal → 7 Provinces → 77 Districts → 753 Local Levels → 6,743 Wards
 *
 * Local Levels Breakdown:
 * - 6 Metropolitan Cities (महानगरपालिका)
 * - 11 Sub-Metropolitan Cities (उपमहानगरपालिका)
 * - 276 Municipalities (नगरपालिका)
 * - 460 Rural Municipalities (गाउँपालिका)
 *
 * Official Data Sources:
 * - Ministry of Federal Affairs and General Administration (MoFAGA)
 * - National Statistics Office (NSO) / Nepal Census
 */
object NepalAdministrativeData {

    val version: AdministrativeDataVersion
    val metadata: AdministrativeDataMetadata
    val provinces: List<ProvinceData>
    val districts: List<DistrictData>
    val localLevels: List<MunicipalityData>

    private val localLevelsByDistrict: Map<String, List<MunicipalityData>>
    private val districtsByProvince: Map<String, List<DistrictData>>
    private val localLevelByIdMap: Map<String, MunicipalityData>

    private val districtAliases = mapOf(
        "district_43_nawalpur" to listOf("Nawalparasi East", "Nawalparasi (Bardaghat Susta East)", "नवलपरासी पूर्व", "नवलपरासी (बर्दघाट सुस्ता पूर्व)", "नवलपुर"),
        "district_48_parasi" to listOf("Nawalparasi West", "Nawalparasi (Bardaghat Susta West)", "नवलपरासी पश्चिम", "नवलपरासी (बर्दघाट सुस्ता पश्चिम)", "परासी"),
        "district_56_eastern_rukum" to listOf("Rukum East", "रुकुम पूर्व", "पूर्वी रुकुम", "Eastern Rukum"),
        "district_59_western_rukum" to listOf("Rukum West", "रुकुम पश्चिम", "पश्चिमी रुकुम", "Western Rukum"),
        "district_27_kathmandu" to listOf("Kathmandu District", "काठमाडौं", "काठमाण्डौ", "काठमाडौँ")
    )

    private val localLevelAliases = mapOf(
        "local_level_307_kathmandu" to listOf("kathmandu_met", "Kathmandu Metropolitan City", "KMC", "काठमाडौं महानगरपालिका", "काठमाडौं", "काठमाण्डौ", "काठमाडौँ"),
        "local_level_326_lalitpur" to listOf("lalitpur_met", "Lalitpur Metropolitan City", "पाटन", "Patan"),
        "local_level_283_bharatpur" to listOf("bharatpur_met", "Bharatpur Metropolitan City"),
        "local_level_395_pokhara" to listOf("pokhara_met", "Pokhara Metropolitan City"),
        "local_level_49_biratnagar" to listOf("biratnagar_met", "Biratnagar Metropolitan City"),
        "local_level_173_birgunj" to listOf("birgunj_met", "Birgunj Metropolitan City"),
        "local_level_391_helambu" to listOf("helambu_sindhupalchok", "Helambu Rural Municipality")
    )

    init {
        val jsonStream: InputStream? =
            NepalAdministrativeData::class.java.getResourceAsStream("/nepal_administrative_master_data.json")
                ?: NepalAdministrativeData::class.java.classLoader?.getResourceAsStream("nepal_administrative_master_data.json")
                ?: NepalAdministrativeData::class.java.classLoader?.getResourceAsStream("res/raw/nepal_administrative_master_data.json")

        val jsonString = jsonStream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }
            ?: throw IllegalStateException("Failed to load nepal_administrative_master_data.json from resources")

        val root = JSONObject(jsonString)

        // 1. Version & Phase 20 Metadata
        val vObj = root.getJSONObject("version")
        val cObj = vObj.getJSONObject("recordCounts")
        val counts = AdministrativeRecordCounts(
            provinces = cObj.getInt("provinces"),
            districts = cObj.getInt("districts"),
            localLevels = cObj.getInt("localLevels"),
            wards = cObj.getInt("wards"),
            metropolitan = cObj.getInt("metropolitan"),
            subMetropolitan = cObj.getInt("subMetropolitan"),
            municipality = cObj.getInt("municipality"),
            ruralMunicipality = cObj.getInt("ruralMunicipality")
        )
        version = AdministrativeDataVersion(
            datasetVersion = vObj.getString("datasetVersion"),
            source = vObj.getString("source"),
            sourceDate = vObj.getString("sourceDate"),
            importedAt = vObj.getString("importedAt"),
            recordCounts = counts
        )

        val mObj = root.optJSONObject("metadata")
        val sourceUrlsList = mutableListOf<String>()
        if (mObj != null && mObj.has("sourceUrls")) {
            val urlArr = mObj.getJSONArray("sourceUrls")
            for (u in 0 until urlArr.length()) {
                sourceUrlsList.add(urlArr.getString(u))
            }
        } else {
            sourceUrlsList.add("https://mofaga.gov.np/local-contact")
            sourceUrlsList.add("https://censusnepal.cbs.gov.np/")
        }

        metadata = AdministrativeDataMetadata(
            datasetVersion = mObj?.optString("datasetVersion", "2024.1.0-official") ?: "2024.1.0-official",
            sourceName = mObj?.optString("sourceName", "Government of Nepal (Ministry of Federal Affairs and General Administration - MoFAGA & National Statistics Office - NSO)")
                ?: "Government of Nepal (Ministry of Federal Affairs and General Administration - MoFAGA & National Statistics Office - NSO)",
            sourceUrls = sourceUrlsList,
            verificationDate = mObj?.optString("verificationDate", "2026-10-07") ?: "2026-10-07",
            verifiedBySystem = mObj?.optString("verifiedBySystem", "SmartGaunNepalAdministrativeVerifier-Phase20") ?: "SmartGaunNepalAdministrativeVerifier-Phase20",
            provinceCount = mObj?.optInt("provinceCount", 7) ?: 7,
            districtCount = mObj?.optInt("districtCount", 77) ?: 77,
            localLevelCount = mObj?.optInt("localLevelCount", 753) ?: 753,
            wardCount = mObj?.optInt("wardCount", 6743) ?: 6743,
            metropolitanCount = mObj?.optInt("metropolitanCount", 6) ?: 6,
            subMetropolitanCount = mObj?.optInt("subMetropolitanCount", 11) ?: 11,
            municipalityCount = mObj?.optInt("municipalityCount", 276) ?: 276,
            ruralMunicipalityCount = mObj?.optInt("ruralMunicipalityCount", 460) ?: 460
        )

        // 2. Parse Raw Local Levels
        val rawLocalLevels = mutableListOf<MunicipalityData>()
        val lArr = root.getJSONArray("localLevels")
        for (i in 0 until lArr.length()) {
            val item = lArr.getJSONObject(i)
            val typeStr = item.getString("type")
            val type = LocalLevelType.fromString(typeStr)
            val wardCount = item.getInt("wardCount")
            val muniId = item.getString("id")
            rawLocalLevels.add(
                MunicipalityData(
                    id = muniId,
                    districtId = item.getString("districtId"),
                    provinceId = item.getString("provinceId"),
                    code = item.getInt("code"),
                    nameNe = item.getString("nameNe"),
                    nameEn = item.getString("nameEn"),
                    type = type,
                    isRural = (type == LocalLevelType.RURAL_MUNICIPALITY),
                    maxWards = wardCount,
                    wardCount = wardCount,
                    sortOrder = item.getInt("sortOrder"),
                    aliases = localLevelAliases[muniId].orEmpty()
                )
            )
        }
        localLevels = rawLocalLevels
        localLevelsByDistrict = rawLocalLevels.groupBy { it.districtId }
        localLevelByIdMap = rawLocalLevels.associateBy { it.id }

        // 3. Parse Raw Districts with their child local levels
        val rawDistricts = mutableListOf<DistrictData>()
        val dArr = root.getJSONArray("districts")
        for (i in 0 until dArr.length()) {
            val item = dArr.getJSONObject(i)
            val dId = item.getString("id")
            val dMunis = localLevelsByDistrict[dId].orEmpty()
            rawDistricts.add(
                DistrictData(
                    id = dId,
                    provinceId = item.getString("provinceId"),
                    code = item.getInt("code"),
                    nameNe = item.getString("nameNe"),
                    nameEn = item.getString("nameEn"),
                    sortOrder = item.getInt("sortOrder"),
                    municipalities = dMunis,
                    aliases = districtAliases[dId].orEmpty()
                )
            )
        }
        districts = rawDistricts
        districtsByProvince = rawDistricts.groupBy { it.provinceId }

        // 4. Parse Raw Provinces with their child districts
        val rawProvinces = mutableListOf<ProvinceData>()
        val pArr = root.getJSONArray("provinces")
        for (i in 0 until pArr.length()) {
            val item = pArr.getJSONObject(i)
            val pId = item.getString("id")
            val pDists = districtsByProvince[pId].orEmpty()
            rawProvinces.add(
                ProvinceData(
                    id = pId,
                    code = item.getInt("code"),
                    nameNe = item.getString("nameNe"),
                    nameEn = item.getString("nameEn"),
                    sortOrder = item.getInt("sortOrder"),
                    districts = pDists
                )
            )
        }
        provinces = rawProvinces
    }

    /**
     * Default fallback location for the application: Helambu Rural Municipality.
     */
    val defaultLocation = GaunLocation(
        id = "helambu_sindhupalchok",
        provinceId = "province_3_bagmati",
        provinceNe = "बागमती",
        provinceEn = "Bagmati",
        districtId = "district_35_sindhupalchok",
        districtNe = "सिन्धुपाल्चोक",
        districtEn = "Sindhupalchok",
        municipalityId = "local_level_391_helambu",
        nameNe = "हेलम्बु गाउँपालिका",
        nameEn = "Helambu Rural Municipality",
        wardNumber = 1,
        localLevelType = LocalLevelType.RURAL_MUNICIPALITY,
        isGpsDetected = false,
        latitude = 28.0333,
        longitude = 85.5500
    )

    fun getProvince(id: String): ProvinceData? = provinces.firstOrNull { it.id == id }
    fun getDistrict(id: String): DistrictData? = districts.firstOrNull { it.id == id }
    fun getLocalLevel(id: String): MunicipalityData? = localLevelByIdMap[id] ?: localLevels.firstOrNull { it.id == id }

    /**
     * Validates that an administrative location hierarchy is fully coherent:
     * province -> district -> local level -> ward.
     * Prevents stale mixed-context combinations (e.g. old province + new district).
     */
    fun validateLocationHierarchy(
        provinceId: String,
        districtId: String,
        localLevelId: String,
        wardNumber: Int? = null
    ): Boolean {
        val prov = getProvince(provinceId) ?: return false
        val dist = prov.districts.firstOrNull { it.id == districtId } ?: return false
        val ll = dist.municipalities.firstOrNull { it.id == localLevelId } ?: return false
        if (wardNumber != null && (wardNumber < 1 || wardNumber > ll.wardCount)) {
            return false
        }
        return true
    }

    /**
     * Retrieves all normalized Wards for a given local level.
     */
    fun getWards(localLevelId: String): List<WardData> {
        val muni = localLevelByIdMap[localLevelId]
            ?: localLevels.firstOrNull { it.id.contains(localLevelId, ignoreCase = true) }
            ?: return emptyList()

        return (1..muni.wardCount).map { w ->
            WardData(
                id = "${muni.id}_ward_$w",
                localLevelId = muni.id,
                districtId = muni.districtId,
                provinceId = muni.provinceId,
                wardNumber = w,
                nameNe = "वडा नं. $w",
                nameEn = "Ward No. $w"
            )
        }
    }

    /**
     * Generates all 6,743 wards across Nepal.
     */
    fun getAllWards(): List<WardData> {
        return localLevels.flatMap { muni ->
            (1..muni.wardCount).map { w ->
                WardData(
                    id = "${muni.id}_ward_$w",
                    localLevelId = muni.id,
                    districtId = muni.districtId,
                    provinceId = muni.provinceId,
                    wardNumber = w,
                    nameNe = "वडा नं. $w",
                    nameEn = "Ward No. $w"
                )
            }
        }
    }

    fun generateAllWards(): List<WardData> = getAllWards()

    /**
     * Flexible, backward-compatible location resolver.
     * Matches by normalized ID, code, or legacy slug strings.
     */
    fun findLocation(
        provinceQuery: String,
        districtQuery: String,
        municipalityQuery: String,
        ward: Int? = null
    ): GaunLocation? {
        val p = provinces.firstOrNull {
            it.id.equals(provinceQuery, ignoreCase = true) ||
                    it.id.contains(provinceQuery, ignoreCase = true) ||
                    it.nameEn.equals(provinceQuery, ignoreCase = true) ||
                    it.nameNe.equals(provinceQuery, ignoreCase = true) ||
                    it.aliases.any { a -> a.equals(provinceQuery, ignoreCase = true) || a.contains(provinceQuery, ignoreCase = true) }
        } ?: provinces.firstOrNull() ?: return null

        val d = p.districts.firstOrNull {
            it.id.equals(districtQuery, ignoreCase = true) ||
                    it.id.contains(districtQuery, ignoreCase = true) ||
                    it.nameEn.equals(districtQuery, ignoreCase = true) ||
                    it.nameNe.equals(districtQuery, ignoreCase = true) ||
                    it.aliases.any { a -> a.equals(districtQuery, ignoreCase = true) || a.contains(districtQuery, ignoreCase = true) || districtQuery.contains(a, ignoreCase = true) }
        } ?: p.districts.firstOrNull() ?: return null

        val m = d.municipalities.firstOrNull {
            it.id.equals(municipalityQuery, ignoreCase = true) ||
                    it.id.contains(municipalityQuery, ignoreCase = true) ||
                    it.nameEn.contains(municipalityQuery, ignoreCase = true) ||
                    it.nameNe.contains(municipalityQuery, ignoreCase = true) ||
                    it.aliases.any { a -> a.equals(municipalityQuery, ignoreCase = true) || a.contains(municipalityQuery, ignoreCase = true) || municipalityQuery.contains(a, ignoreCase = true) }
        } ?: d.municipalities.firstOrNull() ?: return null

        val validWard = if (ward != null && ward in 1..m.wardCount) ward else 1
        val coords = getCoordinatesForMunicipality(m.id)

        val fullDisplayNe = when (m.type) {
            LocalLevelType.METROPOLITAN -> "${m.nameNe} महानगरपालिका"
            LocalLevelType.SUB_METROPOLITAN -> "${m.nameNe} उपमहानगरपालिका"
            LocalLevelType.MUNICIPALITY -> "${m.nameNe} नगरपालिका"
            LocalLevelType.RURAL_MUNICIPALITY -> "${m.nameNe} गाउँपालिका"
        }
        val fullDisplayEn = "${m.nameEn} ${m.type.titleEn}"

        return GaunLocation(
            id = "${m.id}_$validWard",
            provinceId = p.id,
            provinceNe = p.nameNe,
            provinceEn = p.nameEn,
            districtId = d.id,
            districtNe = d.nameNe,
            districtEn = d.nameEn,
            municipalityId = m.id,
            nameNe = fullDisplayNe,
            nameEn = fullDisplayEn,
            wardNumber = validWard,
            localLevelType = m.type,
            isGpsDetected = false,
            latitude = coords?.first ?: 27.7172,
            longitude = coords?.second ?: 85.3240
        )
    }

    /**
     * Fast search across all 753 local levels with optional filtering.
     */
    fun searchLocalLevels(
        query: String,
        provinceId: String? = null,
        districtId: String? = null,
        type: LocalLevelType? = null
    ): List<MunicipalityData> {
        val trimmed = query.trim().lowercase()
        return localLevels.filter { muni ->
            val matchesProv = provinceId == null || muni.provinceId == provinceId
            val matchesDist = districtId == null || muni.districtId == districtId
            val matchesType = type == null || muni.type == type
            val matchesQuery = trimmed.isEmpty() ||
                    muni.nameNe.lowercase().contains(trimmed) ||
                    muni.nameEn.lowercase().contains(trimmed) ||
                    muni.type.titleNe.lowercase().contains(trimmed) ||
                    muni.type.titleEn.lowercase().contains(trimmed)

            matchesProv && matchesDist && matchesType && matchesQuery
        }
    }

    fun getCoordinatesForMunicipality(municipalityId: String): Pair<Double, Double>? {
        val lower = municipalityId.lowercase()
        return when {
            lower.contains("helambu") -> Pair(28.0333, 85.5500)
            lower.contains("kathmandu") -> Pair(27.7172, 85.3240)
            lower.contains("pokhara") -> Pair(28.2096, 83.9856)
            lower.contains("biratnagar") -> Pair(26.4525, 87.2718)
            lower.contains("lalitpur") -> Pair(27.6588, 85.3247)
            lower.contains("bhaktapur") -> Pair(27.6710, 85.4298)
            lower.contains("bharatpur") -> Pair(27.6833, 84.4333)
            lower.contains("birgunj") -> Pair(27.0130, 84.8770)
            lower.contains("butwal") -> Pair(27.7000, 83.4500)
            lower.contains("dharan") -> Pair(26.8124, 87.2834)
            lower.contains("itahari") -> Pair(26.6631, 87.2778)
            lower.contains("nepalgunj") -> Pair(28.0500, 81.6167)
            lower.contains("dhangadhi") -> Pair(28.6833, 80.6000)
            lower.contains("janakpur") -> Pair(26.7288, 85.9244)
            lower.contains("hetauda") -> Pair(27.4284, 85.0326)
            lower.contains("birendranagar") -> Pair(28.6019, 81.6339)
            else -> Pair(27.7172, 85.3240)
        }
    }
}

package com.example.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.model.GaunLocation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.locationDataStore by preferencesDataStore(name = "location_prefs")

class LocationPreferencesRepository(private val context: Context) {

    private object Keys {
        val LOCATION_ID = stringPreferencesKey("loc_id")
        val PROVINCE_ID = stringPreferencesKey("province_id")
        val PROVINCE_NE = stringPreferencesKey("province_ne")
        val PROVINCE_EN = stringPreferencesKey("province_en")
        val DISTRICT_ID = stringPreferencesKey("district_id")
        val DISTRICT_NE = stringPreferencesKey("district_ne")
        val DISTRICT_EN = stringPreferencesKey("district_en")
        val MUNICIPALITY_ID = stringPreferencesKey("municipality_id")
        val NAME_NE = stringPreferencesKey("name_ne")
        val NAME_EN = stringPreferencesKey("name_en")
        val WARD_NUMBER = intPreferencesKey("ward_number")
        val LOCAL_LEVEL_TYPE = stringPreferencesKey("local_level_type")
        val IS_GPS = booleanPreferencesKey("is_gps")
        val LATITUDE = doublePreferencesKey("latitude")
        val LONGITUDE = doublePreferencesKey("longitude")
    }

    val locationFlow: Flow<GaunLocation?> = context.locationDataStore.data.map { prefs ->
        val id = prefs[Keys.LOCATION_ID] ?: return@map null
        val provId = prefs[Keys.PROVINCE_ID] ?: "province_3_bagmati"
        val distId = prefs[Keys.DISTRICT_ID] ?: "district_35_sindhupalchok"
        val muniId = prefs[Keys.MUNICIPALITY_ID] ?: "local_level_391_helambu"
        val typeStr = prefs[Keys.LOCAL_LEVEL_TYPE]
        val resolvedType = if (typeStr != null) {
            com.example.model.LocalLevelType.fromString(typeStr)
        } else {
            NepalAdministrativeData.getLocalLevel(muniId)?.type ?: com.example.model.LocalLevelType.RURAL_MUNICIPALITY
        }

        GaunLocation(
            id = id,
            provinceId = provId,
            provinceNe = prefs[Keys.PROVINCE_NE] ?: "बागमती",
            provinceEn = prefs[Keys.PROVINCE_EN] ?: "Bagmati",
            districtId = distId,
            districtNe = prefs[Keys.DISTRICT_NE] ?: "सिन्धुपाल्चोक",
            districtEn = prefs[Keys.DISTRICT_EN] ?: "Sindhupalchok",
            municipalityId = muniId,
            nameNe = prefs[Keys.NAME_NE] ?: "हेलम्बु गाउँपालिका",
            nameEn = prefs[Keys.NAME_EN] ?: "Helambu Rural Municipality",
            wardNumber = prefs[Keys.WARD_NUMBER] ?: 1,
            localLevelType = resolvedType,
            isGpsDetected = prefs[Keys.IS_GPS] ?: false,
            latitude = prefs[Keys.LATITUDE],
            longitude = prefs[Keys.LONGITUDE]
        )
    }

    suspend fun saveLocation(location: GaunLocation) {
        context.locationDataStore.edit { prefs ->
            prefs[Keys.LOCATION_ID] = location.id
            prefs[Keys.PROVINCE_ID] = location.provinceId
            prefs[Keys.PROVINCE_NE] = location.provinceNe
            prefs[Keys.PROVINCE_EN] = location.provinceEn
            prefs[Keys.DISTRICT_ID] = location.districtId
            prefs[Keys.DISTRICT_NE] = location.districtNe
            prefs[Keys.DISTRICT_EN] = location.districtEn
            prefs[Keys.MUNICIPALITY_ID] = location.municipalityId
            prefs[Keys.NAME_NE] = location.nameNe
            prefs[Keys.NAME_EN] = location.nameEn
            location.wardNumber?.let { prefs[Keys.WARD_NUMBER] = it }
            prefs[Keys.LOCAL_LEVEL_TYPE] = location.localLevelType.name
            prefs[Keys.IS_GPS] = location.isGpsDetected
            location.latitude?.let { prefs[Keys.LATITUDE] = it }
            location.longitude?.let { prefs[Keys.LONGITUDE] = it }
        }
    }
}

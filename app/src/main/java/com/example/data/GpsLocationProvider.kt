package com.example.data

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import com.example.model.GaunLocation
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.tasks.await
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class GpsLocationProvider(private val context: Context) {

    private val fusedClient = LocationServices.getFusedLocationProviderClient(context)

    @SuppressLint("MissingPermission")
    suspend fun getCurrentGaunLocation(): GaunLocation? {
        return try {
            val cts = CancellationTokenSource()
            val location: Location? = fusedClient.getCurrentLocation(
                Priority.PRIORITY_BALANCED_POWER_ACCURACY,
                cts.token
            ).await() ?: fusedClient.lastLocation.await()

            if (location != null) {
                mapCoordinatesToNearestGaun(location.latitude, location.longitude)
            } else {
                null
            }
        } catch (_: Exception) {
            null
        }
    }

    private fun mapCoordinatesToNearestGaun(lat: Double, lon: Double): GaunLocation {
        var closestLoc = NepalAdministrativeData.defaultLocation
        var minDistance = Double.MAX_VALUE

        for (province in NepalAdministrativeData.provinces) {
            for (district in province.districts) {
                for (municipality in district.municipalities) {
                    val coords = NepalAdministrativeData.getCoordinatesForMunicipality(municipality.id) ?: continue
                    val dist = calculateDistance(lat, lon, coords.first, coords.second)
                    if (dist < minDistance) {
                        minDistance = dist
                        closestLoc = GaunLocation(
                            id = "${municipality.id}_${district.id}",
                            provinceId = province.id,
                            provinceNe = province.nameNe,
                            provinceEn = province.nameEn,
                            districtId = district.id,
                            districtNe = district.nameNe,
                            districtEn = district.nameEn,
                            municipalityId = municipality.id,
                            nameNe = municipality.nameNe,
                            nameEn = municipality.nameEn,
                            wardNumber = 1,
                            localLevelType = municipality.type,
                            isGpsDetected = true,
                            latitude = lat,
                            longitude = lon
                        )
                    }
                }
            }
        }

        return closestLoc
    }

    private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return r * c
    }
}

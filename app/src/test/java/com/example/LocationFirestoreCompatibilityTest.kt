package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.FirebaseRepository
import com.example.data.LocationPreferencesRepository
import com.example.data.NepalAdministrativeData
import com.example.model.LocalLevelType
import com.example.model.LocalNotice
import com.example.model.NoticeCategory
import com.example.model.NoticePriority
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Phase 21: Location & Firestore / Persistence Compatibility Test.
 *
 * Verifies that location preferences persistence preserves authoritative IDs and LocalLevelType,
 * maintains backward compatibility, and enforces security boundaries on administrative data writes.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class LocationFirestoreCompatibilityTest {

    private lateinit var context: Context
    private lateinit var locationPrefs: LocationPreferencesRepository
    private lateinit var firebaseRepo: FirebaseRepository

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        locationPrefs = LocationPreferencesRepository(context)
        firebaseRepo = FirebaseRepository(context)
    }

    @Test
    fun testLocationPreferencesSaveAndRestore() = runBlocking {
        val ktmLocation = NepalAdministrativeData.findLocation(
            provinceQuery = "province_3_bagmati",
            districtQuery = "district_27_kathmandu",
            municipalityQuery = "local_level_307_kathmandu",
            ward = 32
        )!!

        locationPrefs.saveLocation(ktmLocation)

        val restored = locationPrefs.locationFlow.first()
        assertNotNull(restored)
        assertEquals(ktmLocation.id, restored?.id)
        assertEquals("province_3_bagmati", restored?.provinceId)
        assertEquals("district_27_kathmandu", restored?.districtId)
        assertEquals("local_level_307_kathmandu", restored?.municipalityId)
        assertEquals(32, restored?.wardNumber)
        assertEquals(LocalLevelType.METROPOLITAN, restored?.localLevelType)
        assertEquals("काठमाण्डौं महानगरपालिका", restored?.nameNe)
        assertEquals("Kathmandu Metropolitan City", restored?.nameEn)
    }

    @Test
    fun testBackwardCompatibilityWithLegacyLocations() = runBlocking {
        // Save Helambu location
        val helambu = NepalAdministrativeData.defaultLocation
        locationPrefs.saveLocation(helambu)

        val restored = locationPrefs.locationFlow.first()
        assertNotNull(restored)
        assertEquals("province_3_bagmati", restored?.provinceId)
        assertEquals("district_35_sindhupalchok", restored?.districtId)
        assertEquals("local_level_391_helambu", restored?.municipalityId)
        assertEquals(LocalLevelType.RURAL_MUNICIPALITY, restored?.localLevelType)
    }

    @Test
    fun testFirestoreNoticeSecurityBoundaries() {
        // Unauthenticated / citizen user cannot publish notices
        assertFalse(firebaseRepo.isCurrentAdmin())

        val unauthorizedNotice = LocalNotice(
            id = "unauth_notice_1",
            title = "Unverified Notice",
            description = "Unauthorized write attempt",
            dateIso = "2026-10-07",
            category = NoticeCategory.CIVIC,
            priority = NoticePriority.NORMAL,
            locationName = "काठमाण्डौं महानगरपालिका"
        )

        val publishResult = firebaseRepo.addNotice(unauthorizedNotice)
        assertFalse("Citizen cannot write notices to Firestore", publishResult)

        // Only authenticated admin can publish
        firebaseRepo.setDemoAdminRole(true)
        assertTrue(firebaseRepo.isCurrentAdmin())

        val authorizedResult = firebaseRepo.addNotice(unauthorizedNotice)
        assertTrue("Admin can publish notices", authorizedResult)

        // Reset back to citizen
        firebaseRepo.signOut()
        assertFalse(firebaseRepo.isCurrentAdmin())
    }
}

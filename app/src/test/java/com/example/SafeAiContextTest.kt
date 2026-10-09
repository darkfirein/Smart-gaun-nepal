package com.example

import com.example.data.AppAiContext
import com.example.data.InformationSourceCategory
import com.example.data.SafeAiContextLayer
import com.example.model.EmergencyContact
import com.example.model.GaunLanguage
import com.example.model.GaunLocation
import com.example.model.HealthFacility
import com.example.model.LocalNotice
import com.example.model.WeatherCondition
import com.example.model.WeatherData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class SafeAiContextTest {

    private val sampleLocation = com.example.data.NepalAdministrativeData.defaultLocation

    private val sampleWeather = WeatherData(
        locationId = "helambu_sindhupalchok",
        locationNameNe = "हेलम्बु गाउँपालिका",
        locationNameEn = "Helambu",
        temperatureC = 18.5,
        condition = WeatherCondition(0, "सफा", "Clear sky", "clear_day"),
        fetchedTimestamp = 1700000000000L
    )

    private val sampleContext = AppAiContext(
        location = sampleLocation,
        language = GaunLanguage.NEPALI,
        weather = sampleWeather,
        notices = emptyList(),
        healthFacilities = emptyList(),
        emergencyContacts = emptyList()
    )

    @Test
    fun testVerifiedLocalMetadata() {
        val meta = SafeAiContextLayer.buildMetadata(
            category = InformationSourceCategory.VERIFIED_LOCAL,
            prompt = "नागरिकता सिफारिस",
            appContext = sampleContext,
            isAvailable = true
        )
        assertEquals(InformationSourceCategory.VERIFIED_LOCAL, meta.category)
        assertNotNull(meta.sourceName)
        assertEquals("हेलम्बु गाउँपालिका", meta.authority)
        assertNotNull(meta.verifiedAt)
        assertTrue(meta.isAvailable)
    }

    @Test
    fun testLiveExternalMetadata() {
        val meta = SafeAiContextLayer.buildMetadata(
            category = InformationSourceCategory.LIVE_EXTERNAL,
            prompt = "मौसम के छ?",
            appContext = sampleContext,
            isAvailable = true
        )
        assertEquals(InformationSourceCategory.LIVE_EXTERNAL, meta.category)
        assertEquals("Open-Meteo Live API", meta.sourceName)
        assertEquals(1700000000000L, meta.lastUpdated)
        assertEquals(1700000000000L, meta.verifiedAt)
        assertTrue(meta.isAvailable)
    }

    @Test
    fun testAiGeneralMetadata() {
        val meta = SafeAiContextLayer.buildMetadata(
            category = InformationSourceCategory.AI_GENERAL,
            prompt = "सफलता भनेको के हो?",
            appContext = sampleContext,
            isAvailable = true
        )
        assertEquals(InformationSourceCategory.AI_GENERAL, meta.category)
        assertNull(meta.authority) // Missing authorities are NOT fabricated
        assertNull(meta.lastUpdated) // Missing timestamps are NOT fabricated
        assertNull(meta.verifiedAt)
        assertTrue(meta.isAvailable)
    }

    @Test
    fun testMissingTimestampNotFabricated() {
        val meta = SafeAiContextLayer.buildMetadata(
            category = InformationSourceCategory.AI_GENERAL,
            prompt = "सामान्य जानकारी",
            appContext = null,
            isAvailable = true
        )
        assertNull("Missing timestamp must remain null rather than fabricated", meta.lastUpdated)
    }

    @Test
    fun testMissingAuthorityNotFabricated() {
        val meta = SafeAiContextLayer.buildMetadata(
            category = InformationSourceCategory.AI_GENERAL,
            prompt = "नमस्ते",
            appContext = null,
            isAvailable = true
        )
        assertNull("Missing authority must remain null rather than fabricated", meta.authority)
    }

    @Test
    fun testBackwardCompatibleFirestoreRecords() {
        // Map from old Firestore database without Phase 15 provenance fields
        val legacyNoticeMap = mapOf(
            "title" to "पुरानो सूचना",
            "description" to "विवरण",
            "dateIso" to "2024-01-01",
            "category" to "civic"
        )
        val notice = LocalNotice.fromFirestoreMap("old_notice_1", legacyNoticeMap)
        assertEquals("पुरानो सूचना", notice.title)
        assertNull(notice.sourceName)
        assertNull(notice.authority)
        assertNull(notice.updatedAt)
        assertNull(notice.verifiedAt)
        assertNull(notice.verifiedBy)

        val legacyContactMap = mapOf(
            "nameNe" to "राम शर्मा",
            "contactNumber" to "9800000000"
        )
        val contact = EmergencyContact.fromFirestoreMap("contact_1", legacyContactMap)
        assertEquals("राम शर्मा", contact.nameNe)
        assertNull(contact.sourceName)
        assertNull(contact.authority)
        assertNull(contact.updatedAt)

        val legacyFacilityMap = mapOf(
            "nameNe" to "हेलम्बु स्वास्थ्य चौकी",
            "contactNumber" to "01-123456"
        )
        val facility = HealthFacility.fromFirestoreMap("fac_1", legacyFacilityMap)
        assertEquals("हेलम्बु स्वास्थ्य चौकी", facility.nameNe)
        assertNull(facility.sourceName)
        assertNull(facility.authority)
        assertNull(facility.updatedAt)
    }

    @Test
    fun testVerifiedLocalPreference() {
        val category = SafeAiContextLayer.classifyResponseCategory(
            prompt = "नागरिकता सिफारिसको लागि के चाहिन्छ?",
            responseText = ""
        )
        assertEquals(InformationSourceCategory.VERIFIED_LOCAL, category)

        val response = SafeAiContextLayer.getOfflineFallbackResponse(
            prompt = "नागरिकता सिफारिस",
            language = GaunLanguage.NEPALI,
            appContext = sampleContext
        )
        assertEquals(InformationSourceCategory.VERIFIED_LOCAL, response.metadata?.category)
        assertTrue(response.text.contains("नागरिकता"))
    }

    @Test
    fun testLiveExternalWeather() {
        val category = SafeAiContextLayer.classifyResponseCategory(
            prompt = "आजको मौसम कस्तो छ?",
            responseText = ""
        )
        assertEquals(InformationSourceCategory.LIVE_EXTERNAL, category)

        val response = SafeAiContextLayer.getOfflineFallbackResponse(
            prompt = "मौसम",
            language = GaunLanguage.NEPALI,
            appContext = sampleContext
        )
        assertEquals(InformationSourceCategory.LIVE_EXTERNAL, response.metadata?.category)
        assertTrue(response.text.contains("18.5°C"))
    }

    @Test
    fun testAiGeneralNotOfficial() {
        val category = SafeAiContextLayer.classifyResponseCategory(
            prompt = "जीवन कसरी सुधार्ने?",
            responseText = "सकारात्मक सोच्नुहोस्"
        )
        assertEquals(InformationSourceCategory.AI_GENERAL, category)

        val text = SafeAiContextLayer.formatGeneralGuidanceText("यो एक सल्लाह हो", GaunLanguage.NEPALI)
        assertTrue(text.contains("सामान्य AI जानकारी"))
        assertTrue(text.contains("आधिकारिक सरकारी सूचना वा निर्णय होइन"))
    }

    @Test
    fun testCredentialAndTokenExclusion() {
        val sensitiveInput = "My password: secretPass123 and token=abcToken_xyz987"
        val sanitized = SafeAiContextLayer.sanitizeInputText(sensitiveInput)
        assertFalse(sanitized.contains("secretPass123"))
        assertFalse(sanitized.contains("abcToken_xyz987"))
        assertTrue(sanitized.contains("[REDACTED]"))

        val instruction = SafeAiContextLayer.buildSystemInstruction(sampleContext)
        assertFalse(instruction.contains("secretPass123"))
        assertTrue(instruction.contains("गोपनीयता: प्रयोगकर्ताको कुनै पनि पासवर्ड, प्रमाणीकरण टोकन"))
    }
}

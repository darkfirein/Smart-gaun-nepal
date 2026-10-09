package com.example

import com.example.data.AppAiContext
import com.example.data.InformationSourceCategory
import com.example.data.NepalAdministrativeData
import com.example.data.SafeAiContextLayer
import com.example.model.GaunLanguage
import com.example.model.GaunLocation
import com.example.model.LocalLevelType
import com.example.model.WeatherCondition
import com.example.model.WeatherData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Phase 21: AI Location Context Integration Test.
 *
 * Verifies that AI context is cleanly constructed from authoritative location IDs,
 * detects and rejects stale or mixed administrative contexts, and properly attributes
 * VERIFIED_LOCAL, LIVE_EXTERNAL, and AI_GENERAL categories.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class AiLocationContextIntegrationTest {

    private val locHelambu = NepalAdministrativeData.defaultLocation

    private val locKathmandu = NepalAdministrativeData.findLocation(
        provinceQuery = "province_3_bagmati",
        districtQuery = "district_27_kathmandu",
        municipalityQuery = "local_level_307_kathmandu",
        ward = 3
    )!!

    private val sampleWeather = WeatherData(
        locationId = "loc_1",
        locationNameNe = "स्थान",
        locationNameEn = "Location",
        temperatureC = 20.0,
        condition = WeatherCondition(0, "सफा", "Clear", "clear_day"),
        fetchedTimestamp = 1700000000000L
    )

    @Test
    fun testAiContextSwitchesCleanlyBetweenLocations() {
        // Context A: Helambu
        val contextA = AppAiContext(
            location = locHelambu,
            language = GaunLanguage.NEPALI,
            weather = sampleWeather,
            notices = emptyList(),
            healthFacilities = emptyList(),
            emergencyContacts = emptyList()
        )
        val instructionA = SafeAiContextLayer.buildSystemInstruction(contextA)
        assertTrue(instructionA.contains("हेलम्बु गाउँपालिका"))
        assertTrue(instructionA.contains("Helambu Rural Municipality"))
        assertTrue(instructionA.contains("सिन्धुपाल्चोक"))
        assertFalse(instructionA.contains("काठमाण्डौं"))

        // Context B: Kathmandu
        val contextB = AppAiContext(
            location = locKathmandu,
            language = GaunLanguage.NEPALI,
            weather = sampleWeather,
            notices = emptyList(),
            healthFacilities = emptyList(),
            emergencyContacts = emptyList()
        )
        val instructionB = SafeAiContextLayer.buildSystemInstruction(contextB)
        assertTrue(instructionB.contains("काठमाण्डौं महानगरपालिका") || instructionB.contains("काठमाडौँ"))
        assertTrue(instructionB.contains("Kathmandu"))
        assertTrue(instructionB.contains("वडा नं 3"))
        assertFalse("No stale Helambu references should remain in Kathmandu context", instructionB.contains("हेलम्बु"))
        assertFalse("No stale Sindhupalchok references should remain", instructionB.contains("सिन्धुपाल्चोक"))
    }

    @Test
    fun testStaleMixedContextIsDetectedAndRejected() {
        // Mixed/inconsistent location: Koshi province + Kathmandu district
        val corruptLocation = GaunLocation(
            id = "corrupt_1",
            provinceId = "province_1_koshi",
            provinceNe = "काेशी",
            provinceEn = "Koshi",
            districtId = "district_27_kathmandu", // Wrong province!
            districtNe = "काठमाडौँ",
            districtEn = "Kathmandu",
            municipalityId = "local_level_307_kathmandu",
            nameNe = "काठमाण्डौं महानगरपालिका",
            nameEn = "Kathmandu Metropolitan City",
            wardNumber = 1,
            localLevelType = LocalLevelType.METROPOLITAN
        )

        assertFalse(
            "validateAiLocationContext must reject mismatched province and district",
            SafeAiContextLayer.validateAiLocationContext(corruptLocation)
        )

        val corruptContext = AppAiContext(
            location = corruptLocation,
            language = GaunLanguage.NEPALI,
            weather = sampleWeather,
            notices = emptyList(),
            healthFacilities = emptyList(),
            emergencyContacts = emptyList()
        )

        val instruction = SafeAiContextLayer.buildSystemInstruction(corruptContext)
        assertTrue(
            "Instruction must warn that location is unverified/stale",
            instruction.contains("प्रमाणित स्थान अनुपलब्ध") || instruction.contains("Unverified")
        )
    }

    @Test
    fun testSourceCategoryAttributionBoundaries() {
        val validContext = AppAiContext(
            location = locKathmandu,
            language = GaunLanguage.NEPALI,
            weather = sampleWeather,
            notices = emptyList(),
            healthFacilities = emptyList(),
            emergencyContacts = emptyList()
        )

        // Administrative query -> VERIFIED_LOCAL
        val civicMeta = SafeAiContextLayer.buildMetadata(
            category = InformationSourceCategory.VERIFIED_LOCAL,
            prompt = "नागरिकता सिफारिसका नियम",
            appContext = validContext
        )
        assertEquals(InformationSourceCategory.VERIFIED_LOCAL, civicMeta.category)
        assertTrue(civicMeta.authority?.contains("काठमाण्डौं") == true || civicMeta.authority?.contains("काठमाडौं") == true)

        // Live weather -> LIVE_EXTERNAL
        val weatherMeta = SafeAiContextLayer.buildMetadata(
            category = InformationSourceCategory.LIVE_EXTERNAL,
            prompt = "आज पानी पर्छ कि पर्दैन?",
            appContext = validContext
        )
        assertEquals(InformationSourceCategory.LIVE_EXTERNAL, weatherMeta.category)
        assertEquals("Open-Meteo Live API", weatherMeta.sourceName)

        // General guidance -> AI_GENERAL
        val generalMeta = SafeAiContextLayer.buildMetadata(
            category = InformationSourceCategory.AI_GENERAL,
            prompt = "कुखुरा पालनको तरिका",
            appContext = validContext
        )
        assertEquals(InformationSourceCategory.AI_GENERAL, generalMeta.category)
        assertEquals(null, generalMeta.authority)
    }
}

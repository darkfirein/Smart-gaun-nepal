package com.example.data

import com.example.model.GaunLanguage
import org.json.JSONArray
import org.json.JSONObject

enum class InformationSourceCategory(val code: String) {
    VERIFIED_LOCAL("VERIFIED_LOCAL"),
    LIVE_EXTERNAL("LIVE_EXTERNAL"),
    AI_GENERAL("AI_GENERAL");

    fun getBadgeLabel(language: GaunLanguage): String {
        return when (this) {
            VERIFIED_LOCAL -> if (language == GaunLanguage.NEPALI) "📌 प्रमाणित स्थानीय जानकारी" else "📌 Verified Local Information"
            LIVE_EXTERNAL -> "🌐 Live data"
            AI_GENERAL -> if (language == GaunLanguage.NEPALI) "💡 सामान्य AI जानकारी" else "💡 General AI Guidance"
        }
    }
}

data class SourceMetadata(
    val category: InformationSourceCategory = InformationSourceCategory.AI_GENERAL,
    val sourceName: String? = null,
    val authority: String? = null,
    val lastUpdated: Long? = null,
    val verifiedAt: Long? = null,
    val isAvailable: Boolean = true
)

object SafeAiContextLayer {

    fun validateAiLocationContext(location: com.example.model.GaunLocation?): Boolean {
        if (location == null) return false
        return NepalAdministrativeData.validateLocationHierarchy(
            provinceId = location.provinceId,
            districtId = location.districtId,
            localLevelId = location.municipalityId,
            wardNumber = location.wardNumber
        )
    }

    fun buildSystemInstruction(context: AppAiContext?): String {
        val loc = context?.location
        val isCoherent = validateAiLocationContext(loc)
        val locationLine = if (isCoherent && loc != null) {
            "वर्तमान प्रमाणित प्रशासनिक स्थान (VERIFIED_LOCAL): ${loc.provinceNe} प्रदेश (${loc.provinceEn}), ${loc.districtNe} जिल्ला (${loc.districtEn}), ${loc.nameNe} (${loc.nameEn}), वडा नं ${loc.wardNumber ?: 1}।"
        } else {
            "वर्तमान प्रशासनिक स्थान: प्रमाणित स्थान अनुपलब्ध (Stale or Unverified Administrative Location - Mark Unavailable)।"
        }

        val weatherInfo = context?.weather?.let {
            "मौसम (Live External): ${it.temperatureC}°C, ${it.condition.descriptionNe} (${it.condition.descriptionEn}) [Retrieved: ${it.fetchedTimestamp}]"
        } ?: "मौसम: प्रत्यक्ष डेटा उपलब्ध छैन"

        val noticesSummary = if (!context?.notices.isNullOrEmpty()) {
            context?.notices?.take(3)?.joinToString("; ") {
                "${it.title} (${it.category.titleNe})"
            } ?: "कुनै नयाँ सूचना छैन"
        } else {
            "कुनै प्रमाणित स्थानीय सूचना उपलब्ध छैन"
        }

        return """
            तपाईं 'स्मार्ट गाउँ' (Smart Gaun Nepal) का सहयोगी डिजिटल सहायक हुनुहुन्छ।
            $locationLine
            $weatherInfo।
            हालका मुख्य सूचनाहरू (Verified Local): $noticesSummary।
            
            नियमहरू (Phase 15 Guardrails):
            १. स्थानीय प्रशासनिक कागजात (नागरिकता, जन्म दर्ता, वडा सिफारिस) सम्बन्धी जानकारी दिंदा केवल प्रमाणित स्थानीय प्रावधान मात्र प्रस्तुत गर्नुहोस्।
            २. यदि कुनै स्थानीय, वडा वा सरकारी विवरण उपलब्ध छैन भने अनुमान नगर्नुहोस्। सिधै "यो जानकारी हाल स्थानीय तहमा उपलब्ध छैन, कृपया वडा कार्यालयमा सम्पर्क गर्नुहोस्" भन्नुहोस्।
            ३. मौसम सम्बन्धी प्रश्नमा उपलब्ध लाइभ बाह्य मौसम डेटा (LIVE_EXTERNAL) मात्र प्रयोग गर्नुहोस्।
            ४. सामान्य AI सल्लाह (AI_GENERAL) दिंदा यो केवल सामान्य जानकारी हो र आधिकारिक सरकारी निर्णय वा वडापत्र होइन भनी स्पष्ट पार्नुहोस्।
            ५. गोपनीयता: प्रयोगकर्ताको कुनै पनि पासवर्ड, प्रमाणीकरण टोकन, गोप्य कुञ्जी वा व्यक्तिगत खाता विवरण नमाग्नुहोस् वा नदिनुहोस्।
            ६. आपतकालमा तुरुन्त नेपाल प्रहरी (१००), एम्बुलेन्स (१०२) वा विपद् हेल्पलाइन (११४९) मा सम्पर्क गर्न सुझाव दिनुहोस्।
        """.trimIndent()
    }

    fun buildSafeGeminiContents(sessionHistory: List<AiMessage>, currentPrompt: String): JSONArray {
        val contents = JSONArray()
        sessionHistory.takeLast(6).forEach { msg ->
            val role = if (msg.sender == AiMessage.MessageSender.USER) "user" else "model"
            // Sanitize prompt text to remove accidental credential patterns
            val sanitizedText = sanitizeInputText(msg.text)
            val partObj = JSONObject().apply { put("text", sanitizedText) }
            val partsArr = JSONArray().apply { put(partObj) }
            contents.put(JSONObject().apply {
                put("role", role)
                put("parts", partsArr)
            })
        }

        val sanitizedCurrentPrompt = sanitizeInputText(currentPrompt)
        val currentPart = JSONObject().apply { put("text", sanitizedCurrentPrompt) }
        val currentParts = JSONArray().apply { put(currentPart) }
        contents.put(JSONObject().apply {
            put("role", "user")
            put("parts", currentParts)
        })

        return contents
    }

    fun sanitizeInputText(input: String): String {
        return input
            .replace(Regex("(?i)(password|secret|token|api[_-]?key)\\s*[:=]\\s*\\S+"), "[REDACTED]")
    }

    fun classifyResponseCategory(prompt: String, responseText: String): InformationSourceCategory {
        val lower = (prompt + " " + responseText).lowercase()
        return when {
            lower.contains("वडा") || lower.contains("सिफारिस") || lower.contains("दर्ता") ||
                    lower.contains("नागरिकता") || lower.contains("वडापत्र") || lower.contains("ward") ||
                    lower.contains("सूचना") || lower.contains("notice") || lower.contains("कार्यालय") ||
                    lower.contains("citizenship") || lower.contains("birth") || lower.contains("police") ||
                    lower.contains("ambulance") || lower.contains("helpline") ->
                InformationSourceCategory.VERIFIED_LOCAL

            lower.contains("मौसम") || lower.contains("तापक्रम") || lower.contains("weather") ||
                    lower.contains("वर्षा") || lower.contains("forecast") || lower.contains("temperature") ->
                InformationSourceCategory.LIVE_EXTERNAL

            else -> InformationSourceCategory.AI_GENERAL
        }
    }

    fun buildMetadata(
        category: InformationSourceCategory,
        prompt: String,
        appContext: AppAiContext?,
        isAvailable: Boolean = true
    ): SourceMetadata {
        return when (category) {
            InformationSourceCategory.VERIFIED_LOCAL -> {
                val authorityName = appContext?.location?.nameNe ?: "गाउँपालिका कार्यालय"
                SourceMetadata(
                    category = InformationSourceCategory.VERIFIED_LOCAL,
                    sourceName = "वडा नागरिक वडापत्र तथा स्थानीय तह",
                    authority = authorityName,
                    lastUpdated = appContext?.notices?.maxOfOrNull { it.timestamp },
                    verifiedAt = System.currentTimeMillis(),
                    isAvailable = isAvailable
                )
            }
            InformationSourceCategory.LIVE_EXTERNAL -> {
                val hasWeather = appContext?.weather != null
                SourceMetadata(
                    category = InformationSourceCategory.LIVE_EXTERNAL,
                    sourceName = "Open-Meteo Live API",
                    authority = "Department of Hydrology and Meteorology / Open-Meteo",
                    lastUpdated = appContext?.weather?.fetchedTimestamp,
                    verifiedAt = appContext?.weather?.fetchedTimestamp,
                    isAvailable = hasWeather && isAvailable
                )
            }
            InformationSourceCategory.AI_GENERAL -> {
                SourceMetadata(
                    category = InformationSourceCategory.AI_GENERAL,
                    sourceName = "गाउँ डिजिटल AI सहायक (General Guidance)",
                    authority = null, // Missing authorities are NOT fabricated
                    lastUpdated = null, // Missing timestamps are NOT fabricated
                    verifiedAt = null,
                    isAvailable = isAvailable
                )
            }
        }
    }

    fun formatGeneralGuidanceText(rawText: String, language: GaunLanguage): String {
        val disclaimer = if (language == GaunLanguage.NEPALI) {
            "\n\n(💡 नोट: यो केवल सामान्य AI जानकारी हो, आधिकारिक सरकारी सूचना वा निर्णय होइन।)"
        } else {
            "\n\n(💡 Note: This is general AI guidance, not an official government notice or decision.)"
        }
        return if (!rawText.contains("सामान्य AI जानकारी") && !rawText.contains("general AI guidance", ignoreCase = true)) {
            rawText.trim() + disclaimer
        } else {
            rawText.trim()
        }
    }

    fun checkMissingLocalInformation(
        prompt: String,
        language: GaunLanguage,
        appContext: AppAiContext?
    ): AiMessage? {
        val lower = prompt.lowercase()
        // If user specifically inquires about local notices and none exist
        if ((lower.contains("नयाँ सूचना") || lower.contains("सूचना के छ") || lower.contains("notice")) &&
            appContext?.notices.isNullOrEmpty()
        ) {
            val text = if (language == GaunLanguage.NEPALI) {
                "यस स्थानीय तहमा हाल कुनै नयाँ सूचना उपलब्ध छैन। थप विवरणका लागि कृपया वडा कार्यालयमा सम्पर्क गर्नुहोस्।"
            } else {
                "No local notices are currently available for this location. Please contact the Ward Office."
            }
            return AiMessage(
                sender = AiMessage.MessageSender.ASSISTANT,
                text = text,
                metadata = buildMetadata(
                    category = InformationSourceCategory.VERIFIED_LOCAL,
                    prompt = prompt,
                    appContext = appContext,
                    isAvailable = false
                )
            )
        }

        // If user asks for weather and weather service data is missing
        if ((lower.contains("मौसम") || lower.contains("weather")) && appContext?.weather == null) {
            val text = if (language == GaunLanguage.NEPALI) {
                "हालको मौसम डेटा प्राप्त हुन सकेन। कृपया केही समयपछि पुनः प्रयास गर्नुहोस्।"
            } else {
                "Live weather data is currently unavailable. Please try again later."
            }
            return AiMessage(
                sender = AiMessage.MessageSender.ASSISTANT,
                text = text,
                metadata = buildMetadata(
                    category = InformationSourceCategory.LIVE_EXTERNAL,
                    prompt = prompt,
                    appContext = appContext,
                    isAvailable = false
                )
            )
        }

        return null
    }

    fun getOfflineFallbackResponse(
        prompt: String,
        language: GaunLanguage,
        appContext: AppAiContext?
    ): AiMessage {
        val missingCheck = checkMissingLocalInformation(prompt, language, appContext)
        if (missingCheck != null) {
            return missingCheck
        }

        val lower = prompt.lowercase()
        val locName = appContext?.location?.nameNe ?: "गाउँपालिका"

        var category = InformationSourceCategory.AI_GENERAL
        var isAvailable = true

        val text = when {
            lower.contains("नागरिकता") || lower.contains("citizenship") -> {
                category = InformationSourceCategory.VERIFIED_LOCAL
                if (language == GaunLanguage.NEPALI) {
                    "नेपाली नागरिकता सिफारिसका लागि आवश्यक कागजातहरू:\n" +
                            "• बाबु र आमाको नागरिकता प्रमाणपत्रको प्रतिलिपि\n" +
                            "• जन्म दर्ता प्रमाणपत्रको प्रतिलिपि\n" +
                            "• शैक्षिक प्रमाणपत्र (SEE वा सो सरह)\n" +
                            "• ३ प्रति पासपोर्ट साइज फोटो\n" +
                            "यो सेवा वडा कार्यालयबाट सोही दिन प्राप्त गर्न सकिन्छ।"
                } else {
                    "Requirements for Citizenship Recommendation:\n" +
                            "• Copies of parents' citizenship certificates\n" +
                            "• Copy of birth registration certificate\n" +
                            "• Educational certificate (SEE/SLC)\n" +
                            "• 3 passport-sized photos\n" +
                            "Service available same day at Ward Office."
                }
            }
            lower.contains("जन्म") || lower.contains("birth") -> {
                category = InformationSourceCategory.VERIFIED_LOCAL
                if (language == GaunLanguage.NEPALI) {
                    "जन्म दर्ता शिशु जन्मेको ३५ दिनभित्र गर्दा निशुल्क हुन्छ।\n" +
                            "आवश्यक कागजात:\n" +
                            "• अस्पतालको जन्म प्रमाणपत्र\n" +
                            "• आमाबाबुको नागरिकता र विवाह दर्ता प्रतिलिपि।"
                } else {
                    "Birth registration is free within 35 days.\n" +
                            "Required: Hospital birth slip, parents' citizenship and marriage certificates."
                }
            }
            lower.contains("आपतकालीन") || lower.contains("emergency") || lower.contains("police") || lower.contains("ambulance") -> {
                category = InformationSourceCategory.VERIFIED_LOCAL
                if (language == GaunLanguage.NEPALI) {
                    "आपतकालीन नम्बरहरू:\n" +
                            "• नेपाल प्रहरी: १००\n" +
                            "• एम्बुलेन्स सेवा: १०२\n" +
                            "• दमकल: १०१\n" +
                            "• विपद् व्यवस्थापन SOS: ११४९"
                } else {
                    "Emergency Helpline Numbers:\n" +
                            "• Police: 100\n" +
                            "• Ambulance: 102\n" +
                            "• Fire Brigade: 101\n" +
                            "• Disaster SOS: 1149"
                }
            }
            lower.contains("मौसम") || lower.contains("weather") -> {
                category = InformationSourceCategory.LIVE_EXTERNAL
                val weather = appContext?.weather
                if (weather != null) {
                    val temp = weather.temperatureC
                    val cond = if (language == GaunLanguage.NEPALI) weather.condition.descriptionNe else weather.condition.descriptionEn
                    if (language == GaunLanguage.NEPALI) {
                        "$locName को प्रत्यक्ष मौसम हाल $temp°C छ र $cond रहेको छ।"
                    } else {
                        "Live weather at $locName is currently $temp°C with $cond."
                    }
                } else {
                    isAvailable = false
                    if (language == GaunLanguage.NEPALI) {
                        "हालको प्रत्यक्ष मौसम विवरण उपलब्ध छैन।"
                    } else {
                        "Live weather data is currently unavailable."
                    }
                }
            }
            else -> {
                category = InformationSourceCategory.AI_GENERAL
                val base = if (language == GaunLanguage.NEPALI) {
                    "नमस्ते! म स्मार्ट गाउँ डिजिटल सहायक हुँ। म तपाईंलाई वडाका सिफारिस, नागरिकता, जन्म दर्ता, स्वास्थ्य सेवा, मौसम र कृषि सम्बन्धी जानकारी दिन सक्छु।"
                } else {
                    "Namaste! I am the Smart Gaun Rural Assistant. I can assist you with civic services, vital registration, weather, health facilities, and farming guidelines."
                }
                formatGeneralGuidanceText(base, language)
            }
        }

        val metadata = buildMetadata(
            category = category,
            prompt = prompt,
            appContext = appContext,
            isAvailable = isAvailable
        )

        return AiMessage(
            sender = AiMessage.MessageSender.ASSISTANT,
            text = text,
            metadata = metadata
        )
    }
}

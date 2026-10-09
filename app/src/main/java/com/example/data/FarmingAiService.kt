package com.example.data

import com.example.data.gemini.BackendGeminiGateway
import com.example.data.gemini.GeminiGateway
import com.example.model.GaunLanguage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

interface FarmingAiService {
    suspend fun getAdvice(
        prompt: String,
        language: GaunLanguage,
        locationName: String,
        isOnline: Boolean
    ): FarmingAiMessage
}

class GeminiFarmingAiService(
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build(),
    private val gateway: GeminiGateway = BackendGeminiGateway(client)
) : FarmingAiService {

    override suspend fun getAdvice(
        prompt: String,
        language: GaunLanguage,
        locationName: String,
        isOnline: Boolean
    ): FarmingAiMessage = withContext(Dispatchers.IO) {
        if (!isOnline) {
            return@withContext getOfflineFarmingAdvice(prompt, language, locationName)
        }

        try {
            val systemInstruction = """
                तपाईं नेपालको ग्रामीण कृषि विज्ञ (Agricultural Specialist / JT) हुनुहुन्छ।
                स्थान: $locationName।
                नेपालका पहाडी तथा तराई क्षेत्रका कृषकहरूलाई धान, मकै, कोदो, गहुँ, तरकारी, फलफूल तथा पशुपालन सम्बन्धी वैज्ञानिक तथा परम्परागत जैविक सल्लाह सरल भाषामा दिनुहोस्।
            """.trimIndent()

            val result = gateway.generateContent(
                prompt = prompt,
                systemInstruction = systemInstruction
            )

            result.fold(
                onSuccess = { text ->
                    if (text.isNotBlank()) {
                        FarmingAiMessage(
                            sender = FarmingAiMessage.MessageSender.ASSISTANT,
                            text = text.trim()
                        )
                    } else {
                        getOfflineFarmingAdvice(prompt, language, locationName)
                    }
                },
                onFailure = {
                    getOfflineFarmingAdvice(prompt, language, locationName)
                }
            )
        } catch (_: Exception) {
            getOfflineFarmingAdvice(prompt, language, locationName)
        }
    }

    private fun getOfflineFarmingAdvice(prompt: String, language: GaunLanguage, locationName: String): FarmingAiMessage {
        val lower = prompt.lowercase()
        val text = when {
            lower.contains("धान") || lower.contains("paddy") || lower.contains("rice") -> {
                if (language == GaunLanguage.NEPALI) {
                    "🌾 धान बाली सम्बन्धी सल्लाह:\n" +
                            "• रोपेको ३५-४० दिनपछि पहिलो पटक युरिया मलको टपड्रेसिङ गर्नुहोस्।\n" +
                            "• गबारो कीरा लागेमा जैविक नीमयुक्त विषादी (Azadirachtin 3ml प्रति लिटर पानी) प्रयोग गर्नुहोस्।\n" +
                            "• खेतमा पानीको उचित निकास व्यवस्थापन गर्नुहोस्।"
                } else {
                    "🌾 Paddy Cultivation Advisory:\n" +
                            "• Apply first top-dressing of urea 35-40 days after transplanting.\n" +
                            "• For stem borer, spray neem-based organic pesticide (3ml per liter of water).\n" +
                            "• Maintain 3-5 cm standing water during tillering."
                }
            }
            lower.contains("मकै") || lower.contains("maize") || lower.contains("corn") -> {
                if (language == GaunLanguage.NEPALI) {
                    "🌽 मकै बाली सल्लाह (फौजी कीरा नियन्त्रण):\n" +
                            "• अमेरिकी फौजी कीरा (Fall Armyworm) देखिएमा मकैको पातको गुभोमा खरानी वा बालुवा हाल्नुहोस्।\n" +
                            "• निमको पातको रस वा जैविक विषादी प्रयोग गर्नुहोस्।"
                } else {
                    "🌽 Maize Advisory (Fall Armyworm Control):\n" +
                    "• If fall armyworm appears, apply fine wood ash or sand in the whorls.\n" +
                    "• Spray bio-pesticides or Neem-based extracts in early morning."
                }
            }
            lower.contains("तरकारी") || lower.contains("vegetable") || lower.contains("टमाटर") || lower.contains("tomato") -> {
                if (language == GaunLanguage.NEPALI) {
                    "🍅 तरकारी खेती सल्लाह (डढुवा रोग):\n" +
                            "• गोलभेँडा तथा आलुमा डढुवा रोगबाट बच्न पानी जम्न नदिनुहोस्।\n" +
                            "• लक्षण देखिनु अगावै बोर्डो मिश्रण (Bordeaux mixture 1%) छर्कनुहोस्।"
                } else {
                    "🍅 Vegetable Advisory (Late Blight Prevention):\n" +
                            "• Ensure good soil drainage for tomatoes and potatoes.\n" +
                            "• Spray 1% Bordeaux mixture preventively during continuous humid rainy days."
                }
            }
            else -> {
                if (language == GaunLanguage.NEPALI) {
                    "🌱 $locationName क्षेत्रका लागि मौसमी कृषि सल्लाह:\n" +
                            "• हालको मौसम अनुसार बीउको उपचार गरी मात्र रोप्नुहोस्।\n" +
                            "• कम्पोस्ट मल र गाईवस्तुको मलमुत्रको झोल प्रयोग गरी माटोको उर्वराशक्ति बढाउनुहोस्।"
                } else {
                    "🌱 Seasonal Agro Advisory for $locationName:\n" +
                            "• Always treat seeds before sowing to protect against fungal infections.\n" +
                            "• Incorporate compost and cattle manure slurry to preserve soil nutrients."
                }
            }
        }

        return FarmingAiMessage(
            sender = FarmingAiMessage.MessageSender.ASSISTANT,
            text = text
        )
    }
}

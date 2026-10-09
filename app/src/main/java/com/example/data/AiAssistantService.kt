package com.example.data

import com.example.data.gemini.BackendGeminiGateway
import com.example.data.gemini.GeminiGateway
import com.example.model.GaunLanguage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

class AiAssistantService(
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build(),
    private val gateway: GeminiGateway = BackendGeminiGateway(client)
) {

    suspend fun askAssistant(
        prompt: String,
        sessionHistory: List<AiMessage>,
        language: GaunLanguage,
        appContext: AppAiContext?
    ): AiMessage = withContext(Dispatchers.IO) {
        // Safe guardrail: check for explicitly requested missing local information
        val missingLocal = SafeAiContextLayer.checkMissingLocalInformation(prompt, language, appContext)
        if (missingLocal != null) {
            return@withContext missingLocal
        }

        try {
            val systemInstruction = SafeAiContextLayer.buildSystemInstruction(appContext)
            val contents = SafeAiContextLayer.buildSafeGeminiContents(sessionHistory, prompt)

            val result = gateway.generateContent(
                prompt = prompt,
                systemInstruction = systemInstruction,
                historyContentsJson = contents
            )

            result.fold(
                onSuccess = { text ->
                    if (text.isNotBlank()) {
                        val category = SafeAiContextLayer.classifyResponseCategory(prompt, text)
                        val finalText = if (category == InformationSourceCategory.AI_GENERAL) {
                            SafeAiContextLayer.formatGeneralGuidanceText(text, language)
                        } else {
                            text.trim()
                        }
                        val metadata = SafeAiContextLayer.buildMetadata(
                            category = category,
                            prompt = prompt,
                            appContext = appContext,
                            isAvailable = true
                        )
                        AiMessage(
                            sender = AiMessage.MessageSender.ASSISTANT,
                            text = finalText,
                            metadata = metadata
                        )
                    } else {
                        SafeAiContextLayer.getOfflineFallbackResponse(prompt, language, appContext)
                    }
                },
                onFailure = {
                    SafeAiContextLayer.getOfflineFallbackResponse(prompt, language, appContext)
                }
            )
        } catch (_: Exception) {
            SafeAiContextLayer.getOfflineFallbackResponse(prompt, language, appContext)
        }
    }
}

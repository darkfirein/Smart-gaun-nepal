package com.example.data

data class AiMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val sender: MessageSender,
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isSpoken: Boolean = false,
    val isError: Boolean = false,
    val metadata: SourceMetadata? = null
) {
    enum class MessageSender {
        USER,
        ASSISTANT,
        SYSTEM
    }
}

data class FarmingAiMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val sender: MessageSender,
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val cropName: String? = null,
    val adviceCategory: String? = null
) {
    enum class MessageSender {
        USER,
        ASSISTANT
    }
}

data class AiContextItem(
    val category: String,
    val title: String,
    val content: String
)

data class AppAiContext(
    val location: com.example.model.GaunLocation,
    val language: com.example.model.GaunLanguage,
    val weather: com.example.model.WeatherData?,
    val notices: List<com.example.model.LocalNotice>,
    val healthFacilities: List<com.example.model.HealthFacility>,
    val emergencyContacts: List<com.example.model.EmergencyContact>
)

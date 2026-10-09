package com.example.model

data class WeatherCondition(
    val code: Int,
    val descriptionNe: String,
    val descriptionEn: String,
    val iconKey: String
)

data class DailyForecastItem(
    val dayNameEn: String,
    val dayNameNe: String,
    val dateIso: String,
    val maxTempC: Double,
    val minTempC: Double,
    val condition: WeatherCondition,
    val rainProbabilityPercent: Int = 0
)

data class WeatherData(
    val locationId: String,
    val locationNameNe: String,
    val locationNameEn: String,
    val temperatureC: Double,
    val feelsLikeC: Double? = null,
    val humidityPercent: Int? = null,
    val windSpeedKmh: Double? = null,
    val condition: WeatherCondition,
    val forecast: List<DailyForecastItem> = emptyList(),
    val fetchedTimestamp: Long = System.currentTimeMillis()
)

sealed interface WeatherUiState {
    data object Idle : WeatherUiState
    data object Loading : WeatherUiState
    data class Success(
        val data: WeatherData,
        val isFromCache: Boolean = false
    ) : WeatherUiState
    data class Error(
        val messageNe: String,
        val messageEn: String,
        val cachedData: WeatherData? = null
    ) : WeatherUiState
    data class Offline(
        val messageNe: String,
        val messageEn: String,
        val cachedData: WeatherData? = null
    ) : WeatherUiState
}

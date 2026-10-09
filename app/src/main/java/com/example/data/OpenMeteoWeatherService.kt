package com.example.data

import com.example.model.DailyForecastItem
import com.example.model.GaunLocation
import com.example.model.WeatherCondition
import com.example.model.WeatherData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit

class OpenMeteoWeatherService(
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()
) : WeatherService {

    override suspend fun fetchWeather(
        latitude: Double,
        longitude: Double,
        location: GaunLocation
    ): Result<WeatherData> = withContext(Dispatchers.IO) {
        try {
            val url = "https://api.open-meteo.com/v1/forecast?" +
                    "latitude=$latitude&longitude=$longitude" +
                    "&current=temperature_2m,relative_humidity_2m,apparent_temperature,weather_code,wind_speed_10m" +
                    "&daily=weather_code,temperature_2m_max,temperature_2m_min,precipitation_probability_max" +
                    "&timezone=auto"

            val request = Request.Builder().url(url).build()
            val response = client.newCall(request).execute()

            if (!response.isSuccessful) {
                return@withContext Result.failure(Exception("HTTP Error: ${response.code}"))
            }

            val body = response.body?.string() ?: return@withContext Result.failure(Exception("Empty body"))
            val json = JSONObject(body)

            val current = json.getJSONObject("current")
            val temp = current.getDouble("temperature_2m")
            val feelsLike = current.optDouble("apparent_temperature", temp)
            val humidity = current.optInt("relative_humidity_2m", 60)
            val wind = current.optDouble("wind_speed_10m", 5.0)
            val code = current.optInt("weather_code", 0)

            val condition = mapWmoCode(code)

            val daily = json.optJSONObject("daily")
            val forecast = mutableListOf<DailyForecastItem>()

            if (daily != null) {
                val times = daily.getJSONArray("time")
                val codes = daily.getJSONArray("weather_code")
                val maxTemps = daily.getJSONArray("temperature_2m_max")
                val minTemps = daily.getJSONArray("temperature_2m_min")
                val rainProbs = daily.optJSONArray("precipitation_probability_max")

                val count = minOf(times.length(), 5)
                for (i in 0 until count) {
                    val dateStr = times.getString(i)
                    val date = try {
                        LocalDate.parse(dateStr, DateTimeFormatter.ISO_LOCAL_DATE)
                    } catch (_: Exception) {
                        LocalDate.now().plusDays(i.toLong())
                    }
                    val dayOfWeek = date.dayOfWeek
                    val dayEn = dayOfWeek.name.take(3).lowercase().replaceFirstChar { it.uppercase() }
                    val dayNe = when (dayOfWeek.value) {
                        1 -> "सोम"
                        2 -> "मंगलबार"
                        3 -> "बुधबार"
                        4 -> "बिहीबार"
                        5 -> "शुक्रबार"
                        6 -> "शनिबार"
                        else -> "आइतबार"
                    }

                    val dCode = codes.getInt(i)
                    val maxT = maxTemps.getDouble(i)
                    val minT = minTemps.getDouble(i)
                    val rainProb = rainProbs?.optInt(i, 0) ?: 0

                    forecast.add(
                        DailyForecastItem(
                            dayNameEn = dayEn,
                            dayNameNe = dayNe,
                            dateIso = dateStr,
                            maxTempC = maxT,
                            minTempC = minT,
                            condition = mapWmoCode(dCode),
                            rainProbabilityPercent = rainProb
                        )
                    )
                }
            }

            val data = WeatherData(
                locationId = location.id,
                locationNameNe = location.nameNe,
                locationNameEn = location.nameEn,
                temperatureC = temp,
                feelsLikeC = feelsLike,
                humidityPercent = humidity,
                windSpeedKmh = wind,
                condition = condition,
                forecast = forecast,
                fetchedTimestamp = System.currentTimeMillis()
            )

            Result.success(data)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun mapWmoCode(code: Int): WeatherCondition {
        return when (code) {
            0 -> WeatherCondition(code, "सफा आकाश", "Clear Sky", "clear")
            1, 2 -> WeatherCondition(code, "आंशिक बदली", "Partly Cloudy", "partly_cloudy")
            3 -> WeatherCondition(code, "बादल लागेको", "Overcast", "cloudy")
            45, 48 -> WeatherCondition(code, "हुस्सु / कुहिरो", "Foggy / Mist", "fog")
            51, 53, 55 -> WeatherCondition(code, "हल्का झरी", "Drizzle", "drizzle")
            61, 63, 65 -> WeatherCondition(code, "वर्षा / झरी", "Rainy", "rain")
            71, 73, 75 -> WeatherCondition(code, "हिमपात", "Snowfall", "snow")
            80, 81, 82 -> WeatherCondition(code, "भारी वर्षा", "Heavy Rain", "heavy_rain")
            95, 96, 99 -> WeatherCondition(code, "चट्याङसहित पानी", "Thunderstorm", "thunderstorm")
            else -> WeatherCondition(code, "सामान्य मौसम", "Moderate Weather", "cloudy")
        }
    }
}

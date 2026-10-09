package com.example.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.model.DailyForecastItem
import com.example.model.WeatherCondition
import com.example.model.WeatherData
import kotlinx.coroutines.flow.firstOrNull
import org.json.JSONArray
import org.json.JSONObject

val Context.weatherDataStore by preferencesDataStore(name = "weather_cache")

class WeatherCacheRepository(private val context: Context) {

    suspend fun saveWeather(locationId: String, weather: WeatherData) {
        val json = JSONObject().apply {
            put("locationId", weather.locationId)
            put("locationNameNe", weather.locationNameNe)
            put("locationNameEn", weather.locationNameEn)
            put("temperatureC", weather.temperatureC)
            put("feelsLikeC", weather.feelsLikeC ?: JSONObject.NULL)
            put("humidityPercent", weather.humidityPercent ?: JSONObject.NULL)
            put("windSpeedKmh", weather.windSpeedKmh ?: JSONObject.NULL)
            put("timestamp", weather.fetchedTimestamp)

            val condObj = JSONObject().apply {
                put("code", weather.condition.code)
                put("descriptionNe", weather.condition.descriptionNe)
                put("descriptionEn", weather.condition.descriptionEn)
                put("iconKey", weather.condition.iconKey)
            }
            put("condition", condObj)

            val forecastArray = JSONArray()
            weather.forecast.forEach { item ->
                val fObj = JSONObject().apply {
                    put("dayEn", item.dayNameEn)
                    put("dayNe", item.dayNameNe)
                    put("dateIso", item.dateIso)
                    put("maxTemp", item.maxTempC)
                    put("minTemp", item.minTempC)
                    put("rainProb", item.rainProbabilityPercent)
                    val fcCond = JSONObject().apply {
                        put("code", item.condition.code)
                        put("descNe", item.condition.descriptionNe)
                        put("descEn", item.condition.descriptionEn)
                        put("icon", item.condition.iconKey)
                    }
                    put("condition", fcCond)
                }
                forecastArray.put(fObj)
            }
            put("forecast", forecastArray)
        }

        context.weatherDataStore.edit { prefs ->
            prefs[stringPreferencesKey("weather_$locationId")] = json.toString()
            prefs[stringPreferencesKey("last_weather")] = json.toString()
        }
    }

    suspend fun getCachedWeather(locationId: String): WeatherData? {
        val prefs = context.weatherDataStore.data.firstOrNull() ?: return null
        val raw = prefs[stringPreferencesKey("weather_$locationId")]
            ?: prefs[stringPreferencesKey("last_weather")]
            ?: return null
        return parseWeatherData(raw)
    }

    suspend fun getLastCachedWeather(): WeatherData? {
        val prefs = context.weatherDataStore.data.firstOrNull() ?: return null
        val raw = prefs[stringPreferencesKey("last_weather")] ?: return null
        return parseWeatherData(raw)
    }

    private fun parseWeatherData(raw: String): WeatherData? {
        return try {
            val obj = JSONObject(raw)
            val condObj = obj.getJSONObject("condition")
            val condition = WeatherCondition(
                code = condObj.getInt("code"),
                descriptionNe = condObj.getString("descriptionNe"),
                descriptionEn = condObj.getString("descriptionEn"),
                iconKey = condObj.getString("iconKey")
            )

            val forecastList = mutableListOf<DailyForecastItem>()
            val forecastArray = obj.optJSONArray("forecast")
            if (forecastArray != null) {
                for (i in 0 until forecastArray.length()) {
                    val f = forecastArray.getJSONObject(i)
                    val fc = f.getJSONObject("condition")
                    forecastList.add(
                        DailyForecastItem(
                            dayNameEn = f.optString("dayEn", "Day"),
                            dayNameNe = f.optString("dayNe", "दिन"),
                            dateIso = f.optString("dateIso", ""),
                            maxTempC = f.optDouble("maxTemp", 22.0),
                            minTempC = f.optDouble("minTemp", 14.0),
                            rainProbabilityPercent = f.optInt("rainProb", 0),
                            condition = WeatherCondition(
                                code = fc.optInt("code", 0),
                                descriptionNe = fc.optString("descNe", "सफा"),
                                descriptionEn = fc.optString("descEn", "Clear"),
                                iconKey = fc.optString("icon", "clear")
                            )
                        )
                    )
                }
            }

            WeatherData(
                locationId = obj.getString("locationId"),
                locationNameNe = obj.getString("locationNameNe"),
                locationNameEn = obj.getString("locationNameEn"),
                temperatureC = obj.getDouble("temperatureC"),
                feelsLikeC = if (obj.isNull("feelsLikeC")) null else obj.getDouble("feelsLikeC"),
                humidityPercent = if (obj.isNull("humidityPercent")) null else obj.getInt("humidityPercent"),
                windSpeedKmh = if (obj.isNull("windSpeedKmh")) null else obj.getDouble("windSpeedKmh"),
                condition = condition,
                forecast = forecastList,
                fetchedTimestamp = obj.optLong("timestamp", System.currentTimeMillis())
            )
        } catch (_: Exception) {
            null
        }
    }
}

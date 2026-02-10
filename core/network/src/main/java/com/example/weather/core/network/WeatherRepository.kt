package com.example.weather.core.network

import com.example.weather.core.model.City
import com.example.weather.core.model.WeatherData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class WeatherRepository @Inject constructor(
    private val api: WeatherApi,
    @Named("OPENWEATHER_API_KEY") private val apiKey: String,
) {

    suspend fun getWeather(lat: Double, lon: Double): Result<WeatherData> = withContext(Dispatchers.IO) {
        try {
            val current = api.getCurrentWeather(lat = lat, lon = lon, apiKey = apiKey)
            val forecast = api.getForecast(lat = lat, lon = lon, apiKey = apiKey)
            Result.success(forecast.toWeatherData(current))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun searchCities(query: String): Result<List<City>> = withContext(Dispatchers.IO) {
        try {
            if (query.isBlank()) return@withContext Result.success(emptyList())
            val result = api.searchCities(query = query, apiKey = apiKey)
            Result.success(result.map { it.toCity() })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getCityFromCoordinates(lat: Double, lon: Double): Result<City?> = withContext(Dispatchers.IO) {
        try {
            val result = api.reverseGeocode(lat = lat, lon = lon, apiKey = apiKey)
            Result.success(result.firstOrNull()?.toCity())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

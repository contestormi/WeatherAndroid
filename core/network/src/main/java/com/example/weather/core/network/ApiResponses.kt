package com.example.weather.core.network

import com.example.weather.core.model.CurrentWeather
import com.example.weather.core.model.DailyForecast
import com.example.weather.core.model.HourlyForecast
import com.example.weather.core.model.WeatherCondition
import com.example.weather.core.model.WeatherData
import com.google.gson.annotations.SerializedName

data class CurrentWeatherResponse(
    @SerializedName("main") val main: MainData,
    @SerializedName("wind") val wind: WindData,
    @SerializedName("weather") val weather: List<WeatherResponse>,
)

data class MainData(
    @SerializedName("temp") val temp: Double,
    @SerializedName("feels_like") val feelsLike: Double,
    @SerializedName("humidity") val humidity: Int,
    @SerializedName("pressure") val pressure: Int,
)

data class WindData(
    @SerializedName("speed") val speed: Double,
)

data class WeatherResponse(
    @SerializedName("id") val id: Int,
    @SerializedName("main") val main: String,
    @SerializedName("description") val description: String,
    @SerializedName("icon") val icon: String,
)

data class ForecastResponse(
    @SerializedName("list") val list: List<ForecastItem>,
)

data class ForecastItem(
    @SerializedName("dt") val dt: Long,
    @SerializedName("main") val main: MainData,
    @SerializedName("weather") val weather: List<WeatherResponse>,
    @SerializedName("pop") val pop: Double? = 0.0,
)

data class GeocodingResponse(
    @SerializedName("name") val name: String,
    @SerializedName("country") val country: String,
    @SerializedName("lat") val lat: Double,
    @SerializedName("lon") val lon: Double,
    @SerializedName("state") val state: String? = null,
)

fun GeocodingResponse.toCity() = com.example.weather.core.model.City(
    id = "${lat}_${lon}",
    name = name,
    country = country,
    lat = lat,
    lon = lon,
)

fun CurrentWeatherResponse.toCurrentWeather() = CurrentWeather(
    temp = main.temp,
    feelsLike = main.feelsLike,
    humidity = main.humidity,
    pressure = main.pressure,
    windSpeed = wind.speed,
    weather = weather.map { it.toWeatherCondition() },
)

fun ForecastItem.toHourlyForecast() = HourlyForecast(
    time = dt,
    temp = main.temp,
    feelsLike = main.feelsLike,
    weather = weather.map { it.toWeatherCondition() },
    pop = pop ?: 0.0,
)

fun ForecastResponse.toWeatherData(current: CurrentWeatherResponse): WeatherData {
    val hourlyForecasts = list.take(24).map { it.toHourlyForecast() }

    val dailyMap = mutableMapOf<String, MutableList<ForecastItem>>()
    for (item in list) {
        val dateKey = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
            .format(java.util.Date(item.dt * 1000))
        dailyMap.getOrPut(dateKey) { mutableListOf() }.add(item)
    }

    val dailyForecasts = dailyMap.map { (_, items) ->
        val dayItem = items.minByOrNull { it.dt } ?: items.first()
        val temps = items.flatMap { listOf(it.main.temp) }
        DailyForecast(
            date = dayItem.dt,
            tempMin = temps.minOrNull() ?: dayItem.main.temp,
            tempMax = temps.maxOrNull() ?: dayItem.main.temp,
            weather = dayItem.weather.map { it.toWeatherCondition() },
            pop = items.maxOfOrNull { it.pop ?: 0.0 } ?: 0.0,
        )
    }.take(7)

    return WeatherData(
        current = current.toCurrentWeather(),
        daily = dailyForecasts,
        hourly = hourlyForecasts,
    )
}

private fun WeatherResponse.toWeatherCondition() = WeatherCondition(
    id = id,
    main = main,
    description = description,
    icon = icon,
)

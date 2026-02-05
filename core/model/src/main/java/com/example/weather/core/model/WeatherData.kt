package com.example.weather.core.model

data class WeatherData(
    val current: CurrentWeather,
    val daily: List<DailyForecast>,
    val hourly: List<HourlyForecast>,
)

data class CurrentWeather(
    val temp: Double,
    val feelsLike: Double,
    val humidity: Int,
    val pressure: Int,
    val windSpeed: Double,
    val weather: List<WeatherCondition>,
)

data class DailyForecast(
    val date: Long,
    val tempMin: Double,
    val tempMax: Double,
    val weather: List<WeatherCondition>,
    val pop: Double,
)

data class HourlyForecast(
    val time: Long,
    val temp: Double,
    val feelsLike: Double,
    val weather: List<WeatherCondition>,
    val pop: Double,
)

data class WeatherCondition(
    val id: Int,
    val main: String,
    val description: String,
    val icon: String,
)

data class City(
    val id: String,
    val name: String,
    val country: String,
    val lat: Double,
    val lon: Double,
)

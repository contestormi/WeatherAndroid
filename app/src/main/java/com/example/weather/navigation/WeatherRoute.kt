package com.example.weather.navigation

import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavType

sealed class WeatherRoute {

    data object Weather : WeatherRoute() {
        const val route = "weather"
    }

    data object CitySearch : WeatherRoute() {
        const val route = "city_search"
    }

    data class Hourly(val dayIndex: Int) : WeatherRoute() {
        companion object {
            const val route = "hourly/{dayIndex}"
            const val ARG_DAY_INDEX = "dayIndex"

            fun fromBackStackEntry(entry: NavBackStackEntry): Hourly {
                val dayIndex = entry.arguments?.getInt(ARG_DAY_INDEX) ?: 0
                return Hourly(dayIndex = dayIndex)
            }
        }

        val routeWithArgs: String get() = "hourly/$dayIndex"
    }
}

package com.example.weather.navigation.destinations

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.navigation.compose.navigate
import com.example.weather.feature.weather.WeatherScreen
import com.example.weather.navigation.WeatherRoute

@Composable
fun WeatherDestination(
    navController: NavController,
    onRequestLocationPermission: () -> Unit,
) {
    WeatherScreen(
        onNavigateToCitySearch = { navController.navigate(CitySearchRoute) },
        onRequestLocationPermission = onRequestLocationPermission,
        onNavigateToHourlyForecast = { dayIndex ->
            navController.navigate(HourlyRoute(dayIndex = dayIndex))
        },
    )
}

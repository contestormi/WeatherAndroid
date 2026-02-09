package com.example.weather.navigation.destinations

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import com.example.weather.feature.weather.WeatherScreen
import com.example.weather.navigation.WeatherRoute

@Composable
fun WeatherDestination(
    navController: NavController,
    onRequestLocationPermission: () -> Unit,
) {
    WeatherScreen(
        onNavigateToCitySearch = { navController.navigate(WeatherRoute.CitySearch.route) },
        onRequestLocationPermission = onRequestLocationPermission,
        onNavigateToHourlyForecast = { dayIndex ->
            navController.navigate(WeatherRoute.Hourly(dayIndex = dayIndex).routeWithArgs)
        },
    )
}

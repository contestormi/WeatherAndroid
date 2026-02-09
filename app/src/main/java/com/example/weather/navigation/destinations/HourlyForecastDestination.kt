package com.example.weather.navigation.destinations

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import com.example.weather.feature.forecast.HourlyForecastScreen
import com.example.weather.feature.weather.WeatherViewModel
import com.example.weather.navigation.WeatherRoute
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.runtime.collectAsState

@Composable
fun HourlyForecastDestination(
    navController: NavController,
    backStackEntry: NavBackStackEntry,
) {
    val hourlyRoute = WeatherRoute.Hourly.fromBackStackEntry(backStackEntry)
    val parentEntry = remember(backStackEntry) {
        navController.getBackStackEntry(WeatherRoute.Weather.route)
    }
    val weatherViewModel: WeatherViewModel = hiltViewModel(parentEntry)
    val uiState by weatherViewModel.hourlyScreenState(hourlyRoute.dayIndex).collectAsState()
    HourlyForecastScreen(
        dayTitle = uiState.dayTitle,
        items = uiState.items,
        onBack = { navController.popBackStack() },
    )
}

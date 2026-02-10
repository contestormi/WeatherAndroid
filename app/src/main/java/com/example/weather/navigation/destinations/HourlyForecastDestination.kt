package com.example.weather.navigation.destinations

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.navigation.NavController
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.weather.feature.forecast.HourlyForecastScreen
import com.example.weather.feature.weather.WeatherViewModel
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun HourlyForecastDestination(
    navController: NavController,
    dayIndex: Int,
) {
    val weatherViewModel: WeatherViewModel = hiltViewModel()
    val uiState by weatherViewModel
        .hourlyScreenState(dayIndex)
        .collectAsStateWithLifecycle()
    HourlyForecastScreen(
        dayTitle = uiState.dayTitle,
        items = uiState.items,
        onBack = { navController.popBackStack() },
    )
}

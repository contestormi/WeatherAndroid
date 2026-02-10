package com.example.weather.navigation.destinations

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import com.example.weather.feature.city.CitySearchScreen

@Composable
fun CitySearchDestination(
    navController: NavController,
) {
    CitySearchScreen(
        onCitySelected = { navController.popBackStack() },
        onDismiss = { navController.popBackStack() },
    )
}

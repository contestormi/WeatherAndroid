package com.example.weather.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.weather.navigation.destinations.CitySearchDestination
import com.example.weather.navigation.destinations.HourlyForecastDestination
import com.example.weather.navigation.destinations.WeatherDestination

@Composable
fun WeatherNavGraph(
    onRequestLocationPermission: () -> Unit,
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = WeatherRoute,
    ) {
        composable<WeatherRoute> {
            WeatherDestination(
                navController = navController,
                onRequestLocationPermission = onRequestLocationPermission,
            )
        }
        composable<CitySearchRoute> {
            CitySearchDestination(navController = navController)
        }
        composable<HourlyRoute> { backStackEntry ->
            val route: HourlyRoute = backStackEntry.toRoute()
            HourlyForecastDestination(
                navController = navController,
                dayIndex = route.dayIndex,
            )
        }
    }
}

package com.example.weather.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
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
        startDestination = WeatherRoute.Weather.route,
    ) {
        composable(WeatherRoute.Weather.route) {
            WeatherDestination(
                navController = navController,
                onRequestLocationPermission = onRequestLocationPermission,
            )
        }
        composable(WeatherRoute.CitySearch.route) {
            CitySearchDestination(navController = navController)
        }
        composable(
            route = WeatherRoute.Hourly.route,
            arguments = listOf(
                navArgument(WeatherRoute.Hourly.ARG_DAY_INDEX) { type = NavType.IntType },
            ),
        ) { backStackEntry ->
            HourlyForecastDestination(
                navController = navController,
                backStackEntry = backStackEntry,
            )
        }
    }
}

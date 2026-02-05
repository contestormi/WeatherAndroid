package com.example.weather
import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.view.WindowCompat
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.navigation.navArgument
import com.example.weather.feature.city.CitySearchScreen
import com.example.weather.feature.city.CityViewModel
import com.example.weather.feature.forecast.HourlyForecastScreen
import com.example.weather.feature.weather.WeatherScreen
import com.example.weather.feature.weather.WeatherViewModel
import com.example.weather.ui.theme.WeatherTheme
import dagger.hilt.android.AndroidEntryPoint
import androidx.hilt.navigation.compose.hiltViewModel

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            WeatherTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                ) {
                    WeatherNavGraph(
                        onRequestLocationPermission = {
                            locationPermissionLauncher.launch(
                                arrayOf(
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION,
                                ),
                            )
                        },
                    )
                }
            }
        }
    }
}

@androidx.compose.runtime.Composable
private fun WeatherNavGraph(
    onRequestLocationPermission: () -> Unit,
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "weather",
    ) {
        composable("weather") {
            val viewModel: WeatherViewModel = hiltViewModel()
            LaunchedEffect(Unit) {
                viewModel.handleIntent(com.example.weather.feature.weather.WeatherIntent.LoadInitial)
            }
            WeatherScreen(
                viewModel = viewModel,
                onNavigateToCitySearch = { navController.navigate("city_search") },
                onRequestLocationPermission = onRequestLocationPermission,
                onNavigateToHourlyForecast = {
                    val state = viewModel.state.value
                    navController.navigate("hourly/${state.selectedDayIndex}")
                },
            )
        }
        composable("city_search") {
            val viewModel: CityViewModel = hiltViewModel()
            CitySearchScreen(
                viewModel = viewModel,
                onCitySelected = {
                    navController.popBackStack()
                },
                onDismiss = { navController.popBackStack() },
            )
        }
        composable(
            route = "hourly/{dayIndex}",
            arguments = listOf(navArgument("dayIndex") {
                type = NavType.IntType
            }),
        ) { backStackEntry ->
            val dayIndex = backStackEntry.arguments?.getInt("dayIndex") ?: 0
            val parentEntry = remember(backStackEntry) {
                navController.getBackStackEntry("weather")
            }
            val weatherViewModel: WeatherViewModel = hiltViewModel(parentEntry)
            val state by weatherViewModel.state.collectAsState()
            val day = state.weatherData?.daily?.getOrNull(dayIndex)
            val context = LocalContext.current
            val dayTitle = day?.let {
                java.text.SimpleDateFormat(
                    "EEEE, d MMMM",
                    java.util.Locale.forLanguageTag("ru")
                )
                    .format(java.util.Date(it.date * 1000))
            } ?: context.getString(com.example.weather.core.strings.R.string.forecast)
            val hourly = if (day != null) {
                val dayStart = (day.date / 86400) * 86400
                val dayEnd = dayStart + 86400
                state.weatherData?.hourly?.filter { it.time in dayStart until dayEnd }
                    ?: emptyList()
            } else emptyList()
            HourlyForecastScreen(
                dayTitle = dayTitle,
                hourlyForecasts = hourly,
                onBack = { navController.popBackStack() },
            )
        }
    }
}

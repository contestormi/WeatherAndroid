package com.example.weather.feature.weather

import android.provider.CalendarContract.Colors
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeatherScreen(
    viewModel: WeatherViewModel = hiltViewModel(),
    onNavigateToCitySearch: () -> Unit,
    onRequestLocationPermission: () -> Unit,
    onNavigateToHourlyForecast: (dayIndex: Int) -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.handleIntent(WeatherIntent.LoadInitial)
    }
    LaunchedEffect(Unit) {
        viewModel.navigation.collect { nav ->
            when (nav) {
                is WeatherNavigation.ToCitySearch -> onNavigateToCitySearch()
                is WeatherNavigation.RequestLocationPermission -> onRequestLocationPermission()
                is WeatherNavigation.ToHourlyForecast -> onNavigateToHourlyForecast(nav.dayIndex)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        state.cityName
                            ?: stringResource(id = com.example.weather.core.strings.R.string.weather),
                        style = MaterialTheme.typography.titleLarge,
                    )
                },
                actions = {
                    if (state.isRefreshing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    } else if (state.hasWeatherData) {
                        IconButton(onClick = {
                            viewModel.handleIntent(
                                WeatherIntent.Refresh
                            )
                        }) {
                            Icon(
                                Icons.Default.Refresh,
                                contentDescription = stringResource(id = com.example.weather.core.strings.R.string.retry)
                            )
                        }
                    }
                    IconButton(onClick = { viewModel.handleIntent(WeatherIntent.RequestLocation) }) {
Icon(
                        Icons.Default.LocationOn,
                        contentDescription = stringResource(id = com.example.weather.core.strings.R.string.my_location)
                    )
                    }

                    IconButton(onClick = { viewModel.handleIntent(WeatherIntent.OpenCitySearch) }) {
Icon(
                        Icons.Default.Search,
                        contentDescription = stringResource(id = com.example.weather.core.strings.R.string.search_city)
                    )
                    }
                },
            )
        },
    ) { padding ->
        if (state.isLoading && !state.hasWeatherData) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else if (state.error != null && !state.hasWeatherData) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        state.error.orEmpty(),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.error,
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    IconButton(onClick = { viewModel.handleIntent(WeatherIntent.Refresh) }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = stringResource(id = com.example.weather.core.strings.R.string.retry)
                        )
                    }
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState()),
            ) {
                if (state.hasWeatherData) {
                    val currentWeatherUi by viewModel.currentWeatherUi.collectAsStateWithLifecycle(initialValue = null)
                    currentWeatherUi?.let { current ->
                        CurrentWeatherSection(current = current)
                    }

                    val dailyForecastItems by viewModel.dailyForecastItems.collectAsStateWithLifecycle(initialValue = persistentListOf())
                    DailyForecastSection(
                        items = dailyForecastItems,
                        selectedIndex = state.selectedDayIndex,
                        onDayClick = {
                            viewModel.handleIntent(WeatherIntent.SelectDay(it))
                        },
                    )

                    val hourlyPreviewItems by viewModel.hourlyPreviewItems.collectAsStateWithLifecycle(initialValue = persistentListOf())

                    AnimatedVisibility(visible = hourlyPreviewItems.isNotEmpty()) {
                        Column {
                            HourlyPreviewSection(
                                items = hourlyPreviewItems,
                                onShowAllClick = {
                                    viewModel.handleIntent(
                                        WeatherIntent.OpenHourlyForecast
                                    )
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CurrentWeatherSection(
    current: CurrentWeatherUi,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .padding(16.dp)
            .fillMaxSize(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(
                alpha = 0.5f
            ),
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            if (current.iconUrl != null) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(current.iconUrl)
                        .build(),
                    contentDescription = current.description,
                    modifier = Modifier.size(120.dp),
                    contentScale = ContentScale.Fit,
                )
            }

            Text(
                text = "${current.tempC}°",
                style = MaterialTheme.typography.displayLarge,
                fontWeight = FontWeight.Light,
            )

            Text(
                text = stringResource(
                    id = com.example.weather.core.strings.R.string.feels_like,
                    current.feelsLikeC
                ),
                style = MaterialTheme.typography.bodyLarge,
            )

            current.description?.let { description ->
                Text(
                    text = description,
                    style = MaterialTheme.typography.titleMedium,
                )
            }

            Row(
                modifier = Modifier.padding(top = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(24.dp),
            ) {
                WeatherDetailItem(
                    stringResource(id = com.example.weather.core.strings.R.string.humidity),
                    stringResource(
                        id = com.example.weather.core.strings.R.string.humidity_value,
                        current.humidity
                    )
                )

                WeatherDetailItem(
                    stringResource(id = com.example.weather.core.strings.R.string.pressure),
                    stringResource(
                        id = com.example.weather.core.strings.R.string.pressure_value,
                        current.pressure,
                        stringResource(id = com.example.weather.core.strings.R.string.pressure_unit)
                    )
                )

                WeatherDetailItem(
                    stringResource(id = com.example.weather.core.strings.R.string.wind),
                    stringResource(
                        id = com.example.weather.core.strings.R.string.wind_speed_value,
                        current.windSpeedKph,
                        stringResource(id = com.example.weather.core.strings.R.string.wind_speed_unit)
                    )
                )
            }
        }
    }
}

@Composable
private fun WeatherDetailItem(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
        )

        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
        )
    }
}

@Composable
private fun DailyForecastSection(
    items: ImmutableList<DailyForecastItem>,
    selectedIndex: Int,
    onDayClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Text(
            text = stringResource(id = com.example.weather.core.strings.R.string.weekly_forecast),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(16.dp, 8.dp),
        )

        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            itemsIndexed(items) { index, item ->
                val isSelected = index == selectedIndex
                Card(
                    modifier = Modifier
                        .widthIn(min = 100.dp)
                        .clickable { onDayClick(index) },
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected)
                            MaterialTheme.colorScheme.primaryContainer
                        else
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                    ),
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = item.dateText,
                            style = MaterialTheme.typography.labelMedium,
                        )

                        if (item.iconUrl != null) {
                            AsyncImage(
                                model = item.iconUrl,
                                contentDescription = null,
                                modifier = Modifier.size(40.dp),
                            )
                        }

                        Text(
                            text = item.tempRangeText,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HourlyPreviewSection(
    items: ImmutableList<HourlyPreviewItem>,
    onShowAllClick: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp, 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(id = com.example.weather.core.strings.R.string.hourly_forecast),
                style = MaterialTheme.typography.titleMedium,
            )

            Text(
                text = stringResource(id = com.example.weather.core.strings.R.string.more_details),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable(onClick = onShowAllClick),
            )
        }

        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(items.take(8)) { item ->
                HourlyPreviewCard(
                    timeText = item.timeText,
                    tempText = item.tempText,
                    iconUrl = item.iconUrl,
                )
            }
        }
    }
}

@Composable
private fun HourlyPreviewCard(
    timeText: String,
    tempText: String,
    iconUrl: String?,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.widthIn(min = 70.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
        ),
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = timeText,
                style = MaterialTheme.typography.labelSmall,
            )

            if (iconUrl != null) {
                AsyncImage(
                    model = iconUrl,
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                )
            }

            Text(
                text = tempText,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

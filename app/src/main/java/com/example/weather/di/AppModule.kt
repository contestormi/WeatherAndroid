package com.example.weather.di

import android.content.Context
import com.example.weather.BuildConfig
import com.example.weather.core.database.CitiesRepository
import com.example.weather.core.database.WeatherDatabase
import com.example.weather.core.database.createWeatherDatabase
import com.example.weather.core.location.LocationProvider
import com.example.weather.core.network.WeatherApi
import com.example.weather.core.network.WeatherRepository
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideWeatherApi(okHttpClient: OkHttpClient): WeatherApi =
        Retrofit.Builder()
            .baseUrl("https://api.openweathermap.org/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(WeatherApi::class.java)

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .addInterceptor(
            HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            },
        )
        .build()

    @Provides
    @Singleton
    fun provideWeatherRepository(api: WeatherApi): WeatherRepository =
        WeatherRepository(api, BuildConfig.OPENWEATHER_API_KEY)

    @Provides
    @Singleton
    fun provideWeatherDatabase(@ApplicationContext context: Context): WeatherDatabase =
        createWeatherDatabase(context)

    @Provides
    @Singleton
    fun provideCitiesRepository(database: WeatherDatabase): CitiesRepository =
        CitiesRepository(database.citiesDao())

    @Provides
    @Singleton
    fun provideFusedLocationClient(@ApplicationContext context: Context): FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    @Provides
    @Singleton
    fun provideLocationProvider(
        @ApplicationContext context: Context,
        fusedLocationClient: FusedLocationProviderClient,
    ): LocationProvider = LocationProvider(context, fusedLocationClient)
}

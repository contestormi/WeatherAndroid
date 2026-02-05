package com.example.weather.core.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.weather.core.database.entity.SavedCityEntity

@Database(
    entities = [SavedCityEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class WeatherDatabase : RoomDatabase() {
    abstract fun citiesDao(): CitiesDao
}

fun createWeatherDatabase(context: Context): WeatherDatabase = Room.databaseBuilder(
    context.applicationContext,
    WeatherDatabase::class.java,
    "weather_db",
).build()

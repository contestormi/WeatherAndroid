package com.example.weather.core.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.weather.core.database.entity.SavedCityEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CitiesDao {

    @Query("SELECT * FROM saved_cities ORDER BY `order` ASC, name ASC")
    fun getAllCities(): Flow<List<SavedCityEntity>>

    @Query("SELECT * FROM saved_cities WHERE isSelected = 1 LIMIT 1")
    suspend fun getSelectedCity(): SavedCityEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(city: SavedCityEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(cities: List<SavedCityEntity>)

    @Query("UPDATE saved_cities SET isSelected = 0")
    suspend fun clearSelection()

    @Query("UPDATE saved_cities SET isSelected = 1 WHERE id = :cityId")
    suspend fun setSelected(cityId: String)

    @Query("DELETE FROM saved_cities WHERE id = :cityId")
    suspend fun delete(cityId: String)

    @Query("SELECT COUNT(*) FROM saved_cities")
    suspend fun getCount(): Int

    @Query("SELECT * FROM saved_cities ORDER BY `order` ASC LIMIT 1")
    suspend fun getFirstCity(): SavedCityEntity?
}

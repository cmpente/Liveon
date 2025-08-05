package com.altlifegames.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.altlifegames.data.db.entity.CareerEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CareerDao {
    @Query("SELECT * FROM careers")
    fun getAllCareers(): Flow<List<CareerEntity>>

    @Query("SELECT * FROM careers WHERE id = :id")
    suspend fun getCareerById(id: String): CareerEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCareer(career: CareerEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCareers(careers: List<CareerEntity>)
}
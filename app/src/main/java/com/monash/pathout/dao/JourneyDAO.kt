package com.monash.pathout.dao

import androidx.room.*
import com.monash.pathout.entity.Journey
import kotlinx.coroutines.flow.Flow

@Dao
interface JourneyDAO {

    @Query("SELECT * FROM journey ORDER BY journey_name ASC")
    fun getAll(): Flow<List<Journey>>

    @Query("SELECT COUNT(uid) FROM journey ORDER BY uid ASC")
    fun getNumberofJourneys(): Int

    @Query("SELECT * FROM journey WHERE uid = :journeyId LIMIT 1")
    suspend fun findByID(journeyId: Int): Journey?

    @Insert
    suspend fun insert(journey: Journey)

    @Delete
    suspend fun delete(journey: Journey)

    @Update
    suspend fun update(journey: Journey)

    @Query("DELETE FROM journey")
    suspend fun deleteAll()
}
package com.example.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ForecastDao {

    // --- Macro Events Queries ---
    @Query("SELECT * FROM macro_events ORDER BY lastUpdated DESC")
    fun getAllMacroEvents(): Flow<List<MacroEvent>>

    @Query("SELECT * FROM macro_events WHERE isWatched = 1 ORDER BY lastUpdated DESC")
    fun getWatchedEvents(): Flow<List<MacroEvent>>

    @Query("SELECT * FROM macro_events WHERE id = :id")
    suspend fun getEventById(id: Int): MacroEvent?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMacroEvents(events: List<MacroEvent>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMacroEvent(event: MacroEvent): Long

    @Update
    suspend fun updateMacroEvent(event: MacroEvent)

    @Query("UPDATE macro_events SET isWatched = :isWatched WHERE id = :id")
    suspend fun updateWatchStatus(id: Int, isWatched: Boolean)

    @Query("UPDATE macro_events SET userProbability = :userProb WHERE id = :id")
    suspend fun updateUserProbability(id: Int, userProb: Float)

    // --- User Predictions Queries ---
    @Query("SELECT * FROM user_predictions ORDER BY timestamp DESC")
    fun getAllUserPredictions(): Flow<List<UserPredictionRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserPrediction(prediction: UserPredictionRecord): Long

    @Query("DELETE FROM user_predictions WHERE id = :id")
    suspend fun deletePredictionById(id: Int)

    // --- Custom Analyses Queries ---
    @Query("SELECT * FROM custom_analyses ORDER BY timestamp DESC")
    fun getAllCustomAnalyses(): Flow<List<CustomAnalysis>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomAnalysis(analysis: CustomAnalysis): Long

    @Delete
    suspend fun deleteCustomAnalysis(analysis: CustomAnalysis)
}

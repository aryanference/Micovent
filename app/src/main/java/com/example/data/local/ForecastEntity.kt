package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "macro_events")
data class MacroEvent(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String,
    val category: String, // "GEOPOLITICS", "FINANCE", "ECONOMICS", "TECH", "CLIMATE"
    val aiProbability: Float, // 0.0 to 1.0 (e.g. 0.72 representing 72% chance of happening)
    val consensusProbability: Float, // Market estimate
    val userProbability: Float? = null, // What the user predicted
    val lastUpdated: Long = System.currentTimeMillis(),
    val isWatched: Boolean = false,
    val historicalTrend: String = "0.5,0.5,0.5", // Comma-separated floats e.g., "0.52,0.61,0.72" for drawing charts
    val status: String = "OPEN", // "OPEN", "RESOLVED"
    val outcomeAnswer: String? = null, // "YES", "NO", "ANNULLED"
    val scenarioAnalysis: String = "", // AI detailed markdown or json text
    val region: String = "GLOBAL",
    val tags: String = "" // Comma-separated tags
)

@Entity(tableName = "user_predictions")
data class UserPredictionRecord(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val eventId: Int, // Refers to MacroEvent.id or -1 for custom user research queries
    val eventTitle: String,
    val predictedProbability: Float,
    val actualProbabilityAtTime: Float,
    val timestamp: Long = System.currentTimeMillis(),
    val resolvedStatus: String = "PENDING", // "PENDING", "CORRECT", "INCORRECT", "GRADED"
    val brierScore: Float? = null, // Brier score for forecast calibration
    val aiCritique: String? = null
)

@Entity(tableName = "custom_analyses")
data class CustomAnalysis(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val query: String,
    val formattedResponse: String, // Saved markdown summary from Gemini
    val probabilityOfOccurrence: Float, // Parsed probability
    val keyDrivers: String, // Comma-separated or bullet list of key variables
    val timestamp: Long = System.currentTimeMillis()
)

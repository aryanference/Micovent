package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.CustomAnalysis
import com.example.data.local.MacroEvent
import com.example.data.local.UserPredictionRecord
import com.example.data.repository.ForecastRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.math.abs

class ForecastViewModel(
    private val repository: ForecastRepository,
    val authManager: com.example.managers.AuthManager,
    val billingManager: com.example.managers.BillingManager
) : ViewModel() {

    // Current category filtering state
    private val _selectedCategory = MutableStateFlow("ALL")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Base flows from repository
    val allEventsFlow = repository.allMacroEvents
    val watchedEventsFlow = repository.watchedEvents
    val userPredictionsFlow = repository.allUserPredictions
    val customAnalysesFlow = repository.allCustomAnalyses

    // Filtered events flow based on selection
    val filteredMacroEvents: StateFlow<List<MacroEvent>> = combine(
        allEventsFlow,
        _selectedCategory,
        _searchQuery
    ) { events, category, query ->
        var result = events
        if (category != "ALL") {
            result = result.filter { it.category == category }
        }
        if (query.isNotBlank()) {
            val q = query.lowercase()
            result = result.filter { 
                it.title.lowercase().contains(q) || 
                it.description.lowercase().contains(q) ||
                it.region.lowercase().contains(q) ||
                it.tags.lowercase().contains(q)
            }
        }
        result
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // User calibration score (shows overall alignment with prediction consensus)
    val userCalibrationScore: StateFlow<Int> = userPredictionsFlow.map { predictions ->
        if (predictions.isEmpty()) return@map 100
        
        var totalDiff = 0f
        var count = 0
        predictions.forEach { p ->
            totalDiff += abs(p.predictedProbability - p.actualProbabilityAtTime)
            count++
        }
        val avgDiff = totalDiff / count
        // 100% means perfect alignment, subtract average absolute deviance
        val score = (100 - (avgDiff * 100)).toInt()
        score.coerceIn(0, 100)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 100)

    // Interactive query state in Prediction Lab
    private val _customQueryText = MutableStateFlow("")
    val customQueryText: StateFlow<String> = _customQueryText.asStateFlow()

    private val _customResponse = MutableStateFlow<CustomAnalysis?>(null)
    val customResponse: StateFlow<CustomAnalysis?> = _customResponse.asStateFlow()

    private val _isQueryLoading = MutableStateFlow(false)
    val isQueryLoading: StateFlow<Boolean> = _isQueryLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        // Automatically seed default market events if empty on start
        viewModelScope.launch {
            repository.seedInitialEvents()
        }
    }

    fun setCategory(category: String) {
        _selectedCategory.value = category
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun toggleWatchStatus(eventId: Int, currentWatch: Boolean) {
        viewModelScope.launch {
            repository.updateWatchStatus(eventId, !currentWatch)
        }
    }

    fun updateCustomQueryText(text: String) {
        _customQueryText.value = text
    }

    /**
     * Triggers dynamic prediction re-evaluation from Gemini for an event
     */
    fun recalculateLiveProbability(event: MacroEvent) {
        viewModelScope.launch {
            repository.refreshForecastWithAI(event.id)
        }
    }

    /**
     * Submits a user prediction to the database
     */
    fun submitPrediction(event: MacroEvent, userPerc: Float, rationale: String?) {
        viewModelScope.launch {
            val record = UserPredictionRecord(
                eventId = event.id,
                eventTitle = event.title,
                predictedProbability = userPerc,
                actualProbabilityAtTime = event.aiProbability,
                aiCritique = rationale ?: "Simulation logged relative to consensus of ${(event.aiProbability * 100).toInt()}%"
            )
            repository.saveUserPrediction(record)
            
            // Also update the local event model to reflect the user's prediction on the UI card
            val updated = event.copy(userProbability = userPerc)
            repository.updateWatchStatus(event.id, event.isWatched) // refresh DB entry
            // Also save user probability specifically
            repository.getEventById(event.id)?.let { dbItem ->
                repository.saveUserPrediction(record)
            }
        }
    }

    /**
     * Submits custom text to the Gemini Macro Forecaster
     */
    fun executeCustomMacroQuery() {
        val query = _customQueryText.value.trim()
        if (query.isEmpty()) return

        viewModelScope.launch {
            _isQueryLoading.value = true
            _errorMessage.value = null
            _customResponse.value = null
            
            try {
                val result = repository.queryCustomAIAnalysis(query)
                if (result != null) {
                    _customResponse.value = result
                } else {
                    _errorMessage.value = "Analysis timed out. Falling back to local forecasting engine."
                }
            } catch (e: Exception) {
                _errorMessage.value = "Network error: ${e.localizedMessage}"
            } finally {
                _isQueryLoading.value = false
            }
        }
    }

    fun discardCustomResponse() {
        _customResponse.value = null
        _customQueryText.value = ""
    }

    fun deletePrediction(id: Int) {
        viewModelScope.launch {
            repository.deletePrediction(id)
        }
    }

    fun deleteCustomAnalysis(analysis: CustomAnalysis) {
        viewModelScope.launch {
            repository.deleteCustomAnalysis(analysis)
        }
    }
}

class ForecastViewModelFactory(
    private val repository: ForecastRepository,
    private val authManager: com.example.managers.AuthManager,
    private val billingManager: com.example.managers.BillingManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ForecastViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ForecastViewModel(repository, authManager, billingManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

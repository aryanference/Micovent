package com.example.managers

import android.content.Context
import android.util.Log
import com.example.BuildConfig
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await

class AuthManager private constructor() {

    private var auth: FirebaseAuth? = null
    
    private val _currentUser = MutableStateFlow<FirebaseUser?>(null)
    val currentUser: StateFlow<FirebaseUser?> = _currentUser.asStateFlow()

    private val _isConfigured = MutableStateFlow(false)
    val isConfigured: StateFlow<Boolean> = _isConfigured.asStateFlow()

    fun initialize(context: Context) {
        val apiKey = BuildConfig.FIREBASE_API_KEY
        val appId = BuildConfig.FIREBASE_APP_ID
        val projectId = BuildConfig.FIREBASE_PROJECT_ID
        
        if (apiKey.isNullOrEmpty() || apiKey == "\"MOCK_API_KEY\"" || apiKey == "MOCK_API_KEY" || appId.isNullOrEmpty() || projectId.isNullOrEmpty()) {
            Log.w("AuthManager", "Firebase credentials missing in Secrets. Local fallback active.")
            _isConfigured.value = false
            return
        }

        try {
            if (FirebaseApp.getApps(context).isEmpty()) {
                val options = FirebaseOptions.Builder()
                    .setApiKey(apiKey)
                    .setApplicationId(appId)
                    .setProjectId(projectId)
                    .build()
                FirebaseApp.initializeApp(context, options)
            }
            auth = FirebaseAuth.getInstance()
            
            // Listen to auth state changes
            auth?.addAuthStateListener { firebaseAuth ->
                _currentUser.value = firebaseAuth.currentUser
            }
            
            _currentUser.value = auth?.currentUser
            _isConfigured.value = true
            
        } catch (e: Exception) {
            Log.e("AuthManager", "Failed to initialize Firebase", e)
            _isConfigured.value = false
        }
    }

    suspend fun signIn(email: String, pass: String): Result<FirebaseUser> {
        if (!_isConfigured.value) return Result.failure(Exception("Firebase not configured via AI Studio Secrets."))
        return try {
            val result = auth!!.signInWithEmailAndPassword(email, pass).await()
            Result.success(result.user!!)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun register(email: String, pass: String): Result<FirebaseUser> {
        if (!_isConfigured.value) return Result.failure(Exception("Firebase not configured via AI Studio Secrets."))
        return try {
            val result = auth!!.createUserWithEmailAndPassword(email, pass).await()
            Result.success(result.user!!)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun signOut() {
        auth?.signOut()
    }

    companion object {
        @Volatile
        private var instance: AuthManager? = null

        fun getInstance(): AuthManager {
            return instance ?: synchronized(this) {
                instance ?: AuthManager().also { instance = it }
            }
        }
    }
}

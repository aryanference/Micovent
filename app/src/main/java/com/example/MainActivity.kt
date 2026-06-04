package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.data.local.ForecastDatabase
import com.example.data.repository.ForecastRepository
import com.example.ui.ForecastViewModel
import com.example.ui.ForecastViewModelFactory
import com.example.ui.screens.MicoventTerminalApp
import com.example.ui.theme.MyApplicationTheme

import androidx.lifecycle.lifecycleScope
import com.example.managers.AuthManager
import com.example.managers.BillingManager

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    
    // Acquire the local Room database references and initiate our clean state pipelines
    val database = ForecastDatabase.getDatabase(applicationContext)
    val dao = database.forecastDao()
    val repository = ForecastRepository(dao)
    
    val authManager = AuthManager.getInstance()
    authManager.initialize(applicationContext)
    
    val billingManager = BillingManager(this, lifecycleScope)
    
    val viewModel: ForecastViewModel by viewModels {
      ForecastViewModelFactory(repository, authManager, billingManager)
    }
    
    setContent {
      MyApplicationTheme {
        MicoventTerminalApp(viewModel = viewModel)
      }
    }
  }
}

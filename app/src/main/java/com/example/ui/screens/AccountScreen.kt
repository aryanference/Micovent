package com.example.ui.screens

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.ForecastViewModel
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.ElectricOrange
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.SlateGray
import com.example.ui.theme.SurfaceBorder
import kotlinx.coroutines.launch

@Composable
fun AccountScreen(viewModel: ForecastViewModel) {
    val currentUser by viewModel.authManager.currentUser.collectAsState()
    val isConfigured by viewModel.authManager.isConfigured.collectAsState()
    val isProUser by viewModel.billingManager.isProUser.collectAsState()
    val availableProducts by viewModel.billingManager.availableProducts.collectAsState()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (!isConfigured) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF2A1015), RoundedCornerShape(8.dp))
                    .border(1.dp, Color(0xFF6B1D27), RoundedCornerShape(8.dp))
                    .padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Warning, contentDescription = "Warning", tint = ElectricOrange, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        "Firebase not configured. Please add FIREBASE_API_KEY, FIREBASE_APP_ID, and FIREBASE_PROJECT_ID to AI Studio Secrets.",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        lineHeight = 16.sp
                    )
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        if (currentUser == null) {
            // Auth section
            Text(
                "TERMINAL ACCESS IDENTITY",
                color = NeonCyan,
                fontSize = 14.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email", color = SlateGray, fontFamily = FontFamily.Monospace) },
                textStyle = LocalTextStyle.current.copy(color = Color.White),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NeonCyan,
                    unfocusedBorderColor = SurfaceBorder
                ),
                modifier = Modifier.fillMaxWidth().semantics { contentDescription = "Email Input" }
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password", color = SlateGray, fontFamily = FontFamily.Monospace) },
                textStyle = LocalTextStyle.current.copy(color = Color.White),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NeonCyan,
                    unfocusedBorderColor = SurfaceBorder
                ),
                modifier = Modifier.fillMaxWidth().semantics { contentDescription = "Password Input" }
            )
            
            errorMsg?.let {
                Spacer(modifier = Modifier.height(8.dp))
                Text(it, color = ElectricOrange, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
            }

            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            val result = viewModel.authManager.signIn(email, password)
                            if (result.isFailure) errorMsg = result.exceptionOrNull()?.message
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DarkSurface),
                    border = BorderStroke(1.dp, NeonCyan),
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier.weight(1f).padding(end = 8.dp)
                ) {
                    Text("AUTHORIZE", color = NeonCyan, fontFamily = FontFamily.Monospace, fontSize = 12.sp)
                }
                
                Button(
                    onClick = {
                        coroutineScope.launch {
                            val result = viewModel.authManager.register(email, password)
                            if (result.isFailure) errorMsg = result.exceptionOrNull()?.message
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier.weight(1f).padding(start = 8.dp)
                ) {
                    Text("REGISTER", color = Color.Black, fontFamily = FontFamily.Monospace, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        } else {
            // Logged in UI
            Text(
                "IDENTITY: ${currentUser?.email}",
                color = NeonGreen,
                fontSize = 14.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { viewModel.authManager.signOut() },
                colors = ButtonDefaults.buttonColors(containerColor = DarkSurface),
                border = BorderStroke(1.dp, SurfaceBorder),
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("DISCONNECT", color = SlateGray, fontFamily = FontFamily.Monospace, fontSize = 12.sp)
            }

            Spacer(modifier = Modifier.height(32.dp))
            HorizontalDivider(color = SurfaceBorder)
            Spacer(modifier = Modifier.height(32.dp))

            // Pro Subscription status
            Text(
                "MICOVENT TERMINAL // PRO",
                color = NeonCyan,
                fontSize = 14.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))

            if (isProUser) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF0F1A13), RoundedCornerShape(8.dp))
                        .border(1.dp, NeonGreen, RoundedCornerShape(8.dp))
                        .padding(16.dp)
                ) {
                    Text(
                        "PRIORITY PIPELINE ACTIVE. Unlimited AI queries and advanced historic modelling unlocked.",
                        color = NeonGreen,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        lineHeight = 16.sp
                    )
                }
            } else {
                Text(
                    "Standard clearance active. Deep research queries are rate-limited.",
                    color = SlateGray,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                val product = availableProducts.firstOrNull()
                Button(
                    onClick = {
                        if (product != null) {
                            (context as? Activity)?.let { activity ->
                                viewModel.billingManager.launchBillingFlow(activity, product)
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier.fillMaxWidth(),
                    enabled = product != null
                ) {
                    Text(
                        if (product != null) "UPGRADE CLEARANCE (${product.subscriptionOfferDetails?.firstOrNull()?.pricingPhases?.pricingPhaseList?.firstOrNull()?.formattedPrice ?: ""})"
                        else "CONNECTING TO BILLING NODE...",
                        color = Color.Black,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

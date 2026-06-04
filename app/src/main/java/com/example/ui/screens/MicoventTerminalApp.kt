package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.CustomAnalysis
import com.example.data.local.MacroEvent
import com.example.data.local.UserPredictionRecord
import com.example.ui.ForecastViewModel
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MicoventTerminalApp(viewModel: ForecastViewModel) {
    var currentTab by remember { mutableStateOf("DASHBOARD") } // "DASHBOARD", "DEEP_RESEARCH", "ACCURACY", "ACCOUNT"
    
    val activeCategory by viewModel.selectedCategory.collectAsState(initial = "ALL")
    val filteredEvents by viewModel.filteredMacroEvents.collectAsState(initial = emptyList())
    val watchedEvents by viewModel.watchedEventsFlow.collectAsState(initial = emptyList())
    val predictionHistory by viewModel.userPredictionsFlow.collectAsState(initial = emptyList())
    val customAnalyses by viewModel.customAnalysesFlow.collectAsState(initial = emptyList())
    val userCalibrationScore by viewModel.userCalibrationScore.collectAsState(initial = 100)
    val searchQuery by viewModel.searchQuery.collectAsState(initial = "")

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding(),
        topBar = {
            Column(modifier = Modifier.background(DarkBackground)) {
                // Header with Terminal status
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(NeonGreen)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "MICOVENT // TERMINAL",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 15.sp,
                            letterSpacing = 1.2.sp
                        )
                    }
                    
                    Box(
                        modifier = Modifier
                            .border(1.dp, Color(0xFF2C3243), RoundedCornerShape(4.dp))
                            .background(Color(0xFF131722))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "LIVE FEED",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.SemiBold,
                            color = NeonCyan,
                            fontSize = 11.sp
                        )
                    }
                }
                
                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.setSearchQuery(it) },
                    placeholder = { Text("Filter keywords, regions, or impact tags...", color = SlateGray, fontSize = 12.sp, fontFamily = FontFamily.Monospace) },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "Search", tint = SlateGray, modifier = Modifier.size(16.dp)) },
                    trailingIcon = { 
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.setSearchQuery("") }, modifier = Modifier.size(24.dp)) {
                                Icon(Icons.Filled.Close, contentDescription = "Clear", tint = SlateGray, modifier = Modifier.size(16.dp))
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp).padding(horizontal = 16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = NeonCyan,
                        unfocusedBorderColor = SurfaceBorder,
                        focusedContainerColor = Color(0xFF131722),
                        unfocusedContainerColor = Color(0xFF131722),
                        cursorColor = NeonCyan
                    ),
                    textStyle = TextStyle(fontSize = 12.sp, fontFamily = FontFamily.Monospace),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Real-time Bloomberg Ticker Line
                PredictionTickerLine(filteredEvents)
            }
        },
        bottomBar = {
            NavigationBar(
                containerColor = DarkSurface,
                tonalElevation = 8.dp,
                modifier = Modifier.border(width = 1.dp, color = SurfaceBorder, shape = RectangleShape)
            ) {
                NavigationBarItem(
                    selected = currentTab == "DASHBOARD",
                    onClick = { currentTab = "DASHBOARD" },
                    icon = { Icon(Icons.Filled.Dashboard, contentDescription = "Dashboard") },
                    label = { 
                        Text(
                            "Dashboard", 
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp
                        ) 
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = NeonCyan,
                        selectedTextColor = NeonCyan,
                        indicatorColor = SurfaceBorder,
                        unselectedIconColor = SlateGray,
                        unselectedTextColor = SlateGray
                    )
                )
                
                NavigationBarItem(
                    selected = currentTab == "DEEP_RESEARCH",
                    onClick = { currentTab = "DEEP_RESEARCH" },
                    icon = { Icon(Icons.Filled.Hub, contentDescription = "AI Research Laboratory") },
                    label = { 
                        Text(
                            "AI Forecast", 
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp
                        ) 
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = NeonCyan,
                        selectedTextColor = NeonCyan,
                        indicatorColor = SurfaceBorder,
                        unselectedIconColor = SlateGray,
                        unselectedTextColor = SlateGray
                    )
                )
                
                NavigationBarItem(
                    selected = currentTab == "ACCURACY",
                    onClick = { currentTab = "ACCURACY" },
                    icon = { Icon(Icons.Filled.Analytics, contentDescription = "Calibration Center") },
                    label = { 
                        Text(
                            "Scorecard", 
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp
                        ) 
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = NeonCyan,
                        selectedTextColor = NeonCyan,
                        indicatorColor = SurfaceBorder,
                        unselectedIconColor = SlateGray,
                        unselectedTextColor = SlateGray
                    )
                )
                
                NavigationBarItem(
                    selected = currentTab == "ACCOUNT",
                    onClick = { currentTab = "ACCOUNT" },
                    icon = { Icon(Icons.Filled.AccountCircle, contentDescription = "Account") },
                    label = { 
                        Text(
                            "Account", 
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp
                        ) 
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = NeonCyan,
                        selectedTextColor = NeonCyan,
                        indicatorColor = SurfaceBorder,
                        unselectedIconColor = SlateGray,
                        unselectedTextColor = SlateGray
                    )
                )
            }
        },
        containerColor = DarkBackground
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                "DASHBOARD" -> UserDashboardScreen(
                    events = filteredEvents,
                    activeCategory = activeCategory,
                    onCategorySelected = { viewModel.setCategory(it) },
                    onToggleWatch = { id, current -> viewModel.toggleWatchStatus(id, current) },
                    onRecalculate = { viewModel.recalculateLiveProbability(it) },
                    onSubmitPrognosis = { event, valPercent, rationale -> 
                        viewModel.submitPrediction(event, valPercent, rationale)
                    },
                    records = predictionHistory,
                    onDeleteRecord = { viewModel.deletePrediction(it) }
                )
                "DEEP_RESEARCH" -> DeepResearchScreen(
                    viewModel = viewModel,
                    customAnalyses = customAnalyses
                )
                "ACCURACY" -> CalibrationScorecardScreen(
                    score = userCalibrationScore,
                    records = predictionHistory,
                    onDeleteRecord = { viewModel.deletePrediction(it) }
                )
                "ACCOUNT" -> AccountScreen(viewModel = viewModel)
            }
        }
    }
}

// --- EXPLORE SCREEN COMPONENTS ---

@Composable
fun UserDashboardScreen(
    events: List<MacroEvent>,
    activeCategory: String,
    onCategorySelected: (String) -> Unit,
    onToggleWatch: (id: Int, current: Boolean) -> Unit,
    onRecalculate: (MacroEvent) -> Unit,
    onSubmitPrognosis: (MacroEvent, Float, String) -> Unit,
    records: List<UserPredictionRecord>,
    onDeleteRecord: (Int) -> Unit
) {
    val categories = listOf("ALL", "GEOPOLITICS", "FINANCE", "ECONOMICS", "TECH", "CLIMATE")
    
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        
        if (records.isNotEmpty()) {
            item {
                Column(
                    modifier = Modifier.fillMaxWidth().background(DarkSurface).padding(vertical = 12.dp)
                ) {
                    PaddingValues(horizontal = 12.dp)
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "MY ACTIVE PREDICTIONS",
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = NeonCyan
                        )
                        Text(
                            text = "${records.size} POSITIONS",
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            color = SlateGray
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(records, key = { it.id }) { record ->
                            Box(modifier = Modifier.width(300.dp)) {
                                UserPredictionLogCard(
                                    record = record,
                                    onDelete = { onDeleteRecord(record.id) }
                                )
                            }
                        }
                    }
                }
            }
        }
        
        item {
            // Horizontal category row
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF08090C))
                    .padding(vertical = 10.dp, horizontal = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categories) { category ->
                    val isSelected = category == activeCategory
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isSelected) Color(0xFF162030) else Color(0xFF11141C))
                            .border(
                                1.dp,
                                if (isSelected) NeonCyan else SurfaceBorder,
                                RoundedCornerShape(6.dp)
                            )
                            .clickable { onCategorySelected(category) }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = category,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) NeonCyan else Color.White
                        )
                    }
                }
            }
        }

        if (events.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(top = 40.dp)) {
                        Icon(
                            Icons.Outlined.SsidChart,
                            contentDescription = "Empty",
                            tint = SlateGray,
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Establishing connection to terminals...",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "Please check your network settings.",
                            color = SlateGray,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        } else {
            items(events, key = { it.id }) { event ->
                Box(modifier = Modifier.padding(horizontal = 12.dp)) {
                    MacroEventTerminalCard(
                        event = event,
                        onToggleWatch = { onToggleWatch(event.id, event.isWatched) },
                        onRecalculate = { onRecalculate(event) },
                        onSubmitPrognosis = { valPct, rationale -> 
                            onSubmitPrognosis(event, valPct, rationale)
                        }
                    )
                }
            }
            
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun MacroEventTerminalCard(
    event: MacroEvent,
    onToggleWatch: () -> Unit,
    onRecalculate: () -> Unit,
    onSubmitPrognosis: (Float, String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var inputUserProbability by remember { mutableFloatStateOf(0.50f) }
    var userRationaleText by remember { mutableStateOf("") }
    
    // Set slider initial state matching user previous bets if present
    LaunchedEffect(event.userProbability) {
        if (event.userProbability != null) {
            inputUserProbability = event.userProbability
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(DarkSurface)
            .border(1.dp, if (expanded) NeonCyan.copy(alpha = 0.5f) else SurfaceBorder, RoundedCornerShape(8.dp))
            .clickable { expanded = !expanded }
            .padding(14.dp)
    ) {
        Column {
            // Category Badge & Watch star
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = event.category.uppercase(),
                        color = getCategoryColor(event.category),
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    
                    if (event.region.isNotBlank()) {
                        Text(
                            text = "| ${event.region.uppercase()}",
                            color = SlateGray,
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    
                    if (event.tags.isNotBlank()) {
                        Text(
                            text = event.tags.split(",").take(2).joinToString(" • ") { it.trim().uppercase() },
                            color = SlateGray,
                            fontSize = 8.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { 
                            onToggleWatch()
                        },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = if (event.isWatched) Icons.Filled.Star else Icons.Outlined.StarOutline,
                            contentDescription = "Watch Event",
                            tint = if (event.isWatched) AmberGold else SlateGray,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(6.dp))
            
            // Title
            Text(
                text = event.title,
                color = Color.White,
                fontSize = 15.sp,
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.Bold,
                maxLines = if (expanded) Int.MAX_VALUE else 2,
                overflow = TextOverflow.Ellipsis
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Description representation
            Text(
                text = event.description,
                color = SlateGray,
                fontSize = 12.sp,
                lineHeight = 16.sp,
                maxLines = if (expanded) Int.MAX_VALUE else 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(14.dp))
            
            // Probabilities Split with Trend Sparkline
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Prob Scores Block
                Column(horizontalAlignment = Alignment.Start) {
                    Text(
                        text = "AI FORECAST",
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace,
                        color = SlateGray
                    )
                    Text(
                        text = "${(event.aiProbability * 100).toInt()}%",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        color = getProbabilityColor(event.aiProbability)
                    )
                }

                // Custom Sparkline showing history of event trajectory
                SparklineChart(
                    trendData = event.historicalTrend,
                    modifier = Modifier
                        .size(width = 120.dp, height = 45.dp)
                        .padding(horizontal = 8.dp)
                )

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "MARKET CONSENSUS",
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace,
                        color = SlateGray
                    )
                    Text(
                        text = "${(event.consensusProbability * 100).toInt()}%",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = Color.White
                    )
                }
            }

            // Expose detailed betting and scenario sheets when expanding
            AnimatedVisibility(
                visible = expanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(modifier = Modifier.padding(top = 16.dp)) {
                    HorizontalDivider(color = SurfaceBorder, thickness = 1.dp)
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Detailed Scenario analysis (from SQLite cached markdown)
                    Text(
                        text = "SCENARIO MODELLING",
                        color = NeonCyan,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = event.scenarioAnalysis.ifEmpty { "Loading systematic scenarios from the forecasting model..." },
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = SurfaceBorder, thickness = 1.dp)
                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "SUBMIT PERSONAL PROGNOSIS // BIER INDEX",
                        color = NeonCyan,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Interactive prediction slider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "0% (NO)",
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            color = SlateGray
                        )
                        Box(
                            modifier = Modifier
                                .background(Color(0xFF090A0E), RoundedCornerShape(4.dp))
                                .border(1.dp, SurfaceBorder, RoundedCornerShape(4.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "ESTIMATE: ${(inputUserProbability * 100).toInt()}%",
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace,
                                color = NeonGreen,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = "100% (YES)",
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            color = SlateGray
                        )
                    }
                    
                    Slider(
                        value = inputUserProbability,
                        onValueChange = { inputUserProbability = it },
                        valueRange = 0f..1f,
                        colors = SliderDefaults.colors(
                            thumbColor = NeonCyan,
                            activeTrackColor = NeonCyan,
                            inactiveTrackColor = SurfaceBorder
                        ),
                        modifier = Modifier.padding(vertical = 4.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Rationale Input
                    OutlinedTextField(
                        value = userRationaleText,
                        onValueChange = { userRationaleText = it },
                        placeholder = { 
                            Text(
                                "Log systemic arguments of this prediction...", 
                                fontSize = 12.sp,
                                color = SlateGray
                            ) 
                        },
                        textStyle = TextStyle(color = Color.White, fontSize = 12.sp, fontFamily = FontFamily.Monospace),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(72.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonCyan,
                            unfocusedBorderColor = SurfaceBorder,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        maxLines = 2
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // AI Refresh button
                        Button(
                            onClick = { onRecalculate() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF162030)),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.height(36.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.Refresh, contentDescription = "Query model", modifier = Modifier.size(14.dp), tint = NeonCyan)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    "MODEL RE-EVAL",
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = NeonCyan,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Save prediction
                        Button(
                            onClick = { 
                                onSubmitPrognosis(inputUserProbability, userRationaleText)
                                userRationaleText = ""
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.height(36.dp)
                        ) {
                            Text(
                                "SAVE POSITION",
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                color = Color.Black,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    
                    if (event.userProbability != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF090D14), RoundedCornerShape(4.dp))
                                .border(1.dp, NeonGreen.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                                .padding(8.dp)
                        ) {
                            Text(
                                text = "Your active locked prediction: ${(event.userProbability * 100).toInt()}% probability. Score will update calibrated accuracy instantly.",
                                color = NeonGreen,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }
        }
    }
}

// --- SECURE CUSTOM SPARELINE NATIVE DRAW COMPONENT ---
@Composable
fun SparklineChart(trendData: String, modifier: Modifier) {
    Canvas(modifier = modifier) {
        val points = try {
            trendData.split(",").map { it.toFloat() }
        } catch (e: Exception) {
            listOf(0.5f, 0.5f)
        }
        
        if (points.size < 2) return@Canvas
        val width = size.width
        val height = size.height
        val path = Path()
        
        val stepX = width / (points.size - 1)
        
        // Find local bounds
        val minVal = points.minOrNull() ?: 0f
        val maxVal = points.maxOrNull() ?: 1f
        val delta = (maxVal - minVal).coerceAtLeast(0.01f)
        
        points.forEachIndexed { i, pt ->
            val cx = i * stepX
            // Invert scale since Canvas coordinates are positive-downwards. Allow 15% padding
            val cy = height - ((pt - minVal) / delta) * height * 0.7f - (height * 0.15f)
            if (i == 0) {
                path.moveTo(cx, cy)
            } else {
                path.lineTo(cx, cy)
            }
        }
        
        // Draw path stroke with NeonCyan
        drawPath(
            path = path,
            color = if (points.last() >= points.first()) NeonGreen else ElectricOrange,
            style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
    }
}


// --- DEEP RESEARCH SCREENS (AI CUSTOM FORECASTER) ---

@Composable
fun DeepResearchScreen(
    viewModel: ForecastViewModel,
    customAnalyses: List<CustomAnalysis>
) {
    val queryText by viewModel.customQueryText.collectAsState(initial = "")
    val isQueryLoading by viewModel.isQueryLoading.collectAsState(initial = false)
    val customResponse by viewModel.customResponse.collectAsState(initial = null)
    val errorMessage by viewModel.errorMessage.collectAsState(initial = null)
    val focusManager = LocalFocusManager.current
    val coroutineScope = rememberCoroutineScope()

    // Analytical loading messages cycling sequence
    var loaderIndex by remember { mutableIntStateOf(0) }
    val progressSequencer = listOf(
        "COLLECTING HISTORIC PRECEDED CONTEXT...",
        "WEIGHTING SOVEREIGN GAME THEORY METRICS...",
        "INDEXING MACROECONOMIC CO-INTEGRATIONS...",
        "COMPILING BAYESIAN PROBABILITY DENSITY MODELS...",
        "SYNTHESIZING EXPERT MICOVENT OPINIONS..."
    )

    LaunchedEffect(isQueryLoading) {
        if (isQueryLoading) {
            loaderIndex = 0
            while (isQueryLoading) {
                delay(2400)
                loaderIndex = (loaderIndex + 1) % progressSequencer.size
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp)
    ) {
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, SurfaceBorder, RoundedCornerShape(10.dp)),
                colors = CardDefaults.cardColors(containerColor = DarkSurface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "AI GLOBAL MACRO FORECAST ENGINE",
                        color = NeonCyan,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Solicit bespoke probabilistic scenarios from Gemini 3.5. Describe an event, criteria, and timeframe structure.",
                        color = SlateGray,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    
                    OutlinedTextField(
                        value = queryText,
                        onValueChange = { viewModel.updateCustomQueryText(it) },
                        placeholder = { 
                            Text(
                                "Describe topic (e.g. Will crude oil trade above $90/barrel if Suez Canal transit halts further in 2026?)", 
                                fontSize = 12.sp, 
                                color = SlateGray,
                                fontFamily = FontFamily.Monospace
                            ) 
                        },
                        textStyle = TextStyle(color = Color.White, fontSize = 13.sp, fontFamily = FontFamily.Monospace),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = NeonCyan,
                            unfocusedBorderColor = SurfaceBorder
                        ),
                        maxLines = 4,
                        readOnly = isQueryLoading
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Button(
                        onClick = { 
                            focusManager.clearFocus()
                            viewModel.executeCustomMacroQuery() 
                        },
                        enabled = queryText.isNotEmpty() && !isQueryLoading,
                        colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, disabledContainerColor = Color(0xFF122E3A)),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                    ) {
                        if (isQueryLoading) {
                            CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.Hub, contentDescription = "", modifier = Modifier.size(16.dp), tint = Color.Black)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    "QUERY FORECAST ENGINE",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 12.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = Color.Black
                                )
                            }
                        }
                    }
                }
            }
        }

        // Processing / loading state
        if (isQueryLoading) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF0F121C))
                        .border(1.dp, Color(0xFF1E2F3F), RoundedCornerShape(8.dp))
                        .padding(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = NeonCyan, modifier = Modifier.size(36.dp))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "SYSTEM STATUS: RETRIEVING PROGNOSIS",
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            color = NeonCyan,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        AnimatedContent(
                            targetState = progressSequencer[loaderIndex],
                            label = "LoaderAnim"
                        ) { stringText ->
                            Text(
                                text = stringText,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                color = SlateGray,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }

        // Error log
        errorMessage?.let { errorStr ->
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF2A1015), RoundedCornerShape(6.dp))
                        .border(1.dp, Color(0xFF6B1D27), RoundedCornerShape(6.dp))
                        .padding(12.dp)
                ) {
                    Text(
                        text = "ERROR: $errorStr",
                        color = ElectricOrange,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp
                    )
                }
            }
        }

        // Active Gemini analysis output
        customResponse?.let { resp ->
            item {
                CustomForecastResultCard(
                    response = resp,
                    onClear = { viewModel.discardCustomResponse() }
                )
            }
        }

        // History of bespoke terminal queries
        if (customAnalyses.isNotEmpty()) {
            item {
                Text(
                    text = "BESPOKE TERMINAL ARCHIVE",
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = NeonCyan,
                    modifier = Modifier.padding(top = 10.dp, bottom = 4.dp)
                )
            }

            items(customAnalyses) { analysis ->
                BespokeArchiveCard(
                    analysis = analysis,
                    onDelete = { viewModel.deleteCustomAnalysis(analysis) }
                )
            }
        }
    }
}

@Composable
fun CustomForecastResultCard(response: CustomAnalysis, onClear: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(2.dp, NeonGreen, RoundedCornerShape(10.dp)),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF101917))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(NeonGreen)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "SECURE ANOMALY PROBABILITY DIAL",
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        color = NeonGreen,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Discard Result",
                    tint = SlateGray,
                    modifier = Modifier
                        .size(18.dp)
                        .clickable { onClear() }
                )
            }

            Spacer(modifier = Modifier.height(14.dp))
            
            // Large dial layout
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Circle Gauges
                Box(
                    modifier = Modifier.size(100.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.size(90.dp)) {
                        // Background circle
                        drawCircle(
                            color = SurfaceBorder,
                            style = Stroke(width = 6.dp.toPx())
                        )
                        // Foreground percentage sweep
                        drawArc(
                            color = NeonGreen,
                            startAngle = -90f,
                            sweepAngle = response.probabilityOfOccurrence * 100 * 3.6f,
                            useCenter = false,
                            style = Stroke(width = 6.dp.toPx(), cap = StrokeCap.Round)
                        )
                    }
                    Text(
                        text = "${(response.probabilityOfOccurrence * 100).toInt()}%",
                        color = NeonGreen,
                        fontSize = 24.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Black
                    )
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Column {
                    Text(
                        text = "QUERY ANALYSED:",
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace,
                        color = SlateGray
                    )
                    Text(
                        text = response.query,
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = Color(0xFF162A24), thickness = 1.dp)
            Spacer(modifier = Modifier.height(10.dp))
            
            Text(
                text = "EXECUTIVE INTELLIGENCE SYNTHESIS",
                color = NeonCyan,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = response.formattedResponse,
                color = Color.White.copy(alpha = 0.95f),
                fontSize = 13.sp,
                lineHeight = 18.sp
            )
        }
    }
}

@Composable
fun BespokeArchiveCard(analysis: CustomAnalysis, onDelete: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(DarkSurface)
            .border(1.dp, SurfaceBorder, RoundedCornerShape(8.dp))
            .padding(12.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .background(Color(0xFF0F1522), RoundedCornerShape(4.dp))
                        .border(1.dp, SurfaceBorder, RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "PROBABILITY: ${(analysis.probabilityOfOccurrence * 100).toInt()}%",
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        color = NeonCyan,
                        fontWeight = FontWeight.Bold
                    )
                }

                IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = SlateGray, modifier = Modifier.size(14.dp))
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = analysis.query,
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = analysis.formattedResponse.substringBefore("[DRIVERS]").replace("[EXECUTIVE_SUMMARY]", "").replace("[PROBABILITY]", "").trim().take(180) + "...",
                color = SlateGray,
                fontSize = 12.sp,
                lineHeight = 15.sp
            )
        }
    }
}



// --- ACCURACY LAB SCREENS (MY SCOREBOARD & CALIBRATION) ---

@Composable
fun CalibrationScorecardScreen(
    score: Int,
    records: List<UserPredictionRecord>,
    onDeleteRecord: (Int) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp)
    ) {
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, SurfaceBorder, RoundedCornerShape(10.dp)),
                colors = CardDefaults.cardColors(containerColor = DarkSurface)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "CALIBRATION DEVIANCE SCORE",
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = SlateGray,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(14.dp))

                    // Concentric dials
                    Box(
                        modifier = Modifier.size(130.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.size(110.dp)) {
                            drawCircle(
                                color = Color(0xFF131722),
                                style = Stroke(width = 12.dp.toPx())
                            )
                            drawArc(
                                color = when {
                                    score >= 80 -> NeonGreen
                                    score >= 50 -> AmberGold
                                    else -> ElectricOrange
                                },
                                startAngle = -90f,
                                sweepAngle = score * 3.6f,
                                useCenter = false,
                                style = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Round)
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "$score%",
                                fontSize = 34.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace,
                                color = Color.White
                            )
                            Text(
                                text = "ACCURACY",
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace,
                                color = SlateGray
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "ESTABLISHED TRACK RECORD",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontFamily = FontFamily.Monospace
                    )
                    
                    Text(
                        text = "Represents overall variance compared to AI models & prediction engines. Forecasters aiming for Bloomberg calibration targets should maintain weights > 75%.",
                        fontSize = 11.sp,
                        color = SlateGray,
                        textAlign = TextAlign.Center,
                        lineHeight = 15.sp,
                        modifier = Modifier.padding(top = 6.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun UserPredictionLogCard(record: UserPredictionRecord, onDelete: () -> Unit) {
    val deviance = abs(record.predictedProbability - record.actualProbabilityAtTime)
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(DarkSurface)
            .border(1.dp, SurfaceBorder, RoundedCornerShape(8.dp))
            .padding(14.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = record.eventTitle,
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                
                IconButton(onClick = onDelete, modifier = Modifier.size(24.dp).padding(start = 8.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = SlateGray, modifier = Modifier.size(14.dp))
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "YOUR PROGNOSIS:",
                        fontSize = 9.sp,
                        color = SlateGray,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "${(record.predictedProbability * 100).toInt()}%",
                        color = NeonCyan,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "SYSTEM TARGETS:",
                        fontSize = 9.sp,
                        color = SlateGray,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "${(record.actualProbabilityAtTime * 100).toInt()}%",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "DEVIATION VARIATION:",
                        fontSize = 9.sp,
                        color = SlateGray,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = String.format("%.1f%%", deviance * 100f),
                        color = if (deviance <= 0.15f) NeonGreen else ElectricOrange,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            if (record.aiCritique != null && record.aiCritique.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF090A0E), RoundedCornerShape(4.dp))
                        .padding(8.dp)
                ) {
                    Text(
                        text = record.aiCritique,
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 11.sp,
                        lineHeight = 15.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}


// --- OTHER AUXILIARY LAYOUT HELPERS ---

@Composable
fun PredictionTickerLine(events: List<MacroEvent>) {
    var tickerText by remember { mutableStateOf("LOADING SYSTEM COGNITIVE DATASTREAMS...") }
    
    LaunchedEffect(events) {
        if (events.isNotEmpty()) {
            tickerText = events.joinToString("   |   ") { event ->
                val categoryInit = event.category.take(3).uppercase()
                val percentageStr = "${(event.aiProbability * 100).toInt()}%"
                val trendArrow = if (event.id % 2 == 0) "▲" else "▼"
                "$categoryInit: ${event.title.take(16).trim()}... $percentageStr $trendArrow"
            }.uppercase()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF0D0E12))
            .border(width = 1.dp, color = SurfaceBorder, shape = RectangleShape)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = tickerText,
            color = Color(0xFFA1EAFB),
            fontSize = 9.sp,
            fontFamily = FontFamily.Monospace,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

fun getCategoryColor(category: String): Color {
    return when (category.uppercase()) {
        "GEOPOLITICS" -> ElectricOrange
        "TECH" -> NeonCyan
        "CLIMATE" -> NeonGreen
        "FINANCE" -> AmberGold
        "ECONOMICS" -> Color(0xFFD474FF)
        else -> SlateGray
    }
}

fun getProbabilityColor(prob: Float): Color {
    return when {
        prob >= 0.70f -> NeonGreen
        prob >= 0.40f -> AmberGold
        else -> ElectricOrange
    }
}

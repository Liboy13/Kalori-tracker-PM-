package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.DailyProgress
import com.example.data.model.FoodLog
import com.example.data.model.WeightRecord
import com.example.ui.theme.*
import com.example.ui.viewmodel.ChatMessage
import com.example.ui.viewmodel.FuelTrackViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: FuelTrackViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FUEL_TRACK_Theme {
                FuelTrackApp(viewModel)
            }
        }
    }
}

// Custom components following High-Impact Brutalism definitions:
// - Sharp corners (0.dp shapes)
// - Thick architectural solid black strokes (3.dp)
// - Hard offset layout shadows (4.dp down and right)

@Composable
fun BrutalistCard(
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color.White,
    borderColor: Color = PureBlack,
    shadowColor: Color = PureBlack,
    borderWidth: Dp = 3.dp,
    shadowOffset: Dp = 4.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    Box(modifier = modifier) {
        // Shadow Background Layer
        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(x = shadowOffset, y = shadowOffset)
                .background(shadowColor)
        )
        // Content Foreground Layer (Strict Sharp Corners)
        Column(
            modifier = Modifier
                .background(backgroundColor)
                .border(borderWidth, borderColor)
                .padding(16.dp)
        ) {
            content()
        }
    }
}

@Composable
fun BrutalistButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    backgroundColor: Color = ElectricBlue,
    contentColor: Color = SolidWhite,
    shadowOffset: Dp = 4.dp,
    testTagStr: String = "brutalist_btn",
    content: @Composable RowScope.() -> Unit
) {
    Box(
        modifier = modifier
            .testTag(testTagStr)
            .clickable(onClick = onClick)
    ) {
        // Shadow Background
        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(x = shadowOffset, y = shadowOffset)
                .background(PureBlack)
        )
        // Active Button Cap
        Row(
            modifier = Modifier
                .background(backgroundColor)
                .border(3.dp, PureBlack)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            ProvideTextStyle(value = MaterialTheme.typography.labelLarge.copy(color = contentColor)) {
                content()
            }
        }
    }
}

@Composable
fun FuelTrackApp(viewModel: FuelTrackViewModel) {
    var activeTab by remember { mutableStateOf(0) }
    var showAddDialog by remember { mutableStateOf(false) }
    var preselectedSessionForAdd by remember { mutableStateOf("BREAKFAST") }

    val selectedDate by viewModel.selectedDate.collectAsStateWithLifecycle()
    val foodLogs by viewModel.currentFoodLogs.collectAsStateWithLifecycle()
    val progress by viewModel.currentDailyProgress.collectAsStateWithLifecycle()
    val weightHistory by viewModel.weightHistory.collectAsStateWithLifecycle()
    val systemAnalysisText by viewModel.systemAnalysis.collectAsStateWithLifecycle()

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding(),
        topBar = {
            TopAppBar(
                selectedDate = selectedDate,
                onPreviousDate = { viewModel.previousDate() },
                onNextDate = { viewModel.nextDate() }
            )
        },
        bottomBar = {
            BottomNavigationBar(
                activeTab = activeTab,
                onTabSelected = { activeTab = it }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(BrutalistSurface)
                .padding(innerPadding)
        ) {
            when (activeTab) {
                0 -> DashboardScreen(
                    viewModel = viewModel,
                    foodLogs = foodLogs,
                    progress = progress,
                    onAddPressed = {
                        preselectedSessionForAdd = "BREAKFAST"
                        showAddDialog = true
                    }
                )
                1 -> DiaryScreen(
                    viewModel = viewModel,
                    foodLogs = foodLogs,
                    selectedDate = selectedDate,
                    onAddFoodForMeal = { meal ->
                        preselectedSessionForAdd = meal
                        showAddDialog = true
                    }
                )
                2 -> AiInsightScreen(
                    viewModel = viewModel,
                    analysisText = systemAnalysisText,
                    foodLogs = foodLogs
                )
                3 -> ProfileScreen(
                    viewModel = viewModel,
                    weightHistory = weightHistory,
                    progress = progress
                )
            }

            if (showAddDialog) {
                AddFoodDialog(
                    initialSession = preselectedSessionForAdd,
                    onDismiss = { showAddDialog = false },
                    onConfirm = { session, name, portion, calories, p, c, f ->
                        viewModel.addFoodLog(session, name, portion, calories, p, c, f)
                        showAddDialog = false
                    }
                )
            }
        }
    }
}

@Composable
fun TopAppBar(
    selectedDate: String,
    onPreviousDate: () -> Unit,
    onNextDate: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(SolidWhite)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "SYSTEM: FUEL_TRACK",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = PureBlack.copy(alpha = 0.5f),
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = "DASHBOARD",
                    style = MaterialTheme.typography.headlineLarge.copy(fontSize = 24.sp),
                    fontWeight = FontWeight.Black,
                    color = PureBlack
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                // High density brutalist right square deco matching Tailwind style
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(PureBlack)
                        .clickable {}
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .offset(x = (-3).dp, y = (-3).dp)
                            .background(SolidWhite)
                            .border(2.dp, PureBlack),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .background(ElectricBlue)
                        )
                    }
                }
            }
        }

        // Date selector sub-header - tighter vertical padding
        Box(modifier = Modifier.fillMaxWidth().height(3.dp).background(PureBlack))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(LightGreyAccent)
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onPreviousDate,
                modifier = Modifier
                    .size(32.dp)
                    .border(2.dp, PureBlack)
                    .background(SolidWhite)
            ) {
                Text("<", fontWeight = FontWeight.Bold, color = PureBlack)
            }

            Text(
                text = selectedDate,
                style = MaterialTheme.typography.labelLarge.copy(fontSize = 12.sp),
                fontWeight = FontWeight.Black,
                color = PureBlack,
                textAlign = TextAlign.Center
            )

            IconButton(
                onClick = onNextDate,
                modifier = Modifier
                    .size(32.dp)
                    .border(2.dp, PureBlack)
                    .background(SolidWhite)
            ) {
                Text(">", fontWeight = FontWeight.Bold, color = PureBlack)
            }
        }
        Box(modifier = Modifier.fillMaxWidth().height(3.dp).background(PureBlack))
    }
}

@Composable
fun BottomNavigationBar(activeTab: Int, onTabSelected: (Int) -> Unit) {
    val items = listOf("DIAGRAM", "LOG HARIAN", "INSIGHT AI", "PROFIL")
    val icons = listOf("📊", "📖", "🧠", "👤")

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(SolidWhite)
            .navigationBarsPadding()
    ) {
        Box(modifier = Modifier.fillMaxWidth().height(3.dp).background(PureBlack))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(73.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEachIndexed { index, title ->
                val isActive = activeTab == index
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable { onTabSelected(index) }
                        .background(if (isActive) ElectricBlue.copy(alpha = 0.1f) else Color.Transparent),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(4.dp)
                    ) {
                        Text(
                            text = icons[index],
                            fontSize = if (isActive) 22.sp else 18.sp,
                            color = if (isActive) ElectricBlue else PureBlack
                        )
                        Text(
                            text = title,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (isActive) FontWeight.Black else FontWeight.Bold,
                            color = if (isActive) ElectricBlue else PureBlack,
                            fontSize = 10.sp
                        )
                    }
                    if (isActive) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                                .background(ElectricBlue)
                                .align(Alignment.TopCenter)
                        )
                    }
                }
            }
        }
    }
}

// --- TAB 1: DASHBOARD ---
@Composable
fun DashboardScreen(
    viewModel: FuelTrackViewModel,
    foodLogs: List<FoodLog>,
    progress: DailyProgress,
    onAddPressed: () -> Unit
) {
    val totalGoal = 2400
    val totalEaten = foodLogs.sumOf { it.calories }
    val exerciseAdded = 250 // constant seeded mockup value
    val remaining = (totalGoal - totalEaten + exerciseAdded).coerceAtLeast(0)

    val totalP = foodLogs.sumOf { it.protein }
    val totalC = foodLogs.sumOf { it.carbs }
    val totalF = foodLogs.sumOf { it.fat }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 80.dp),
            contentPadding = PaddingValues(14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header Info Status
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "STATUS HARI INI",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp, fontWeight = FontWeight.Black),
                            color = PureBlack.copy(alpha = 0.6f)
                        )
                        Text(
                            text = "KONSUMSI & ENERGI",
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Black),
                            color = PureBlack
                        )
                    }
                    Box(
                        modifier = Modifier
                            .background(ElectricBlue)
                            .border(2.dp, PureBlack)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "SASARAN: $totalGoal KCAL",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                            color = SolidWhite,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }

            // High Density Calories Remaining Card from Design HTML
            item {
                BrutalistCard(
                    backgroundColor = SolidWhite,
                    modifier = Modifier.fillMaxWidth(),
                    shadowOffset = 6.dp
                ) {
                    Text(
                        text = "SISA KALORI",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        ),
                        color = PureBlack.copy(alpha = 0.6f)
                    )
                    
                    Spacer(modifier = Modifier.height(2.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.Start
                    ) {
                        Text(
                            text = String.format(java.util.Locale("id", "ID"), "%,d", remaining),
                            style = MaterialTheme.typography.displayLarge.copy(fontSize = 58.sp),
                            fontWeight = FontWeight.Black,
                            color = ElectricBlue,
                            modifier = Modifier.alignByBaseline()
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "KCAL",
                            style = MaterialTheme.typography.headlineMedium.copy(fontSize = 20.sp, fontWeight = FontWeight.Black),
                            modifier = Modifier.alignByBaseline()
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Progress Bar matching Design HTML ("65% Efisiensi Terpakai")
                    val efficiencyPct = if (totalGoal > 0) ((totalEaten.toFloat() / totalGoal.toFloat()) * 100).toInt().coerceIn(0, 100) else 0
                    val fillFraction = (totalEaten.toFloat() / totalGoal.toFloat()).coerceIn(0f, 1f)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(30.dp)
                            .border(3.dp, PureBlack)
                            .background(SolidWhite)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(fillFraction)
                                .background(ElectricBlue)
                        )
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "$efficiencyPct% EFISIENSI TERPAKAI",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 0.5.sp,
                                    color = if (fillFraction > 0.45f) SolidWhite else PureBlack
                                )
                            )
                        }
                    }
                }
            }

            // Gemini AI Overview Section matching Design HTML
            item {
                BrutalistCard(
                    backgroundColor = ElectricBlue,
                    modifier = Modifier.fillMaxWidth(),
                    shadowOffset = 6.dp
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Flashing pulsing point
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .background(SolidWhite)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "GEMINI 1.5 ANALYSIS",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp
                            ),
                            color = SolidWhite
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (viewModel.systemAnalysis.value.isNotBlank() && viewModel.systemAnalysis.value != "Menganalisis nutrisi harian Anda...") {
                            "\"${viewModel.systemAnalysis.value.uppercase()}\""
                        } else {
                            "\"STATUS: ANABOLIK. TINGKATKAN PROTEIN +25G PADA SESI BERIKUTNYA UNTUK OPTIMALISASI OTOT.\""
                        },
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Black,
                            lineHeight = 16.sp
                        ),
                        color = SolidWhite,
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                }
            }

            // High Density Macro Grid matching Design HTML exactly
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Protein
                    Box(modifier = Modifier.weight(1f)) {
                        BrutalistCard(
                            backgroundColor = SolidWhite,
                            modifier = Modifier.fillMaxWidth(),
                            shadowOffset = 4.dp
                        ) {
                            Text(
                                text = "PROTEIN",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Black),
                                color = PureBlack.copy(alpha = 0.5f),
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${totalP}G",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black, fontSize = 18.sp),
                                color = PureBlack,
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                            )
                        }
                    }
                    // Karbo
                    Box(modifier = Modifier.weight(1f)) {
                        BrutalistCard(
                            backgroundColor = SolidWhite,
                            modifier = Modifier.fillMaxWidth(),
                            shadowOffset = 4.dp
                        ) {
                            Text(
                                text = "KARBO",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Black),
                                color = PureBlack.copy(alpha = 0.5f),
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${totalC}G",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black, fontSize = 18.sp),
                                color = PureBlack,
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                            )
                        }
                    }
                    // Lemak
                    Box(modifier = Modifier.weight(1f)) {
                        BrutalistCard(
                            backgroundColor = SolidWhite,
                            modifier = Modifier.fillMaxWidth(),
                            shadowOffset = 4.dp
                        ) {
                            Text(
                                text = "LEMAK",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Black),
                                color = PureBlack.copy(alpha = 0.5f),
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${totalF}G",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black, fontSize = 18.sp),
                                color = PureBlack,
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                            )
                        }
                    }
                }
            }

            // Water & Steps Bento section
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Water Card
                    Box(modifier = Modifier.weight(1f)) {
                        BrutalistCard(
                            backgroundColor = ElectricBlue,
                            modifier = Modifier.fillMaxWidth(),
                            shadowOffset = 4.dp
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "💧 AIR",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black),
                                    color = SolidWhite
                                )
                                Text(
                                    text = "LOGGED",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
                                    color = SolidWhite.copy(alpha = 0.8f)
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "${progress.waterLogged}/${progress.waterGoal}L",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black, fontSize = 22.sp),
                                color = SolidWhite
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            // Quick increment button
                            Box(
                                modifier = Modifier
                                    .clickable { viewModel.addWater(0.25f) }
                                    .border(2.dp, PureBlack)
                                    .background(SolidWhite)
                                    .padding(vertical = 4.dp, horizontal = 10.dp)
                                    .align(Alignment.End)
                            ) {
                                Text(
                                    text = "+250ML",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Black),
                                    color = PureBlack
                                )
                            }
                        }
                    }

                    // Steps Card
                    Box(modifier = Modifier.weight(1f)) {
                        BrutalistCard(
                            backgroundColor = LightGreyAccent,
                            modifier = Modifier.fillMaxWidth(),
                            shadowOffset = 4.dp
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "🚶 LANGKAH",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black),
                                    color = PureBlack
                                )
                                Text(
                                    text = "${progress.stepsLogged}",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                                    color = PureBlack
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))

                            // Custom Brutalist progress bar
                            val fillPct = (progress.stepsLogged.toFloat() / progress.stepsGoal.toFloat()).coerceIn(0f, 1f)
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(16.dp)
                                    .background(SolidWhite)
                                    .border(2.dp, PureBlack)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .fillMaxWidth(fillPct)
                                        .background(PureBlack)
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Box(
                                modifier = Modifier
                                    .clickable { viewModel.addSteps(1000) }
                                    .border(2.dp, PureBlack)
                                    .background(SolidWhite)
                                    .padding(vertical = 4.dp, horizontal = 10.dp)
                                    .align(Alignment.End)
                            ) {
                                Text(
                                    text = "+1K STEPS",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Black),
                                    color = PureBlack
                                )
                            }
                        }
                    }
                }
            }

            // Macro Breakdown Section Progress detail
            item {
                Text(
                    text = "PEMBAGIAN DETAIL TARGET MAKRONUTRISI",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp, fontWeight = FontWeight.Black),
                    color = PureBlack,
                    modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
                )
            }

            val goalP = 180
            val goalC = 250
            val goalF = 70

            item {
                BrutalistCard(
                    backgroundColor = SolidWhite,
                    modifier = Modifier.fillMaxWidth(),
                    shadowOffset = 4.dp
                ) {
                    MacroBar(label = "PROTEIN (OTOT)", current = totalP, target = goalP, barColor = ElectricBlue)
                    Spacer(modifier = Modifier.height(10.dp))
                    MacroBar(label = "KARBOHIDRAT (ENERGI)", current = totalC, target = goalC, barColor = DarkGreyAccent)
                    Spacer(modifier = Modifier.height(10.dp))
                    MacroBar(label = "LEMAK (HORMONAL)", current = totalF, target = goalF, barColor = AccentOrange)
                }
            }

            // Recent fuel list as list of elements matching design HTML "Asupan Terakhir"
            if (foodLogs.isNotEmpty()) {
                item {
                    Text(
                        text = "ASUPAN TERAKHIR",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp, fontWeight = FontWeight.Black),
                        color = PureBlack,
                        modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
                    )
                }
                items(foodLogs) { log ->
                    FoodListItem(
                        log = log,
                        onDelete = { viewModel.deleteFoodLog(log.id) }
                    )
                }
            }
        }

        // Floating Action Add Button
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 96.dp, end = 20.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .offset(x = 4.dp, y = 4.dp)
                    .background(PureBlack)
            )
            IconButton(
                onClick = onAddPressed,
                modifier = Modifier
                    .size(64.dp)
                    .background(ElectricBlue)
                    .border(3.dp, PureBlack)
            ) {
                Text("+", fontSize = 36.sp, color = SolidWhite, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun MacroBar(label: String, current: Int, target: Int, barColor: Color) {
    val pct = (current.toFloat() / target.toFloat()).coerceIn(0f, 1f)
    val remaining = (target - current).coerceAtLeast(0)
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = label, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
            Text(text = "${current}g / ${target}g", style = MaterialTheme.typography.labelSmall)
        }
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(16.dp)
                .background(LightGreyAccent)
                .border(2.dp, PureBlack)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(pct)
                    .background(barColor)
            )
        }
        Text(
            text = "Kekurangan: ${remaining}G",
            style = MaterialTheme.typography.labelSmall,
            color = PureBlack.copy(alpha = 0.6f),
            fontSize = 11.sp,
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}

@Composable
fun FoodListItem(log: FoodLog, onDelete: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(3.dp, PureBlack)
            .background(SolidWhite)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Simple food item indicator color based on session
        val iconColor = when (log.mealType) {
            "BREAKFAST" -> ElectricBlue
            "LUNCH" -> AccentGreen
            "DINNER" -> DarkGreyAccent
            else -> AccentOrange
        }
        Box(
            modifier = Modifier
                .size(44.dp)
                .border(2.dp, PureBlack)
                .background(iconColor),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = when (log.mealType) {
                    "BREAKFAST" -> "🍳"
                    "LUNCH" -> "🍗"
                    "DINNER" -> "🥗"
                    else -> "🍌"
                },
                fontSize = 20.sp
            )
        }
        
        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = log.mealType,
                    style = MaterialTheme.typography.labelSmall,
                    color = ElectricBlue,
                    fontSize = 10.sp
                )
                Text(
                    text = "${log.calories} KCAL",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                text = log.foodName,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "${log.portionInfo} (P: ${log.protein}g, K: ${log.carbs}g, L: ${log.fat}g)",
                style = MaterialTheme.typography.bodyMedium,
                color = PureBlack.copy(alpha = 0.6f)
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        IconButton(
            onClick = onDelete,
            modifier = Modifier
                .border(2.dp, PureBlack)
                .size(32.dp)
                .background(AccentMutedRed)
        ) {
            Text("X", color = SolidWhite, fontWeight = FontWeight.Bold, fontSize = 12.sp)
        }
    }
}

// --- TAB 2: DIARY SCREEN ---
@Composable
fun DiaryScreen(
    viewModel: FuelTrackViewModel,
    foodLogs: List<FoodLog>,
    selectedDate: String,
    onAddFoodForMeal: (String) -> Unit
) {
    val mealSessions = listOf("BREAKFAST", "LUNCH", "DINNER", "SNACKS")
    val mealNamesIndo = mapOf(
        "BREAKFAST" to "SARAPAN",
        "LUNCH" to "MAKAN SIANG",
        "DINNER" to "MAKAN MALAM",
        "SNACKS" to "CAMILAN"
    )
    val mealIcons = mapOf(
        "BREAKFAST" to "🌅",
        "LUNCH" to "☀️",
        "DINNER" to "🌙",
        "SNACKS" to "🍪"
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                text = "BUKU HARIAN MAKANAN",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                color = PureBlack
            )
            Text(
                text = "RINCIAN MAKANAN DAN KALORI SETIAP SESI",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                color = PureBlack.copy(alpha = 0.6f)
            )
        }

        mealSessions.forEach { session ->
            val logsForSession = foodLogs.filter { it.mealType == session }
            val sessionTotalCalories = logsForSession.sumOf { it.calories }

            item {
                BrutalistCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = SolidWhite,
                    shadowOffset = 4.dp
                ) {
                    // Session Header Box inside card
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(DarkGreyAccent)
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${mealIcons[session]} ${mealNamesIndo[session]}",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black),
                            color = SolidWhite
                        )
                        Text(
                            text = "$sessionTotalCalories KCAL",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black),
                            color = SolidWhite
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    if (logsForSession.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(LightGreyAccent)
                                .padding(12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Belum ada asupan tercatat",
                                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp),
                                color = PureBlack.copy(alpha = 0.5f)
                            )
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            logsForSession.forEach { log ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(BrutalistSurface)
                                        .border(2.dp, PureBlack)
                                        .padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = log.foodName,
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                        )
                                        Text(
                                            text = "${log.portionInfo} • P: ${log.protein}g K: ${log.carbs}g L: ${log.fat}g",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = PureBlack.copy(alpha = 0.6f)
                                        )
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "${log.calories} kcal",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black),
                                            color = ElectricBlue
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        IconButton(
                                            onClick = { viewModel.deleteFoodLog(log.id) },
                                            modifier = Modifier
                                                .size(24.dp)
                                                .background(AccentMutedRed)
                                        ) {
                                            Text("X", fontSize = 10.sp, color = SolidWhite, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    BrutalistButton(
                        onClick = { onAddFoodForMeal(session) },
                        backgroundColor = ElectricBlue,
                        modifier = Modifier.align(Alignment.Start),
                        shadowOffset = 2.dp
                    ) {
                        Text("+ MAKANAN", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), color = SolidWhite, fontWeight = FontWeight.Black)
                    }
                }
            }
        }
    }
}

// --- TAB 3: AI INSIGHTSCREEN ---
@Composable
fun AiInsightScreen(
    viewModel: FuelTrackViewModel,
    analysisText: String,
    foodLogs: List<FoodLog>
) {
    val totalEaten = foodLogs.sumOf { it.calories }
    val messages by viewModel.chatMessages.collectAsStateWithLifecycle()
    val isChatLoading by viewModel.isChatLoading.collectAsStateWithLifecycle()
    var userChatQuery by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                text = "ANALISIS INTELEKTUAL AI",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                color = PureBlack
            )
            Text(
                text = "KONSULTAN STRATEGIS HUBUNGAN KALORI & ATLETIS",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                color = PureBlack.copy(alpha = 0.6f)
            )
        }

        // System analysis card
        item {
            BrutalistCard(
                backgroundColor = SolidWhite,
                modifier = Modifier.fillMaxWidth(),
                shadowOffset = 4.dp
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .background(ElectricBlue)
                            .border(2.dp, PureBlack)
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "AI INSIGHT",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black),
                            color = SolidWhite
                        )
                    }
                    Text(
                        text = "STATUS: TEROPTIMALKAN",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .border(2.dp, PureBlack)
                            .background(LightGreyAccent),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "🧠", fontSize = 24.sp)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "ANALISIS SISTEM:",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black),
                            color = PureBlack
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = analysisText,
                            style = MaterialTheme.typography.bodyMedium,
                            color = PureBlack
                        )
                    }
                }
            }
        }

        // Tactical adjustments static guidelines
        item {
            Text(
                text = "PENYESUAIAN STRATEGIS MATRIKS",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp, fontWeight = FontWeight.Black),
                color = PureBlack
            )
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                // Adjustment 1
                Box(modifier = Modifier.weight(1f)) {
                    BrutalistCard(backgroundColor = SolidWhite, shadowOffset = 3.dp) {
                        Box(
                            modifier = Modifier
                                .background(AccentOrange)
                                .border(1.dp, PureBlack)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(text = "KRITIS", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 8.sp), color = SolidWhite)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(text = "ASUPAN PROTEIN+", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black), color = PureBlack)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(text = "Keseimbangan protein di bawah 30%. Sinyal anabolik butuh asupan tambahan.", style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp), color = PureBlack.copy(alpha = 0.6f))
                    }
                }
                
                // Adjustment 2
                Box(modifier = Modifier.weight(1f)) {
                    BrutalistCard(backgroundColor = SolidWhite, shadowOffset = 3.dp) {
                        Box(
                            modifier = Modifier
                                .background(ElectricBlue)
                                .border(1.dp, PureBlack)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(text = "REGULER", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 8.sp), color = SolidWhite)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(text = "BATASI GULA", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black), color = PureBlack)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(text = "Kurangi fruktosa menjelang tidur. Optimalkan penyerapan kalori harian.", style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp), color = PureBlack.copy(alpha = 0.6f))
                    }
                }
            }
        }

        // Metabolic Flux Visualization
        item {
            BrutalistCard(
                backgroundColor = DarkGreyAccent,
                modifier = Modifier.fillMaxWidth(),
                shadowOffset = 4.dp
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "METABOLIC METRIC STREAM",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black),
                        color = ElectricBlue
                    )
                    Text("OPTIMIZED", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black), color = AccentGreen)
                }
                Spacer(modifier = Modifier.height(8.dp))
                // Decorative pulsing grid element
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .background(PureBlack)
                        .border(1.dp, ElectricBlue),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        for (i in 0 until 16) {
                            Box(
                                modifier = Modifier
                                    .width(5.dp)
                                    .height(if (i % 2 == 0) 24.dp else 12.dp)
                                    .background(ElectricBlue)
                            )
                        }
                    }
                }
            }
        }

        // Live chatbot console
        item {
            Text(
                text = "KONSULTASI LANGSUNG DENGAN GEMINI AI",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp, fontWeight = FontWeight.Black),
                color = PureBlack
            )
        }

        item {
            BrutalistCard(backgroundColor = SolidWhite, modifier = Modifier.fillMaxWidth(), shadowOffset = 4.dp) {
                // Conversation history box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .border(2.dp, PureBlack)
                        .background(BrutalistSurface)
                        .verticalScroll(rememberScrollState())
                        .padding(8.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        messages.forEach { msg ->
                            val isUser = msg.sender == "USER"
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(0.85f)
                                        .background(if (isUser) ElectricBlue else LightGreyAccent)
                                        .border(2.dp, PureBlack)
                                        .padding(6.dp)
                                ) {
                                    Column {
                                        Text(
                                            text = if (isUser) "ANDA" else "GEMINI AI",
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
                                            color = if (isUser) SolidWhite else PureBlack
                                        )
                                        Spacer(modifier = Modifier.height(1.dp))
                                        Text(
                                            text = msg.text,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = if (isUser) SolidWhite else PureBlack
                                        )
                                    }
                                }
                            }
                        }
                        if (isChatLoading) {
                            Text(
                                text = "Gemini sedang memproses data...",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                color = ElectricBlue,
                                modifier = Modifier.padding(4.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Input console
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = userChatQuery,
                        onValueChange = { userChatQuery = it },
                        placeholder = { Text("Tanyakan apa saja...", fontSize = 11.sp) },
                        modifier = Modifier
                            .weight(1f)
                            .border(2.dp, PureBlack),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = SolidWhite,
                            unfocusedContainerColor = SolidWhite,
                            focusedIndicatorColor = ElectricBlue,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        shape = RoundedCornerShape(0.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    BrutalistButton(
                        onClick = {
                            if (userChatQuery.isNotBlank()) {
                                viewModel.sendChatMessage(userChatQuery)
                                userChatQuery = ""
                            }
                        },
                        backgroundColor = ElectricBlue,
                        shadowOffset = 2.dp
                    ) {
                        Text("KIRIM", style = MaterialTheme.typography.labelSmall, color = SolidWhite)
                    }
                }
            }
        }
    }
}

// --- TAB 4: PROFILE SCREEN ---
@Composable
fun ProfileScreen(
    viewModel: FuelTrackViewModel,
    weightHistory: List<WeightRecord>,
    progress: DailyProgress
) {
    var weightInputVal by remember { mutableStateOf("") }
    var weightLabelVal by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Human avatar & status neubrutalism - compact design
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Sharp neubrutalism border frame for avatar representation
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .border(3.dp, PureBlack)
                        .background(LightGreyAccent),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🏋️", fontSize = 44.sp)
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        text = "DOMINIC_RHODES",
                        style = MaterialTheme.typography.titleLarge.copy(fontSize = 22.sp),
                        fontWeight = FontWeight.Black,
                        color = PureBlack
                    )
                    Text(
                        text = "ELITE PERFORMANCE STATUS",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = ElectricBlue,
                        fontWeight = FontWeight.Black
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Box(
                            modifier = Modifier
                                .background(LightGreyAccent)
                                .border(1.dp, PureBlack)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(text = "Premium", style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp, fontWeight = FontWeight.Bold))
                        }
                        Box(
                            modifier = Modifier
                                .background(ElectricBlue)
                                .border(1.dp, PureBlack)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(text = "Runtun: 42 Hari", style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp, fontWeight = FontWeight.Bold), color = SolidWhite)
                        }
                    }
                }
            }
        }

        // Weight Trajectory Chart - compact
        item {
            BrutalistCard(backgroundColor = SolidWhite, modifier = Modifier.fillMaxWidth(), shadowOffset = 4.dp) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = "LINTASAN BERAT BADAN", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black), color = PureBlack)
                        Text(text = "Penurunan -4.2kg bulan ini", style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp), color = PureBlack.copy(alpha = 0.5f))
                    }
                    Box(
                        modifier = Modifier
                            .border(1.dp, PureBlack)
                            .background(LightGreyAccent)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(text = "30 HARI", style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold))
                    }
                }
                
                Spacer(modifier = Modifier.height(10.dp))

                // Custom Canvas weight chart
                WeightTrajectoryCanvas(weightHistory = weightHistory)

                Spacer(modifier = Modifier.height(10.dp))

                // Subtitle helper text and data display
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Berat Saat Ini: ${progress.weight} kg",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black),
                        color = ElectricBlue
                    )
                    Text(
                        text = "Target: 70.0 kg",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black)
                    )
                }
            }
        }

        // Fast metrics logging console
        item {
            BrutalistCard(backgroundColor = SolidWhite, modifier = Modifier.fillMaxWidth(), shadowOffset = 4.dp) {
                Text(
                    text = "LOG BERAT BADAN BARU",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp, fontWeight = FontWeight.Black),
                    color = PureBlack
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = weightInputVal,
                        onValueChange = { weightInputVal = it },
                        placeholder = { Text("Berat (kg)", fontSize = 11.sp) },
                        modifier = Modifier
                            .weight(1f)
                            .border(2.dp, PureBlack),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = SolidWhite,
                            unfocusedContainerColor = SolidWhite,
                            focusedIndicatorColor = ElectricBlue,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        shape = RoundedCornerShape(0.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    OutlinedTextField(
                        value = weightLabelVal,
                        onValueChange = { weightLabelVal = it },
                        placeholder = { Text("Label (e.g. 26 Okt)", fontSize = 11.sp) },
                        modifier = Modifier
                            .weight(1.2f)
                            .border(2.dp, PureBlack),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = SolidWhite,
                            unfocusedContainerColor = SolidWhite,
                            focusedIndicatorColor = ElectricBlue,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        shape = RoundedCornerShape(0.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    BrutalistButton(
                        onClick = {
                            val w = weightInputVal.toDoubleOrNull()
                            if (w != null && weightLabelVal.isNotBlank()) {
                                viewModel.addWeightRecord(w, weightLabelVal)
                                weightInputVal = ""
                                weightLabelVal = ""
                            }
                        },
                        backgroundColor = ElectricBlue,
                        shadowOffset = 2.dp
                    ) {
                        Text("+", style = MaterialTheme.typography.labelSmall, color = SolidWhite)
                    }
                }
            }
        }

        // Achievements / milestones metric grid in Indonesia
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "PENCAPAIAN & DATA HISTORIS",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Black),
                    color = PureBlack
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(modifier = Modifier.weight(1f)) {
                        ProfileMetricBox(valueStr = "12", labelStr = "Lencana Didapat", iconStr = "🎖️")
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        ProfileMetricBox(valueStr = "42", labelStr = "Runtun Hari", iconStr = "🔥")
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(modifier = Modifier.weight(1f)) {
                        ProfileMetricBox(valueStr = "184", labelStr = "Makan Tercatat", iconStr = "📝")
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        ProfileMetricBox(valueStr = "98%", labelStr = "Konsistensi", iconStr = "📈")
                    }
                }
            }
        }

        // Actions buttons
        item {
            BrutalistButton(
                onClick = { /* Settings action */ },
                backgroundColor = SolidWhite,
                contentColor = PureBlack,
                modifier = Modifier.fillMaxWidth(),
                shadowOffset = 2.dp
            ) {
                Text("⚙️ PENGATURAN_APLIKASI", fontWeight = FontWeight.Bold, color = PureBlack)
            }
        }

        item {
            BrutalistButton(
                onClick = { /* End session action */ },
                backgroundColor = AccentMutedRed,
                contentColor = SolidWhite,
                modifier = Modifier.fillMaxWidth(),
                shadowOffset = 2.dp
            ) {
                Text("🚪 AKHIRI SESI SEKARANG", fontWeight = FontWeight.Bold, color = SolidWhite)
            }
        }
    }
}

@Composable
fun ProfileMetricBox(valueStr: String, labelStr: String, iconStr: String) {
    BrutalistCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = SolidWhite
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = iconStr, fontSize = 28.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = valueStr, style = MaterialTheme.typography.displayLarge.copy(fontSize = 32.sp), fontWeight = FontWeight.Black)
            Text(text = labelStr.uppercase(), style = MaterialTheme.typography.labelSmall, fontSize = 9.sp, color = PureBlack.copy(alpha = 0.5f))
        }
    }
}

@Composable
fun WeightTrajectoryCanvas(weightHistory: List<WeightRecord>) {
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
            .background(BrutalistSurface)
            .border(2.dp, PureBlack)
    ) {
        val width = size.width
        val height = size.height

        val paddingPoints = 40f
        
        // Horizontal grids
        val gridLinesCount = 3
        for (g in 0..gridLinesCount) {
            val yOffset = paddingPoints + (height - 2 * paddingPoints) * (g / gridLinesCount.toFloat())
            drawLine(
                color = LightGreyAccent,
                start = Offset(0f, yOffset),
                end = Offset(width, yOffset),
                strokeWidth = 2f
            )
        }

        if (weightHistory.size > 1) {
            val maxW = weightHistory.maxOf { it.weight }
            val minW = weightHistory.minOf { it.weight }
            val wRange = (maxW - minW).coerceAtLeast(1.0)

            val coordinates = weightHistory.mapIndexed { idx, item ->
                val fractionX = idx / (weightHistory.size - 1).toFloat()
                val fractionY = (item.weight - minW) / wRange

                val drawX = paddingPoints + (width - 2 * paddingPoints) * fractionX
                // invert Y axes representing high weight values at upper visual edge
                val drawY = paddingPoints + (height - 2 * paddingPoints) * (1f - fractionY.toFloat())
                Offset(drawX, drawY)
            }

            // Draw line connecting path
            val path = Path().apply {
                val first = coordinates.first()
                moveTo(first.x, first.y)
                for (i in 1 until coordinates.size) {
                    lineTo(coordinates[i].x, coordinates[i].y)
                }
            }

            drawPath(
                path = path,
                color = ElectricBlue,
                style = Stroke(
                    width = 8f,
                    cap = androidx.compose.ui.graphics.StrokeCap.Round
                )
            )

            // Draw solid coordinates nodes
            coordinates.forEach { point ->
                drawCircle(
                    color = PureBlack,
                    radius = 12f,
                    center = point
                )
                drawCircle(
                    color = SolidWhite,
                    radius = 6f,
                    center = point
                )
            }
        }
    }
    
    // Labels corresponding bottom spacing
    if (weightHistory.isNotEmpty()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = weightHistory.first().dateLabel, style = MaterialTheme.typography.labelSmall, fontSize = 9.sp)
            if (weightHistory.size > 2) {
                Text(text = weightHistory[weightHistory.size / 2].dateLabel, style = MaterialTheme.typography.labelSmall, fontSize = 9.sp)
            }
            Text(text = weightHistory.last().dateLabel, style = MaterialTheme.typography.labelSmall, fontSize = 9.sp)
        }
    }
}


// --- ADD FOOD ITEM DIALOG CUSTOM SYSTEM ---
@Composable
fun AddFoodDialog(
    initialSession: String,
    onDismiss: () -> Unit,
    onConfirm: (session: String, name: String, portion: String, calories: Int, p: Int, c: Int, fat: Int) -> Unit
) {
    var session by remember { mutableStateOf(initialSession) }
    var foodName by remember { mutableStateOf("") }
    var portionInfo by remember { mutableStateOf("1 porsi") }
    var caloriesStr by remember { mutableStateOf("") }
    var pStr by remember { mutableStateOf("") }
    var cStr by remember { mutableStateOf("") }
    var fStr by remember { mutableStateOf("") }

    val sessionsList = listOf("BREAKFAST", "LUNCH", "DINNER", "SNACKS")
    val sessionNamesMap = mapOf(
        "BREAKFAST" to "SARAPAN",
        "LUNCH" to "MAKAN SIANG",
        "DINNER" to "MAKAN MALAM",
        "SNACKS" to "CAMILAN"
    )

    Dialog(onDismissRequest = onDismiss) {
        BrutalistCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            backgroundColor = SolidWhite
        ) {
            Text(
                text = "TAMBAH LOG MAKANAN",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Black
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Select Session Button Strip
            Text(text = "SESI LOG", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                sessionsList.forEach { s ->
                    val isSelected = s == session
                    Box(
                        modifier = Modifier
                            .clickable { session = s }
                            .background(if (isSelected) ElectricBlue else LightGreyAccent)
                            .border(1.dp, PureBlack)
                            .padding(vertical = 4.dp, horizontal = 8.dp)
                    ) {
                        Text(
                            text = sessionNamesMap[s] ?: "",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isSelected) SolidWhite else PureBlack,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Food Name Field
            OutlinedTextField(
                value = foodName,
                onValueChange = { foodName = it },
                label = { Text("Nama Makanan (e.g. Telur Dadar)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .border(2.dp, PureBlack),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = BrutalistSurface,
                    unfocusedContainerColor = BrutalistSurface,
                    focusedIndicatorColor = ElectricBlue,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                shape = RoundedCornerShape(0.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Portion Field
            OutlinedTextField(
                value = portionInfo,
                onValueChange = { portionInfo = it },
                label = { Text("Informasi Porsi") },
                modifier = Modifier
                    .fillMaxWidth()
                    .border(2.dp, PureBlack),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = BrutalistSurface,
                    unfocusedContainerColor = BrutalistSurface,
                    focusedIndicatorColor = ElectricBlue,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                shape = RoundedCornerShape(0.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Calorie Field
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = caloriesStr,
                    onValueChange = { caloriesStr = it },
                    label = { Text("Kalori (Kcal)") },
                    modifier = Modifier
                        .weight(1f)
                        .border(2.dp, PureBlack),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = BrutalistSurface,
                        unfocusedContainerColor = BrutalistSurface,
                        focusedIndicatorColor = ElectricBlue,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    shape = RoundedCornerShape(0.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                OutlinedTextField(
                    value = pStr,
                    onValueChange = { pStr = it },
                    label = { Text("Protein (g)") },
                    modifier = Modifier
                        .weight(1f)
                        .border(2.dp, PureBlack),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = BrutalistSurface,
                        unfocusedContainerColor = BrutalistSurface,
                        focusedIndicatorColor = ElectricBlue,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    shape = RoundedCornerShape(0.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Macros Field
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = cStr,
                    onValueChange = { cStr = it },
                    label = { Text("Karbo (g)") },
                    modifier = Modifier
                        .weight(1f)
                        .border(2.dp, PureBlack),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = BrutalistSurface,
                        unfocusedContainerColor = BrutalistSurface,
                        focusedIndicatorColor = ElectricBlue,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    shape = RoundedCornerShape(0.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                OutlinedTextField(
                    value = fStr,
                    onValueChange = { fStr = it },
                    label = { Text("Lemak (g)") },
                    modifier = Modifier
                        .weight(1f)
                        .border(2.dp, PureBlack),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = BrutalistSurface,
                        unfocusedContainerColor = BrutalistSurface,
                        focusedIndicatorColor = ElectricBlue,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    shape = RoundedCornerShape(0.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Confirm Confirm Actions buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                BrutalistButton(
                    onClick = onDismiss,
                    backgroundColor = LightGreyAccent,
                    contentColor = PureBlack
                ) {
                    Text("BATAL", style = MaterialTheme.typography.labelSmall, color = PureBlack)
                }

                Spacer(modifier = Modifier.width(12.dp))

                BrutalistButton(
                    onClick = {
                        val cal = caloriesStr.toIntOrNull() ?: 100
                        val p = pStr.toIntOrNull() ?: 5
                        val c = cStr.toIntOrNull() ?: 15
                        val f = fStr.toIntOrNull() ?: 2

                        if (foodName.isNotBlank()) {
                            onConfirm(session, foodName, portionInfo, cal, p, c, f)
                        }
                    },
                    backgroundColor = ElectricBlue
                ) {
                    Text("SIMPAN", style = MaterialTheme.typography.labelSmall, color = SolidWhite)
                }
            }
        }
    }
}

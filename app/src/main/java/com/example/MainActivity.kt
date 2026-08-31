package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Fastfood
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.DayEnum
import com.example.ui.MessViewModel
import com.example.ui.components.AiScanTimetableDialog
import com.example.ui.components.EditMealDialog
import com.example.ui.screens.FoodCategoriesScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.TodayScreen
import com.example.ui.screens.WeeklyMenuScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.SleekBorder
import com.example.ui.theme.SleekNavBackground
import com.example.ui.theme.SleekOnPrimaryContainer
import com.example.ui.theme.SleekPrimaryContainer
import com.example.ui.theme.SleekSecondaryContainer
import com.example.ui.theme.SleekTerracottaPrimary
import com.example.ui.theme.SleekTextPrimary
import com.example.ui.theme.SleekTextSecondary
import com.example.util.MealTimeUtils

enum class NavigationTab(
    val title: String,
    val selectedIcon: androidx.compose.ui.graphics.vector.ImageVector,
    val unselectedIcon: androidx.compose.ui.graphics.vector.ImageVector,
    val testTag: String
) {
    TODAY("Today", Icons.Filled.Restaurant, Icons.Outlined.Restaurant, "nav_today"),
    WEEKLY("Weekly", Icons.Filled.CalendarMonth, Icons.Outlined.CalendarMonth, "nav_weekly"),
    FOOD_LIBRARY("Food Library", Icons.Filled.Fastfood, Icons.Outlined.Fastfood, "nav_food_library"),
    SETTINGS("Settings", Icons.Filled.Settings, Icons.Outlined.Settings, "nav_settings")
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MessMateApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessMateApp(viewModel: MessViewModel = viewModel()) {
    val allMeals by viewModel.allMeals.collectAsStateWithLifecycle()
    val activeState by viewModel.activeMealState.collectAsStateWithLifecycle()
    val selectedDay by viewModel.selectedDay.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedMealFilter by viewModel.selectedMealFilter.collectAsStateWithLifecycle()
    val editingMeal by viewModel.editingMeal.collectAsStateWithLifecycle()
    val allFoodItems by viewModel.allFoodItems.collectAsStateWithLifecycle()
    val allCategories by viewModel.allCategories.collectAsStateWithLifecycle()
    val foodCategoryFilter by viewModel.foodCategoryFilter.collectAsStateWithLifecycle()
    val foodSearchQuery by viewModel.foodSearchQuery.collectAsStateWithLifecycle()
    val notificationsEnabled by viewModel.notificationsEnabled.collectAsStateWithLifecycle()
    val reminderMinutes by viewModel.reminderMinutes.collectAsStateWithLifecycle()
    val messName by viewModel.messName.collectAsStateWithLifecycle()

    // AI Timetable Scanner State
    val showAiScanDialog by viewModel.showAiScanDialog.collectAsStateWithLifecycle()
    val isAiScanning by viewModel.isAiScanning.collectAsStateWithLifecycle()
    val aiScanStatusText by viewModel.aiScanStatusText.collectAsStateWithLifecycle()
    val aiScanResult by viewModel.aiScanResult.collectAsStateWithLifecycle()
    val selectedImageUri by viewModel.selectedImageUri.collectAsStateWithLifecycle()
    val aiScanError by viewModel.aiScanError.collectAsStateWithLifecycle()

    var currentTab by remember { mutableStateOf(NavigationTab.TODAY) }
    val realCurrentDay = remember { MealTimeUtils.getCurrentDayNumber() }
    val context = LocalContext.current

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("messmate_scaffold"),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "🍽️",
                            fontSize = 22.sp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(horizontalAlignment = Alignment.Start) {
                            Text(
                                text = "MessMate",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = SleekTextPrimary
                            )
                            Text(
                                text = messName,
                                style = MaterialTheme.typography.labelSmall,
                                color = SleekTerracottaPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                ),
                actions = {
                    IconButton(
                        onClick = { viewModel.openAiScanDialog() },
                        modifier = Modifier.testTag("topbar_ai_scan_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "Scan Timetable with AI",
                            tint = SleekTerracottaPrimary
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = SleekSecondaryContainer,
                        modifier = Modifier.padding(end = 16.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (activeState.isCurrentlyActive) SleekTerracottaPrimary else SleekTextSecondary
                                    )
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = DayEnum.fromDayNumber(realCurrentDay).shortName.uppercase(),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = SleekTextPrimary
                            )
                        }
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = SleekNavBackground,
                tonalElevation = 0.dp
            ) {
                NavigationTab.entries.forEach { tab ->
                    val isSelected = currentTab == tab
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = {
                            currentTab = tab
                            if (tab == NavigationTab.TODAY) {
                                viewModel.selectDay(realCurrentDay)
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                                contentDescription = tab.title
                            )
                        },
                        label = {
                            Text(
                                text = tab.title,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 11.sp
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = SleekTerracottaPrimary,
                            selectedTextColor = SleekTerracottaPrimary,
                            unselectedIconColor = SleekTextSecondary,
                            unselectedTextColor = SleekTextSecondary,
                            indicatorColor = SleekPrimaryContainer
                        ),
                        modifier = Modifier.testTag(tab.testTag)
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AnimatedContent(
                targetState = currentTab,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "tab_transition"
            ) { tab ->
                when (tab) {
                    NavigationTab.TODAY -> {
                        TodayScreen(
                            activeState = activeState,
                            allMeals = allMeals,
                            selectedDay = selectedDay,
                            onDaySelect = { viewModel.selectDay(it) },
                            onEditMeal = { viewModel.openEditMeal(it) },
                            onToggleFavorite = { id, current -> viewModel.toggleFavorite(id, current) },
                            onShareDay = { viewModel.shareDayMenu(it) }
                        )
                    }
                    NavigationTab.WEEKLY -> {
                        WeeklyMenuScreen(
                            allMeals = allMeals,
                            selectedDay = selectedDay,
                            searchQuery = searchQuery,
                            selectedMealFilter = selectedMealFilter,
                            onDaySelect = { viewModel.selectDay(it) },
                            onSearchChange = { viewModel.setSearchQuery(it) },
                            onMealFilterChange = { viewModel.setMealFilter(it) },
                            onEditMeal = { viewModel.openEditMeal(it) },
                            onToggleFavorite = { id, current -> viewModel.toggleFavorite(id, current) },
                            onOpenAiScan = { viewModel.openAiScanDialog() }
                        )
                    }
                    NavigationTab.FOOD_LIBRARY -> {
                        FoodCategoriesScreen(
                            foodItems = allFoodItems,
                            categories = allCategories,
                            selectedCategory = foodCategoryFilter,
                            searchQuery = foodSearchQuery,
                            onSelectCategory = { viewModel.setFoodCategoryFilter(it) },
                            onSearchQueryChange = { viewModel.setFoodSearchQuery(it) },
                            onAddFoodItem = { name, category, emoji ->
                                viewModel.addFoodItem(name, category, emoji)
                            },
                            onDeleteFoodItem = { viewModel.deleteFoodItem(it) },
                            onAddFoodToTimetable = { day, mealType, foodName ->
                                viewModel.addFoodToTimetable(day, mealType, foodName)
                            }
                        )
                    }
                    NavigationTab.SETTINGS -> {
                        SettingsScreen(
                            messName = messName,
                            notificationsEnabled = notificationsEnabled,
                            reminderMinutes = reminderMinutes,
                            onMessNameChange = { viewModel.updateMessName(it) },
                            onNotificationsToggle = { viewModel.setNotificationsToggle(it) },
                            onReminderMinutesChange = { viewModel.updateReminderMinutes(it) },
                            onResetMenu = { viewModel.resetMenuToDefaults() },
                            onResetFoodLibrary = { viewModel.resetFoodLibrary() },
                            onOpenAiScan = { viewModel.openAiScanDialog() }
                        )
                    }
                }
            }

            // Edit Meal Dialog Sheet
            editingMeal?.let { meal ->
                EditMealDialog(
                    meal = meal,
                    onDismiss = { viewModel.closeEditMeal() },
                    onSave = { mealId, items, start, end, note ->
                        viewModel.saveMealEdit(mealId, items, start, end, note)
                    }
                )
            }

            // AI Timetable Scanner Modal Sheet
            if (showAiScanDialog) {
                AiScanTimetableDialog(
                    selectedImageUri = selectedImageUri,
                    isScanning = isAiScanning,
                    statusText = aiScanStatusText,
                    scanResult = aiScanResult,
                    errorMessage = aiScanError,
                    onSelectImageUri = { viewModel.setSelectedImageUri(it) },
                    onStartScan = { uri, note ->
                        viewModel.scanTimetableImage(context, uri, note)
                    },
                    onLoadSampleTemplate = { template ->
                        viewModel.loadSampleTimetableTemplate(template)
                    },
                    onApplyResult = { result, customMessName, importDishes ->
                        viewModel.applyScannedTimetable(result, customMessName, importDishes)
                        currentTab = NavigationTab.WEEKLY
                    },
                    onDismiss = { viewModel.closeAiScanDialog() }
                )
            }
        }
    }
}


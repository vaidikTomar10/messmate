package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.local.MealEntity
import com.example.data.model.DayEnum
import com.example.data.model.MealType
import com.example.ui.components.AppFooter
import com.example.ui.components.MealCard
import com.example.ui.theme.SleekBorder
import com.example.ui.theme.SleekPrimaryContainer
import com.example.ui.theme.SleekSecondaryContainer
import com.example.ui.theme.SleekTerracottaPrimary
import com.example.ui.theme.SleekTextPrimary
import com.example.ui.theme.SleekTextSecondary
import com.example.util.MealTimeUtils

import androidx.compose.ui.unit.sp

@Composable
fun WeeklyMenuScreen(
    allMeals: List<MealEntity>,
    selectedDay: Int,
    searchQuery: String,
    selectedMealFilter: MealType?,
    onDaySelect: (Int) -> Unit,
    onSearchChange: (String) -> Unit,
    onMealFilterChange: (MealType?) -> Unit,
    onEditMeal: (MealEntity) -> Unit,
    onToggleFavorite: (Long, Boolean) -> Unit,
    onOpenAiScan: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val realCurrentDay = remember { MealTimeUtils.getCurrentDayNumber() }
    val currentMinutes = remember { MealTimeUtils.getCurrentMinutesOfDay() }

    val filteredMeals = remember(allMeals, selectedDay, searchQuery, selectedMealFilter) {
        if (searchQuery.isNotBlank()) {
            // Search across entire week if user enters search text
            allMeals.filter { meal ->
                meal.items.contains(searchQuery, ignoreCase = true) ||
                meal.specialNote.contains(searchQuery, ignoreCase = true) ||
                meal.mealType.contains(searchQuery, ignoreCase = true)
            }.let { list ->
                if (selectedMealFilter != null) {
                    list.filter { it.mealType.equals(selectedMealFilter.name, ignoreCase = true) }
                } else list
            }
        } else {
            // Filter by day and optional meal category
            allMeals.filter { it.dayOfWeek == selectedDay }
                .let { list ->
                    if (selectedMealFilter != null) {
                        list.filter { it.mealType.equals(selectedMealFilter.name, ignoreCase = true) }
                    } else list
                }
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
        // AI Timetable Scanner Action Banner
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp)
                .clickable { onOpenAiScan() }
                .testTag("ai_scan_banner_button"),
            shape = RoundedCornerShape(16.dp),
            color = SleekSecondaryContainer,
            border = BorderStroke(1.dp, SleekBorder)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(SleekPrimaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = SleekTerracottaPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "AI Timetable Scanner",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = SleekTextPrimary
                        )
                        Text(
                            text = "Upload menu photo to auto-set 7-day schedule",
                            style = MaterialTheme.typography.bodySmall,
                            color = SleekTextSecondary,
                            fontSize = 11.sp
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = SleekTerracottaPrimary
                ) {
                    Text(
                        text = "Scan Image",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        fontSize = 11.sp
                    )
                }
            }
        }

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp)
                .testTag("menu_search_input"),
            placeholder = { Text("Search dishes: Paneer, Biryani, Dosa...") },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = "Search food")
            },
            trailingIcon = {
                if (searchQuery.isNotBlank()) {
                    IconButton(onClick = { onSearchChange("") }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear search")
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
            )
        )

        // Meal Filter Chips (All, Breakfast, Lunch, Snacks, Dinner)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = selectedMealFilter == null,
                onClick = { onMealFilterChange(null) },
                label = { Text("All Meals") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("filter_all")
            )

            MealType.entries.forEach { type ->
                FilterChip(
                    selected = selectedMealFilter == type,
                    onClick = {
                        onMealFilterChange(if (selectedMealFilter == type) null else type)
                    },
                    label = { Text("${type.emoji} ${type.displayName}") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("filter_${type.name.lowercase()}")
                )
            }
        }

        // Day Tabs (Only shown if NOT in search mode)
        if (searchQuery.isBlank()) {
            ScrollableTabRow(
                selectedTabIndex = selectedDay - 1,
                edgePadding = 16.dp,
                containerColor = MaterialTheme.colorScheme.background,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedDay - 1]),
                        color = MaterialTheme.colorScheme.primary,
                        height = 3.dp
                    )
                },
                divider = {}
            ) {
                DayEnum.entries.forEachIndexed { index, day ->
                    val isToday = day.dayNumber == realCurrentDay
                    Tab(
                        selected = selectedDay == day.dayNumber,
                        onClick = { onDaySelect(day.dayNumber) },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = day.shortName,
                                    fontWeight = if (selectedDay == day.dayNumber) FontWeight.Bold else FontWeight.Normal
                                )
                                if (isToday) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = MaterialTheme.colorScheme.primary
                                    ) {
                                        Text(
                                            text = "TODAY",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onPrimary,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
                                            fontSize = 9.sp
                                        )
                                    }
                                }
                            }
                        },
                        modifier = Modifier.testTag("tab_day_${day.shortName.lowercase()}")
                    )
                }
            }
        } else {
            // Search result header
            Text(
                text = "Search Results for \"$searchQuery\" (${filteredMeals.size} found)",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }

        // List of meals
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .testTag("weekly_meals_list"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            if (filteredMeals.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("No dishes match your filter", style = MaterialTheme.typography.bodyLarge)
                            Text(
                                "Try searching for a different food or reset the filter",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                }
            } else {
                items(filteredMeals, key = { it.id }) { meal ->
                    Column {
                        if (searchQuery.isNotBlank()) {
                            Text(
                                text = "📅 ${DayEnum.fromDayNumber(meal.dayOfWeek).fullName}",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(bottom = 6.dp)
                            )
                        }
                        MealCard(
                            meal = meal,
                            currentDay = realCurrentDay,
                            currentMinutes = currentMinutes,
                            onEditClick = { onEditMeal(meal) },
                            onFavoriteToggle = { onToggleFavorite(meal.id, meal.isFavorite) }
                        )
                    }
                }
            }

            item {
                AppFooter()
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

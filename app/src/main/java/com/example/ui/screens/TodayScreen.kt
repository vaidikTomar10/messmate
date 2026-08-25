package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.MealEntity
import com.example.data.model.ActiveMealState
import com.example.data.model.DayEnum
import com.example.data.model.MealActiveStatus
import com.example.data.model.MealType
import com.example.ui.components.AppFooter
import com.example.ui.components.MealCard
import com.example.ui.theme.SleekBorder
import com.example.ui.theme.SleekOnPrimaryContainer
import com.example.ui.theme.SleekPrimaryContainer
import com.example.ui.theme.SleekSecondaryContainer
import com.example.ui.theme.SleekTerracottaPrimary
import com.example.ui.theme.SleekTextPrimary
import com.example.ui.theme.SleekTextSecondary
import com.example.util.MealTimeUtils

@Composable
fun TodayScreen(
    activeState: ActiveMealState,
    allMeals: List<MealEntity>,
    selectedDay: Int,
    onDaySelect: (Int) -> Unit,
    onEditMeal: (MealEntity) -> Unit,
    onToggleFavorite: (Long, Boolean) -> Unit,
    onShareDay: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val realCurrentDay = remember { MealTimeUtils.getCurrentDayNumber() }
    val currentMinutes = remember { MealTimeUtils.getCurrentMinutesOfDay() }
    val selectedDayMeals = allMeals.filter { it.dayOfWeek == selectedDay }
    val isViewingToday = selectedDay == realCurrentDay

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("today_screen_list"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. DAY SELECTOR PILL STRIP
        item {
            DaySelectorStrip(
                selectedDay = selectedDay,
                realCurrentDay = realCurrentDay,
                onDaySelect = onDaySelect
            )
        }

        // 2. HERO ACTIVE MEAL CARD (if viewing today)
        if (isViewingToday) {
            item {
                HeroLiveMealCard(
                    activeState = activeState,
                    onEditClick = {
                        val mealToEdit = activeState.activeMeal ?: activeState.nextMeal
                        if (mealToEdit != null) {
                            val entity = allMeals.firstOrNull { it.id == mealToEdit.id }
                            if (entity != null) onEditMeal(entity)
                        }
                    }
                )
            }

            // 3. UP NEXT PREVIEW (if currently active and next meal exists)
            if (activeState.isCurrentlyActive && activeState.nextMeal != null) {
                item {
                    val next = activeState.nextMeal
                    val nextEntity = allMeals.firstOrNull { it.id == next.id }
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (nextEntity != null) onEditMeal(nextEntity)
                            }
                            .testTag("up_next_card"),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = SleekSecondaryContainer
                        ),
                        border = BorderStroke(1.dp, SleekBorder)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "UP NEXT",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = SleekTerracottaPrimary,
                                    letterSpacing = 1.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "${next.mealType.displayName} ${next.mealType.emoji}",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = SleekTextPrimary
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "${MealTimeUtils.format12Hour(next.startTime)} • ${if (next.rawItems.isNotBlank()) next.rawItems else "Check menu"}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = SleekTextSecondary,
                                    maxLines = 1
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.6f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = "View next meal",
                                    tint = SleekTerracottaPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // 4. SECTION HEADER FOR SELECTED DAY
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isViewingToday) "Today's Schedule" else "${DayEnum.fromDayNumber(selectedDay).fullName}'s Schedule",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = SleekTextPrimary
                    )
                    Text(
                        text = "Full day mess timetable",
                        style = MaterialTheme.typography.bodySmall,
                        color = SleekTextSecondary
                    )
                }

                FilledTonalButton(
                    onClick = { onShareDay(selectedDay) },
                    modifier = Modifier.testTag("share_day_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = SleekSecondaryContainer,
                        contentColor = SleekTerracottaPrimary
                    ),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Icon(
                        Icons.Default.Share,
                        contentDescription = "Share",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Share", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                }
            }
        }

        // 5. MEAL CARDS (Breakfast, Lunch, Snacks, Dinner)
        if (selectedDayMeals.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = SleekSecondaryContainer)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("No meals added for this day", style = MaterialTheme.typography.bodyLarge, color = SleekTextPrimary)
                    }
                }
            }
        } else {
            items(selectedDayMeals, key = { it.id }) { meal ->
                MealCard(
                    meal = meal,
                    currentDay = realCurrentDay,
                    currentMinutes = currentMinutes,
                    onEditClick = { onEditMeal(meal) },
                    onFavoriteToggle = { onToggleFavorite(meal.id, meal.isFavorite) }
                )
            }
        }

        item {
            AppFooter()
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HeroLiveMealCard(
    activeState: ActiveMealState,
    onEditClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val active = activeState.activeMeal
    val next = activeState.nextMeal
    val isNow = activeState.isCurrentlyActive && active != null

    val targetMeal = active ?: next

    if (targetMeal == null) return

    val itemsList = remember(targetMeal.rawItems) {
        if (targetMeal.rawItems.isBlank()) emptyList()
        else targetMeal.rawItems.split("•", ",").map { it.trim() }.filter { it.isNotBlank() }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("hero_active_card"),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(
            containerColor = SleekPrimaryContainer
        ),
        border = BorderStroke(1.dp, SleekSecondaryContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(22.dp)
        ) {
            // Header Row: Status badge + time + edit
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = SleekTerracottaPrimary,
                    modifier = Modifier.testTag("hero_status_pill")
                ) {
                    Text(
                        text = if (isNow) "NOW SERVING" else "UP NEXT",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                Text(
                    text = activeState.timeRemainingFormatted,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = SleekTerracottaPrimary
                )

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = onEditClick,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.6f))
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit meal",
                        tint = SleekTerracottaPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Meal Title & Time Range
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${targetMeal.mealType.displayName} ${targetMeal.mealType.emoji}",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = SleekOnPrimaryContainer
                )
            }

            Text(
                text = "${MealTimeUtils.format12Hour(targetMeal.startTime)} – ${MealTimeUtils.format12Hour(targetMeal.endTime)}",
                style = MaterialTheme.typography.bodyLarge,
                color = SleekTextSecondary,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Modern Dish Chips (White pill cards on peach canvas)
            if (itemsList.isNotEmpty()) {
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsList.forEach { dish ->
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = Color.White.copy(alpha = 0.75f),
                            shadowElevation = 1.dp
                        ) {
                            Text(
                                text = dish,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = SleekOnPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                            )
                        }
                    }
                }
            } else {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "No food items set yet",
                        style = MaterialTheme.typography.bodyMedium,
                        color = SleekTextSecondary,
                        modifier = Modifier.padding(14.dp)
                    )
                }
            }

            // Bottom Pulse / Indicator bar
            Spacer(modifier = Modifier.height(18.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(SleekTerracottaPrimary)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (targetMeal.specialNote.isNotBlank()) "✨ ${targetMeal.specialNote}" else "Mess timetable synced & active",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = SleekTerracottaPrimary
                )
            }
        }
    }
}

@Composable
fun DaySelectorStrip(
    selectedDay: Int,
    realCurrentDay: Int,
    onDaySelect: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        DayEnum.entries.forEach { day ->
            val isSelected = day.dayNumber == selectedDay
            val isToday = day.dayNumber == realCurrentDay

            Surface(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .clickable { onDaySelect(day.dayNumber) }
                    .testTag("day_pill_${day.shortName.lowercase()}"),
                shape = RoundedCornerShape(20.dp),
                color = when {
                    isSelected -> SleekTerracottaPrimary
                    else -> SleekSecondaryContainer
                },
                shadowElevation = if (isSelected) 3.dp else 0.dp
            ) {
                Column(
                    modifier = Modifier
                        .width(48.dp)
                        .height(60.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = day.shortName.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        color = if (isSelected) Color.White.copy(alpha = 0.85f) else SleekTextSecondary.copy(alpha = 0.7f)
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = "${day.dayNumber}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) Color.White else SleekTextPrimary
                    )

                    if (isToday && !isSelected) {
                        Box(
                            modifier = Modifier
                                .padding(top = 2.dp)
                                .size(4.dp)
                                .clip(CircleShape)
                                .background(SleekTerracottaPrimary)
                        )
                    }
                }
            }
        }
    }
}


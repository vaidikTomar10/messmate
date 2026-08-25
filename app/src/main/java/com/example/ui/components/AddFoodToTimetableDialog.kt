package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.FoodItemEntity
import com.example.data.model.DayEnum
import com.example.data.model.MealType
import com.example.ui.theme.SleekBorder
import com.example.ui.theme.SleekOnPrimaryContainer
import com.example.ui.theme.SleekPrimaryContainer
import com.example.ui.theme.SleekSecondaryContainer
import com.example.ui.theme.SleekTerracottaPrimary
import com.example.ui.theme.SleekTextPrimary
import com.example.ui.theme.SleekTextSecondary
import com.example.util.MealTimeUtils

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddFoodToTimetableDialog(
    foodItem: FoodItemEntity,
    onDismiss: () -> Unit,
    onConfirmAdd: (dayOfWeek: Int, mealType: String, foodName: String) -> Unit
) {
    var selectedDay by remember { mutableIntStateOf(MealTimeUtils.getCurrentDayNumber()) }
    var selectedMealType by remember {
        mutableStateOf(
            when (foodItem.category.lowercase()) {
                "breakfast" -> MealType.BREAKFAST
                "snacks & drinks", "snacks" -> MealType.SNACKS
                "desserts & sweets", "desserts" -> MealType.DINNER
                else -> MealType.LUNCH
            }
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = foodItem.iconEmoji,
                    fontSize = 24.sp
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Add to Timetable",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = SleekTextPrimary
                    )
                    Text(
                        text = foodItem.name,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = SleekTerracottaPrimary
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 1. Select Day
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.CalendarMonth,
                            contentDescription = null,
                            tint = SleekTerracottaPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Select Day",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = SleekTextPrimary
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        DayEnum.entries.forEach { day ->
                            val isSelected = selectedDay == day.dayNumber
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedDay = day.dayNumber },
                                label = {
                                    Text(
                                        text = day.shortName,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = SleekTerracottaPrimary,
                                    selectedLabelColor = androidx.compose.ui.graphics.Color.White,
                                    containerColor = SleekSecondaryContainer,
                                    labelColor = SleekTextPrimary
                                ),
                                border = if (isSelected) null else BorderStroke(1.dp, SleekBorder)
                            )
                        }
                    }
                }

                // 2. Select Meal Type
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Restaurant,
                            contentDescription = null,
                            tint = SleekTerracottaPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Select Meal",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = SleekTextPrimary
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        MealType.entries.forEach { type ->
                            val isSelected = selectedMealType == type
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedMealType = type },
                                leadingIcon = {
                                    Text(type.emoji, fontSize = 14.sp)
                                },
                                label = {
                                    Text(
                                        text = type.displayName,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = SleekTerracottaPrimary,
                                    selectedLabelColor = androidx.compose.ui.graphics.Color.White,
                                    containerColor = SleekSecondaryContainer,
                                    labelColor = SleekTextPrimary
                                ),
                                border = if (isSelected) null else BorderStroke(1.dp, SleekBorder)
                            )
                        }
                    }
                }

                // Summary Preview Card
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = SleekPrimaryContainer,
                    border = BorderStroke(1.dp, SleekSecondaryContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "✨", fontSize = 20.sp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Will be added to:",
                                style = MaterialTheme.typography.labelSmall,
                                color = SleekTextSecondary
                            )
                            Text(
                                text = "${DayEnum.fromDayNumber(selectedDay).fullName} • ${selectedMealType.displayName} ${selectedMealType.emoji}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = SleekOnPrimaryContainer
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirmAdd(selectedDay, selectedMealType.name, foodItem.name)
                },
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = SleekTerracottaPrimary
                ),
                modifier = Modifier.testTag("confirm_add_food_to_timetable")
            ) {
                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Add to Timetable", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, SleekBorder)
            ) {
                Text("Cancel", color = SleekTextSecondary)
            }
        }
    )
}

package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.MealEntity
import com.example.data.model.DayEnum
import com.example.data.model.MealType
import com.example.ui.theme.SleekBorder
import com.example.ui.theme.SleekSecondaryContainer
import com.example.ui.theme.SleekTerracottaPrimary
import com.example.ui.theme.SleekTextPrimary
import com.example.ui.theme.SleekTextSecondary

@Composable
fun EditMealDialog(
    meal: MealEntity,
    onDismiss: () -> Unit,
    onSave: (mealId: Long, items: String, startTime: String, endTime: String, specialNote: String) -> Unit
) {
    var itemsText by remember(meal) { mutableStateOf(meal.items) }
    var startTimeText by remember(meal) { mutableStateOf(meal.startTime) }
    var endTimeText by remember(meal) { mutableStateOf(meal.endTime) }
    var noteText by remember(meal) { mutableStateOf(meal.specialNote) }

    val mealType = MealType.fromString(meal.mealType)
    val dayName = DayEnum.fromDayNumber(meal.dayOfWeek).fullName

    // Categories mapping for category-based quick addition
    val categoryDishes = remember {
        mapOf(
            "Breakfast" to listOf("Poha", "Aloo Paratha", "Masala Dosa", "Idli Sambar", "Medu Vada", "Methi Thepla", "Rava Upma", "Puri Bhaji", "Boiled Eggs", "Chai", "Coffee"),
            "Breads & Roti" to listOf("Butter Roti", "Phulka Roti", "Butter Naan", "Garlic Naan", "Bhature", "Missi Roti", "Laccha Paratha"),
            "Dal & Curries" to listOf("Rajma Masala", "Chole Masala", "Shahi Paneer", "Paneer Butter Masala", "Matar Paneer", "Dal Tadka", "Dal Makhani", "Dal Fry", "Kadhi Pakoda", "Egg Curry", "Mix Veg"),
            "Rice & Biryani" to listOf("Steamed Basmati Rice", "Jeera Rice", "Veg Dum Biryani", "Veg Pulao", "Ghee Rice", "Peas Pulao", "Fried Rice"),
            "Snacks & Drinks" to listOf("Crispy Samosa", "Veg Cutlet", "Bread Pakoda", "Onion Pakora", "Pav Bhaji", "Maggi", "Bhel Puri", "Masala Chai", "Filter Coffee", "Masala Chaas"),
            "Desserts & Sweets" to listOf("Gulab Jamun", "Rice Kheer", "Moong Dal Halwa", "Rasgulla", "Rasmalai", "Vanilla Ice Cream", "Mango Kulfi", "Chocolate Brownie"),
            "Sides & Salads" to listOf("Boondi Raita", "Mix Veg Raita", "Cucumber Salad", "Green Salad", "Sweet Curd", "Roasted Papad", "Pickle", "Mint Chutney")
        )
    }

    var activeCategory by remember(meal.mealType) {
        mutableStateOf(
            when (mealType) {
                MealType.BREAKFAST -> "Breakfast"
                MealType.LUNCH -> "Dal & Curries"
                MealType.SNACKS -> "Snacks & Drinks"
                MealType.DINNER -> "Dal & Curries"
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
                    text = "${mealType.emoji} Edit ${mealType.displayName}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = SleekTextPrimary
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = dayName,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = SleekTerracottaPrimary
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Food Items (Separate with '•' or commas)",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = SleekTextSecondary
                )

                OutlinedTextField(
                    value = itemsText,
                    onValueChange = { itemsText = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("edit_meal_items_input"),
                    placeholder = { Text("e.g. Rajma, Rice, Roti, Salad") },
                    leadingIcon = {
                        Icon(Icons.Default.Fastfood, contentDescription = "Food items", tint = SleekTerracottaPrimary)
                    },
                    minLines = 3,
                    maxLines = 5,
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SleekTerracottaPrimary,
                        unfocusedBorderColor = SleekBorder
                    )
                )

                // Category Quick Picker
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Category, contentDescription = null, tint = SleekTerracottaPrimary, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Add from Food Categories:",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = SleekTextPrimary
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))

                    // Category Selector Pills
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        categoryDishes.keys.forEach { category ->
                            val isSelected = activeCategory == category
                            FilterChip(
                                selected = isSelected,
                                onClick = { activeCategory = category },
                                label = { Text(category, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = SleekTerracottaPrimary,
                                    selectedLabelColor = Color.White,
                                    containerColor = SleekSecondaryContainer,
                                    labelColor = SleekTextPrimary
                                ),
                                border = if (isSelected) null else BorderStroke(1.dp, SleekBorder)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Dishes in Selected Category
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val currentDishes = categoryDishes[activeCategory] ?: emptyList()
                        currentDishes.forEach { dish ->
                            SuggestionChip(
                                onClick = {
                                    if (itemsText.isBlank()) {
                                        itemsText = dish
                                    } else {
                                        itemsText = "$itemsText • $dish"
                                    }
                                },
                                label = { Text(dish, style = MaterialTheme.typography.labelSmall) },
                                icon = { Icon(Icons.Default.Add, contentDescription = "Add $dish", tint = SleekTerracottaPrimary) },
                                colors = SuggestionChipDefaults.suggestionChipColors(
                                    containerColor = SleekSecondaryContainer.copy(alpha = 0.7f),
                                    labelColor = SleekTextPrimary
                                ),
                                border = BorderStroke(1.dp, SleekBorder)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = "Meal Timings (24-Hour Format)",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = SleekTextSecondary
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = startTimeText,
                        onValueChange = { startTimeText = it },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("edit_meal_start_time_input"),
                        label = { Text("Start (HH:mm)") },
                        leadingIcon = {
                            Icon(Icons.Default.AccessTime, contentDescription = "Start time", tint = SleekTerracottaPrimary)
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SleekTerracottaPrimary,
                            unfocusedBorderColor = SleekBorder
                        )
                    )

                    OutlinedTextField(
                        value = endTimeText,
                        onValueChange = { endTimeText = it },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("edit_meal_end_time_input"),
                        label = { Text("End (HH:mm)") },
                        leadingIcon = {
                            Icon(Icons.Default.AccessTime, contentDescription = "End time", tint = SleekTerracottaPrimary)
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SleekTerracottaPrimary,
                            unfocusedBorderColor = SleekBorder
                        )
                    )
                }

                OutlinedTextField(
                    value = noteText,
                    onValueChange = { noteText = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("edit_meal_note_input"),
                    label = { Text("Special Note (Optional)") },
                    placeholder = { Text("e.g. Special Feast, Sweet Included") },
                    leadingIcon = {
                        Icon(Icons.AutoMirrored.Filled.Notes, contentDescription = "Special note", tint = SleekTerracottaPrimary)
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SleekTerracottaPrimary,
                        unfocusedBorderColor = SleekBorder
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(meal.id, itemsText, startTimeText, endTimeText, noteText)
                },
                modifier = Modifier.testTag("save_meal_button"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = SleekTerracottaPrimary
                )
            ) {
                Text("Save Changes", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("cancel_meal_button"),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, SleekBorder)
            ) {
                Text("Cancel", color = SleekTextSecondary)
            }
        }
    )
}


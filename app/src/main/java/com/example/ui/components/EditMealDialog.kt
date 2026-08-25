package com.example.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.local.MealEntity
import com.example.data.model.DayEnum
import com.example.data.model.MealType

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

    // Quick suggestions for easy addition
    val quickSuggestions = remember(meal.mealType) {
        when (mealType) {
            MealType.BREAKFAST -> listOf("Poha", "Aloo Paratha", "Masala Dosa", "Idli Sambar", "Thepla", "Upma", "Puri Bhaji", "Chai", "Coffee", "Boiled Eggs", "Banana")
            MealType.LUNCH -> listOf("Rajma", "Chole", "Paneer", "Dal Tadka", "Dal Makhani", "Kadhi Pakoda", "Jeera Rice", "Roti", "Salad", "Curd", "Raita", "Papad")
            MealType.SNACKS -> listOf("Samosa", "Veg Cutlet", "Bread Pakoda", "Pakora", "Pav Bhaji", "Maggi", "Bhel Puri", "Masala Chai", "Filter Coffee")
            MealType.DINNER -> listOf("Paneer Butter Masala", "Mix Veg", "Biryani", "Egg Curry", "Dal Fry", "Phulka", "Jeera Rice", "Gulab Jamun", "Kheer", "Ice Cream", "Halwa")
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "${mealType.emoji} Edit ${mealType.displayName}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = dayName,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Food Items (Separate with '•' or commas)",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = itemsText,
                    onValueChange = { itemsText = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("edit_meal_items_input"),
                    placeholder = { Text("e.g. Rajma, Rice, Roti, Salad") },
                    leadingIcon = {
                        Icon(Icons.Default.Fastfood, contentDescription = "Food items")
                    },
                    minLines = 3,
                    maxLines = 5,
                    shape = RoundedCornerShape(12.dp)
                )

                // Quick item pills
                Text(
                    text = "Quick Add Suggestions:",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    quickSuggestions.forEach { suggestion ->
                        SuggestionChip(
                            onClick = {
                                if (itemsText.isBlank()) {
                                    itemsText = suggestion
                                } else {
                                    itemsText = "$itemsText • $suggestion"
                                }
                            },
                            label = { Text(suggestion, style = MaterialTheme.typography.labelSmall) },
                            icon = { Icon(Icons.Default.Add, contentDescription = "Add $suggestion") },
                            colors = SuggestionChipDefaults.suggestionChipColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Meal Timings (24-Hour Format)",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
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
                            Icon(Icons.Default.AccessTime, contentDescription = "Start time")
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = endTimeText,
                        onValueChange = { endTimeText = it },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("edit_meal_end_time_input"),
                        label = { Text("End (HH:mm)") },
                        leadingIcon = {
                            Icon(Icons.Default.AccessTime, contentDescription = "End time")
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
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
                        Icon(Icons.Default.Notes, contentDescription = "Special note")
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(meal.id, itemsText, startTimeText, endTimeText, noteText)
                },
                modifier = Modifier.testTag("save_meal_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text("Save Changes")
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("cancel_meal_button")
            ) {
                Text("Cancel")
            }
        }
    )
}

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
import com.example.ui.theme.SleekBorder
import com.example.ui.theme.SleekSecondaryContainer
import com.example.ui.theme.SleekTerracottaPrimary
import com.example.ui.theme.SleekTextPrimary
import com.example.ui.theme.SleekTextSecondary

@Composable
fun AddFoodItemDialog(
    existingCategories: List<String>,
    onDismiss: () -> Unit,
    onConfirmCreate: (name: String, category: String, emoji: String) -> Unit
) {
    var foodName by remember { mutableStateOf("") }
    var selectedCategory by remember {
        mutableStateOf(existingCategories.firstOrNull() ?: "Breakfast")
    }
    var customCategoryText by remember { mutableStateOf("") }
    var isCreatingNewCategory by remember { mutableStateOf(false) }
    var selectedEmoji by remember { mutableStateOf("🍽️") }

    val presetCategories = remember(existingCategories) {
        val standard = listOf(
            "Breakfast",
            "Breads & Roti",
            "Dal & Curries",
            "Rice & Biryani",
            "Snacks & Drinks",
            "Desserts & Sweets",
            "Sides & Salads"
        )
        (standard + existingCategories).distinct()
    }

    val emojiPresets = listOf("🍽️", "🥞", "🫓", "🍲", "🍚", "☕", "🍨", "🥗", "🥪", "🥟", "🧀", "🍳", "🍕", "🍜")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Add New Food",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = SleekTextPrimary
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
                // Food Name Input
                OutlinedTextField(
                    value = foodName,
                    onValueChange = { foodName = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("food_name_input"),
                    label = { Text("Food / Dish Name") },
                    placeholder = { Text("e.g. Paneer Tikka Masala") },
                    leadingIcon = {
                        Icon(Icons.Default.Fastfood, contentDescription = null, tint = SleekTerracottaPrimary)
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SleekTerracottaPrimary,
                        unfocusedBorderColor = SleekBorder
                    )
                )

                // Select Category
                Column {
                    Text(
                        text = "Choose Category",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = SleekTextPrimary
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        presetCategories.forEach { category ->
                            val isSelected = !isCreatingNewCategory && selectedCategory == category
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    isCreatingNewCategory = false
                                    selectedCategory = category
                                },
                                label = {
                                    Text(
                                        text = category,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = SleekTerracottaPrimary,
                                    selectedLabelColor = Color.White,
                                    containerColor = SleekSecondaryContainer,
                                    labelColor = SleekTextPrimary
                                ),
                                border = if (isSelected) null else BorderStroke(1.dp, SleekBorder)
                            )
                        }

                        // Option to add new category
                        FilterChip(
                            selected = isCreatingNewCategory,
                            onClick = { isCreatingNewCategory = true },
                            label = { Text("+ Custom Category", fontWeight = FontWeight.SemiBold) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = SleekTerracottaPrimary,
                                selectedLabelColor = Color.White,
                                containerColor = SleekSecondaryContainer,
                                labelColor = SleekTerracottaPrimary
                            ),
                            border = if (isCreatingNewCategory) null else BorderStroke(1.dp, SleekBorder)
                        )
                    }

                    if (isCreatingNewCategory) {
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = customCategoryText,
                            onValueChange = { customCategoryText = it },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("New Category Name") },
                            placeholder = { Text("e.g. Chinese & Italian") },
                            leadingIcon = {
                                Icon(Icons.Default.Category, contentDescription = null, tint = SleekTerracottaPrimary)
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = SleekTerracottaPrimary,
                                unfocusedBorderColor = SleekBorder
                            )
                        )
                    }
                }

                // Choose Emoji
                Column {
                    Text(
                        text = "Choose Icon",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = SleekTextPrimary
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        emojiPresets.forEach { emoji ->
                            val isSelected = selectedEmoji == emoji
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedEmoji = emoji },
                                label = { Text(emoji, fontSize = 18.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = SleekSecondaryContainer,
                                    containerColor = Color.Transparent
                                ),
                                border = if (isSelected) BorderStroke(2.dp, SleekTerracottaPrimary) else BorderStroke(1.dp, SleekBorder)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            val finalCategory = if (isCreatingNewCategory) customCategoryText.trim() else selectedCategory.trim()
            Button(
                onClick = {
                    if (foodName.isNotBlank() && finalCategory.isNotBlank()) {
                        onConfirmCreate(foodName.trim(), finalCategory, selectedEmoji)
                    }
                },
                enabled = foodName.isNotBlank() && finalCategory.isNotBlank(),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = SleekTerracottaPrimary
                ),
                modifier = Modifier.testTag("save_new_food_button")
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Add Food", fontWeight = FontWeight.Bold)
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

package com.example.ui.screens

import android.widget.Toast
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.PostAdd
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import com.example.data.local.FoodItemEntity
import com.example.data.model.DayEnum
import com.example.data.model.MealType
import com.example.ui.components.AddFoodItemDialog
import com.example.ui.components.AddFoodToTimetableDialog
import com.example.ui.components.AppFooter
import com.example.ui.theme.SleekBorder
import com.example.ui.theme.SleekOnPrimaryContainer
import com.example.ui.theme.SleekPrimaryContainer
import com.example.ui.theme.SleekSecondaryContainer
import com.example.ui.theme.SleekTerracottaPrimary
import com.example.ui.theme.SleekTextPrimary
import com.example.ui.theme.SleekTextSecondary

@Composable
fun FoodCategoriesScreen(
    foodItems: List<FoodItemEntity>,
    categories: List<String>,
    selectedCategory: String?,
    searchQuery: String,
    onSelectCategory: (String?) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onAddFoodItem: (name: String, category: String, emoji: String) -> Unit,
    onDeleteFoodItem: (Long) -> Unit,
    onAddFoodToTimetable: (dayOfWeek: Int, mealType: String, foodName: String) -> Unit
) {
    val context = LocalContext.current
    var showAddDialog by remember { mutableStateOf(false) }
    var foodItemForTimetable by remember { mutableStateOf<FoodItemEntity?>(null) }

    // Derived category list (standard + custom from items)
    val allDistinctCategories = remember(foodItems, categories) {
        val standard = listOf(
            "Breakfast",
            "Breads & Roti",
            "Dal & Curries",
            "Rice & Biryani",
            "Snacks & Drinks",
            "Desserts & Sweets",
            "Sides & Salads"
        )
        (standard + foodItems.map { it.category } + categories).distinct().filter { it.isNotBlank() }
    }

    // Filtered food list
    val filteredList = remember(foodItems, selectedCategory, searchQuery) {
        foodItems.filter { item ->
            val matchesCategory = selectedCategory == null || item.category.equals(selectedCategory, ignoreCase = true)
            val matchesSearch = searchQuery.isBlank() ||
                    item.name.contains(searchQuery, ignoreCase = true) ||
                    item.category.contains(searchQuery, ignoreCase = true)
            matchesCategory && matchesSearch
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("food_categories_screen"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Hero Header Card with Quick Add Action
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("food_catalog_hero_card"),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = SleekPrimaryContainer),
                border = BorderStroke(1.dp, SleekSecondaryContainer)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "FOOD LIBRARY",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = SleekTerracottaPrimary,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Menu Catalog & Categories",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = SleekOnPrimaryContainer
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${foodItems.size} dishes in ${allDistinctCategories.size} categories",
                            style = MaterialTheme.typography.bodySmall,
                            color = SleekTextSecondary
                        )
                    }

                    Button(
                        onClick = { showAddDialog = true },
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SleekTerracottaPrimary),
                        modifier = Modifier.testTag("add_new_food_fab"),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Food", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }

        // 2. Search Bar
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("food_search_input"),
                placeholder = { Text("Search dishes, curries, breads...", color = SleekTextSecondary.copy(alpha = 0.7f)) },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = "Search", tint = SleekTerracottaPrimary)
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchQueryChange("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear search", tint = SleekTextSecondary)
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(18.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = SleekSecondaryContainer.copy(alpha = 0.5f),
                    unfocusedContainerColor = SleekSecondaryContainer.copy(alpha = 0.35f),
                    focusedBorderColor = SleekTerracottaPrimary,
                    unfocusedBorderColor = SleekBorder
                )
            )
        }

        // 3. Category Horizontal Pills Filter
        item {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Categories",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = SleekTextPrimary
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    if (selectedCategory != null) {
                        Text(
                            text = "Clear filter",
                            style = MaterialTheme.typography.labelSmall,
                            color = SleekTerracottaPrimary,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.clickable { onSelectCategory(null) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // "All" chip
                    FilterChip(
                        selected = selectedCategory == null,
                        onClick = { onSelectCategory(null) },
                        label = {
                            Text(
                                text = "All (${foodItems.size})",
                                fontWeight = if (selectedCategory == null) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = SleekTerracottaPrimary,
                            selectedLabelColor = Color.White,
                            containerColor = SleekSecondaryContainer,
                            labelColor = SleekTextPrimary
                        ),
                        border = if (selectedCategory == null) null else BorderStroke(1.dp, SleekBorder)
                    )

                    allDistinctCategories.forEach { category ->
                        val count = foodItems.count { it.category.equals(category, ignoreCase = true) }
                        val isSelected = selectedCategory?.equals(category, ignoreCase = true) == true
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                if (isSelected) onSelectCategory(null) else onSelectCategory(category)
                            },
                            label = {
                                Text(
                                    text = "$category ($count)",
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
                }
            }
        }

        // 4. Section Subtitle
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (selectedCategory != null) "$selectedCategory (${filteredList.size})" else "All Dishes (${filteredList.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = SleekTextPrimary
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "Tap + to add to schedule",
                    style = MaterialTheme.typography.labelSmall,
                    color = SleekTextSecondary
                )
            }
        }

        // 5. Empty State
        if (filteredList.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = SleekSecondaryContainer)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "🔍", fontSize = 32.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No dishes found",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = SleekTextPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Try another search or tap 'Add Food' to create one.",
                            style = MaterialTheme.typography.bodySmall,
                            color = SleekTextSecondary
                        )
                    }
                }
            }
        }

        // 6. Food Item Cards
        items(filteredList, key = { it.id }) { item ->
            FoodItemCard(
                item = item,
                onAddToTimetable = { foodItemForTimetable = item },
                onDelete = { onDeleteFoodItem(item.id) }
            )
        }

        // 7. Footer Credits
        item {
            AppFooter()
            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    // Dialog: Create New Food Item
    if (showAddDialog) {
        AddFoodItemDialog(
            existingCategories = allDistinctCategories,
            onDismiss = { showAddDialog = false },
            onConfirmCreate = { name, category, emoji ->
                onAddFoodItem(name, category, emoji)
                showAddDialog = false
                Toast.makeText(context, "Added '$name' to $category", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Dialog: Add Food to Timetable
    foodItemForTimetable?.let { item ->
        AddFoodToTimetableDialog(
            foodItem = item,
            onDismiss = { foodItemForTimetable = null },
            onConfirmAdd = { dayOfWeek, mealType, foodName ->
                onAddFoodToTimetable(dayOfWeek, mealType, foodName)
                foodItemForTimetable = null
                val dayName = DayEnum.fromDayNumber(dayOfWeek).fullName
                val typeName = MealType.fromString(mealType).displayName
                Toast.makeText(context, "Added '$foodName' to $dayName $typeName!", Toast.LENGTH_SHORT).show()
            }
        )
    }
}

@Composable
fun FoodItemCard(
    item: FoodItemEntity,
    onAddToTimetable: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("food_card_${item.id}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, SleekBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Emoji circle
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(SleekSecondaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(text = item.iconEmoji, fontSize = 22.sp)
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Food Name & Category Pill
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = SleekTextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = SleekSecondaryContainer.copy(alpha = 0.7f)
                ) {
                    Text(
                        text = item.category,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = SleekTerracottaPrimary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Action: Add to Timetable
            Button(
                onClick = onAddToTimetable,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = SleekPrimaryContainer,
                    contentColor = SleekTerracottaPrimary
                ),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                modifier = Modifier.testTag("add_to_timetable_button_${item.id}")
            ) {
                Icon(
                    Icons.Default.PostAdd,
                    contentDescription = "Add to timetable",
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "+ Timetable",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            // Optional delete for custom or any item
            if (item.isCustom) {
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(34.dp)
                ) {
                    Icon(
                        Icons.Default.DeleteOutline,
                        contentDescription = "Delete dish",
                        tint = SleekTextSecondary.copy(alpha = 0.6f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

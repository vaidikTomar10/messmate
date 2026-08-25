package com.example.data.repository

import com.example.data.local.FoodDao
import com.example.data.local.FoodItemEntity
import com.example.data.local.MealDao
import com.example.data.local.MealEntity
import com.example.data.model.MealType
import kotlinx.coroutines.flow.Flow

class MealRepository(
    private val mealDao: MealDao,
    private val foodDao: FoodDao
) {

    val allMeals: Flow<List<MealEntity>> = mealDao.getAllMealsFlow()
    val allFoodItems: Flow<List<FoodItemEntity>> = foodDao.getAllFoodItemsFlow()
    val allCategories: Flow<List<String>> = foodDao.getAllCategoriesFlow()

    fun getMealsForDay(dayOfWeek: Int): Flow<List<MealEntity>> {
        return mealDao.getMealsForDayFlow(dayOfWeek)
    }

    suspend fun getMealsForDaySync(dayOfWeek: Int): List<MealEntity> {
        return mealDao.getMealsForDaySync(dayOfWeek)
    }

    suspend fun getAllMealsSync(): List<MealEntity> {
        return mealDao.getAllMealsSync()
    }

    suspend fun getMealById(id: Long): MealEntity? {
        return mealDao.getMealById(id)
    }

    suspend fun updateMeal(meal: MealEntity) {
        mealDao.updateMeal(meal)
    }

    suspend fun insertMeal(meal: MealEntity) {
        mealDao.insertMeal(meal)
    }

    suspend fun toggleFavorite(id: Long, isFavorite: Boolean) {
        mealDao.toggleFavorite(id, isFavorite)
    }

    suspend fun addFoodItemToMeal(dayOfWeek: Int, mealType: String, foodName: String) {
        val dayMeals = mealDao.getMealsForDaySync(dayOfWeek)
        val targetMeal = dayMeals.firstOrNull { it.mealType.equals(mealType, ignoreCase = true) }
        if (targetMeal != null) {
            val updatedItems = if (targetMeal.items.isBlank()) {
                foodName.trim()
            } else {
                "${targetMeal.items.trim()} • ${foodName.trim()}"
            }
            mealDao.updateMeal(targetMeal.copy(items = updatedItems))
        }
    }

    suspend fun insertFoodItem(name: String, category: String, iconEmoji: String = "🍽️"): Long {
        return foodDao.insertFoodItem(
            FoodItemEntity(
                name = name.trim(),
                category = category.trim(),
                iconEmoji = iconEmoji,
                isCustom = true
            )
        )
    }

    suspend fun deleteFoodItem(id: Long) {
        foodDao.deleteFoodItemById(id)
    }

    suspend fun ensureDefaultDataPopulated() {
        val mealCount = mealDao.getMealCount()
        if (mealCount == 0) {
            populateDefaultMenu()
        }

        val foodCount = foodDao.getFoodItemCount()
        if (foodCount == 0) {
            populateDefaultFoodLibrary()
        }
    }

    suspend fun resetToDefaultMenu() {
        mealDao.deleteAllMeals()
        populateDefaultMenu()
    }

    suspend fun resetFoodLibrary() {
        foodDao.deleteAllFoodItems()
        populateDefaultFoodLibrary()
    }

    private suspend fun populateDefaultFoodLibrary() {
        val defaultFoodList = listOf(
            // Breakfast
            FoodItemEntity(name = "Poha", category = "Breakfast", iconEmoji = "🥞"),
            FoodItemEntity(name = "Aloo Paratha", category = "Breakfast", iconEmoji = "🫓"),
            FoodItemEntity(name = "Masala Dosa", category = "Breakfast", iconEmoji = "🥞"),
            FoodItemEntity(name = "Idli Sambar", category = "Breakfast", iconEmoji = "🥟"),
            FoodItemEntity(name = "Medu Vada", category = "Breakfast", iconEmoji = "🍩"),
            FoodItemEntity(name = "Methi Thepla", category = "Breakfast", iconEmoji = "🫓"),
            FoodItemEntity(name = "Rava Upma", category = "Breakfast", iconEmoji = "🥣"),
            FoodItemEntity(name = "Puri Bhaji", category = "Breakfast", iconEmoji = "🫓"),
            FoodItemEntity(name = "Bread Omelette", category = "Breakfast", iconEmoji = "🍳"),
            FoodItemEntity(name = "Boiled Eggs", category = "Breakfast", iconEmoji = "🥚"),
            FoodItemEntity(name = "Uttapam", category = "Breakfast", iconEmoji = "🥞"),
            FoodItemEntity(name = "Cornflakes & Milk", category = "Breakfast", iconEmoji = "🥣"),

            // Breads & Roti
            FoodItemEntity(name = "Butter Roti", category = "Breads & Roti", iconEmoji = "🫓"),
            FoodItemEntity(name = "Phulka Roti", category = "Breads & Roti", iconEmoji = "🫓"),
            FoodItemEntity(name = "Butter Naan", category = "Breads & Roti", iconEmoji = "🫓"),
            FoodItemEntity(name = "Garlic Naan", category = "Breads & Roti", iconEmoji = "🫓"),
            FoodItemEntity(name = "Bhature", category = "Breads & Roti", iconEmoji = "🫓"),
            FoodItemEntity(name = "Missi Roti", category = "Breads & Roti", iconEmoji = "🫓"),
            FoodItemEntity(name = "Laccha Paratha", category = "Breads & Roti", iconEmoji = "🫓"),
            FoodItemEntity(name = "Tandoori Roti", category = "Breads & Roti", iconEmoji = "🫓"),

            // Dal & Curries
            FoodItemEntity(name = "Rajma Masala", category = "Dal & Curries", iconEmoji = "🍲"),
            FoodItemEntity(name = "Chole Masala", category = "Dal & Curries", iconEmoji = "🍲"),
            FoodItemEntity(name = "Shahi Paneer", category = "Dal & Curries", iconEmoji = "🧀"),
            FoodItemEntity(name = "Paneer Butter Masala", category = "Dal & Curries", iconEmoji = "🧀"),
            FoodItemEntity(name = "Matar Paneer", category = "Dal & Curries", iconEmoji = "🧀"),
            FoodItemEntity(name = "Palak Paneer", category = "Dal & Curries", iconEmoji = "🥬"),
            FoodItemEntity(name = "Dal Tadka", category = "Dal & Curries", iconEmoji = "🍲"),
            FoodItemEntity(name = "Dal Makhani", category = "Dal & Curries", iconEmoji = "🍲"),
            FoodItemEntity(name = "Dal Fry", category = "Dal & Curries", iconEmoji = "🍲"),
            FoodItemEntity(name = "Kadhi Pakoda", category = "Dal & Curries", iconEmoji = "🍲"),
            FoodItemEntity(name = "Aloo Gobi Masala", category = "Dal & Curries", iconEmoji = "🥦"),
            FoodItemEntity(name = "Dum Aloo Kashmiri", category = "Dal & Curries", iconEmoji = "🥔"),
            FoodItemEntity(name = "Egg Curry", category = "Dal & Curries", iconEmoji = "🍳"),
            FoodItemEntity(name = "Mix Veg Kolhapuri", category = "Dal & Curries", iconEmoji = "🥕"),

            // Rice & Biryani
            FoodItemEntity(name = "Steamed Basmati Rice", category = "Rice & Biryani", iconEmoji = "🍚"),
            FoodItemEntity(name = "Jeera Rice", category = "Rice & Biryani", iconEmoji = "🍚"),
            FoodItemEntity(name = "Veg Dum Biryani", category = "Rice & Biryani", iconEmoji = "🍲"),
            FoodItemEntity(name = "Veg Pulao", category = "Rice & Biryani", iconEmoji = "🍚"),
            FoodItemEntity(name = "Ghee Rice", category = "Rice & Biryani", iconEmoji = "🍚"),
            FoodItemEntity(name = "Peas Pulao", category = "Rice & Biryani", iconEmoji = "🍚"),
            FoodItemEntity(name = "Kashmiri Pulao", category = "Rice & Biryani", iconEmoji = "🍚"),
            FoodItemEntity(name = "Veg Fried Rice", category = "Rice & Biryani", iconEmoji = "🥡"),
            FoodItemEntity(name = "Curd Rice", category = "Rice & Biryani", iconEmoji = "🍚"),

            // Snacks & Drinks
            FoodItemEntity(name = "Crispy Samosa", category = "Snacks & Drinks", iconEmoji = "🥟"),
            FoodItemEntity(name = "Veg Cutlet", category = "Snacks & Drinks", iconEmoji = "🥪"),
            FoodItemEntity(name = "Bread Pakoda", category = "Snacks & Drinks", iconEmoji = "🥪"),
            FoodItemEntity(name = "Onion Pakora", category = "Snacks & Drinks", iconEmoji = "🧅"),
            FoodItemEntity(name = "Mumbai Pav Bhaji", category = "Snacks & Drinks", iconEmoji = "🍞"),
            FoodItemEntity(name = "Classic Maggi", category = "Snacks & Drinks", iconEmoji = "🍜"),
            FoodItemEntity(name = "Bhel Puri", category = "Snacks & Drinks", iconEmoji = "🥗"),
            FoodItemEntity(name = "Masala Chai", category = "Snacks & Drinks", iconEmoji = "☕"),
            FoodItemEntity(name = "Filter Coffee", category = "Snacks & Drinks", iconEmoji = "☕"),
            FoodItemEntity(name = "Ginger Tea", category = "Snacks & Drinks", iconEmoji = "🫖"),
            FoodItemEntity(name = "Masala Chaas", category = "Snacks & Drinks", iconEmoji = "🥛"),
            FoodItemEntity(name = "Sweet Lassi", category = "Snacks & Drinks", iconEmoji = "🥤"),
            FoodItemEntity(name = "Cold Coffee", category = "Snacks & Drinks", iconEmoji = "🥤"),

            // Desserts & Sweets
            FoodItemEntity(name = "Gulab Jamun", category = "Desserts & Sweets", iconEmoji = "🍯"),
            FoodItemEntity(name = "Rice Kheer", category = "Desserts & Sweets", iconEmoji = "🥣"),
            FoodItemEntity(name = "Moong Dal Halwa", category = "Desserts & Sweets", iconEmoji = "🍮"),
            FoodItemEntity(name = "Sooji Halwa", category = "Desserts & Sweets", iconEmoji = "🍮"),
            FoodItemEntity(name = "Rasgulla", category = "Desserts & Sweets", iconEmoji = "⚪"),
            FoodItemEntity(name = "Rasmalai", category = "Desserts & Sweets", iconEmoji = "🥛"),
            FoodItemEntity(name = "Vanilla Ice Cream", category = "Desserts & Sweets", iconEmoji = "🍨"),
            FoodItemEntity(name = "Mango Kulfi", category = "Desserts & Sweets", iconEmoji = "🥭"),
            FoodItemEntity(name = "Chocolate Brownie", category = "Desserts & Sweets", iconEmoji = "🍫"),
            FoodItemEntity(name = "Crispy Jalebi", category = "Desserts & Sweets", iconEmoji = "🥨"),

            // Sides & Salads
            FoodItemEntity(name = "Boondi Raita", category = "Sides & Salads", iconEmoji = "🥣"),
            FoodItemEntity(name = "Mix Veg Raita", category = "Sides & Salads", iconEmoji = "🥣"),
            FoodItemEntity(name = "Cucumber Salad", category = "Sides & Salads", iconEmoji = "🥒"),
            FoodItemEntity(name = "Fresh Green Salad", category = "Sides & Salads", iconEmoji = "🥗"),
            FoodItemEntity(name = "Sweet Curd", category = "Sides & Salads", iconEmoji = "🥛"),
            FoodItemEntity(name = "Roasted Papad", category = "Sides & Salads", iconEmoji = "🍘"),
            FoodItemEntity(name = "Mixed Pickle", category = "Sides & Salads", iconEmoji = "🫙"),
            FoodItemEntity(name = "Mint Chutney", category = "Sides & Salads", iconEmoji = "🌿")
        )
        foodDao.insertAll(defaultFoodList)
    }

    private suspend fun populateDefaultMenu() {
        val defaultMeals = listOf(
            // Monday (1)
            MealEntity(
                dayOfWeek = 1,
                mealType = MealType.BREAKFAST.name,
                items = "Poha • Mint Chutney • Boiled Egg / Banana • Masala Chai & Filter Coffee",
                startTime = "07:30",
                endTime = "09:30",
                specialNote = "Fresh hot breakfast"
            ),
            MealEntity(
                dayOfWeek = 1,
                mealType = MealType.LUNCH.name,
                items = "Rajma Masala • Steamed Rice • Butter Roti • Cucumber Salad • Sweet Curd",
                startTime = "12:30",
                endTime = "14:30",
                specialNote = "Chef Special Rajma"
            ),
            MealEntity(
                dayOfWeek = 1,
                mealType = MealType.SNACKS.name,
                items = "Crispy Samosa • Green Mint Chutney • Masala Tea",
                startTime = "17:00",
                endTime = "18:30"
            ),
            MealEntity(
                dayOfWeek = 1,
                mealType = MealType.DINNER.name,
                items = "Aloo Gobi Masala • Dal Tadka • Phulka Roti • Jeera Rice • Gulab Jamun",
                startTime = "20:00",
                endTime = "22:00",
                specialNote = "Sweet included"
            ),

            // Tuesday (2)
            MealEntity(
                dayOfWeek = 2,
                mealType = MealType.BREAKFAST.name,
                items = "Aloo Paratha • Butter Cubes • Mango Pickle • Curd • Hot Tea",
                startTime = "07:30",
                endTime = "09:30"
            ),
            MealEntity(
                dayOfWeek = 2,
                mealType = MealType.LUNCH.name,
                items = "Chole Masala • Bhature • Jeera Rice • Sliced Onion & Lemon • Boondi Raita",
                startTime = "12:30",
                endTime = "14:30",
                specialNote = "Punjabi Chole Bhature"
            ),
            MealEntity(
                dayOfWeek = 2,
                mealType = MealType.SNACKS.name,
                items = "Veg Cutlet • Tomato Ketchup • Filter Coffee",
                startTime = "17:00",
                endTime = "18:30"
            ),
            MealEntity(
                dayOfWeek = 2,
                mealType = MealType.DINNER.name,
                items = "Shahi Paneer • Dal Makhani • Butter Tawa Roti • Peas Pulao • Kheer",
                startTime = "20:00",
                endTime = "22:00",
                specialNote = "Rich Paneer Feast"
            ),

            // Wednesday (3)
            MealEntity(
                dayOfWeek = 3,
                mealType = MealType.BREAKFAST.name,
                items = "Crispy Masala Dosa • Coconut Chutney • Vegetable Sambar • Tea",
                startTime = "07:30",
                endTime = "09:30"
            ),
            MealEntity(
                dayOfWeek = 3,
                mealType = MealType.LUNCH.name,
                items = "Kadhi Pakoda • Steamed Basmati Rice • Mix Veg Sabzi • Chapati • Roasted Papad",
                startTime = "12:30",
                endTime = "14:30"
            ),
            MealEntity(
                dayOfWeek = 3,
                mealType = MealType.SNACKS.name,
                items = "Bread Pakoda • Tamarind Chutney • Ginger Chai",
                startTime = "17:00",
                endTime = "18:30"
            ),
            MealEntity(
                dayOfWeek = 3,
                mealType = MealType.DINNER.name,
                items = "Paneer Bhurji / Egg Curry • Dal Fry • Hot Phulka • Jeera Rice • Vanilla Ice Cream",
                startTime = "20:00",
                endTime = "22:00"
            ),

            // Thursday (4)
            MealEntity(
                dayOfWeek = 4,
                mealType = MealType.BREAKFAST.name,
                items = "Idli • Medu Vada • Sambar • Coconut & Peanut Chutney • Filter Coffee",
                startTime = "07:30",
                endTime = "09:30"
            ),
            MealEntity(
                dayOfWeek = 4,
                mealType = MealType.LUNCH.name,
                items = "Kala Chana Curry • Veg Dum Biryani • Mirchi Ka Salan • Roti • Onion Raita",
                startTime = "12:30",
                endTime = "14:30"
            ),
            MealEntity(
                dayOfWeek = 4,
                mealType = MealType.SNACKS.name,
                items = "Crispy Onion Pakora • Fried Green Chillies • Masala Tea",
                startTime = "17:00",
                endTime = "18:30"
            ),
            MealEntity(
                dayOfWeek = 4,
                mealType = MealType.DINNER.name,
                items = "Mix Veg Kolhapuri • Dal Tadka • Butter Roti • Steamed Rice • Sponge Rasgulla",
                startTime = "20:00",
                endTime = "22:00"
            ),

            // Friday (5)
            MealEntity(
                dayOfWeek = 5,
                mealType = MealType.BREAKFAST.name,
                items = "Methi Thepla • Sweet Chhundo • Fresh Curd • Masala Chai",
                startTime = "07:30",
                endTime = "09:30"
            ),
            MealEntity(
                dayOfWeek = 5,
                mealType = MealType.LUNCH.name,
                items = "Dal Fry • Aloo Jeera • Ghee Rice • Butter Roti • Green Salad • Masala Chaas",
                startTime = "12:30",
                endTime = "14:30"
            ),
            MealEntity(
                dayOfWeek = 5,
                mealType = MealType.SNACKS.name,
                items = "Mumbai Pav Bhaji • Butter Toasted Pav • Diced Onions & Lemon",
                startTime = "17:00",
                endTime = "18:30",
                specialNote = "Popular Friday Snack"
            ),
            MealEntity(
                dayOfWeek = 5,
                mealType = MealType.DINNER.name,
                items = "Matar Paneer • Yellow Moong Dal • Tawa Roti • Jeera Rice • Moong Dal Halwa",
                startTime = "20:00",
                endTime = "22:00",
                specialNote = "Special Halwa Night"
            ),

            // Saturday (6)
            MealEntity(
                dayOfWeek = 6,
                mealType = MealType.BREAKFAST.name,
                items = "Rava Upma • Coconut Chutney • Banana / Boiled Egg • Hot Milk & Tea",
                startTime = "07:30",
                endTime = "09:30"
            ),
            MealEntity(
                dayOfWeek = 6,
                mealType = MealType.LUNCH.name,
                items = "Dum Aloo Kashmiri • Dal Makhani • Phulka Roti • Veg Pulao • Masala Curd",
                startTime = "12:30",
                endTime = "14:30"
            ),
            MealEntity(
                dayOfWeek = 6,
                mealType = MealType.SNACKS.name,
                items = "Classic Maggi Noodles • Cold Beverage / Hot Chai",
                startTime = "17:00",
                endTime = "18:30"
            ),
            MealEntity(
                dayOfWeek = 6,
                mealType = MealType.DINNER.name,
                items = "Hyderabadi Veg Biryani • Gravy Salan • Mint Raita • Butter Roti • Chocolate Brownie",
                startTime = "20:00",
                endTime = "22:00",
                specialNote = "Saturday Biryani Special"
            ),

            // Sunday (7)
            MealEntity(
                dayOfWeek = 7,
                mealType = MealType.BREAKFAST.name,
                items = "Puri Bhaji • Sooji Halwa • Mixed Pickle • Special Masala Chai",
                startTime = "08:00",
                endTime = "10:00",
                specialNote = "Sunday Extended Breakfast"
            ),
            MealEntity(
                dayOfWeek = 7,
                mealType = MealType.LUNCH.name,
                items = "Royal Shahi Paneer • Butter Naan • Dal Maharani • Kashmiri Pulao • Rasmalai",
                startTime = "13:00",
                endTime = "15:00",
                specialNote = "Grand Sunday Feast"
            ),
            MealEntity(
                dayOfWeek = 7,
                mealType = MealType.SNACKS.name,
                items = "Bhel Puri • Sev Puri • Cutting Chai",
                startTime = "17:30",
                endTime = "18:30"
            ),
            MealEntity(
                dayOfWeek = 7,
                mealType = MealType.DINNER.name,
                items = "Paneer Tikka Masala • Dal Tadka • Butter Roti • Fried Rice • Mango Kulfi",
                startTime = "20:00",
                endTime = "22:00",
                specialNote = "Weekend Kulfi Dessert"
            )
        )
        mealDao.insertAll(defaultMeals)
    }
}

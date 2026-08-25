package com.example.data.model

enum class MealType(
    val displayName: String,
    val defaultStart: String,
    val defaultEnd: String,
    val order: Int,
    val emoji: String
) {
    BREAKFAST("Breakfast", "07:30", "09:30", 1, "🍳"),
    LUNCH("Lunch", "12:30", "14:30", 2, "🍛"),
    SNACKS("Snacks", "17:00", "18:30", 3, "☕"),
    DINNER("Dinner", "20:00", "22:00", 4, "🍲");

    companion object {
        fun fromString(value: String): MealType {
            return entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: BREAKFAST
        }
    }
}

enum class DayEnum(val dayNumber: Int, val shortName: String, val fullName: String) {
    MONDAY(1, "Mon", "Monday"),
    TUESDAY(2, "Tue", "Tuesday"),
    WEDNESDAY(3, "Wed", "Wednesday"),
    THURSDAY(4, "Thu", "Thursday"),
    FRIDAY(5, "Fri", "Friday"),
    SATURDAY(6, "Sat", "Saturday"),
    SUNDAY(7, "Sun", "Sunday");

    companion object {
        fun fromDayNumber(day: Int): DayEnum {
            return entries.firstOrNull { it.dayNumber == day } ?: MONDAY
        }
    }
}

enum class MealActiveStatus {
    ACTIVE_NOW,
    UPCOMING_TODAY,
    UPCOMING_TOMORROW,
    COMPLETED
}

data class ActiveMealState(
    val activeMeal: MealPresentation?,
    val nextMeal: MealPresentation?,
    val statusText: String,
    val timeRemainingFormatted: String,
    val isCurrentlyActive: Boolean
)

data class MealPresentation(
    val id: Long,
    val dayOfWeek: Int,
    val mealType: MealType,
    val itemsList: List<String>,
    val rawItems: String,
    val startTime: String,
    val endTime: String,
    val specialNote: String,
    val isFavorite: Boolean,
    val status: MealActiveStatus
)

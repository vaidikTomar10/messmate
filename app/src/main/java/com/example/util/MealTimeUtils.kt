package com.example.util

import com.example.data.local.MealEntity
import com.example.data.model.ActiveMealState
import com.example.data.model.DayEnum
import com.example.data.model.MealActiveStatus
import com.example.data.model.MealPresentation
import com.example.data.model.MealType
import java.util.Calendar
import java.util.Locale

object MealTimeUtils {

    /**
     * Converts calendar to Monday-based day index: 1 (Mon) .. 7 (Sun)
     */
    fun getCurrentDayNumber(calendar: Calendar = Calendar.getInstance()): Int {
        val calDay = calendar.get(Calendar.DAY_OF_WEEK)
        return if (calDay == Calendar.SUNDAY) 7 else calDay - 1
    }

    /**
     * Returns current minutes from start of day (0..1439)
     */
    fun getCurrentMinutesOfDay(calendar: Calendar = Calendar.getInstance()): Int {
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)
        return hour * 60 + minute
    }

    /**
     * Parses "HH:mm" (24-hour) string to minutes of day
     */
    fun parseTimeToMinutes(timeStr: String): Int {
        return try {
            val parts = timeStr.trim().split(":")
            val h = parts[0].toInt()
            val m = if (parts.size > 1) parts[1].toInt() else 0
            h * 60 + m
        } catch (e: Exception) {
            0
        }
    }

    /**
     * Formats 24h "HH:mm" to 12h with AM/PM e.g. "1:00 PM"
     */
    fun format12Hour(timeStr: String): String {
        return try {
            val parts = timeStr.trim().split(":")
            val h = parts[0].toInt()
            val m = if (parts.size > 1) parts[1].toInt() else 0
            val amPm = if (h >= 12) "PM" else "AM"
            val displayH = when {
                h == 0 -> 12
                h > 12 -> h - 12
                else -> h
            }
            String.format(Locale.getDefault(), "%d:%02d %s", displayH, m, amPm)
        } catch (e: Exception) {
            timeStr
        }
    }

    fun formatTimeRange(startTime: String, endTime: String): String {
        return "${format12Hour(startTime)} – ${format12Hour(endTime)}"
    }

    /**
     * Splits comma/bullet separated items into cleaned list
     */
    fun parseItemsList(rawItems: String): List<String> {
        if (rawItems.isBlank()) return emptyList()
        val delimiters = arrayOf("•", ",", "\n", "|", ";")
        var current = listOf(rawItems)
        for (delimiter in delimiters) {
            current = current.flatMap { it.split(delimiter) }
        }
        return current.map { it.trim() }.filter { it.isNotEmpty() }
    }

    /**
     * Converts a MealEntity to MealPresentation
     */
    fun toPresentation(entity: MealEntity, currentDay: Int, currentMinutes: Int): MealPresentation {
        val type = MealType.fromString(entity.mealType)
        val startM = parseTimeToMinutes(entity.startTime)
        val endM = parseTimeToMinutes(entity.endTime)

        val status = when {
            entity.dayOfWeek < currentDay -> MealActiveStatus.COMPLETED
            entity.dayOfWeek > currentDay -> MealActiveStatus.UPCOMING_TOMORROW
            else -> {
                when {
                    currentMinutes in startM..endM -> MealActiveStatus.ACTIVE_NOW
                    currentMinutes < startM -> MealActiveStatus.UPCOMING_TODAY
                    else -> MealActiveStatus.COMPLETED
                }
            }
        }

        return MealPresentation(
            id = entity.id,
            dayOfWeek = entity.dayOfWeek,
            mealType = type,
            itemsList = parseItemsList(entity.items),
            rawItems = entity.items,
            startTime = entity.startTime,
            endTime = entity.endTime,
            specialNote = entity.specialNote,
            isFavorite = entity.isFavorite,
            status = status
        )
    }

    /**
     * Computes the currently active meal state and next upcoming meal
     */
    fun calculateActiveMealState(
        allMeals: List<MealEntity>,
        calendar: Calendar = Calendar.getInstance()
    ): ActiveMealState {
        if (allMeals.isEmpty()) {
            return ActiveMealState(
                activeMeal = null,
                nextMeal = null,
                statusText = "No menu available",
                timeRemainingFormatted = "",
                isCurrentlyActive = false
            )
        }

        val currentDay = getCurrentDayNumber(calendar)
        val currentMinutes = getCurrentMinutesOfDay(calendar)

        val todayMeals = allMeals
            .filter { it.dayOfWeek == currentDay }
            .sortedBy { parseTimeToMinutes(it.startTime) }

        val activeEntity = todayMeals.firstOrNull {
            val start = parseTimeToMinutes(it.startTime)
            val end = parseTimeToMinutes(it.endTime)
            currentMinutes in start..end
        }

        if (activeEntity != null) {
            val activePres = toPresentation(activeEntity, currentDay, currentMinutes)
            val endM = parseTimeToMinutes(activeEntity.endTime)
            val remainingM = (endM - currentMinutes).coerceAtLeast(0)
            val hours = remainingM / 60
            val mins = remainingM % 60
            val timeLeftText = if (hours > 0) "${hours}h ${mins}m left" else "${mins} mins left"

            // Next meal after active
            val nextToday = todayMeals.firstOrNull { parseTimeToMinutes(it.startTime) > currentMinutes }
            val nextPres = if (nextToday != null) {
                toPresentation(nextToday, currentDay, currentMinutes)
            } else {
                // Next day's first meal
                val nextDay = if (currentDay == 7) 1 else currentDay + 1
                val tomorrowMeals = allMeals.filter { it.dayOfWeek == nextDay }.sortedBy { parseTimeToMinutes(it.startTime) }
                tomorrowMeals.firstOrNull()?.let { toPresentation(it, currentDay, currentMinutes) }
            }

            return ActiveMealState(
                activeMeal = activePres,
                nextMeal = nextPres,
                statusText = "Happening Now • $timeLeftText",
                timeRemainingFormatted = timeLeftText,
                isCurrentlyActive = true
            )
        }

        // No meal active right now, find the NEXT upcoming meal today
        val nextToday = todayMeals.firstOrNull { parseTimeToMinutes(it.startTime) > currentMinutes }
        if (nextToday != null) {
            val nextPres = toPresentation(nextToday, currentDay, currentMinutes)
            val startM = parseTimeToMinutes(nextToday.startTime)
            val startsIn = (startM - currentMinutes).coerceAtLeast(0)
            val hours = startsIn / 60
            val mins = startsIn % 60
            val countdown = if (hours > 0) "Starts in ${hours}h ${mins}m" else "Starts in ${mins} mins"

            return ActiveMealState(
                activeMeal = null,
                nextMeal = nextPres,
                statusText = "Next Meal • $countdown",
                timeRemainingFormatted = countdown,
                isCurrentlyActive = false
            )
        }

        // All meals for today ended, show tomorrow morning's breakfast!
        val nextDay = if (currentDay == 7) 1 else currentDay + 1
        val tomorrowMeals = allMeals.filter { it.dayOfWeek == nextDay }.sortedBy { parseTimeToMinutes(it.startTime) }
        val tomorrowFirst = tomorrowMeals.firstOrNull()

        return if (tomorrowFirst != null) {
            val pres = toPresentation(tomorrowFirst, currentDay, currentMinutes)
            val nextDayName = DayEnum.fromDayNumber(nextDay).shortName
            ActiveMealState(
                activeMeal = null,
                nextMeal = pres,
                statusText = "Today's meals ended • Next: $nextDayName Breakfast",
                timeRemainingFormatted = "Tomorrow at ${format12Hour(tomorrowFirst.startTime)}",
                isCurrentlyActive = false
            )
        } else {
            ActiveMealState(
                activeMeal = null,
                nextMeal = null,
                statusText = "Menu up to date",
                timeRemainingFormatted = "",
                isCurrentlyActive = false
            )
        }
    }
}

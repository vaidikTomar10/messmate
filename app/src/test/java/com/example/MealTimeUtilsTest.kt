package com.example

import com.example.data.local.MealEntity
import com.example.data.model.MealActiveStatus
import com.example.data.model.MealType
import com.example.util.MealTimeUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

class MealTimeUtilsTest {

    @Test
    fun testParseTimeToMinutes() {
        assertEquals(0, MealTimeUtils.parseTimeToMinutes("00:00"))
        assertEquals(7 * 60 + 30, MealTimeUtils.parseTimeToMinutes("07:30"))
        assertEquals(12 * 60 + 45, MealTimeUtils.parseTimeToMinutes("12:45"))
        assertEquals(23 * 60 + 59, MealTimeUtils.parseTimeToMinutes("23:59"))
        assertEquals(0, MealTimeUtils.parseTimeToMinutes("invalid"))
    }

    @Test
    fun testFormat12Hour() {
        assertEquals("7:30 AM", MealTimeUtils.format12Hour("07:30"))
        assertEquals("12:00 PM", MealTimeUtils.format12Hour("12:00"))
        assertEquals("1:15 PM", MealTimeUtils.format12Hour("13:15"))
        assertEquals("8:45 PM", MealTimeUtils.format12Hour("20:45"))
        assertEquals("12:00 AM", MealTimeUtils.format12Hour("00:00"))
    }

    @Test
    fun testParseItemsList() {
        val items = MealTimeUtils.parseItemsList("Poha • Mint Chutney • Masala Tea")
        assertEquals(3, items.size)
        assertEquals("Poha", items[0])
        assertEquals("Mint Chutney", items[1])
        assertEquals("Masala Tea", items[2])

        val commaItems = MealTimeUtils.parseItemsList("Rajma, Rice, Roti, Salad")
        assertEquals(4, commaItems.size)
        assertEquals("Rajma", commaItems[0])
        assertEquals("Salad", commaItems[3])

        val empty = MealTimeUtils.parseItemsList("")
        assertTrue(empty.isEmpty())
    }

    @Test
    fun testActiveMealDetection_ActiveNow() {
        val testMeals = listOf(
            MealEntity(id = 1, dayOfWeek = 1, mealType = "BREAKFAST", items = "Poha", startTime = "07:30", endTime = "09:30"),
            MealEntity(id = 2, dayOfWeek = 1, mealType = "LUNCH", items = "Rajma Chawal", startTime = "12:30", endTime = "14:30"),
            MealEntity(id = 3, dayOfWeek = 1, mealType = "SNACKS", items = "Samosa", startTime = "17:00", endTime = "18:30"),
            MealEntity(id = 4, dayOfWeek = 1, mealType = "DINNER", items = "Paneer Butter Masala", startTime = "20:00", endTime = "22:00")
        )

        // Set calendar to Monday at 13:00 (Lunch is active)
        val cal = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
            set(Calendar.HOUR_OF_DAY, 13)
            set(Calendar.MINUTE, 0)
        }

        val state = MealTimeUtils.calculateActiveMealState(testMeals, cal)
        assertTrue(state.isCurrentlyActive)
        assertNotNull(state.activeMeal)
        assertEquals(MealType.LUNCH, state.activeMeal?.mealType)
        assertEquals(MealActiveStatus.ACTIVE_NOW, state.activeMeal?.status)
        assertNotNull(state.nextMeal)
        assertEquals(MealType.SNACKS, state.nextMeal?.mealType)
    }

    @Test
    fun testActiveMealDetection_UpcomingToday() {
        val testMeals = listOf(
            MealEntity(id = 1, dayOfWeek = 1, mealType = "BREAKFAST", items = "Poha", startTime = "07:30", endTime = "09:30"),
            MealEntity(id = 2, dayOfWeek = 1, mealType = "LUNCH", items = "Rajma Chawal", startTime = "12:30", endTime = "14:30")
        )

        // Set calendar to Monday at 10:30 (Between breakfast and lunch)
        val cal = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
            set(Calendar.HOUR_OF_DAY, 10)
            set(Calendar.MINUTE, 30)
        }

        val state = MealTimeUtils.calculateActiveMealState(testMeals, cal)
        assertFalse(state.isCurrentlyActive)
        assertEquals(null, state.activeMeal)
        assertNotNull(state.nextMeal)
        assertEquals(MealType.LUNCH, state.nextMeal?.mealType)
    }

    @Test
    fun testActiveMealDetection_DayEnded_ShowsTomorrowMorning() {
        val testMeals = listOf(
            MealEntity(id = 1, dayOfWeek = 1, mealType = "DINNER", items = "Paneer", startTime = "20:00", endTime = "22:00"),
            MealEntity(id = 2, dayOfWeek = 2, mealType = "BREAKFAST", items = "Paratha", startTime = "07:30", endTime = "09:30")
        )

        // Set calendar to Monday 23:00 (Dinner ended)
        val cal = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 0)
        }

        val state = MealTimeUtils.calculateActiveMealState(testMeals, cal)
        assertFalse(state.isCurrentlyActive)
        assertEquals(null, state.activeMeal)
        assertNotNull(state.nextMeal)
        assertEquals(2, state.nextMeal?.dayOfWeek)
        assertEquals(MealType.BREAKFAST, state.nextMeal?.mealType)
    }
}

package com.example

import com.example.data.ai.TimetableAiService
import com.example.data.model.MealType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TimetableAiServiceTest {

    @Test
    fun testGetSmartDemoResult_ContainsAllDaysAndSlots() {
        val result = TimetableAiService.getSmartDemoResult("North Indian Hostel Mess")

        assertEquals("North Indian Hostel Mess", result.messName)
        assertTrue(result.isDemoOrFallback)
        assertEquals(28, result.meals.size) // 7 days * 4 meal types

        // Ensure every day 1..7 has all 4 meal slots
        for (day in 1..7) {
            val dayMeals = result.meals.filter { it.dayOfWeek == day }
            assertEquals(4, dayMeals.size)

            val breakfast = dayMeals.find { it.mealType == MealType.BREAKFAST.name }
            assertNotNull(breakfast)
            assertTrue(breakfast!!.items.isNotBlank())

            val lunch = dayMeals.find { it.mealType == MealType.LUNCH.name }
            assertNotNull(lunch)
            assertTrue(lunch!!.items.isNotBlank())

            val snacks = dayMeals.find { it.mealType == MealType.SNACKS.name }
            assertNotNull(snacks)
            assertTrue(snacks!!.items.isNotBlank())

            val dinner = dayMeals.find { it.mealType == MealType.DINNER.name }
            assertNotNull(dinner)
            assertTrue(dinner!!.items.isNotBlank())
        }

        // Check dishes extracted
        assertFalse(result.extractedDishes.isEmpty())
    }
}

package com.example

import com.example.data.ai.TimetableAiService
import com.example.data.model.MealType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
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

    @Test
    fun testParseGeminiResponse_ValidJson() {
        val geminiResponse = """
            {
              "candidates": [
                {
                  "content": {
                    "parts": [
                      {
                        "text": "{\n  \"messName\": \"Aryabhatta Hostel Mess\",\n  \"meals\": [\n    {\n      \"dayOfWeek\": 1,\n      \"mealType\": \"BREAKFAST\",\n      \"items\": \"Idli • Sambar • Coconut Chutney • Tea\",\n      \"startTime\": \"07:30\",\n      \"endTime\": \"09:00\"\n    },\n    {\n      \"dayOfWeek\": 1,\n      \"mealType\": \"LUNCH\",\n      \"items\": \"Rajma • Chawal • Roti • Raita\",\n      \"startTime\": \"12:30\",\n      \"endTime\": \"14:30\"\n    }\n  ],\n  \"extractedDishes\": [\n    {\n      \"name\": \"Idli\",\n      \"category\": \"Breakfast\",\n      \"iconEmoji\": \"🥟\"\n    }\n  ]\n}"
                      }
                    ]
                  }
                }
              ]
            }
        """.trimIndent()

        val parsed = TimetableAiService.parseGeminiResponse(geminiResponse)
        assertEquals("Aryabhatta Hostel Mess", parsed.messName)
        assertEquals(28, parsed.meals.size)
        val monBreakfast = parsed.meals.first { it.dayOfWeek == 1 && it.mealType == "BREAKFAST" }
        assertTrue(monBreakfast.items.contains("Idli"))
        assertEquals("07:30", monBreakfast.startTime)
        assertEquals("09:00", monBreakfast.endTime)
    }
}

package com.example.nd_mobpriet

import org.junit.Assert.*
import org.junit.Test

class DiaryLogicTest {
    @Test
    fun validPortionCalculatesCaloriesAndTrimsName() {
        val input = validateFoodInput("  Košė  ", "200", "80")
        assertTrue(input.isValid)
        assertEquals("Košė", input.name)
        assertEquals(160, input.calories)
    }

    @Test
    fun decimalCommaAndPointAreAcceptedAndCaloriesAreRounded() {
        assertEquals(44, validateFoodInput("Riešutai", "12,5", "350").calories)
        assertEquals(44, validateFoodInput("Riešutai", "12.5", "350.0").calories)
    }

    @Test
    fun emptyAndTooLongNamesAreRejected() {
        assertNotNull(validateFoodInput("   ", "200", "80").nameError)
        assertFalse(validateFoodInput("x".repeat(101), "200", "80").isValid)
    }

    @Test
    fun invalidPortionsDoNotProduceCalories() {
        listOf("", "abc", "0", "-20", "10001", "NaN", "Infinity", "1,2,3").forEach {
            val input = validateFoodInput("Maistas", it, "80")
            assertFalse("Porcija: $it", input.isValid)
            assertNotNull(input.gramsError)
            assertNull(input.calories)
        }
    }

    @Test
    fun invalidEnergyIsRejectedButZeroIsAllowed() {
        listOf("", "abc", "-1", "1001", "NaN", "Infinity").forEach {
            assertFalse(validateFoodInput("Maistas", "100", it).isValid)
        }
        val water = validateFoodInput("Vanduo", "250", "0")
        assertTrue(water.isValid)
        assertEquals(0, water.calories)
    }

    @Test
    fun totalsFollowEntriesAndRemoval() {
        val oats = DiaryEntry(1, "Košė", "🥣", "Pusryčiai", 200.0, 80.0)
        val banana = DiaryEntry(2, "Bananas", "🍌", "Užkandžiai", 120.0, 87.5)
        assertEquals(DiarySummary(265, 1735, 0, 265f / 2000), summarizeDiary(listOf(oats, banana), 2000))
        assertEquals(105, summarizeDiary(listOf(banana), 2000).consumed)
        assertEquals(DiarySummary(0, 2000, 0, 0f), summarizeDiary(emptyList(), 2000))
    }

    @Test
    fun goalChangeUpdatesRemainderAndOverTargetProgressIsClamped() {
        val entry = DiaryEntry(1, "Maistas", "🍽️", "Pietūs", 250.0, 600.0)
        assertEquals(500, summarizeDiary(listOf(entry), 2000).remaining)
        assertEquals(DiarySummary(1500, 0, 500, 1f), summarizeDiary(listOf(entry), 1000))
    }
}

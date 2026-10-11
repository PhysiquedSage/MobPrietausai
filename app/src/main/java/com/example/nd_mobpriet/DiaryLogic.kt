package com.example.nd_mobpriet

import java.io.Serializable
import kotlin.math.roundToInt

// Vienas vartotojo įvestas valgymas. Serializable leidžia Android atkurti jį pasukus ekraną.
data class DiaryEntry(
    val id: Int,
    val name: String,
    val emoji: String,
    val category: String,
    val grams: Double,
    val caloriesPer100g: Double
) : Serializable {
    val calories = calculateCalories(grams, caloriesPer100g)
}

fun calculateCalories(grams: Double, caloriesPer100g: Double): Int {
    val calories = grams * caloriesPer100g / 100
    return calories.roundToInt()
}

// Formos patikrinimo rezultatas: skaičiai ir klaidos prie atitinkamų laukų.
// null prie klaidos reiškia, kad laukas užpildytas teisingai.
data class FoodInputResult(
    val name: String,
    val grams: Double,
    val caloriesPer100g: Double,
    val nameError: String?,
    val gramsError: String?,
    val caloriesError: String?
) {
    val isValid = nameError == null && gramsError == null && caloriesError == null

    // Kalorijų peržiūra galima net prieš įvedant pavadinimą.
    val calories = if (gramsError == null && caloriesError == null) {
        calculateCalories(grams, caloriesPer100g)
    } else {
        null
    }
}

fun validateFoodInput(name: String, gramsText: String, caloriesText: String): FoodInputResult {
    // 1. Pašaliname tarpus ir bandome tekstą paversti skaičiais.
    val cleanName = name.trim()
    val grams = gramsText.trim().replace(',', '.').toDoubleOrNull()
    val calories = caloriesText.trim().replace(',', '.').toDoubleOrNull()

    // 2. Patikriname kiekvieną lauką atskirai.
    var nameError: String? = null
    if (cleanName.isEmpty()) {
        nameError = "Įvesk maisto pavadinimą."
    } else if (cleanName.length > 100) {
        nameError = "Pavadinimas gali būti iki 100 simbolių."
    }

    var gramsError: String? = null
    if (grams == null || !grams.isFinite()) {
        gramsError = "Įvesk skaičių, pvz. 200 arba 12,5."
    } else if (grams <= 0 || grams > 10000) {
        gramsError = "Porcija turi būti didesnė už 0 ir iki 10 000 g."
    }

    var caloriesError: String? = null
    if (calories == null || !calories.isFinite()) {
        caloriesError = "Įvesk skaičių, pvz. 80."
    } else if (calories < 0 || calories > 1000) {
        caloriesError = "Įvesk nuo 0 iki 1 000 kcal / 100 g."
    }

    // 3. Grąžiname rezultatą. Neteisinga įvestis pažymėta klaida ir nebus išsaugota.
    return FoodInputResult(
        name = cleanName,
        grams = grams ?: 0.0,
        caloriesPer100g = calories ?: 0.0,
        nameError = nameError,
        gramsError = gramsError,
        caloriesError = caloriesError
    )
}

data class DiarySummary(val consumed: Int, val remaining: Int, val excess: Int, val progress: Float)

fun summarizeDiary(entries: List<DiaryEntry>, goal: Int): DiarySummary {
    require(goal > 0)

    // Sudedame kiekvieno valgymo kalorijas.
    var consumed = 0
    for (entry in entries) {
        consumed += entry.calories
    }

    var remaining = 0
    var excess = 0
    if (consumed <= goal) {
        remaining = goal - consumed
    } else {
        excess = consumed - goal
    }

    // Juosta turi rodyti nuo 0 iki 100 %, net jei tikslas viršytas.
    val progress = (consumed.toFloat() / goal).coerceIn(0f, 1f)
    return DiarySummary(consumed, remaining, excess, progress)
}

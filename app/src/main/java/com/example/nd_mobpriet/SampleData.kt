package com.example.nd_mobpriet

// Pavyzdinis maisto katalogas. Dienoraštyje saugomi vartotojo pasirinkti įrašai.
data class Food(
    val id: Int,
    val name: String,
    val emoji: String,
    val category: String,
    val portion: String,
    val calories: Int,
    val grams: Int
)

val sampleFoods = listOf(
    Food(1, "Avižinė košė", "🥣", "Pusryčiai", "1 dubenėlis · 250 g", 320, 250),
    Food(2, "Vištiena su ryžiais", "🍚", "Pietūs", "1 lėkštė · 300 g", 410, 300),
    Food(3, "Graikiškas jogurtas", "🥛", "Užkandžiai", "1 indelis · 150 g", 145, 150),
    Food(4, "Bananas", "🍌", "Užkandžiai", "1 vienetas · 120 g", 105, 120),
    Food(5, "Daržovių salotos", "🥗", "Vakarienė", "1 dubenėlis · 200 g", 180, 200),
    Food(6, "Avokado skrebutis", "🥑", "Pusryčiai", "1 skrebutis · 100 g", 230, 100),
    Food(7, "Lašiša su daržovėmis", "🐟", "Vakarienė", "1 lėkštė · 250 g", 360, 250),
    Food(8, "Obuolys", "🍎", "Užkandžiai", "1 vienetas · 150 g", 80, 150)
)

val mealCategories = listOf("Visi", "Pusryčiai", "Pietūs", "Vakarienė", "Užkandžiai")

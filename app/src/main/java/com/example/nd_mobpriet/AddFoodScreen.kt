package com.example.nd_mobpriet

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import java.util.Locale

@Composable
fun AddFoodScreen(food: Food?, onSave: (DiaryEntry) -> Unit, onCancel: () -> Unit) {
    // Pasirinkus katalogo maistą užpildome formą. Paspaudus + ji būna tuščia.
    var initialCalories = ""
    if (food != null) {
        val caloriesPer100g = food.calories * 100.0 / food.grams
        initialCalories = String.format(Locale.US, "%.2f", caloriesPer100g)
    }

    // Laukuose laikomas tekstas, nes vartotojas gali įvesti ir netinkamą reikšmę.
    var name by rememberSaveable { mutableStateOf(food?.name ?: "") }
    var grams by rememberSaveable { mutableStateOf(food?.grams?.toString() ?: "") }
    var calories by rememberSaveable { mutableStateOf(initialCalories) }
    var category by rememberSaveable { mutableStateOf(food?.category ?: "Pusryčiai") }
    var submitted by rememberSaveable { mutableStateOf(false) }
    // Pakeitus lauką Compose vėl patikrina įvestį ir atnaujina kalorijų peržiūrą.
    val input = validateFoodInput(name, grams, calories)
    val focusManager = LocalFocusManager.current

    LazyColumn(
        modifier = Modifier.fillMaxSize().imePadding().testTag("foodForm"),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { Text("Ką šiandien valgei?", style = MaterialTheme.typography.headlineSmall) }
        if (food != null) item {
            Text("Katalogo reikšmės yra pavyzdinės. Gali jas pakeisti pagal produkto etiketę.",
                style = MaterialTheme.typography.bodyMedium)
        }
        item {
            OutlinedTextField(
                value = name, onValueChange = { name = it }, label = { Text("Maisto pavadinimas") },
                singleLine = true, isError = submitted && input.nameError != null,
                supportingText = {
                    if (submitted && input.nameError != null) Text(input.nameError)
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
        item {
            OutlinedTextField(
                value = grams, onValueChange = { grams = it }, label = { Text("Porcija (g)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true, isError = submitted && input.gramsError != null,
                supportingText = {
                    if (submitted && input.gramsError != null) Text(input.gramsError)
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
        item {
            OutlinedTextField(
                value = calories, onValueChange = { calories = it }, label = { Text("Kalorijos / 100 g") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true, isError = submitted && input.caloriesError != null,
                supportingText = {
                    if (submitted && input.caloriesError != null) Text(input.caloriesError)
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
        item {
            Text("Valgymas", style = MaterialTheme.typography.titleMedium)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(mealCategories.drop(1)) { meal ->
                    FilterChip(selected = category == meal, onClick = { category = meal }, label = { Text(meal) })
                }
            }
        }
        item {
            OutlinedCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Porcijos kalorijos", style = MaterialTheme.typography.titleMedium)
                    if (input.calories != null) {
                        Text("${input.calories} kcal")
                    } else {
                        Text("Įvesk tinkamą porciją ir kalorijas.")
                    }
                    Text("Porcija (g) × kcal / 100 g ÷ 100", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
        item {
            Button(onClick = {
                // Klaidas rodome tik pabandžius išsaugoti. Neteisingo įrašo nepridedame.
                submitted = true
                if (input.isValid) {
                    val entry = DiaryEntry(
                        id = 0, // Tikrą unikalų numerį priskirs CalorieApp.
                        name = input.name,
                        emoji = food?.emoji ?: "🍽️",
                        category = category,
                        grams = input.grams,
                        caloriesPer100g = input.caloriesPer100g
                    )
                    focusManager.clearFocus()
                    onSave(entry)
                }
            }, modifier = Modifier.fillMaxWidth()) { Text("Pridėti į dienoraštį") }
            TextButton(onClick = onCancel, modifier = Modifier.fillMaxWidth()) { Text("Atšaukti") }
        }
    }
}

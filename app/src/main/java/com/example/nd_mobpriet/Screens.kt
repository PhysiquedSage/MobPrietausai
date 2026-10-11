package com.example.nd_mobpriet

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import kotlin.math.roundToInt
import com.example.nd_mobpriet.ui.theme.ND_MobPrietTheme

@Composable
fun TodayScreen(
    entries: List<DiaryEntry>,
    goal: Int,
    summary: DiarySummary,
    onRemove: (Int) -> Unit,
    onGoalClick: () -> Unit
) {
    var category by rememberSaveable { mutableStateOf("Visi") }
    var deleteId by rememberSaveable { mutableStateOf<Int?>(null) }
    // Filtras keičia tik matomus įrašus. Suvestinė gauta iš viso dienoraščio.
    val foods = entries.filter { entry -> category == "Visi" || entry.category == category }
    val remainingText = if (summary.excess > 0) {
        "Viršyta ${summary.excess} kcal"
    } else {
        "Liko ${summary.remaining} kcal"
    }

    // Suvestinė ir valgių sąrašas slenka kartu.
    LazyColumn(
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Tavo dienos apžvalga", style = MaterialTheme.typography.headlineSmall)
            Text("${entries.size} maisto įrašai", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                        Box(Modifier.size(112.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(
                                progress = { summary.progress }, modifier = Modifier.fillMaxSize(), strokeWidth = 8.dp,
                                trackColor = MaterialTheme.colorScheme.surface
                            )
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("${summary.consumed}", style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold)
                                Text("kcal", style = MaterialTheme.typography.labelLarge)
                            }
                        }
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("Dienos tikslas", style = MaterialTheme.typography.labelLarge)
                            Text("$goal kcal", style = MaterialTheme.typography.titleLarge)
                            Text(remainingText, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                    OutlinedButton(onClick = onGoalClick, modifier = Modifier.fillMaxWidth()) {
                        Text("Peržiūrėti tikslą")
                    }
                }
            }
        }
        item { Text("Dienos valgiai", style = MaterialTheme.typography.titleLarge) }
        item { CategoryFilters(category, onSelect = { category = it }) }
        if (foods.isEmpty()) item {
            Text(if (entries.isEmpty()) "Dar nėra įrašų. Paspausk + ir pridėk pirmą valgį."
                else "Šiam valgymui įrašų nėra.", modifier = Modifier.padding(vertical = 16.dp))
        }
        items(foods, key = { it.id }) { food ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    FoodGraphic(food.emoji)
                    Column(Modifier.weight(1f)) {
                        Text(food.name, style = MaterialTheme.typography.titleMedium)
                        Text(food.category, style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("${food.grams} g", style = MaterialTheme.typography.bodySmall)
                    }
                    Text("${food.calories}\nkcal", style = MaterialTheme.typography.labelLarge)
                    IconButton(onClick = { deleteId = food.id }) {
                        Icon(Icons.Default.Delete, "Pašalinti ${food.name}")
                    }
                }
            }
        }
    }
    // Prieš pašalindami įrašą prašome patvirtinti pasirinkimą dialoge.
    val deletingEntry = entries.find { it.id == deleteId }
    if (deletingEntry != null) AlertDialog(
        onDismissRequest = { deleteId = null },
        title = { Text("Pašalinti įrašą?") },
        text = { Text("${deletingEntry.name} bus pašalintas iš dienoraščio.") },
        confirmButton = {
            TextButton(onClick = {
                onRemove(deletingEntry.id)
                deleteId = null
            }) { Text("Pašalinti") }
        },
        dismissButton = {
            TextButton(onClick = { deleteId = null }) { Text("Atšaukti") }
        }
    )
}

@Composable
fun FoodsScreen(onFoodClick: (Food) -> Unit) {
    var search by rememberSaveable { mutableStateOf("") }
    var category by rememberSaveable { mutableStateOf("Visi") }
    // Produktas rodomas, jeigu atitinka ir paiešką, ir pasirinktą kategoriją.
    val foods = sampleFoods.filter { food ->
        val matchesSearch = food.name.contains(search.trim(), ignoreCase = true)
        val matchesCategory = category == "Visi" || food.category == category
        matchesSearch && matchesCategory
    }

    Column(Modifier.fillMaxSize().padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Atrask maisto pasirinkimus", style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(top = 12.dp))
        OutlinedTextField(
            value = search, onValueChange = { search = it },
            label = { Text("Ieškoti maisto") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            singleLine = true, modifier = Modifier.fillMaxWidth()
        )
        CategoryFilters(category, onSelect = { category = it })
        Text("${foods.size} pasirinkimai · pavyzdiniai duomenys",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        if (foods.isEmpty()) Text("Nieko nerasta. Pabandyk kitą pavadinimą arba kategoriją.")

        // Adaptyvus tinklelis prisitaiko prie ekrano pločio.
        LazyVerticalGrid(
            columns = GridCells.Adaptive(140.dp), modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(bottom = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(foods, key = { it.id }) { food ->
                Card(onClick = { onFoodClick(food) }, modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        FoodGraphic(food.emoji)
                        Text(food.name, style = MaterialTheme.typography.titleMedium)
                        Text(food.category, style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("${food.calories} kcal", color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

@Composable
fun GoalScreen(
    goal: Int,
    summary: DiarySummary,
    onGoalChange: (Int) -> Unit,
    darkTheme: Boolean,
    onDarkThemeChange: (Boolean) -> Unit
) {
    var showPreview by rememberSaveable { mutableStateOf(true) }

    LazyColumn(contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            Text("Susikurk savo ritmą", style = MaterialTheme.typography.headlineSmall)
            Text("Pasirinktas tikslas taikomas dienos suvestinei",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        item {
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Dienos kalorijų tikslas", style = MaterialTheme.typography.titleSmall)
                    Text("$goal kcal", style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.primary)
                    Slider(value = goal.toFloat(), onValueChange = { onGoalChange(it.roundToInt()) },
                        valueRange = 1000f..3500f, steps = 24)
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("1 000", style = MaterialTheme.typography.labelSmall)
                        Text("3 500", style = MaterialTheme.typography.labelSmall)
                    }
                    Text("Tikslą gali keisti po 100 kcal.", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = showPreview, onCheckedChange = { showPreview = it })
                Text("Rodyti dienos progresą")
            }
        }
        if (showPreview) item {
            OutlinedCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Šiandien: ${summary.consumed} iš $goal kcal")
                    LinearProgressIndicator(progress = { summary.progress }, modifier = Modifier.fillMaxWidth())
                }
            }
        }
        item {
            HorizontalDivider()
            Row(Modifier.fillMaxWidth().padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Tamsi tema", style = MaterialTheme.typography.titleMedium)
                    Text("Keičia visos programėlės spalvas", style = MaterialTheme.typography.bodySmall)
                }
                Switch(checked = darkTheme, onCheckedChange = onDarkThemeChange)
            }
            OutlinedButton(
                onClick = {
                    onGoalChange(2000)
                    showPreview = true
                    onDarkThemeChange(false)
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Atkurti nustatymus") }
        }
    }
}

@Composable
fun FoodDetailScreen(food: Food, onAdd: () -> Unit, onBack: () -> Unit) {
    LazyColumn(contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
        item {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.fillMaxWidth().padding(28.dp), horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(food.emoji, fontSize = 64.sp)
                    Text(food.name, style = MaterialTheme.typography.headlineSmall)
                    Text(food.category, style = MaterialTheme.typography.labelLarge)
                }
            }
        }
        item {
            OutlinedCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Porcijos informacija", style = MaterialTheme.typography.titleMedium)
                    Text(food.portion)
                    HorizontalDivider()
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Energinė vertė")
                        Text("${food.calories} kcal", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
        item {
            Text("Katalogo reikšmės yra pavyzdinės. Pridėdamas maistą gali pakeisti porciją ir kalorijas pagal etiketę.",
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        item {
            Button(onClick = onAdd, modifier = Modifier.fillMaxWidth()) { Text("Pridėti porciją") }
            OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text("Grįžti į sąrašą") }
        }
    }
}

@Composable
private fun CategoryFilters(selected: String, onSelect: (String) -> Unit) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(mealCategories) { category ->
            FilterChip(selected = selected == category, onClick = { onSelect(category) }, label = { Text(category) })
        }
    }
}

@Composable
private fun FoodGraphic(emoji: String) {
    Surface(shape = MaterialTheme.shapes.medium, color = MaterialTheme.colorScheme.secondaryContainer) {
        Box(Modifier.size(52.dp), contentAlignment = Alignment.Center) { Text(emoji, fontSize = 28.sp) }
    }
}

@Preview(showBackground = true)
@Composable
private fun FoodsPreview() { ND_MobPrietTheme { FoodsScreen(onFoodClick = {}) } }

@Preview(showBackground = true)
@Composable
private fun GoalPreview() {
    ND_MobPrietTheme {
        GoalScreen(
            goal = 2000,
            summary = summarizeDiary(emptyList(), 2000),
            onGoalChange = {},
            darkTheme = false,
            onDarkThemeChange = {}
        )
    }
}

package com.example.nd_mobpriet

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.*
import com.example.nd_mobpriet.ui.theme.ND_MobPrietTheme
import kotlin.math.roundToInt

//anavigacijos ikoneles ir kokie puslapiai yra programeleje
enum class Screen(val title: String, val icon: ImageVector) {
    Diary("Dienoraštis", Icons.Default.Home),
    Add("Pridėti maistą", Icons.Default.Add),
    Settings("Nustatymai", Icons.Default.Settings)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
// ui funkcijos ir navigacijos paruosimas
fun CalorieApp(darkTheme: Boolean, onDarkThemeChange: (Boolean) -> Unit) {
    val navController = rememberNavController()
    val backStack by navController.currentBackStackEntryAsState()
    val screen = Screen.entries.find { it.name == backStack?.destination?.route } ?: Screen.Diary

    var entries by rememberSaveable { mutableStateOf(listOf<FoodEntry>()) } //maisto duomenys
    var nextId by rememberSaveable { mutableIntStateOf(1) } // ju ID greitam pasiekimui
    var goal by rememberSaveable { mutableIntStateOf(2000) } // pradinis kaloju tiksas
    var showProgress by rememberSaveable { mutableStateOf(true) } //ar rodoma juosta kaloriju trackinimo
    var menuOpen by rememberSaveable { mutableStateOf(false) } // ar atidarytas autoriaus pasirinkimo menu
    var showAuthor by rememberSaveable { mutableStateOf(false) } //ar rodomas autoriaus menu

    var total = 0 // pradine dienos kaloriju suma
    for (entry in entries) total += entry.calories //skaiciavimas kaloriju

    fun openPage(page: Screen) { // puslapiu perjungimas, bet palieka dienorasti kaip pagrindi gryzimo taska
        navController.navigate(page.name) {
            popUpTo(Screen.Diary.name)
            launchSingleTop = true // del puslpio dubliavimo
        }
    }

    Scaffold(
        topBar = {
            TopAppBar( //top baras dienorascio
                title = { Text(screen.title) },
                navigationIcon = {
                    if (screen != Screen.Diary) { //tikrinama ar rodoma gryztamoji rodykle del apie menu
                        IconButton(onClick = { navController.navigateUp() }) { //gryzta i buvusi puslapi
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Atgal") //pati ikonele
                        }
                    }
                },
                actions = {
                    Box { // apie mane ikonele
                        IconButton(onClick = { menuOpen = true }) {
                            Icon(Icons.Default.MoreVert, "Meniu")
                        }
                        DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                            DropdownMenuItem(
                                text = { Text("Autorius") },
                                onClick = { menuOpen = false; showAuthor = true }
                            )
                        }
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                for (page in Screen.entries) {
                    NavigationBarItem(
                        selected = page == screen,
                        onClick = { openPage(page) },
                        icon = { Icon(page.icon, contentDescription = null) },
                        label = { Text(page.title) }
                    )
                }
            }
        },
        floatingActionButton = {
            if (screen == Screen.Diary) {
                FloatingActionButton(onClick = { openPage(Screen.Add) }) {
                    Icon(Icons.Default.Add, "Pridėti maistą")
                }
            }
        }
    ) { padding ->
        // Telefone turinys užima ekrano plotį, dideliame ekrane lieka centre iki 600 dp.
        Box(Modifier.padding(padding).fillMaxSize(), contentAlignment = Alignment.TopCenter) {
            NavHost(
                navController = navController,
                startDestination = Screen.Diary.name,
                modifier = Modifier.widthIn(max = 600.dp).fillMaxSize() // boxas dienorascio tikslo ekrano
            ) {
                composable(Screen.Diary.name) {
                    DiaryScreen(entries, total, goal, showProgress,
                        onDelete = { id -> entries = entries.filter { it.id != id } }) // perduoda ID -> pereina i ta entry ir ji istrina

                }
                composable(Screen.Add.name) {
                    AddFoodScreen(
                        onAdd = { entry ->
                            entries = entries + entry.copy(id = nextId)
                            nextId += 1
                            openPage(Screen.Diary)
                        },
                        onCancel = { navController.navigateUp() }
                    )
                }
                composable(Screen.Settings.name) {
                    SettingsScreen(goal, { goal = it }, showProgress, { showProgress = it },
                        darkTheme, onDarkThemeChange)
                }
            }
        }
    }

    if (showAuthor) {
        AlertDialog(
            onDismissRequest = { showAuthor = false },
            title = { Text("Autorius") },
            text = { Text("Faustas Galeckis\nGrupė: MKDf-23s") },
            confirmButton = {
                TextButton(onClick = { showAuthor = false }) { Text("Uždaryti") }
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AppPreview() {
    ND_MobPrietTheme { CalorieApp(false, {}) }
}

@Composable
fun DiaryScreen(entries: List<FoodEntry>, total: Int, goal: Int, showProgress: Boolean, onDelete: (Int) -> Unit) {
    var filter by rememberSaveable { mutableStateOf("Visi") }
    val visibleEntries = entries.filter { filter == "Visi" || it.meal == filter }
    val remaining = goal - total

    LazyColumn(
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item {
            Card(Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Šiandien suvartota: $total kcal", style = MaterialTheme.typography.titleLarge)
                    Text("Dienos tikslas: $goal kcal")
                    if (remaining >= 0) Text("Liko $remaining kcal")
                    else Text("Tikslas viršytas ${-remaining} kcal")
                    if (showProgress) {
                        LinearProgressIndicator(
                            progress = { (total.toFloat() / goal).coerceIn(0f, 1f) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(listOf("Visi") + meals) { meal ->
                    FilterChip(selected = filter == meal, onClick = { filter = meal }, label = { Text(meal) })
                }
            }
        }
        if (visibleEntries.isEmpty()) item { Text("Įrašų nėra. Pridėk maistą paspaudęs +.") }
        items(visibleEntries, key = { it.id }) { entry ->
            OutlinedCard(Modifier.fillMaxWidth()) {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(entry.name, style = MaterialTheme.typography.titleMedium)
                        Text("${entry.meal} · ${entry.calories} kcal")
                    }
                    IconButton(onClick = { onDelete(entry.id) }) {
                        Icon(Icons.Default.Delete, "Ištrinti ${entry.name}")
                    }
                }
            }
        }
    }
}

@Composable
fun AddFoodScreen(onAdd: (FoodEntry) -> Unit, onCancel: () -> Unit) {
    var name by rememberSaveable { mutableStateOf("") }
    var calorieText by rememberSaveable { mutableStateOf("") }
    var meal by rememberSaveable { mutableStateOf("Pusryčiai") }
    var nameError by rememberSaveable { mutableStateOf("") }
    var calorieError by rememberSaveable { mutableStateOf("") }

    // Slenkama forma lieka pasiekiama ir mažame ekrane, ir atidarius klaviatūrą.
    Column(
        Modifier.fillMaxSize().imePadding().verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedTextField(
            value = name, onValueChange = { name = it; nameError = "" },
            label = { Text("Maisto pavadinimas") }, singleLine = true,
            isError = nameError.isNotEmpty(),
            modifier = Modifier.fillMaxWidth()
        )
        if (nameError.isNotEmpty()) Text(nameError, color = MaterialTheme.colorScheme.error)
        OutlinedTextField(
            value = calorieText, onValueChange = { calorieText = it; calorieError = "" },
            label = { Text("Porcijos kalorijos (kcal)") }, singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            isError = calorieError.isNotEmpty(),
            modifier = Modifier.fillMaxWidth()
        )
        if (calorieError.isNotEmpty()) Text(calorieError, color = MaterialTheme.colorScheme.error)
        Text("Valgymas", style = MaterialTheme.typography.titleMedium)
        // FlowRow perkelia pasirinkimus į kitą eilę, kai ekrane trūksta pločio.
        FlowRow {
            for (option in meals) {
                Row(Modifier.width(150.dp), verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = meal == option, onClick = { meal = option })
                    Text(option)
                }
            }
        }
        Button(
            onClick = {
                // Tekstą paverčiame skaičiumi ir patikriname abu laukus.
                val calories = calorieText.trim().toIntOrNull()
                nameError = if (name.trim().isEmpty() || name.trim().length > 100)
                    "Įvesk pavadinimą nuo 1 iki 100 simbolių." else ""
                calorieError = if (calories == null || calories !in 0..10000)
                    "Įvesk sveiką skaičių nuo 0 iki 10 000." else ""

                if (nameError.isEmpty() && calorieError.isEmpty() && calories != null) {
                    onAdd(FoodEntry(name.trim(), calories, meal))
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) { Text("Pridėti") }
        OutlinedButton(onClick = { name = ""; calorieText = ""; meal = "Pusryčiai"; nameError = ""; calorieError = "" },
            modifier = Modifier.fillMaxWidth()) { Text("Išvalyti formą") }
        TextButton(onClick = onCancel, modifier = Modifier.fillMaxWidth()) { Text("Atšaukti") }
    }
}

@Composable
fun SettingsScreen(goal: Int, onGoalChange: (Int) -> Unit, showProgress: Boolean,
    onProgressChange: (Boolean) -> Unit, darkTheme: Boolean, onDarkThemeChange: (Boolean) -> Unit) {
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Dienos tikslas: $goal kcal", style = MaterialTheme.typography.titleLarge)
        Slider(value = goal.toFloat(), onValueChange = { onGoalChange(it.roundToInt()) },
            valueRange = 1000f..3500f, steps = 24)
        HorizontalDivider()
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = showProgress, onCheckedChange = onProgressChange)
            Text("Rodyti dienos progreso juostą", modifier = Modifier.weight(1f))
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Tamsi tema", modifier = Modifier.weight(1f))
            Switch(checked = darkTheme, onCheckedChange = onDarkThemeChange)
        }
    }
}

@Preview(showBackground = true, name = "Pridėti maistą")
@Composable
fun AddFoodPreview() {
    ND_MobPrietTheme {
        AddFoodScreen(
            onAdd = {},
            onCancel = {}
        )
    }
}

@Preview(showBackground = true, name = "Nustatymai")
@Composable
fun SettingsPreview() {
    ND_MobPrietTheme {
        SettingsScreen(
            goal = 2000,
            onGoalChange = {},
            showProgress = true,
            onProgressChange = {},
            darkTheme = false,
            onDarkThemeChange = {}
        )
    }
}

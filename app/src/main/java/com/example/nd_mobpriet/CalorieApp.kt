package com.example.nd_mobpriet

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.*
import com.example.nd_mobpriet.ui.theme.ND_MobPrietTheme

// Kiekvienas ekranas turi pavadinimą ir ikoną. name naudojamas kaip navigacijos maršrutas.
enum class AppScreen(val title: String, val icon: ImageVector) {
    Today("Šiandien", Icons.Default.Home),
    Foods("Maistas", Icons.AutoMirrored.Filled.List),
    Goal("Tikslas", Icons.Default.Settings),
    FoodDetail("Maisto informacija", Icons.Default.Info),
    AddFood("Pridėti maistą", Icons.Default.Add)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalorieApp(darkTheme: Boolean, onDarkThemeChange: (Boolean) -> Unit) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val currentScreen = AppScreen.entries.find { it.name == currentRoute } ?: AppScreen.Today
    val mainScreens = listOf(AppScreen.Today, AppScreen.Foods, AppScreen.Goal)
    val isMainScreen = currentScreen in mainScreens

    // Bendra būsena: visi ekranai naudojasi tais pačiais įrašais ir tikslu.
    // rememberSaveable išlaiko reikšmes po ekrano pasukimo.
    var entries by rememberSaveable { mutableStateOf(listOf<DiaryEntry>()) }
    var goal by rememberSaveable { mutableIntStateOf(2000) }
    var nextEntryId by rememberSaveable { mutableIntStateOf(1) }
    var selectedFoodId by rememberSaveable { mutableIntStateOf(0) }
    var showAbout by rememberSaveable { mutableStateOf(false) }

    val selectedFood = sampleFoods.find { it.id == selectedFoodId }
    val summary = summarizeDiary(entries, goal)

    fun openTab(screen: AppScreen) {
        navController.navigate(screen.name) {
            // Grįžtame iki pagrindinio ekrano, išsaugodami kitų puslapių filtrus.
            popUpTo(AppScreen.Today.name) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }

    fun addEntry(entry: DiaryEntry) {
        // Priskiriame unikalų numerį ir papildome sąrašą nauju įrašu.
        entries = entries + entry.copy(id = nextEntryId)
        nextEntryId += 1

        // Po išsaugojimo atidarome dienoraštį ir uždarome formą.
        navController.navigate(AppScreen.Today.name) {
            popUpTo(AppScreen.Today.name)
            launchSingleTop = true
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(currentScreen.title) },
                navigationIcon = {
                    if (!isMainScreen) {
                        IconButton(onClick = { navController.navigateUp() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Grįžti atgal")
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { showAbout = true }) {
                        Icon(Icons.Default.Info, "Apie programėlę")
                    }
                }
            )
        },
        bottomBar = {
            if (isMainScreen) {
                NavigationBar {
                    for (screen in mainScreens) {
                        NavigationBarItem(
                            selected = currentScreen == screen,
                            onClick = { openTab(screen) },
                            icon = { Icon(screen.icon, contentDescription = null) },
                            label = { Text(screen.title) }
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            if (currentScreen == AppScreen.Today) {
                FloatingActionButton(onClick = {
                    selectedFoodId = 0 // Naujas maistas, todėl forma bus tuščia.
                    navController.navigate(AppScreen.AddFood.name)
                }) {
                    Icon(Icons.Default.Add, "Pridėti maistą")
                }
            }
        }
    ) { padding ->
        // Vienas composable blokas aprašo vieną ekraną.
        NavHost(
            navController = navController,
            startDestination = AppScreen.Today.name,
            modifier = Modifier.padding(padding).fillMaxSize()
        ) {
            composable(AppScreen.Today.name) {
                TodayScreen(
                    entries = entries,
                    goal = goal,
                    summary = summary,
                    onRemove = { id -> entries = entries.filter { it.id != id } },
                    onGoalClick = { openTab(AppScreen.Goal) }
                )
            }
            composable(AppScreen.Foods.name) {
                FoodsScreen(onFoodClick = { food ->
                    selectedFoodId = food.id
                    navController.navigate(AppScreen.FoodDetail.name)
                })
            }
            composable(AppScreen.Goal.name) {
                GoalScreen(
                    goal = goal,
                    summary = summary,
                    onGoalChange = { goal = it },
                    darkTheme = darkTheme,
                    onDarkThemeChange = onDarkThemeChange
                )
            }
            composable(AppScreen.FoodDetail.name) {
                if (selectedFood != null) {
                    FoodDetailScreen(
                        food = selectedFood,
                        onAdd = { navController.navigate(AppScreen.AddFood.name) },
                        onBack = { navController.navigateUp() }
                    )
                }
            }
            composable(AppScreen.AddFood.name) {
                AddFoodScreen(
                    food = selectedFood,
                    onSave = { entry -> addEntry(entry) },
                    onCancel = { navController.navigateUp() }
                )
            }
        }
    }

    if (showAbout) {
        AlertDialog(
            onDismissRequest = { showAbout = false },
            title = { Text("Kalorijų dienoraštis") },
            text = { Text("Pridėk valgymus, skaičiuok porcijų kalorijas ir stebėk dienos tikslą. Maisto katalogo reikšmės yra pavyzdinės.\n\nFaustas Galeckis · MKDf-23s") },
            confirmButton = {
                TextButton(onClick = { showAbout = false }) { Text("Supratau") }
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AppPreview() {
    ND_MobPrietTheme { CalorieApp(darkTheme = false, onDarkThemeChange = {}) }
}

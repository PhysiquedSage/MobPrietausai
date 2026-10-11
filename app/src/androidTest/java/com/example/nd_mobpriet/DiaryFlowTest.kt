package com.example.nd_mobpriet

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.semantics.SemanticsActions
import org.junit.Rule
import org.junit.Test

class DiaryFlowTest {
    @get:Rule
    val compose = createAndroidComposeRule<MainActivity>()

    @Test
    fun invalidInputIsRejectedThenValidEntryCanBeAddedAndDeleted() {
        compose.onNodeWithContentDescription("Pridėti maistą").performClick()
        compose.onNodeWithText("Pridėti į dienoraštį").performScrollTo().performClick()
        compose.onNodeWithText("Įvesk maisto pavadinimą.").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Maisto pavadinimas").performTextInput("Košė")
        compose.onNodeWithText("Porcija (g)").performScrollTo().performTextInput("200")
        compose.onNodeWithText("Kalorijos / 100 g").performScrollTo().performTextInput("80")
        // LazyColumn nesukuria toli už ekrano esančių elementų, todėl slenkame pačiu sąrašu.
        compose.onNodeWithTag("foodForm").performScrollToNode(hasText("Pridėti į dienoraštį"))
        compose.onNodeWithText("Pridėti į dienoraštį").performScrollTo().performClick()
        compose.onNodeWithText("160").assertIsDisplayed()
        compose.onNodeWithText("Liko 1840 kcal").assertIsDisplayed()
        compose.onNodeWithContentDescription("Pašalinti Košė").performScrollTo().performClick()
        // Atšaukus trynimą įrašas turi likti.
        compose.onNodeWithText("Atšaukti").performClick()
        compose.onNodeWithContentDescription("Pašalinti Košė").assertIsDisplayed().performClick()
        compose.onNodeWithText("Pašalinti", substring = false).performClick()
        compose.onNodeWithText("0").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Liko 2000 kcal").assertIsDisplayed()
    }

    @Test
    fun catalogPrefillsPortionAndEntrySurvivesActivityRecreation() {
        compose.onNodeWithText("Maistas", substring = false).performClick()
        compose.onNodeWithText("Ieškoti maisto").performTextInput("bananas")
        compose.onNodeWithText("Bananas", substring = false).performClick()
        compose.onNodeWithText("Pridėti porciją").performScrollTo().performClick()
        compose.onNodeWithText("Maisto pavadinimas").assertTextContains("Bananas")
        compose.onNodeWithText("Pridėti į dienoraštį").performScrollTo().performClick()
        compose.onNodeWithText("105").assertIsDisplayed()
        compose.onNodeWithText("Peržiūrėti tikslą").performClick()
        compose.onNode(SemanticsMatcher.keyIsDefined(SemanticsActions.SetProgress))
            .performSemanticsAction(SemanticsActions.SetProgress) { it(1000f) }
        compose.onNodeWithText("Šiandien", substring = false).performClick()
        compose.onNodeWithText("Liko 895 kcal").assertIsDisplayed()
        compose.activityRule.scenario.recreate()
        compose.onNodeWithText("105").assertIsDisplayed()
        compose.onNodeWithText("Liko 895 kcal").assertIsDisplayed()
        compose.onNodeWithContentDescription("Pašalinti Bananas").performScrollTo().assertIsDisplayed()
    }
}

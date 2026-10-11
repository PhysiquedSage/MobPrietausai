# Kalorijų dienoraštis

Faustas Galeckis, MKDf-23s.

Paprasta Jetpack Compose programėlė: vartotojas įveda maistą, pasirenka valgymą, nurodo gramus ir kalorijas / 100 g. Programėlė tikrina reikšmes, apskaičiuoja porcijos kalorijas ir atnaujina dienos suvestinę.

## Paleidimas

Android Studio: **Sync Project with Gradle Files**, tada **Run app** su emuliatoriumi arba telefonu.

## Penki ekranai

- **Šiandien** — vartotojo įrašai slenkamame sąraše, bendra kalorijų suma, likutis arba viršytas tikslas. Filtrai keičia sąrašą, bet dienos suma visada skaičiuojama iš visų įrašų. Įrašo trynimas patvirtinamas dialoge.
- **Maistas** — pavyzdinis aštuonių produktų katalogas su paieška, kategorijomis ir slenkamu tinkleliu.
- **Maisto informacija** — katalogo produkto porcija ir kalorijos. Mygtukas „Pridėti porciją“ atidaro iš anksto užpildytą formą, kurios reikšmes galima keisti.
- **Pridėti maistą** — atidaromas paspaudus „+“ arba iš katalogo. Įvedamas pavadinimas, gramai, kcal / 100 g ir valgymas. Teisingas įrašas pridedamas į dienoraštį; neteisingas lieka formoje su klaidų pranešimais. „Atšaukti“ nieko neprideda.
- **Tikslas** — slankiklis keičia bendrą dienos tikslą po 100 kcal (1 000–3 500). Checkbox paslepia arba parodo tikrą dienos progresą, jungiklis pakeičia temą. Atkūrimo mygtukas grąžina nustatymus; maisto įrašai lieka.

## Įvesties apdorojimas ir skaičiavimai

- Pašalinami pavadinimo tarpai pradžioje ir pabaigoje. Pavadinimas privalomas, iki 100 simbolių.
- Gramai turi būti skaičius, daugiau už 0 ir iki 10 000 g.
- Kcal / 100 g turi būti skaičius nuo 0 iki 1 000. Nulinė energinė vertė leidžiama.
- Priimamas kablelis arba taškas: `12,5` ir `12.5`. Tekstas, `NaN` ir begalybė atmetami.
- **Porcijos kcal = gramai × kcal / 100 g ÷ 100**, rezultatas suapvalinamas iki sveiko skaičiaus.
- Dienos suma = visų pridėtų porcijų kalorijų suma. Pridėjus ar pašalinus įrašą ji perskaičiuojama.
- Likutis = tikslas − suma. Viršijus tikslą rodomas viršijimas, o progreso juosta neviršija 100 %.

Visos maisto įvedimo, skaičiavimo, filtravimo, pridėjimo, trynimo ir tikslo keitimo operacijos susijusios su kalorijų dienoraščio paskirtimi.

## Kodo žemėlapis

- `MainActivity.kt` — paleidimas ir temos būsena.
- `CalorieApp.kt` — visi penki navigacijos ekranai, bendri dienoraščio įrašai, tikslas, pridėjimas ir trynimas. Pasirinktas katalogo maistas nustatomas pagal `selectedFoodId`.
- `AddFoodScreen.kt` — forma, įvesties klaidų rodymas ir kalorijų peržiūra.
- `DiaryLogic.kt` — `DiaryEntry`, įvesties tikrinimas, porcijų kalorijų skaičiavimas ir dienos suvestinė. Logika atskirta nuo UI, kad būtų paprasta testuoti.
- `Screens.kt` — dienoraštis, katalogas, tikslas ir maisto informacija.
- `SampleData.kt` — pavyzdinis katalogas. Jo energinės vertės skirtos demonstracijai.
- `ui/theme/` — spalvos, formos ir teksto stiliai.

## Kaip paaiškinti kodo veikimą

Skaityk tokia tvarka: **MainActivity → CalorieApp → AddFoodScreen → DiaryLogic → Screens**.

1. `MainActivity` paleidžia programėlę ir pritaiko spalvų temą.
2. `CalorieApp` saugo bendrą būseną: įrašų sąrašą `entries`, dienos tikslą `goal` ir pasirinktą katalogo produktą `selectedFoodId`. `AppScreen` išvardija penkis ekranus, o `NavHost` nurodo, ką rodyti kiekviename iš jų.
3. `AddFoodScreen` lauko tekstą saugo kintamuosiuose `name`, `grams` ir `calories`. Pakeitus tekstą, `validateFoodInput` vėl patikrina reikšmes.
4. `validateFoodInput` pašalina tarpus, pakeičia kablelį tašku ir bando tekstą paversti skaičiumi. Tuomet paprastais `if / else if` patikrina pavadinimą, gramus ir kalorijas. Klaida grąžinama prie konkretaus lauko.
5. Paspaudus „Pridėti į dienoraštį“, tikrinama `input.isValid`. Jeigu yra klaidų, jos parodomos. Jeigu klaidų nėra, sukuriamas vienas `DiaryEntry` ir perduodamas į `CalorieApp` funkciją `addEntry`.
6. `addEntry` suteikia įrašui unikalų numerį, papildo sąrašą ir atidaro dienoraštį. `summarizeDiary` ciklu sudeda visų įrašų kalorijas, apskaičiuoja likutį bei progresą. `Screens.kt` parodo šias reikšmes.
7. Trynimas pašalina įrašą pagal numerį. Tikslo slankiklis pakeičia `goal`. Abiem atvejais Compose perskaičiuoja suvestinę ir atnaujina ekraną.

Pavyzdys: įvedus **200 g** ir **80 kcal / 100 g**, `calculateCalories` apskaičiuoja **200 × 80 ÷ 100 = 160 kcal**. Jei tikslas 2 000 kcal, likutis yra **2 000 − 160 = 1 840 kcal**.

Keli naudojami Kotlin ir Compose dalykai:

- `data class` aprašo vieną duomenų įrašą; `copy(id = ...)` sukuria jo kopiją su priskirtu numeriu.
- `rememberSaveable` išlaiko UI būseną pasukus ekraną. `DiaryEntry` turi `Serializable`, todėl Android gali išsaugoti ir atkurti įrašų sąrašą be mūsų rankinio laukų skaidymo.
- `filter` palieka tik sąlygą atitinkančius įrašus, `find` suranda vieną pasirinktą produktą.
- `toDoubleOrNull()` grąžina skaičių arba `null`, jei tekstas netinkamas. `isFinite()` atmeta begalybę ir `NaN`.
- `onClick` reaguoja į paspaudimą, `onValueChange` — į lauko reikšmės pakeitimą. `onSave` perduoda paruoštą įrašą iš formos į pagrindinį ekraną.

## Kaip patikrinti reikalavimą

1. Dienoraštis pradžioje tuščias: 0 kcal, tikslas 2 000 kcal.
2. Paspausk „+“, tada „Pridėti į dienoraštį“ palikęs tuščius laukus: rodoma klaida, įrašas nepridedamas.
3. Įvesk „Košė“, 200 g ir 80 kcal / 100 g. Peržiūra rodo **160 kcal**.
4. Išsaugok: dienos suma **160 kcal**, likutis **1 840 kcal**.
5. Pakeisk tikslą į 1 000 kcal: likutis **840 kcal**.
6. Pridėk antrą įrašą, išbandyk filtrus. Dienos suma turi apimti abu įrašus nepriklausomai nuo filtro.
7. Atšauk trynimą — įrašas lieka. Patvirtink trynimą — suma sumažėja.
8. „Maistas“ rask bananą, atidaryk jį, pasirink „Pridėti porciją“. Formoje galima pakeisti numatytą porciją.
9. Išbandyk neigiamus skaičius, netinkamą tekstą, 0 g, 0 kcal / 100 g bei porciją su kableliu.
10. Pasuk ekraną: įrašai, tikslas ir neužbaigta forma turi išlikti.

## Šios versijos ribos

Duomenys laikomi `rememberSaveable` būsenoje. Tai nėra nuolatinė duomenų bazė: iš naujo pradėjus programėlės sesiją įrašai gali būti prarasti. Dar nėra dienų istorijos ar automatinio dienoraščio pakeitimo vidurnaktį. Katalogo reikšmes galima pataisyti pagal tikrą produkto etiketę. 2 000 kcal yra demonstracinis pradinis tikslas.

## Testai

- `DiaryLogicTest` tikrina porcijų skaičiavimą, kablelį ir tašką, apvalinimą, neteisingą įvestį, nulines kalorijas, dienos sumą, trynimą ir tikslo viršijimą.
- `DiaryFlowTest` emuliatoriuje tikrina formos klaidas, pridėjimą, trynimo atšaukimą ir patvirtinimą, katalogo formą ir būsenos atkūrimą.

## Pamokos

- [Scrollable list](https://developer.android.com/codelabs/basic-android-kotlin-compose-training-add-scrollable-list)
- [Material theming](https://developer.android.com/codelabs/basic-android-kotlin-compose-material-theming)
- [Navigation Compose](https://developer.android.com/codelabs/basic-android-kotlin-compose-navigation)
- [Material komponentai](https://developer.android.com/develop/ui/compose/components)

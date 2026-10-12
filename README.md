# Kalorijų dienoraštis — paprasta versija

Autorius: **Faustas Galeckis, MKDf-23s**.

Programėlėje yra trys puslapiai. Vartotojas įveda maisto pavadinimą ir porcijos kalorijas, pasirenka valgymą, o programėlė sudeda įrašų kalorijas ir parodo likutį iki dienos tikslo.

## Kas pašalinta, kad būtų paprasčiau

- Maisto katalogas ir atskiras produkto informacijos ekranas.
- Gramų bei kcal / 100 g įvedimas ir porcijos kalorijų formulė. Dabar įvedamos jau žinomos visos porcijos kalorijos.
- Atskiros tikrinimo rezultatų ir suvestinės klasės, pasirinktų katalogo produktų būsena.
- Automatiniai testai, jų bibliotekos ir testų paleidimo nustatymai.

Pagrindinį kodą sudaro `MainActivity.kt`, `Screens.kt` ir `FoodEntry.kt`. Spalvos, teksto stiliai bei formos lieka `ui/theme` aplanke.

## 1. MainActivity.kt — paleidimas

`onCreate` paleidžia ekraną. `setContent` nurodo, kad UI kuriamas su Compose. `ND_MobPrietTheme` pritaiko spalvas ir formas, o `CalorieApp` rodo pačią programėlę.

Kintamasis `darkTheme` saugo, ar pasirinkta tamsi tema. Nustatymų jungiklis pakeičia šią reikšmę ir Compose atnaujina spalvas. `SideEffect` po temos pakeitimo pritaiko ir telefono būsenos juostos ikonų kontrastą, kad laikrodis būtų įskaitomas.

## 2. FoodEntry.kt — vieno įrašo duomenys

`FoodEntry` yra `data class` su keturiais laukais:

- `name` — maisto pavadinimas.
- `calories` — visos įvestos porcijos kalorijos.
- `meal` — pusryčiai, pietūs, vakarienė arba užkandžiai.
- `id` — unikalus įrašo numeris, naudojamas trynimui ir sąrašui.

`Serializable` leidžia Android išlaikyti šiuos duomenis pasukus ekraną. Tai nėra duomenų bazė.

## 3. Screens.kt: CalorieApp — bendra būsena ir navigacija

`Screen` išvardija tik tris ekranus: `Diary`, `Add` ir `Settings`.

Pagrindiniai kintamieji:

- `entries` — visų pridėtų maisto įrašų sąrašas.
- `nextId` — numeris, kurį gaus kitas naujas įrašas.
- `goal` — dienos kalorijų tikslas; pradinė reikšmė 2 000.
- `showProgress` — ar dienoraštyje rodyti progreso juostą.
- `menuOpen` ir `showAuthor` — ar atidarytas meniu ir autoriaus dialogas.

Jie saugomi su `rememberSaveable`, todėl reikšmės gali būti atkurtos pasukus ekraną. Kai vartotojas pakeičia būseną, Compose iš naujo parodo susijusią UI dalį.

Bendra kalorijų suma apskaičiuojama paprastu ciklu:

```kotlin
var total = 0
for (entry in entries) total += entry.calories
```

`Scaffold` apjungia viršutinę juostą, apatinę navigaciją, „+“ mygtuką ir puslapių turinį. `NavHost` kiekvienam maršrutui priskiria vieną ekraną. `openPage` atidaro puslapį, o `navigateUp` grąžina į dienoraštį. Android „Atgal“ taip pat veikia per navigacijos valdiklį.

Pridėjimas: įrašui suteikiamas `nextId`, jis pridedamas į `entries`, skaitiklis padidinamas ir atidaromas dienoraštis. Trynimas: `filter` palieka tik įrašus, kurių numeris nesutampa su trinamu numeriu.

Viršuje paspaudus trijų taškų mygtuką atsidaro `DropdownMenu`. Pasirinkus **„Autorius“**, `showAuthor` pakeičiamas į `true` ir atsidaro `AlertDialog` su vardu, pavarde bei grupe.

## 4. Screens.kt — trys puslapiai

### Dienoraštis

`DiaryScreen` rodo bendrą kalorijų sumą, tikslą ir likutį. Likutis apskaičiuojamas taip:

```kotlin
val remaining = goal - total
```

Jeigu likutis neigiamas, rodoma, kiek tikslas viršytas. Progreso juosta rodo `total / goal`, o `coerceIn(0f, 1f)` apriboja ją nuo 0 iki 100 %.

`LazyColumn` leidžia slinkti įrašų sąrašą. `LazyRow` leidžia slinkti kategorijų filtrus į šoną. Pasirinkus `FilterChip`, sąraše lieka tik atitinkamo valgymo įrašai. Bendra dienos suma visada skaičiuojama iš visų įrašų.

Prie įrašo esantis šiukšliadėžės mygtukas jį ištrina ir suma perskaičiuojama.

### Pridėti maistą

`AddFoodScreen` turi tik du įvedimo laukus ir valgymo pasirinkimą:

1. Pavadinimas: negali būti tuščias arba ilgesnis nei 100 simbolių. Tarpai pradžioje ir pabaigoje pašalinami su `trim()`.
2. Kalorijos: tekstas paverčiamas sveiku skaičiumi su `toIntOrNull()`. Priimamos reikšmės nuo 0 iki 10 000. Raidės, neigiami skaičiai, trupmenos ir per didelės reikšmės atmetamos.
3. `RadioButton` parenka, kuriam valgymui priklauso įrašas. `FlowRow` automatiškai perkelia pasirinkimus į naują eilę, jeigu jie netelpa pagal ekrano plotį.

Paspaudus **„Pridėti“**, tikrinami abu laukai. Jeigu yra klaida, ji parodoma po atitinkamu lauku ir įrašas nepridedamas. Jei abu laukai teisingi, sukuriamas `FoodEntry` ir per `onAdd` perduodamas į `CalorieApp`.

**„Išvalyti formą“** ištuština laukus ir grąžina „Pusryčiai“. **„Atšaukti“** grąžina į dienoraštį nepridėdamas įrašo.

### Nustatymai

- `Slider` keičia dienos tikslą nuo 1 000 iki 3 500 kcal po 100 kcal.
- `Checkbox` įjungia arba išjungia dienoraščio progreso juostą.
- `Switch` įjungia arba išjungia tamsią temą.

Šie valdikliai keičia tikrą bendrą programėlės būseną.

## Kaip atitinka reikalavimus

| Reikalavimas | Kaip įgyvendintas |
| --- | --- |
| Vartotojo įvesties apdorojimas | Pavadinimo ir kalorijų tikrinimas, teksto pavertimas skaičiumi, klaidų rodymas. |
| Skaičiavimai | Visų įrašų kalorijų suma, likutis, viršijimas ir dienos progresas. |
| Reakcija į veiksmus | Pridėjimas, trynimas, filtravimas, tikslo keitimas, formos išvalymas. |
| Keli puslapiai ir pilna navigacija | Trys puslapiai su apatine navigacija, grįžimo rodykle ir Android „Atgal“. |
| Vardas, pavardė ir grupė | Autoriaus dialogas: Faustas Galeckis, MKDf-23s. |
| Top App Bar su „Autorius“ meniu | Trijų taškų mygtukas → meniu „Autorius“ → dialogas. |
| Kintami ekrano dydžiai | Elementai užima laisvą plotį, platus turinys centruojamas iki 600 dp, pasirinkimai persikelia į naują eilę, ekranai slenkami, forma prisitaiko prie klaviatūros. |
| Estetika ir patogumas | Vieninga Material tema, žalios spalvos, apvalintos kortelės, nuoseklūs tarpai ir aiškios laukų etiketės. |

## Grafiniai elementai ir jų paskirtis

Naudojami [Material komponentai](https://developer.android.com/develop/ui/compose/components).

| Elementas | Dalyvavimas programos veikime |
| --- | --- |
| `OutlinedTextField` | Maisto pavadinimo ir kalorijų įvedimas, klaidų rodymas. |
| `Button` | Tikrina formą ir prideda įrašą. |
| `OutlinedButton` | Išvalo formą. |
| `TextButton` | Atšaukia įvedimą arba uždaro autoriaus dialogą. |
| `IconButton` ir `Icon` | Atidaro meniu, grąžina atgal, ištrina pasirinktą įrašą. |
| `FloatingActionButton` | Atidaro maisto įvedimo puslapį. |
| `RadioButton` | Parenka įrašo valgymą. |
| `FilterChip` | Filtruoja dienoraščio įrašus. |
| `Slider` | Keičia kalorijų tikslą. |
| `Checkbox` | Valdo progreso juostos matomumą. |
| `Switch` | Pakeičia temą. |
| `LazyColumn` ir `LazyRow` | Rodo bei slenka įrašus ir filtrus. |
| `Card` ir `OutlinedCard` | Rodo kintančią dienos suvestinę ir vartotojo įrašus. |
| `LinearProgressIndicator` | Rodo apskaičiuotą dienos progresą. |
| `TopAppBar` | Rodo dabartinio puslapio pavadinimą ir meniu. |
| `DropdownMenu` ir `DropdownMenuItem` | Leidžia pasirinkti „Autorius“. |
| `AlertDialog` | Parodo autoriaus vardą, pavardę ir grupę. |
| `NavigationBar` ir `NavigationBarItem` | Perjungia tris puslapius ir pažymi pasirinktą. |
| `Text` | Rodo įrašus, sumas, klaidas, etiketes ir autorių. |
| `HorizontalDivider` | Vizualiai atskiria nustatymus. |

## Paprastas demonstravimas dėstytojui

1. „Pridėti maistą“ palik laukus tuščius ir spausk „Pridėti“: turi pasirodyti klaidos.
2. Įvesk „Košė“, 300 kcal, pasirink „Pusryčiai“. Pridėjus suma 300 kcal, likutis 1 700 kcal.
3. Pridėk „Sriuba“, 250 kcal, pasirink „Pietūs“. Suma 550 kcal, likutis 1 450 kcal.
4. Pasirink filtrą „Pietūs“: rodomas tik „Sriuba“, bet visos dienos suma lieka 550 kcal.
5. Nustatymuose sumažink tikslą iki 1 000 kcal. Dienoraščio likutis tampa 450 kcal.
6. Ištrink „Sriuba“: suma tampa 300 kcal, likutis 700 kcal.
7. Įjunk ir išjunk progreso juostą, pakeisk temą.
8. Meniu pasirink „Autorius“ ir parodyk vardą, pavardę bei grupę.
9. Pasuk ekraną arba paleisk didesniame emuliatoriuje: turinys turi tilpti, o forma ir sąrašas slinkti.

## Ribos ir paleidimas

Nuolatinio saugojimo ir dienų istorijos nėra. Įrašai bei nustatymai išlaikomi pasukus ekraną, tačiau naujoje programėlės sesijoje gali būti prarasti. Kalorijos įvedamos sveikais skaičiais, jau žinant porcijos energinę vertę.

Android Studio pasirink **Sync Project with Gradle Files**, tada **Run app**. Testų failai ir testų priklausomybės pašalinti. Kompiliavimas bei lint yra įprastos projekto patikros, jos nekuria automatinių testų.

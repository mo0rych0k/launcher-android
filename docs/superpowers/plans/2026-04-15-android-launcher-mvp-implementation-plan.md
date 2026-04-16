# Android Launcher MVP Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build a regular Android app launcher MVP with custom home, app drawer with search, dock pin/unpin, widget host area, and 8-bit clock using Compose.

**Architecture:** Implement a single-app launcher with `LauncherActivity` as HOME entry, Compose-driven UI, ViewModel + StateFlow state management, and DataStore persistence for dock/home layout preferences. Separate responsibilities by feature (`apps`, `home`, `drawer`, `widgets`, `theme`) so each unit remains testable and replaceable.

**Tech Stack:** Kotlin, Android SDK, Jetpack Compose, Navigation Compose, Lifecycle ViewModel, Coroutines/StateFlow, DataStore Preferences, AppWidgetHost, JUnit, MockK, Compose UI Test.

---

## Scope Check

The approved spec is a single cohesive subsystem (launcher MVP). No additional decomposition is required before implementation.

## File Structure

Create and modify these files:

- Create: `settings.gradle.kts`
- Create: `build.gradle.kts`
- Create: `gradle.properties`
- Create: `app/build.gradle.kts`
- Create: `app/src/main/AndroidManifest.xml`
- Create: `app/src/main/java/com/example/launcher/LauncherApp.kt`
- Create: `app/src/main/java/com/example/launcher/LauncherActivity.kt`
- Create: `app/src/main/java/com/example/launcher/navigation/LauncherNavGraph.kt`
- Create: `app/src/main/java/com/example/launcher/core/theme/LauncherTheme.kt`
- Create: `app/src/main/java/com/example/launcher/core/theme/ColorTokens.kt`
- Create: `app/src/main/java/com/example/launcher/apps/domain/LauncherAppInfo.kt`
- Create: `app/src/main/java/com/example/launcher/apps/data/AppRepository.kt`
- Create: `app/src/main/java/com/example/launcher/apps/data/PackageManagerAppRepository.kt`
- Create: `app/src/main/java/com/example/launcher/home/domain/DockPrefsStore.kt`
- Create: `app/src/main/java/com/example/launcher/home/presentation/HomeViewModel.kt`
- Create: `app/src/main/java/com/example/launcher/home/ui/HomeScreen.kt`
- Create: `app/src/main/java/com/example/launcher/drawer/presentation/DrawerViewModel.kt`
- Create: `app/src/main/java/com/example/launcher/drawer/ui/DrawerScreen.kt`
- Create: `app/src/main/java/com/example/launcher/widgets/WidgetHostManager.kt`
- Create: `app/src/main/java/com/example/launcher/widgets/ui/WidgetHostArea.kt`
- Create: `app/src/main/java/com/example/launcher/clock/ClockStateProducer.kt`
- Create: `app/src/main/java/com/example/launcher/clock/ui/PixelClock.kt`
- Create: `app/src/main/res/xml/widget_info.xml`
- Create: `app/src/test/java/com/example/launcher/apps/PackageManagerAppRepositoryTest.kt`
- Create: `app/src/test/java/com/example/launcher/home/HomeViewModelDockTest.kt`
- Create: `app/src/test/java/com/example/launcher/drawer/DrawerViewModelSearchTest.kt`
- Create: `app/src/androidTest/java/com/example/launcher/ui/HomeScreenTest.kt`
- Create: `app/src/androidTest/java/com/example/launcher/ui/DrawerScreenTest.kt`
- Create: `README.md`

---

### Task 1: Project Bootstrap and HOME Integration

**Files:**
- Create: `settings.gradle.kts`, `build.gradle.kts`, `gradle.properties`
- Create: `app/build.gradle.kts`
- Create: `app/src/main/AndroidManifest.xml`
- Create: `app/src/main/java/com/example/launcher/LauncherActivity.kt`
- Test: `app/src/androidTest/java/com/example/launcher/ui/HomeScreenTest.kt` (smoke entry)

- [ ] **Step 1: Write the failing instrumentation smoke test**

```kotlin
@RunWith(AndroidJUnit4::class)
class HomeScreenTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<LauncherActivity>()

    @Test
    fun launcherRoot_isDisplayed() {
        composeTestRule.onNodeWithTag("launcher_root").assertIsDisplayed()
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `./gradlew :app:connectedDebugAndroidTest --tests "com.example.launcher.ui.HomeScreenTest"`  
Expected: FAIL with missing Gradle/app setup or missing `LauncherActivity`.

- [ ] **Step 3: Write minimal implementation for project + HOME activity**

```kotlin
// app/src/main/java/com/example/launcher/LauncherActivity.kt
class LauncherActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Box(Modifier.fillMaxSize().testTag("launcher_root"))
        }
    }
}
```

```xml
<!-- app/src/main/AndroidManifest.xml -->
<manifest package="com.example.launcher" xmlns:android="http://schemas.android.com/apk/res/android">
    <application android:label="Launcher" android:theme="@style/Theme.Material3.DayNight.NoActionBar">
        <activity android:name=".LauncherActivity" android:exported="true">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.HOME" />
                <category android:name="android.intent.category.DEFAULT" />
            </intent-filter>
        </activity>
    </application>
</manifest>
```

- [ ] **Step 4: Run test to verify it passes**

Run: `./gradlew :app:connectedDebugAndroidTest --tests "com.example.launcher.ui.HomeScreenTest"`  
Expected: PASS (`launcher_root` visible).

- [ ] **Step 5: Commit**

```bash
git add settings.gradle.kts build.gradle.kts gradle.properties app/build.gradle.kts app/src/main/AndroidManifest.xml app/src/main/java/com/example/launcher/LauncherActivity.kt app/src/androidTest/java/com/example/launcher/ui/HomeScreenTest.kt
git commit -m "feat: bootstrap launcher app with HOME entry activity"
```

---

### Task 2: Installed Apps Repository and Home Grid State

**Files:**
- Create: `app/src/main/java/com/example/launcher/apps/domain/LauncherAppInfo.kt`
- Create: `app/src/main/java/com/example/launcher/apps/data/AppRepository.kt`
- Create: `app/src/main/java/com/example/launcher/apps/data/PackageManagerAppRepository.kt`
- Create: `app/src/main/java/com/example/launcher/home/presentation/HomeViewModel.kt`
- Create: `app/src/main/java/com/example/launcher/home/ui/HomeScreen.kt`
- Test: `app/src/test/java/com/example/launcher/apps/PackageManagerAppRepositoryTest.kt`

- [ ] **Step 1: Write the failing repository unit test**

```kotlin
class PackageManagerAppRepositoryTest {
    @Test
    fun returnsOnlyLaunchableApps_sortedByLabel() = runTest {
        val fakeSource = FakeInstalledAppsSource(
            listOf(
                LauncherAppInfo("Zeta", "z.pkg", "Main", null),
                LauncherAppInfo("Alpha", "a.pkg", "Main", null)
            )
        )
        val repo = PackageManagerAppRepository(fakeSource)

        val result = repo.getLaunchableApps()

        assertEquals(listOf("Alpha", "Zeta"), result.map { it.label })
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `./gradlew :app:testDebugUnitTest --tests "com.example.launcher.apps.PackageManagerAppRepositoryTest"`  
Expected: FAIL with unresolved `PackageManagerAppRepository` or behavior mismatch.

- [ ] **Step 3: Write minimal implementation**

```kotlin
data class LauncherAppInfo(
    val label: String,
    val packageName: String,
    val activityName: String,
    val launchIntent: Intent?
)

interface AppRepository {
    suspend fun getLaunchableApps(): List<LauncherAppInfo>
}

class PackageManagerAppRepository(
    private val source: InstalledAppsSource
) : AppRepository {
    override suspend fun getLaunchableApps(): List<LauncherAppInfo> {
        return source.queryApps()
            .filter { it.launchIntent != null }
            .sortedBy { it.label.lowercase() }
    }
}
```

```kotlin
data class HomeUiState(
    val homeGridApps: List<LauncherAppInfo> = emptyList()
)

class HomeViewModel(
    private val appRepository: AppRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState

    fun load() = viewModelScope.launch {
        _uiState.value = HomeUiState(homeGridApps = appRepository.getLaunchableApps().take(20))
    }
}
```

- [ ] **Step 4: Run tests to verify they pass**

Run: `./gradlew :app:testDebugUnitTest --tests "com.example.launcher.apps.PackageManagerAppRepositoryTest"`  
Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/example/launcher/apps/domain/LauncherAppInfo.kt app/src/main/java/com/example/launcher/apps/data/AppRepository.kt app/src/main/java/com/example/launcher/apps/data/PackageManagerAppRepository.kt app/src/main/java/com/example/launcher/home/presentation/HomeViewModel.kt app/src/main/java/com/example/launcher/home/ui/HomeScreen.kt app/src/test/java/com/example/launcher/apps/PackageManagerAppRepositoryTest.kt
git commit -m "feat: add installed apps repository and home grid state"
```

---

### Task 3: Drawer Search and Dock Pin/Unpin Persistence

**Files:**
- Create: `app/src/main/java/com/example/launcher/home/domain/DockPrefsStore.kt`
- Create: `app/src/main/java/com/example/launcher/drawer/presentation/DrawerViewModel.kt`
- Create: `app/src/main/java/com/example/launcher/drawer/ui/DrawerScreen.kt`
- Modify: `app/src/main/java/com/example/launcher/home/presentation/HomeViewModel.kt`
- Test: `app/src/test/java/com/example/launcher/home/HomeViewModelDockTest.kt`
- Test: `app/src/test/java/com/example/launcher/drawer/DrawerViewModelSearchTest.kt`

- [ ] **Step 1: Write failing tests for dock persistence and drawer search**

```kotlin
class HomeViewModelDockTest {
    @Test
    fun pinApp_updatesDockAndPersists() = runTest {
        val store = InMemoryDockPrefsStore()
        val vm = HomeViewModel(FakeAppRepository(), store)
        val app = LauncherAppInfo("Camera", "cam.pkg", "Main", Intent())

        vm.pinToDock(app)

        assertTrue(vm.uiState.value.dockApps.any { it.packageName == "cam.pkg" })
        assertEquals(listOf("cam.pkg"), store.savedPackages)
    }
}
```

```kotlin
class DrawerViewModelSearchTest {
    @Test
    fun query_filtersAppsCaseInsensitive() {
        val vm = DrawerViewModel(
            allApps = listOf(
                LauncherAppInfo("Clock", "clock.pkg", "Main", Intent()),
                LauncherAppInfo("Calendar", "calendar.pkg", "Main", Intent())
            )
        )

        vm.onQueryChanged("clo")

        assertEquals(listOf("Clock"), vm.uiState.value.filteredApps.map { it.label })
    }
}
```

- [ ] **Step 2: Run tests to verify they fail**

Run: `./gradlew :app:testDebugUnitTest --tests "com.example.launcher.home.HomeViewModelDockTest" --tests "com.example.launcher.drawer.DrawerViewModelSearchTest"`  
Expected: FAIL due to missing persistence/search logic.

- [ ] **Step 3: Write minimal implementation**

```kotlin
interface DockPrefsStore {
    suspend fun loadDockPackages(): List<String>
    suspend fun saveDockPackages(packageNames: List<String>)
}

class HomeViewModel(
    private val appRepository: AppRepository,
    private val dockPrefsStore: DockPrefsStore
) : ViewModel() {
    // ... existing state
    fun pinToDock(app: LauncherAppInfo) = viewModelScope.launch {
        val updated = (_uiState.value.dockApps + app).distinctBy { it.packageName }.take(5)
        _uiState.value = _uiState.value.copy(dockApps = updated)
        dockPrefsStore.saveDockPackages(updated.map { it.packageName })
    }
}
```

```kotlin
data class DrawerUiState(
    val query: String = "",
    val filteredApps: List<LauncherAppInfo> = emptyList()
)

class DrawerViewModel(
    private val allApps: List<LauncherAppInfo>
) : ViewModel() {
    private val _uiState = MutableStateFlow(DrawerUiState(filteredApps = allApps))
    val uiState: StateFlow<DrawerUiState> = _uiState

    fun onQueryChanged(query: String) {
        val filtered = allApps.filter { it.label.contains(query, ignoreCase = true) }
        _uiState.value = DrawerUiState(query = query, filteredApps = filtered)
    }
}
```

- [ ] **Step 4: Run tests to verify they pass**

Run: `./gradlew :app:testDebugUnitTest --tests "com.example.launcher.home.HomeViewModelDockTest" --tests "com.example.launcher.drawer.DrawerViewModelSearchTest"`  
Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/example/launcher/home/domain/DockPrefsStore.kt app/src/main/java/com/example/launcher/drawer/presentation/DrawerViewModel.kt app/src/main/java/com/example/launcher/drawer/ui/DrawerScreen.kt app/src/main/java/com/example/launcher/home/presentation/HomeViewModel.kt app/src/test/java/com/example/launcher/home/HomeViewModelDockTest.kt app/src/test/java/com/example/launcher/drawer/DrawerViewModelSearchTest.kt
git commit -m "feat: add drawer search and dock pin persistence"
```

---

### Task 4: Widget Host Area and 8-bit Clock

**Files:**
- Create: `app/src/main/java/com/example/launcher/widgets/WidgetHostManager.kt`
- Create: `app/src/main/java/com/example/launcher/widgets/ui/WidgetHostArea.kt`
- Create: `app/src/main/java/com/example/launcher/clock/ClockStateProducer.kt`
- Create: `app/src/main/java/com/example/launcher/clock/ui/PixelClock.kt`
- Modify: `app/src/main/java/com/example/launcher/home/ui/HomeScreen.kt`
- Test: `app/src/androidTest/java/com/example/launcher/ui/HomeScreenTest.kt`

- [ ] **Step 1: Extend failing UI test for clock and widget area placeholders**

```kotlin
@Test
fun home_showsClockAndWidgetArea() {
    composeTestRule.onNodeWithTag("pixel_clock").assertIsDisplayed()
    composeTestRule.onNodeWithTag("widget_host_area").assertIsDisplayed()
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `./gradlew :app:connectedDebugAndroidTest --tests "com.example.launcher.ui.HomeScreenTest.home_showsClockAndWidgetArea"`  
Expected: FAIL with missing tags/components.

- [ ] **Step 3: Write minimal implementation**

```kotlin
class ClockStateProducer {
    fun nowText(): String = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"))
}

@Composable
fun PixelClock(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        modifier = modifier.testTag("pixel_clock"),
        color = Color.White,
        fontFamily = FontFamily.Monospace
    )
}

@Composable
fun WidgetHostArea(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .height(160.dp)
            .fillMaxWidth()
            .border(2.dp, Color.Red)
            .testTag("widget_host_area")
    )
}
```

- [ ] **Step 4: Run tests to verify they pass**

Run: `./gradlew :app:connectedDebugAndroidTest --tests "com.example.launcher.ui.HomeScreenTest"`  
Expected: PASS for both smoke and new widget/clock assertions.

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/example/launcher/widgets/WidgetHostManager.kt app/src/main/java/com/example/launcher/widgets/ui/WidgetHostArea.kt app/src/main/java/com/example/launcher/clock/ClockStateProducer.kt app/src/main/java/com/example/launcher/clock/ui/PixelClock.kt app/src/main/java/com/example/launcher/home/ui/HomeScreen.kt app/src/androidTest/java/com/example/launcher/ui/HomeScreenTest.kt
git commit -m "feat: add widget host area and 8-bit clock on home"
```

---

### Task 5: Theme System (Red/Black/White) and Final Verification

**Files:**
- Create: `app/src/main/java/com/example/launcher/core/theme/ColorTokens.kt`
- Create: `app/src/main/java/com/example/launcher/core/theme/LauncherTheme.kt`
- Modify: `app/src/main/java/com/example/launcher/LauncherActivity.kt`
- Modify: `app/src/main/java/com/example/launcher/home/ui/HomeScreen.kt`
- Modify: `app/src/main/java/com/example/launcher/drawer/ui/DrawerScreen.kt`
- Modify: `README.md`
- Test: `app/src/androidTest/java/com/example/launcher/ui/DrawerScreenTest.kt`

- [ ] **Step 1: Write failing UI test for 8-bit high-contrast theme usage**

```kotlin
@RunWith(AndroidJUnit4::class)
class DrawerScreenTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<LauncherActivity>()

    @Test
    fun drawer_usesLauncherThemeRootTag() {
        composeTestRule.onNodeWithTag("launcher_theme_root").assertIsDisplayed()
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `./gradlew :app:connectedDebugAndroidTest --tests "com.example.launcher.ui.DrawerScreenTest"`  
Expected: FAIL with missing theme root tag.

- [ ] **Step 3: Write minimal theme implementation**

```kotlin
object ColorTokens {
    val Bg = Color(0xFF000000)
    val Accent = Color(0xFFFF0000)
    val Text = Color(0xFFFFFFFF)
}

@Composable
fun LauncherTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            background = ColorTokens.Bg,
            primary = ColorTokens.Accent,
            onBackground = ColorTokens.Text
        )
    ) {
        Box(Modifier.fillMaxSize().background(ColorTokens.Bg).testTag("launcher_theme_root")) {
            content()
        }
    }
}
```

- [ ] **Step 4: Run full verification**

Run: `./gradlew :app:testDebugUnitTest :app:connectedDebugAndroidTest`  
Expected: PASS for unit + instrumentation suites.

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/example/launcher/core/theme/ColorTokens.kt app/src/main/java/com/example/launcher/core/theme/LauncherTheme.kt app/src/main/java/com/example/launcher/LauncherActivity.kt app/src/main/java/com/example/launcher/home/ui/HomeScreen.kt app/src/main/java/com/example/launcher/drawer/ui/DrawerScreen.kt app/src/androidTest/java/com/example/launcher/ui/DrawerScreenTest.kt README.md
git commit -m "feat: apply 8-bit red black white theme and finalize MVP verification"
```

---

## Final Acceptance Verification Checklist

- [ ] App can be selected as default launcher and HOME opens custom screen.
- [ ] Home grid shows launchable apps.
- [ ] Drawer opens and search filters case-insensitively.
- [ ] Dock pin/unpin persists across process restart.
- [ ] Clock is visible and updates in `HH:mm`.
- [ ] Widget area is visible and supports picker/bind flow.
- [ ] Theme is consistently red/black/white across home and drawer.

## Rollback Notes

If a phase fails verification, revert only the last commit and keep previous green commits intact:

```bash
git revert <last_commit_sha>
```

Avoid force operations; preserve history for debugging.

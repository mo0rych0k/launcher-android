# All Apps Screen Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build a swipe-accessed all-apps page with icon + label rows and live search filtering while preserving current home behavior.

**Architecture:** Keep a single `HomeViewModel` as state owner, extend `HomeUiState` with `allApps` and `searchQuery`, and render `HomeScreen` + `AllAppsScreen` in a 2-page horizontal pager in `LauncherActivity`. Add icon support in domain/data model so `AllAppsScreen` can render real app icons.

**Tech Stack:** Kotlin, AndroidX Compose (Material3/Foundation), Lifecycle ViewModel + StateFlow, JUnit4/coroutines-test.

---

## File Structure (planned changes)

- Modify: `app/src/main/kotlin/com/example/launcher/apps/domain/LauncherAppInfo.kt` (add app icon field)
- Modify: `app/src/main/kotlin/com/example/launcher/apps/data/AndroidInstalledAppsSource.kt` (populate icon)
- Modify: `app/src/main/kotlin/com/example/launcher/home/presentation/HomeViewModel.kt` (new ui state + query handler)
- Modify: `app/src/main/kotlin/com/example/launcher/LauncherActivity.kt` (host pager and wire all-apps)
- Modify: `app/src/main/kotlin/com/example/launcher/home/ui/HomeScreen.kt` (no functional change, page-ready usage)
- Create: `app/src/main/kotlin/com/example/launcher/apps/ui/AllAppsScreen.kt` (search + filtered list + icon rows)
- Modify: `app/src/test/kotlin/com/example/launcher/apps/PackageManagerAppRepositoryTest.kt` (constructor updates)
- Modify: `app/src/test/kotlin/com/example/launcher/home/HomeViewModelDockTest.kt` (constructor updates + new tests)
- Modify: `app/src/test/kotlin/com/example/launcher/drawer/DrawerViewModelSearchTest.kt` (constructor updates)

### Task 1: Add icon support to app domain model

**Files:**
- Modify: `app/src/main/kotlin/com/example/launcher/apps/domain/LauncherAppInfo.kt`
- Modify: `app/src/main/kotlin/com/example/launcher/apps/data/AndroidInstalledAppsSource.kt`
- Test: `app/src/test/kotlin/com/example/launcher/apps/PackageManagerAppRepositoryTest.kt`

- [ ] **Step 1: Write failing test adjustment for new model signature**

```kotlin
// PackageManagerAppRepositoryTest.kt (example fixture update)
LauncherAppInfo(
    label = "Alpha",
    packageName = "a.pkg",
    activityName = "Main",
    launchIntent = android.content.Intent(),
    icon = null
)
```

- [ ] **Step 2: Run test to verify compilation fails before model update**

Run: `./gradlew :app:testDebugUnitTest --tests com.example.launcher.apps.PackageManagerAppRepositoryTest`
Expected: FAIL with constructor mismatch (`LauncherAppInfo` missing `icon` argument).

- [ ] **Step 3: Add icon field to model and source mapping**

```kotlin
// LauncherAppInfo.kt
import android.graphics.drawable.Drawable

data class LauncherAppInfo(
    val label: String,
    val packageName: String,
    val activityName: String,
    val launchIntent: Intent?,
    val icon: Drawable?
)
```

```kotlin
// AndroidInstalledAppsSource.kt (inside map)
val icon = info.loadIcon(packageManager)
LauncherAppInfo(
    label = label,
    packageName = packageName,
    activityName = activityName,
    launchIntent = launchIntent,
    icon = icon
)
```

- [ ] **Step 4: Update tests with explicit `icon = null` and rerun**

Run: `./gradlew :app:testDebugUnitTest --tests com.example.launcher.apps.PackageManagerAppRepositoryTest`
Expected: PASS.

- [ ] **Step 5: Commit task**

```bash
git add app/src/main/kotlin/com/example/launcher/apps/domain/LauncherAppInfo.kt \
  app/src/main/kotlin/com/example/launcher/apps/data/AndroidInstalledAppsSource.kt \
  app/src/test/kotlin/com/example/launcher/apps/PackageManagerAppRepositoryTest.kt
git commit -m "feat: add icon field to launcher app model"
```

### Task 2: Extend home state for all apps and live query

**Files:**
- Modify: `app/src/main/kotlin/com/example/launcher/home/presentation/HomeViewModel.kt`
- Test: `app/src/test/kotlin/com/example/launcher/home/HomeViewModelDockTest.kt`

- [ ] **Step 1: Write failing tests for state extensions**

```kotlin
@Test
fun load_setsAlphabeticalAllAppsAndHomeSubset() = runTest {
    val apps = listOf(
        LauncherAppInfo("zeta", "z.pkg", "Main", Intent(), null),
        LauncherAppInfo("Alpha", "a.pkg", "Main", Intent(), null)
    )
    val vm = HomeViewModel(FakeAppRepository(apps), InMemoryDockPrefsStore())
    vm.load().join()
    assertEquals(listOf("Alpha", "zeta"), vm.uiState.value.allApps.map { it.label })
    assertEquals(listOf("Alpha", "zeta"), vm.uiState.value.homeGridApps.map { it.label })
}
```

```kotlin
@Test
fun onSearchQueryChange_updatesQuery() = runTest {
    val vm = HomeViewModel(FakeAppRepository(), InMemoryDockPrefsStore())
    vm.onSearchQueryChange("cam")
    assertEquals("cam", vm.uiState.value.searchQuery)
}
```

- [ ] **Step 2: Run home tests to verify failure first**

Run: `./gradlew :app:testDebugUnitTest --tests com.example.launcher.home.HomeViewModelDockTest`
Expected: FAIL (`allApps`/`searchQuery`/`onSearchQueryChange` missing).

- [ ] **Step 3: Implement state and viewmodel APIs**

```kotlin
data class HomeUiState(
    val homeGridApps: List<LauncherAppInfo> = emptyList(),
    val dockApps: List<LauncherAppInfo> = emptyList(),
    val allApps: List<LauncherAppInfo> = emptyList(),
    val searchQuery: String = ""
)

fun load() = viewModelScope.launch {
    val apps = appRepository.getLaunchableApps().sortedBy { it.label.lowercase() }
    _uiState.value = _uiState.value.copy(
        allApps = apps,
        homeGridApps = apps.take(20)
    )
}

fun onSearchQueryChange(query: String) {
    _uiState.value = _uiState.value.copy(searchQuery = query)
}
```

- [ ] **Step 4: Re-run targeted tests**

Run: `./gradlew :app:testDebugUnitTest --tests com.example.launcher.home.HomeViewModelDockTest`
Expected: PASS.

- [ ] **Step 5: Commit task**

```bash
git add app/src/main/kotlin/com/example/launcher/home/presentation/HomeViewModel.kt \
  app/src/test/kotlin/com/example/launcher/home/HomeViewModelDockTest.kt
git commit -m "feat: extend home state for all apps and search query"
```

### Task 3: Build `AllAppsScreen` UI with live filtering and icons

**Files:**
- Create: `app/src/main/kotlin/com/example/launcher/apps/ui/AllAppsScreen.kt`
- Modify: `app/src/main/kotlin/com/example/launcher/home/ui/HomeScreen.kt` (only if shared styles/constants needed)

- [ ] **Step 1: Add a failing Compose UI smoke test (optional if no test harness yet)**

```kotlin
// If androidTest harness exists, verify:
// - node with tag "all_apps_screen" exists
// - node with text "Search apps" exists
```

- [ ] **Step 2: Run android tests to confirm missing screen/test failure**

Run: `./gradlew :app:connectedDebugAndroidTest`
Expected: FAIL or skip depending on device; proceed with manual verification if unavailable.

- [ ] **Step 3: Implement All Apps screen**

```kotlin
@Composable
fun AllAppsScreen(
    uiState: HomeUiState,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val normalizedQuery = uiState.searchQuery.trim()
    val filteredApps = remember(uiState.allApps, normalizedQuery) {
        if (normalizedQuery.isBlank()) uiState.allApps
        else uiState.allApps.filter { it.label.contains(normalizedQuery, ignoreCase = true) }
    }
    // TextField + LazyColumn rows(icon + label) + empty state
}
```

```kotlin
// Icon rendering inside row (fallback when icon == null)
app.icon?.let { drawable ->
    Image(
        painter = rememberDrawablePainter(drawable = drawable),
        contentDescription = app.label
    )
}
```

- [ ] **Step 4: Validate by running unit tests and app build**

Run: `./gradlew :app:testDebugUnitTest :app:assembleDebug`
Expected: PASS.

- [ ] **Step 5: Commit task**

```bash
git add app/src/main/kotlin/com/example/launcher/apps/ui/AllAppsScreen.kt
git commit -m "feat: add all apps screen with live search"
```

### Task 4: Wire swipe navigation between Home and All Apps

**Files:**
- Modify: `app/src/main/kotlin/com/example/launcher/LauncherActivity.kt`
- Modify: `app/src/test/kotlin/com/example/launcher/drawer/DrawerViewModelSearchTest.kt` (constructor update only due icon field)

- [ ] **Step 1: Add failing compile path by referencing new screen and pager**

```kotlin
// LauncherActivity.kt target wiring
HorizontalPager(state = pagerState, pageCount = 2) { page ->
    when (page) {
        0 -> HomeScreen(uiState = uiState)
        else -> AllAppsScreen(uiState = uiState, onQueryChange = viewModel::onSearchQueryChange)
    }
}
```

- [ ] **Step 2: Run compile/unit tests to capture integration failures**

Run: `./gradlew :app:testDebugUnitTest`
Expected: FAIL until imports/dependencies/constructors are aligned.

- [ ] **Step 3: Implement full wiring and fix constructor updates**

```kotlin
val pagerState = rememberPagerState(initialPage = 0) { 2 }
LauncherTheme {
    HorizontalPager(state = pagerState) { page ->
        if (page == 0) HomeScreen(uiState = uiState)
        else AllAppsScreen(uiState = uiState, onQueryChange = viewModel::onSearchQueryChange)
    }
}
```

```kotlin
// update older test fixtures to new model constructor
LauncherAppInfo("Clock", "clock.pkg", "Main", Intent(), null)
```

- [ ] **Step 4: Run full verification**

Run: `./gradlew :app:testDebugUnitTest :app:assembleDebug`
Expected:
- Unit tests PASS
- Debug APK builds successfully

- [ ] **Step 5: Commit task**

```bash
git add app/src/main/kotlin/com/example/launcher/LauncherActivity.kt \
  app/src/test/kotlin/com/example/launcher/drawer/DrawerViewModelSearchTest.kt
git commit -m "feat: enable swipe between home and all apps pages"
```

### Task 5: Final polish and regression check

**Files:**
- Modify: `app/src/main/kotlin/com/example/launcher/apps/ui/AllAppsScreen.kt` (if polish needed)
- Modify: `app/src/main/kotlin/com/example/launcher/home/ui/HomeScreen.kt` (if spacing/theming alignment needed)

- [ ] **Step 1: Manual runtime checks**

Run app and verify:
- Swipe right opens all apps page.
- Search filters list live while typing.
- List sorted alphabetically.
- Icon + label visible in rows.
- Empty state visible when query has no matches.

- [ ] **Step 2: Run final checks**

Run: `./gradlew :app:testDebugUnitTest :app:assembleDebug`
Expected: PASS.

- [ ] **Step 3: Commit final polish**

```bash
git add app/src/main/kotlin/com/example/launcher/apps/ui/AllAppsScreen.kt \
  app/src/main/kotlin/com/example/launcher/home/ui/HomeScreen.kt
git commit -m "chore: polish all apps search page behavior"
```

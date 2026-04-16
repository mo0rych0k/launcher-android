# All Apps Screen Design

Date: 2026-04-16
Project: Launcher-Android
Scope: Add a separate "All Apps" screen opened by right swipe, with alphabetical list, app icons, and live search.

## Goals

- Add a dedicated all-apps screen reachable by horizontal swipe from home.
- Show launchable apps in an alphabetically sorted list.
- Show each app row as icon + text label.
- Add live search filtering while typing.
- Keep current home behavior and existing architecture simple (single `HomeViewModel`).

## Non-Goals

- No app grouping by letters/sections in this iteration.
- No debounce or background indexing optimization in this iteration.
- No major navigation framework migration.
- No unrelated refactors outside screens and state needed for this feature.

## Proposed Architecture

Recommended approach: **Variant A**.

- Keep one `HomeViewModel` as the single source of truth for home + all apps.
- Add `AllAppsScreen` composable for search + list rendering.
- In `LauncherActivity`, render two pages in a horizontal container:
  - Page 0: `HomeScreen`
  - Page 1: `AllAppsScreen`
- Swiping right from page 0 opens all apps. Swiping left returns to home.

Why this approach:

- Minimal complexity for current project size.
- Preserves straightforward state ownership.
- Avoids adding a second ViewModel and extra wiring too early.

## State Model

Extend `HomeUiState` with:

- `allApps: List<LauncherAppInfo>` - full sorted launchable apps list.
- `searchQuery: String` - current text in search field.

Existing fields remain:

- `homeGridApps: List<LauncherAppInfo>`
- `dockApps: List<LauncherAppInfo>`

ViewModel behavior:

- `load()` fetches launchable apps once, sorts by `label` case-insensitively, saves into `allApps`.
- `homeGridApps` is derived from first 20 items of sorted list for current home behavior.
- `onSearchQueryChange(query: String)` updates `searchQuery`.

Derived list for all-apps UI:

- `filteredApps` = `allApps` when query is blank.
- Else filter by case-insensitive containment of trimmed query in app label.
- Filtering is live on every keystroke.

## UI Design

### Home + Pager Container

- `LauncherActivity` hosts a horizontal pager/gesture container with 2 pages.
- Home page remains visually unchanged.
- All-apps page receives `uiState` and search callback from `HomeViewModel`.

### All Apps Screen

- Root: full-screen column with test tag `all_apps_screen`.
- Top: search text field with placeholder (e.g. "Search apps").
- Content: `LazyColumn` list of filtered apps.
- Row content:
  - App icon (from `LauncherAppInfo.icon` if available in domain model).
  - App label text.
- Empty state text when no matches (e.g. "Nothing found").

## Data Flow

1. `LauncherActivity` creates and observes `HomeViewModel`.
2. `load()` populates state with sorted apps.
3. User swipes to all-apps page.
4. User types in search field.
5. `onSearchQueryChange()` updates state.
6. UI recomputes filtered list and updates immediately.

## Edge Cases and Behavior Rules

- Blank query shows full sorted list.
- Leading/trailing spaces do not affect matching (`trim` before filtering).
- Matching is case-insensitive.
- If no launchable apps exist, show empty state without crash.
- If no matches for query, show empty state without crash.

## Testing Strategy

### Unit Tests (`HomeViewModel`)

- `load()` stores apps alphabetically (case-insensitive order).
- `load()` keeps home list limited to 20.
- `onSearchQueryChange()` updates `searchQuery`.
- Filtering helper/logic returns expected subsets for:
  - blank query
  - mixed-case query
  - query with surrounding spaces
  - no-match query

### UI Verification (initial smoke level)

- All apps screen renders with expected test tag.
- Search field is visible.
- List updates when typing query.

## Risks and Mitigations

- Risk: icon rendering type mismatch (Drawable vs Painter) in Compose.
  - Mitigation: convert via existing adapter/painter utility used in project.
- Risk: pager API compatibility differences by Compose version.
  - Mitigation: use project-supported pager API and fallback to swipeable row if needed.

## Rollout Plan

1. Extend `HomeUiState` and ViewModel state/update APIs.
2. Implement `AllAppsScreen` with search + list + empty state.
3. Wire pager container in `LauncherActivity`.
4. Add/adjust tests for state and basic UI behavior.
5. Run checks and verify no regression on current home screen.

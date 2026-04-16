# Android Launcher Design (MVP)

## 1) Goal and Scope

Build an Android launcher as a regular app (no root, no system app privileges) using Kotlin and Jetpack Compose.

This MVP includes:
- Home screen (custom launcher UI as default HOME app)
- App drawer with search
- Dock with pin/unpin behavior
- Widget hosting area on home
- Built-in 8-bit digital clock widget
- Minimal 8-bit visual theme with red/black/white palette

This MVP explicitly excludes:
- Full system lock screen replacement
- Deep system-level icon replacement outside launcher UI
- Device owner / enterprise-only controls

## 2) Product Constraints

Because this is a regular Android app:
- The launcher can replace the home screen after user selects it as default.
- System lock screen cannot be fully replaced.
- Launcher can style and render icons within its own UI only.
- Some OEM devices may have custom restrictions for widgets or launcher behavior.

## 3) UX Direction

Style: minimal 8-bit.

Visual rules:
- Primary colors: red, black, white.
- High contrast text and borders.
- Pixel-style font for key headings and clock.
- Simple geometric cards, low visual noise.
- Sparse animations (quick, crisp, non-elastic transitions).

Interaction rules:
- Swipe up from home to open app drawer.
- Tap app icon to launch.
- Long-press app icon for actions (pin/unpin to dock, place on home).
- Dock is always visible on home.

## 4) Architecture

Stack:
- Kotlin
- Jetpack Compose
- Android ViewModel + StateFlow
- Navigation Compose
- DataStore (for user preferences and layout state)

Layering:
- `data` layer
  - Reads installed apps with `PackageManager`
  - Persists launcher settings and pinned apps
- `domain` layer
  - Use-cases for listing apps, search/filter, pin/unpin, sorting
- `ui` layer
  - Compose screens, state mapping, interactions

Primary app entry:
- `LauncherActivity` configured with `HOME` and `DEFAULT` intent filters.

## 5) Components

### 5.1 AppRepository

Responsibilities:
- Query installed launchable apps.
- Map app info to internal model:
  - label
  - package name
  - activity name
  - icon reference
  - launch intent

Behavior:
- Cache recent app list for fast initial render.
- Expose refresh action for package changes.

### 5.2 HomeViewModel

State:
- `homeGridApps`
- `dockApps`
- `clockState`
- `widgetSlots`
- transient UI state (selection, menus)

Actions:
- Launch app
- Pin/unpin app in dock
- Place/remove app on home grid
- Open drawer

### 5.3 DrawerViewModel

State:
- Full app list
- Search query
- Filtered list
- Sort mode (default A-Z)

Actions:
- Update query
- Launch app
- Quick action: pin to dock / place on home

### 5.4 WidgetHostManager

Responsibilities:
- Manage `AppWidgetHost` lifecycle.
- Start system widget picker flow.
- Bind chosen widget IDs to persisted slots.
- Restore widgets after process restart.

Fallback:
- Show placeholder for missing/unavailable widgets with rebind option.

### 5.5 ThemeManager

Responsibilities:
- Centralize color tokens, typography, spacing, border styles.
- Expose theme variants under red/black/white palette (normal/high contrast).
- Keep visual style consistent across home, drawer, settings.

### 5.6 ClockModule

Responsibilities:
- Provide digital 8-bit clock state.
- Update once per minute (or at minute boundary for precision).
- Render compact and large clock variants.

## 6) Data Flow

1. App launches into `LauncherActivity`.
2. `HomeViewModel` requests home state.
3. `AppRepository` returns installed apps and pinned state.
4. Home UI renders grid + dock + clock + widget area.
5. User opens drawer (swipe up or UI action).
6. `DrawerViewModel` serves searchable/sorted app list.
7. User action (launch/pin/place) updates state and persists via DataStore.
8. UI re-renders from updated StateFlow.

## 7) Android Integration Details

Manifest essentials:
- Main launcher activity with:
  - `android.intent.category.HOME`
  - `android.intent.category.DEFAULT`
- Widget host/provider declarations as required.

System events to observe:
- `PACKAGE_ADDED`, `PACKAGE_REMOVED`, `PACKAGE_CHANGED` (or equivalent package change handling strategy)
- Rebuild/refresh app model when package set changes.

Default launcher flow:
- Provide in-app guidance for selecting this launcher as default HOME app.

## 8) Error Handling and Resilience

Scenarios:
- Missing `launchIntent`
  - Show non-blocking message and skip launch.
- Package query failure / empty result
  - Render fallback state with retry.
- Widget binding failure
  - Show placeholder and action to pick/rebind.
- Corrupt persisted layout data
  - Reset only broken section, not whole launcher preferences.

Non-functional handling:
- Avoid app crash on single widget or app metadata failure.
- Log key integration failures for diagnostics.

## 9) Testing Strategy (MVP)

Unit tests:
- App mapping from `PackageManager` model.
- Search/filter behavior.
- Pin/unpin and home placement rules.
- Layout persistence and restore.

Compose UI tests:
- Home grid rendering.
- Drawer open/search/launch flow.
- Dock interactions (pin/unpin).
- Clock renders expected format.

Device/integration tests:
- Set launcher as default and verify home replace flow.
- Launch common apps from home/drawer.
- Add/remove widget and restore after app restart.
- Validate behavior on low-memory or lower-end device.

## 10) Delivery Plan (Approach A)

Phase 1: Launcher core
- `LauncherActivity`, HOME integration, app listing, basic home grid.

Phase 2: Drawer and dock
- App drawer search/sort and dock pinning.

Phase 3: Widgets and clock
- Widget host area + built-in 8-bit clock component.

Phase 4: Theming and polish
- 8-bit visual system (red/black/white), accessibility pass, performance tuning.

## 11) Acceptance Criteria (MVP)

MVP is accepted when:
- User can set app as default launcher and see custom home screen.
- Home screen displays selected apps and a persistent dock.
- Drawer opens and supports search with responsive filtering.
- User can pin/unpin apps to dock and state persists.
- User can add at least one system widget and see it restored on reopen.
- 8-bit theme (red/black/white) is consistently applied across core screens.
- App launches popular installed apps reliably from home and drawer.

## 12) Risks and Mitigations

Risk: OEM-specific launcher/widget limitations.
- Mitigation: test on multiple vendors/Android versions; keep fallback flows.

Risk: Startup performance with large app counts.
- Mitigation: incremental loading and caching; off-main-thread package parsing.

Risk: Visual style hurting readability.
- Mitigation: enforce contrast checks and include high-contrast variant.

Risk: Gesture conflicts and accidental actions.
- Mitigation: explicit hit zones, conservative gesture thresholds, undo for destructive actions.

## 13) Out-of-Scope Follow-ups

Potential next steps after MVP:
- Optional pseudo lock overlay (with clear limitation disclaimers)
- Icon pack support
- Gesture customization
- Backup/restore layout profile
- Notification badges and unread indicators (where allowed)

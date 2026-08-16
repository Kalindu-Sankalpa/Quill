# 📱 IS2205 Mini-Project — Full Project Plan
## App Name: **Quill** — A Developer's Mobile Text & Code Editor
> Inspired by Iotas (Ubuntu) · Built with Material Design 3 · Min SDK API 29 (Android 10) · Tested on Sony Xperia SO-51A

---

## 📋 Table of Contents

1. [Project Overview](#1-project-overview)
2. [App Vision & Design Philosophy](#2-app-vision--design-philosophy)
3. [Technology Stack](#3-technology-stack)
4. [System Architecture](#4-system-architecture)
5. [Module Breakdown & Feature Specifications](#5-module-breakdown--feature-specifications)
6. [Team Roles & Responsibilities](#6-team-roles--responsibilities)
7. [Full Task List](#7-full-task-list)
8. [Sprint Timeline & Milestones](#8-sprint-timeline--milestones)
9. [UI/UX Design Plan](#9-uiux-design-plan)
10. [Database Schema](#10-database-schema)
11. [Key Implementation Details](#11-key-implementation-details)
12. [Libraries & Dependencies](#12-libraries--dependencies)
13. [Critical Focus Areas](#13-critical-focus-areas)
14. [Testing Strategy](#14-testing-strategy)
15. [Submission Checklist](#15-submission-checklist)

---

## 1. Project Overview

| Field              | Details                                                          |
|--------------------|------------------------------------------------------------------|
| **Course**         | IS2205: Mobile Application Design and Development                |
| **Type**           | Mini-Project (Group of 3)                                        |
| **App Name**       | Quill                                                            |
| **Platform**       | Android (Native — Kotlin + Jetpack Compose)                      |
| **IDE**            | Android Studio (Ladybug / latest stable)                         |
| **Min SDK**        | API 29 — Android 10 (Q)                                          |
| **Target SDK**     | API 35 — Android 15                                              |
| **Test Device**    | Sony Xperia 1 II (SO-51A) — Android 10, 21:9 display (6.5 inch) |
| **Theme**          | Material Design 3 with Dynamic Color (API 31+) / static fallback |
| **Primary Users**  | Developers, technical writers, students                          |

### What is Quill?

Quill is a modern, lightweight mobile text and code editor that combines:
- A **focused writing environment** for Markdown documents
- A **syntax-aware code editor** for Kotlin source files
- A **delta-based version control system** with visual diff inspection
- A **crash-resilient auto-save engine** that never lets you lose work

The overall aesthetic draws from the Iotas editor on Ubuntu — clean, distraction-free, sidebar-driven — reimagined with Android's Material You dynamic theming.

---

## 2. App Vision & Design Philosophy

### Core Design Principles

- **Focus First**: The editor is always the hero. No clutter, no bloat.
- **Material Design 3**: Beautiful custom static palette on Android 10 (API 29, our test device); Dynamic Color wallpaper extraction activates as a bonus on Android 12+ (API 31+).
- **One File at a Time**: Single-active-file model with a slide-out sidebar for navigation, exactly as the spec recommends.
- **Developer-grade reliability**: Auto-save, version snapshots, and rollback should feel as powerful as a desktop tool.

### Inspiration Comparison

| Iotas (Ubuntu)                      | Quill (Android)                              |
|-------------------------------------|----------------------------------------------|
| Sidebar file list on the left       | Collapsible navigation drawer (left)         |
| Clean monospace editor area         | Jetpack Compose TextField with span styling  |
| Minimal top toolbar                 | Floating contextual toolbar on text select   |
| Plain dark/light theme              | Material 3 static palette (+ Dynamic Color on API 31+) |
| No version control                  | Delta-based versioning with diff view        |

---

## 3. Technology Stack

### Core

| Layer               | Technology                                                  |
|---------------------|-------------------------------------------------------------|
| **IDE**             | Android Studio (Ladybug or newer)                           |
| **Language**        | Kotlin 2.x                                                  |
| **Min SDK**         | API 29 — Android 10 (Q)                                     |
| **Target SDK**      | API 35 — Android 15                                         |
| **UI Framework**    | Jetpack Compose (Material 3)                                |
| **Architecture**    | MVVM + Clean Architecture (Repository pattern)              |
| **Navigation**      | Jetpack Navigation Compose                                  |
| **DI**              | Hilt (Dagger)                                               |
| **Async**           | Kotlin Coroutines + Flow                                    |
| **Local Storage**   | Android Internal Storage (files) + Room (metadata/versions) |
| **Build System**    | Gradle (Kotlin DSL)                                         |

### Key Libraries

| Purpose                   | Library                                               |
|---------------------------|-------------------------------------------------------|
| **Diff/Patch Engine**     | `java-diff-utils` (open-source, spec-approved)        |
| **Markdown Rendering**    | `Markwon` (fast, extensible Markdown → Spannable)     |
| **Kotlin Formatting**     | `ktfmt` or `ktlint` (optional code formatter)         |
| **File Picker**           | `ActivityResultContracts.OpenDocument`                |
| **Room Database**         | `androidx.room:room-ktx`                              |
| **Coroutines**            | `kotlinx-coroutines-android`                          |
| **DataStore**             | `androidx.datastore:datastore-preferences` (settings) |
| **Lifecycle**             | `androidx.lifecycle:lifecycle-runtime-compose`        |
| **Material 3**            | `androidx.compose.material3`                          |

---

## 4. System Architecture

```
┌──────────────────────────────────────────────────────────────────┐
│                        UI LAYER (Jetpack Compose)                │
│  ┌─────────────┐  ┌──────────────┐  ┌────────────────────────┐  │
│  │  NavDrawer  │  │  EditorScreen│  │  VersionHistoryScreen  │  │
│  │  (Sidebar)  │  │  (Main View) │  │  (Diff + Rollback)     │  │
│  └─────────────┘  └──────────────┘  └────────────────────────┘  │
└────────────────────────────┬─────────────────────────────────────┘
                             │ ViewModel (StateFlow / LiveData)
┌────────────────────────────▼─────────────────────────────────────┐
│                     DOMAIN / VIEWMODEL LAYER                     │
│  EditorViewModel  │  FileViewModel  │  VersionViewModel          │
└────────┬──────────┴────────┬────────┴────────┬─────────────────--┘
         │                   │                  │
┌────────▼───────────────────▼──────────────────▼──────────────────┐
│                      REPOSITORY LAYER                            │
│  FileRepository  │  VersionRepository  │  CrashRecoveryRepo      │
└────────┬──────────┴────────┬────────────┴────────┬───────────────┘
         │                   │                      │
┌────────▼──────┐   ┌────────▼──────────┐  ┌───────▼───────────────┐
│  FileSystem   │   │   Room Database   │  │   TempCache Manager   │
│  (Int. Store) │   │  (Versions, Meta) │  │   (Auto-save .tmp)    │
└───────────────┘   └───────────────────┘  └───────────────────────┘
```

### Data Flow for Save with Versioning

```
User taps "Save" (or auto-save triggers)
        │
        ▼
Is this the FIRST save?
   ├── YES → Write full file content to internal storage
   │          Create Version(id=1, patch=null, isBase=true) in Room
   │
   └── NO  → Get previous content from storage
              Run java-diff-utils: diff(prevContent, newContent) → patch string
              Store patch string in Room as new Version row
              Update file's lastModified timestamp
```

---

## 5. Module Breakdown & Feature Specifications

### Module A — Editor Engine & UI (Member 1)

#### A1. File Operations
- **New File**: Creates an untitled buffer; prompts for filename on first save.
- **Open File**: Uses `ActivityResultContracts.OpenDocument` for `.kt`, `.md`, `.txt` files.
- **Save**: Writes current buffer to internal storage path; triggers versioning logic.
- **Save As**: Saves to a new path/name; resets version history to base.
- **Recent Files**: Stored in Room (`RecentFile` table), shown in sidebar list.
- **Encoding**: Default UTF-8; optionally expose ISO-8859-1 in settings.

#### A2. Editor UI Components
- **Main Editor**: Jetpack Compose `BasicTextField` with custom `VisualTransformation` for syntax highlighting using `AnnotatedString` / `SpanStyle`.
- **Word Wrap Toggle**: Switch in toolbar; when OFF, enable horizontal scroll.
- **Undo / Redo**: Custom in-memory stack (`ArrayDeque<TextFieldValue>`) tracking edit states.
- **Find**: Floating bottom bar with text input; highlights matches in editor.
- **Find & Replace**: Extended bottom sheet with "find" and "replace" fields + "Replace All" action.

#### A3. Syntax Highlighting Engine
- **Architecture**: A `SyntaxHighlighter` interface with two implementations:
  - `KotlinHighlighter`
  - `MarkdownHighlighter`
- **Kotlin Keywords File**: Load from `assets/kotlin_keywords.txt` (one keyword per line). Apply `SpanStyle(color = keywordColor, fontWeight = Bold)` via regex matching.
- **Kotlin Highlighting Rules**:

  | Token Type      | Example                    | Style                         |
  |-----------------|----------------------------|-------------------------------|
  | Keywords        | `fun`, `val`, `class`      | Bold, primary color           |
  | String literals | `"hello world"`            | Green / secondary color       |
  | Comments        | `// comment`, `/* ... */`  | Italic, muted gray            |
  | Annotations     | `@Override`, `@Composable` | Purple / tertiary color       |
  | Numbers         | `42`, `3.14`               | Orange accent                 |

- **Markdown Highlighting Rules**:

  | Token Type      | Example                    | Style                         |
  |-----------------|----------------------------|-------------------------------|
  | Headings        | `# Title`                  | Bold, larger SpanStyle        |
  | Bold            | `**text**`                 | FontWeight.Bold               |
  | Italic          | `*text*`                   | FontStyle.Italic              |
  | Code inline     | `` `code` ``               | Monospace font, bg tint       |
  | Links           | `[label](url)`             | Underline, link color         |
  | Code blocks     | ` ```...``` `              | Monospace block, surface tint |

- **Performance**: Highlighting runs on `Dispatchers.Default`; result posted back to UI via `StateFlow`. Debounce 150ms on each keystroke to avoid lag.

#### A4. Markdown Preview Panel (Optional but Recommended)
- Toggleable via FAB or toolbar icon (split-pane or tab switch).
- Uses `Markwon` library to render Markdown → `Spanned` → `AndroidView(::TextView)` inside Compose.
- Toggle state persisted in `DataStore`.

---

### Module B — Version Control System (Member 2)

#### B1. Delta-Based Versioning

The version control system is the most technically critical part of this project.

**Core concept — NO full-file duplication:**

```
Version 1 (BASE): Full file content stored on disk
Version 2:        Diff patch from V1 → V2 (stored in Room as text)
Version 3:        Diff patch from V2 → V3 (stored in Room as text)
...
```

**Creating a patch (on Save):**
```kotlin
val patch: Patch<String> = DiffUtils.diff(
    originalLines,   // previous version lines
    revisedLines     // current buffer lines
)
val unifiedDiff: List<String> = UnifiedDiffUtils.generateUnifiedDiff(
    "prev", "curr", originalLines, patch, 3
)
val patchString = unifiedDiff.joinToString("\n")
// → Store patchString in Room Version table
```

**Applying a patch (on Rollback):**
```kotlin
// To reconstruct version N, replay patches from base → N
var content = readBaseFile()           // Version 1 always full
for (i in 2..targetVersion) {
    val patch = db.versionDao().getPatch(fileId, i)
    content = applyPatch(content, patch)
}
// Restore this content to the editor buffer
```

#### B2. Room Database — Version Table

```kotlin
@Entity(tableName = "versions")
data class VersionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val fileId: Long,             // FK to files table
    val versionNumber: Int,
    val label: String,            // e.g. "v1", "Auto-save", user-named
    val patchText: String?,       // null for base version
    val isBase: Boolean,
    val createdAt: Long           // System.currentTimeMillis()
)
```

#### B3. Diff View Screen

- Shows two-column or inline line-by-line diff between any two versions.
- Added lines: green background chip.
- Removed lines: red background chip with strikethrough.
- Unchanged lines: muted text.
- Implemented using a `LazyColumn` rendering diff hunks from `java-diff-utils`.

#### B4. Version History UI

- Bottom sheet with a vertical timeline of all versions.
- Each entry shows: version number, label, timestamp, "View Diff" button, "Restore" button.
- Long-press to rename a version label.
- Swipe-to-delete a version (with confirmation dialog) — note: deleting a middle version requires patch recomputation.

#### B5. Read-Only Lock
- `FileEntity.isReadOnly: Boolean` flag in Room.
- When `true`, editor `BasicTextField` sets `enabled = false`.
- Toolbar shows a lock icon; tap to toggle (confirmation dialog).

---

### Module C — Crash Recovery, UI/UX & Markdown Preview (Member 3)

#### C1. Crash Recovery / Auto-Save Engine

```
Every 10 seconds (WorkManager PeriodicWorkRequest or Handler):
  └─ Capture current buffer text
  └─ Write to: /data/data/<package>/cache/recovery/<filename>.tmp
  └─ Log timestamp in DataStore

On App Launch:
  └─ Check if .tmp file exists for the last opened file
  └─ If YES: Show "Unsaved changes found" dialog
       ├─ "Restore" → Load .tmp into editor buffer
       └─ "Discard" → Delete .tmp file
```

**Implementation notes:**
- Use `CoroutineScope(Dispatchers.IO)` with a repeating delay loop inside the ViewModel.
- Or use `Handler(Looper.getMainLooper()).postDelayed(...)` in the UI layer.
- The `.tmp` file is deleted on every successful explicit Save.

#### C2. Material Design 3 Theme System

> ⚠️ **Important — Android 10 (API 29) test device note:**
> `dynamicColorScheme()` (wallpaper-based color extraction) requires **Android 12 / API 31+**.
> Since the Sony Xperia SO-51A runs **Android 10**, it will always use the **static fallback palette**.
> This is expected and fully supported — design a beautiful static Material 3 palette as the default,
> and dynamic color becomes a bonus enhancement for users on Android 12+.

**Theme implementation pattern:**
```kotlin
@Composable
fun QuillTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            // Android 12+ only — dynamic wallpaper colors
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context)
            else dynamicLightColorScheme(context)
        }
        else -> {
            // Android 10 & 11 — use our custom static palette (always used on test device)
            if (darkTheme) QuillDarkColorScheme
            else QuillLightColorScheme
        }
    }
    MaterialTheme(colorScheme = colorScheme, content = content)
}
```

- Define `QuillDarkColorScheme` and `QuillLightColorScheme` as custom `darkColorScheme()` / `lightColorScheme()` with a hand-picked developer-friendly palette (e.g. deep teal primary, amber accent).
- Support **Dark / Light / System** mode toggle in Settings.
- Typography: Use `JetBrains Mono` font for editor area; `Inter` for UI chrome.

**Color token assignments:**

| Token              | Use                                          |
|--------------------|----------------------------------------------|
| `primary`          | Keywords, active selection, FAB              |
| `secondary`        | String literals, secondary actions           |
| `tertiary`         | Annotations, code decorators                 |
| `surface`          | Editor background                            |
| `surfaceVariant`   | Sidebar background, code block tint          |
| `onSurface`        | Default editor text                          |
| `error`            | Deleted lines in diff, error tokens          |

#### C3. Navigation & Layout Architecture

**Screens:**

| Screen                   | Route                     | Description                                  |
|--------------------------|---------------------------|----------------------------------------------|
| Editor (Main)            | `/editor`                 | Primary editing surface                      |
| Version History          | `/versions/{fileId}`      | Timeline + diff viewer                       |
| Diff Viewer              | `/diff/{fileId}/{v1}/{v2}`| Line-by-line comparison                      |
| Settings                 | `/settings`               | Theme, encoding, auto-save interval          |
| File Browser / Open      | `/open`                   | Recent files + file picker trigger           |

**Layout:**
```
┌─────────────────────────────────────────────────────┐
│  TopAppBar: [☰] Quill — filename.kt  [🔒][⋮ menu]  │
├─────────────────────────────────────────────────────┤
│ NavDrawer │                                         │
│ (left,    │         EDITOR AREA                     │
│ slide-in) │      (BasicTextField)                   │
│           │                                         │
│ [Recent   │                                         │
│  Files]   │                                         │
│           │                                         │
│ [History] │                                         │
├─────────────────────────────────────────────────────┤
│  Bottom Toolbar: [Undo][Redo][Find][Format][Preview]│
│  (disappears in full-screen mode)                   │
└─────────────────────────────────────────────────────┘
```

#### C4. Settings Screen

| Setting                 | Type             | Default       |
|-------------------------|------------------|---------------|
| Theme mode              | Radio (3 opts)   | System        |
| Dynamic color           | Toggle           | ON            |
| Editor font size        | Slider (10–24sp) | 14sp          |
| Word wrap               | Toggle           | ON            |
| Auto-save interval      | Dropdown         | 10 seconds    |
| File encoding           | Dropdown         | UTF-8         |
| Show line numbers       | Toggle           | ON (optional) |
| Markdown preview mode   | Radio            | Side-by-side  |

---

## 6. Team Roles & Responsibilities

| Member     | Module             | Primary Deliverables                                               |
|------------|--------------------|--------------------------------------------------------------------|
| Member 1   | A — Editor Engine  | File I/O, editor UI, syntax highlighting, undo/redo, find/replace  |
| Member 2   | B — Version Control| Delta engine (java-diff-utils), Room DB, diff UI, rollback, read-only |
| Member 3   | C — UX & Recovery  | Auto-save crash recovery, Material You theming, navigation, settings, Markdown preview |

> Each member must appear in the demo video presenting **their own module**.

---

## 7. Full Task List

### 🟦 Member 1 — Editor Engine

- [ ] **[M1-01]** Set up Android project in **Android Studio** (New Project → Empty Compose Activity, Kotlin, Min SDK API 29)
- [ ] **[M1-02]** Configure Gradle dependencies for all libraries (Kotlin DSL)
- [ ] **[M1-03]** Implement `FileRepository` with read/write to internal storage
- [ ] **[M1-04]** Build `EditorScreen` composable with `BasicTextField`
- [ ] **[M1-05]** Create `SyntaxHighlighter` interface and dispatcher logic
- [ ] **[M1-06]** Create `assets/kotlin_keywords.txt` and keyword loader
- [ ] **[M1-07]** Implement `KotlinHighlighter` using `AnnotatedString` + `SpanStyle`
- [ ] **[M1-08]** Implement `MarkdownHighlighter` using regex-based span application
- [ ] **[M1-09]** Build `UndoRedoManager` as an `ArrayDeque<TextFieldValue>` stack
- [ ] **[M1-10]** Wire Undo / Redo to toolbar buttons with `enabled` state
- [ ] **[M1-11]** Implement Find bar (animated slide-up from bottom)
- [ ] **[M1-12]** Implement Find & Replace bottom sheet
- [ ] **[M1-13]** Implement "New File" action (clear buffer + prompt for name)
- [ ] **[M1-14]** Implement "Open File" using `ActivityResultContracts.OpenDocument`
- [ ] **[M1-15]** Implement "Save" with encoding (UTF-8 default)
- [ ] **[M1-16]** Implement "Save As" dialog + path logic
- [ ] **[M1-17]** Implement word wrap toggle (horizontal scroll when OFF)
- [ ] **[M1-18]** (Optional) Integrate `ktfmt` for Kotlin code formatting button
- [ ] **[M1-19]** Write unit tests for `KotlinHighlighter` and `UndoRedoManager`
- [ ] **[M1-20]** Peer review Member 2's `VersionRepository` integration in Editor

---

### 🟨 Member 2 — Version Control System

- [ ] **[M2-01]** Design Room database schema (`FileEntity`, `VersionEntity`)
- [ ] **[M2-02]** Implement `AppDatabase` with Room + Hilt injection
- [ ] **[M2-03]** Implement `FileDao` and `VersionDao`
- [ ] **[M2-04]** Integrate `java-diff-utils` into `build.gradle`
- [ ] **[M2-05]** Implement `DeltaEngine`: `createPatch(oldText, newText): String`
- [ ] **[M2-06]** Implement `DeltaEngine`: `applyPatch(baseText, patchString): String`
- [ ] **[M2-07]** Implement `DeltaEngine`: `reconstructVersion(fileId, targetVersion): String` (chain apply)
- [ ] **[M2-08]** Implement `VersionRepository` with create/read/delete version ops
- [ ] **[M2-09]** Wire versioning into the Save flow (base write vs. patch store)
- [ ] **[M2-10]** Build `VersionHistoryScreen` composable (timeline `LazyColumn`)
- [ ] **[M2-11]** Build `DiffViewerScreen` composable (line-by-line colored diff)
- [ ] **[M2-12]** Implement "Restore to version N" action with confirmation dialog
- [ ] **[M2-13]** Implement version label rename (long-press → inline text field)
- [ ] **[M2-14]** Implement version delete with patch recomputation guard
- [ ] **[M2-15]** Implement `isReadOnly` flag toggle + editor lock UI
- [ ] **[M2-16]** Implement `RecentFilesDao` and sidebar binding
- [ ] **[M2-17]** Write unit tests for `DeltaEngine` (patch creation, apply, chain reconstruction)
- [ ] **[M2-18]** Write unit tests for `VersionRepository` flows
- [ ] **[M2-19]** Validate storage efficiency: confirm no full file duplication in DB

---

### 🟩 Member 3 — UX, Recovery & Preview

- [ ] **[M3-01]** Design and implement `QuillTheme` (Material 3 + dynamic color fallback)
- [ ] **[M3-02]** Set up `JetBrains Mono` and `Inter` custom fonts in Compose
- [ ] **[M3-03]** Build app navigation graph (Jetpack Navigation Compose)
- [ ] **[M3-04]** Build `NavigationDrawer` with file list + history shortcut
- [ ] **[M3-05]** Build `TopAppBar` with filename, lock icon, and overflow menu
- [ ] **[M3-06]** Build bottom `EditorToolbar` (Undo, Redo, Find, Format, Preview)
- [ ] **[M3-07]** Implement `CrashRecoveryManager` (coroutine-based 10s auto-save to `.tmp`)
- [ ] **[M3-08]** Implement recovery prompt dialog on app launch (Restore / Discard)
- [ ] **[M3-09]** Integrate `Markwon` for Markdown → HTML rendering
- [ ] **[M3-10]** Build `MarkdownPreviewPanel` composable using `AndroidView`
- [ ] **[M3-11]** Implement preview toggle (FAB or toolbar icon, animated transition)
- [ ] **[M3-12]** Build `SettingsScreen` with all settings from §5.C4
- [ ] **[M3-13]** Persist settings to `DataStore<Preferences>`
- [ ] **[M3-14]** Apply font size setting live to the editor `TextStyle`
- [ ] **[M3-15]** Implement Dark / Light / System theme mode switch
- [ ] **[M3-16]** Build `ContextualToolbar` (appears on text selection: Cut/Copy/Paste/Format)
- [ ] **[M3-17]** Polish: transitions, ripple effects, loading states, empty states
- [ ] **[M3-18]** Write UI tests for navigation flows
- [ ] **[M3-19]** Record and edit the demonstration video

---

### 🔁 Shared / Integration Tasks

- [ ] **[SH-01]** Define shared data models (`FileEntity`, `VersionEntity`, `RecentFile`)
- [ ] **[SH-02]** Agree on `EditorState` data class shared between ViewModels
- [ ] **[SH-03]** Integration: wire `VersionRepository` into `EditorViewModel.onSave()`
- [ ] **[SH-04]** Integration: wire `CrashRecoveryManager` into `EditorViewModel`
- [ ] **[SH-05]** Integration testing: full save → version → rollback flow
- [ ] **[SH-06]** Build signed APK and upload to GitHub Releases
- [ ] **[SH-07]** Write technical documentation report
- [ ] **[SH-08]** Final QA / Bug bash (test on at least 2 emulator configurations)
- [ ] **[SH-09]** Record demo video (all 3 members present)
- [ ] **[SH-10]** Submit GitHub link + APK link + report + video link

---

## 8. Sprint Timeline & Milestones

> Adjust dates to match your actual deadline. The plan below assumes ~4 weeks.

```
Week 1: Foundation & Setup
  ├── [All]   Set up GitHub repo, project structure, shared models
  ├── [M1]    File I/O + basic editor TextField working
  ├── [M2]    Room DB schema + DeltaEngine prototype
  └── [M3]    Theme system + navigation skeleton + DrawerLayout

Week 2: Core Features
  ├── [M1]    Syntax highlighting (Kotlin + Markdown)
  ├── [M1]    Undo/Redo manager
  ├── [M2]    Full versioning pipeline (save → patch → store)
  ├── [M2]    Version history screen
  ├── [M3]    Crash recovery auto-save engine
  └── [M3]    All screens wired to navigation

Week 3: Integration & Polish
  ├── [All]   Integration testing: editor ↔ versioning ↔ recovery
  ├── [M1]    Find & Replace, Save As, word wrap
  ├── [M2]    Diff viewer UI, rollback, read-only lock
  ├── [M3]    Markdown preview, settings screen, theme polish
  └── [All]   Bug fixes from integration

Week 4: Final Delivery
  ├── [All]   Final QA + edge case testing
  ├── [M1]    (Optional) Code formatter integration
  ├── [M2]    (Optional) Version label rename, swipe-to-delete
  ├── [M3]    Demo video recording + editing
  └── [All]   Build APK, write report, submit
```

### Key Milestones

| # | Milestone                                     | Target     |
|---|-----------------------------------------------|------------|
| 1 | Project skeleton + GitHub repo live           | End Week 1 |
| 2 | Editor types text with syntax highlighting    | Mid Week 2 |
| 3 | Save → version created → rollback works       | End Week 2 |
| 4 | Crash recovery + all screens navigable        | Mid Week 3 |
| 5 | Full integration passing + UI polished        | End Week 3 |
| 6 | APK built, report written, video recorded     | End Week 4 |

---

## 9. UI/UX Design Plan

### ⚠️ Sony Xperia SO-51A Display Notes

The Xperia 1 II (SO-51A) has a **21:9 tall aspect ratio** (6.5 inch). This means:
- The editor area gets **more vertical lines** of text visible — great for coding!
- Bottom toolbars should use `WindowInsets` padding to avoid the navigation bar.
- Avoid fixed-height layouts that assume a 16:9 screen; use `fillMaxHeight()` and `weight()` in Compose.
- Test the sidebar drawer on this tall screen — it should not feel cramped.

### Screen Wireframes (Text-based)

#### Main Editor Screen
```
┌─────────────────────────────────────────────┐
│ [≡]  Quill  ─  MainActivity.kt  [🔒] [⋮]   │  ← TopAppBar
├─────────────────────────────────────────────┤
│                                             │
│  fun main() {                               │
│      println("Hello World")  // comment     │
│  }                                          │
│                                             │
│  class MyApp : Application() {             │
│      override fun onCreate() {              │
│          super.onCreate()                   │
│      }                                      │
│  }                                          │
│                                             │
│                                             │
│                           [✏️ Preview FAB]  │
├─────────────────────────────────────────────┤
│  [↩️][↪️]  [🔍]  [{ }]  [👁️ Preview]       │  ← Bottom Toolbar
└─────────────────────────────────────────────┘
```

#### Navigation Drawer
```
┌───────────────────┐
│  📄 Quill          │
│  ─────────────    │
│  📂 Recent Files  │
│  ──────────────   │
│  › MainActivity.kt│
│  › README.md      │
│  › notes.txt      │
│  ─────────────    │
│  🕐 Version History│
│  ⚙️  Settings     │
└───────────────────┘
```

#### Version History Screen
```
┌─────────────────────────────────────────────┐
│ ← Version History — MainActivity.kt         │
├─────────────────────────────────────────────┤
│  ●─── v3  "After refactor"   Jul 10, 14:32  │
│  │    [View Diff ↕] [Restore ↩]             │
│  │                                          │
│  ●─── v2  "Auto-save"        Jul 10, 13:00  │
│  │    [View Diff ↕] [Restore ↩]             │
│  │                                          │
│  ●─── v1  "Initial"          Jul 10, 10:15  │
│       [Base version]                        │
└─────────────────────────────────────────────┘
```

#### Diff Viewer Screen
```
┌─────────────────────────────────────────────┐
│  ← Diff: v1 → v3                            │
├────────────────────┬────────────────────────┤
│ v1 (old)           │ v3 (new)               │
├────────────────────┼────────────────────────┤
│   fun main() {     │   fun main() {         │
│ ─ println("Hello") │ + println("Hi there!") │
│ ─ println("World") │                        │
│   }                │   }                    │
└────────────────────┴────────────────────────┘
  [Restore to v3]
```

---

## 10. Database Schema

### Table: `files`
```sql
CREATE TABLE files (
    id          INTEGER PRIMARY KEY AUTOINCREMENT,
    name        TEXT NOT NULL,
    path        TEXT NOT NULL UNIQUE,     -- internal storage path
    encoding    TEXT DEFAULT 'UTF-8',
    is_read_only INTEGER DEFAULT 0,       -- BOOLEAN (0/1)
    last_opened INTEGER,                  -- Unix timestamp
    created_at  INTEGER NOT NULL
);
```

### Table: `versions`
```sql
CREATE TABLE versions (
    id             INTEGER PRIMARY KEY AUTOINCREMENT,
    file_id        INTEGER NOT NULL,
    version_number INTEGER NOT NULL,
    label          TEXT,                  -- e.g. "v1", "After refactor"
    patch_text     TEXT,                  -- NULL for base version (v1)
    is_base        INTEGER DEFAULT 0,     -- BOOLEAN
    created_at     INTEGER NOT NULL,
    FOREIGN KEY (file_id) REFERENCES files(id) ON DELETE CASCADE
);
```

### Table: `recent_files`
```sql
CREATE TABLE recent_files (
    id          INTEGER PRIMARY KEY AUTOINCREMENT,
    file_id     INTEGER NOT NULL,
    opened_at   INTEGER NOT NULL,
    FOREIGN KEY (file_id) REFERENCES files(id) ON DELETE CASCADE
);
```

### Room Entities (Kotlin)

```kotlin
// FileEntity.kt
@Entity(tableName = "files")
data class FileEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val path: String,
    val encoding: String = "UTF-8",
    val isReadOnly: Boolean = false,
    val lastOpened: Long? = null,
    val createdAt: Long = System.currentTimeMillis()
)

// VersionEntity.kt
@Entity(tableName = "versions")
data class VersionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val fileId: Long,
    val versionNumber: Int,
    val label: String? = null,
    val patchText: String? = null,  // null = base version
    val isBase: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
```

---

## 11. Key Implementation Details

### 11.1 Syntax Highlighting (Performance-Critical)

The biggest UI challenge is making syntax highlighting fast enough to not lag the keyboard.

**Strategy:**
```kotlin
// Inside EditorViewModel
private val highlightDebounce = MutableStateFlow("")

init {
    viewModelScope.launch {
        highlightDebounce
            .debounce(150L)                    // wait 150ms after last keystroke
            .map { text ->
                withContext(Dispatchers.Default) {
                    highlighter.highlight(text) // runs off main thread
                }
            }
            .collect { annotated ->
                _highlightedText.value = annotated
            }
    }
}
```

**Keyword file loading:**
```kotlin
// assets/kotlin_keywords.txt (one per line):
// fun, val, var, class, object, interface, if, else, when, for, while...

fun loadKeywords(context: Context): Set<String> {
    return context.assets
        .open("kotlin_keywords.txt")
        .bufferedReader()
        .readLines()
        .filter { it.isNotBlank() }
        .toSet()
}
```

### 11.2 Delta Engine Core

```kotlin
object DeltaEngine {

    fun createPatch(originalText: String, revisedText: String): String {
        val originalLines = originalText.lines()
        val revisedLines = revisedText.lines()
        val patch = DiffUtils.diff(originalLines, revisedLines)
        val unifiedDiff = UnifiedDiffUtils.generateUnifiedDiff(
            "original", "revised", originalLines, patch, 3
        )
        return unifiedDiff.joinToString("\n")
    }

    fun applyPatch(originalText: String, patchString: String): String {
        val originalLines = originalText.lines()
        val diffLines = patchString.lines()
        val patch = UnifiedDiffUtils.parseUnifiedDiff(diffLines)
        val result = DiffUtils.patch(originalLines, patch)
        return result.joinToString("\n")
    }

    fun reconstructVersion(
        baseContent: String,
        patches: List<String>  // ordered from v1 → vN patches
    ): String {
        var content = baseContent
        for (patch in patches) {
            content = applyPatch(content, patch)
        }
        return content
    }
}
```

### 11.3 Crash Recovery Engine

```kotlin
class CrashRecoveryManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val cacheDir get() = File(context.cacheDir, "recovery").apply { mkdirs() }

    fun getTempFile(fileName: String) = File(cacheDir, "$fileName.tmp")

    fun saveRecovery(fileName: String, content: String) {
        getTempFile(fileName).writeText(content, Charsets.UTF_8)
    }

    fun hasRecovery(fileName: String) = getTempFile(fileName).exists()

    fun readRecovery(fileName: String): String =
        getTempFile(fileName).readText(Charsets.UTF_8)

    fun clearRecovery(fileName: String) {
        getTempFile(fileName).delete()
    }
}

// In EditorViewModel:
private fun startAutoSave() {
    viewModelScope.launch(Dispatchers.IO) {
        while (isActive) {
            delay(10_000L) // every 10 seconds
            val text = _editorState.value.text
            crashRecoveryManager.saveRecovery(currentFileName, text)
        }
    }
}
```

---

## 12. Libraries & Dependencies

Add to `app/build.gradle.kts` (and set `minSdk = 29` / `targetSdk = 35` in the `android {}` block):

```kotlin
android {
    compileSdk = 35
    defaultConfig {
        minSdk = 29          // Android 10 — matches Sony Xperia SO-51A test device
        targetSdk = 35       // Android 15
    }
}

dependencies {
    // Compose BOM
    val composeBom = platform("androidx.compose:compose-bom:2024.12.01")
    implementation(composeBom)

    // Core Compose
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.activity:activity-compose:1.9.3")

    // Navigation
    implementation("androidx.navigation:navigation-compose:2.8.4")

    // Room
    implementation("androidx.room:room-runtime:2.7.0")
    implementation("androidx.room:room-ktx:2.7.0")
    ksp("androidx.room:room-compiler:2.7.0")

    // Hilt (DI)
    implementation("com.google.dagger:hilt-android:2.52")
    ksp("com.google.dagger:hilt-compiler:2.52")
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")

    // DataStore
    implementation("androidx.datastore:datastore-preferences:1.1.1")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0")

    // Lifecycle + ViewModel
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")

    // Diff Engine (Version Control)
    implementation("io.github.java-diff-utils:java-diff-utils:4.12")

    // Markdown Rendering
    implementation("io.noties.markwon:core:4.6.2")
    implementation("io.noties.markwon:ext-tables:4.6.2")
    implementation("io.noties.markwon:syntax-highlight:4.6.2")

    // (Optional) Kotlin Code Formatter
    implementation("com.facebook.ktfmt:ktfmt:0.46")

    // Fonts (JetBrains Mono via Google Fonts)
    implementation("androidx.compose.ui:ui-text-google-fonts")

    // Testing
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.9.0")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
}
```

---

## 13. Critical Focus Areas

These are the areas most likely to make or break the project. Prioritize these above everything else.

### 🔴 Priority 1 — Must Get Right

#### 1. Delta Storage Correctness
The spec explicitly forbids duplicating the base file. Make sure:
- Version 1 is written to disk as a full file.
- Versions 2+ only store the patch string in Room, **not** the file content.
- Reconstruction (chain-apply from base) is thoroughly tested with edge cases (empty files, large files, single-character changes).

#### 2. Crash Recovery Reliability
The `.tmp` file **must** be written even if the user force-kills the app.
- Test by: writing some text → killing the app from the task manager → reopening and verifying the restore dialog appears.
- Make sure the auto-save coroutine is scoped correctly (not cancelled on recomposition).

#### 3. Syntax Highlighting Performance
If highlighting blocks the main thread, the keyboard will stutter.
- **Always** run the highlighter on `Dispatchers.Default`.
- **Always** debounce by at least 100–200ms.
- Test on a physical mid-range device, not just an emulator.

### 🟡 Priority 2 — Important for Full Marks

#### 4. Diff Viewer Accuracy
The visual diff screen must clearly show what changed between versions. Use distinct colors (green for added, red for removed) and test with real Kotlin code edits.

#### 5. Read-Only Lock
The spec calls this out explicitly. Make sure the editor is completely non-editable when `isReadOnly = true`, including keyboard dismissal.

#### 6. Rollback Integrity
When rolling back to version N, the reconstructed content must exactly match what was saved at version N. Write a unit test that creates 5 versions, rolls back to v3, and asserts the content matches the original v3 save.

### 🟢 Priority 3 — Polish & Bonus

#### 7. Markdown Preview
A working Markwon preview adds a lot of demo value. Even a basic toggle between raw Markdown and rendered preview is impressive.

#### 8. Material 3 Static Palette + Dynamic Color
Since your test device (Xperia SO-51A, Android 10 / API 29) does **not** support `dynamicColorScheme()`, your hand-crafted static `QuillDarkColorScheme` / `QuillLightColorScheme` needs to look polished and intentional — this will be what the examiner sees in the demo. The Dynamic Color path on API 31+ is a bonus. Suggested palette: deep indigo/teal primary, amber secondary, dark surface for night mode.

#### 9. Demo Video Quality
Plan the video script in advance. Each member should demo their specific module:
- M1: Type Kotlin code, watch highlighting, use find/replace, undo/redo.
- M2: Save multiple times, open version history, view diff, restore to earlier version.
- M3: Demonstrate crash recovery (kill and reopen), toggle dark mode, toggle Markdown preview.

---

## 14. Testing Strategy

### Unit Tests

| Component              | Test Cases                                                       |
|------------------------|------------------------------------------------------------------|
| `KotlinHighlighter`    | Keywords highlighted, strings colored, comments grayed          |
| `MarkdownHighlighter`  | Headings, bold, italic, code spans detected                     |
| `UndoRedoManager`      | Push, undo, redo, undo past empty stack                         |
| `DeltaEngine`          | Create patch, apply patch, chain-apply 5 versions               |
| `CrashRecoveryManager` | Save tmp, hasRecovery true, read matches, clear deletes file    |

### Integration Tests

| Scenario                                         | Expected Outcome                                         |
|--------------------------------------------------|----------------------------------------------------------|
| New file → type → save → check versions in DB    | 1 version row, `isBase = true`, no patch                 |
| Open file → edit → save → check versions in DB  | 2nd row with `patchText` non-null                        |
| Rollback to v1 → verify editor content           | Content matches v1 exactly                               |
| Kill app mid-edit → reopen → restore dialog      | Dialog appears, restoring loads the auto-saved content   |
| Toggle read-only → attempt edit                  | Editor is non-interactive, keyboard does not appear      |

### Manual QA Checklist

- [ ] App launches without crash on fresh install
- [ ] Creating 10+ versions does not significantly increase storage (only patches, not full copies)
- [ ] Rollback from v10 to v1 produces correct content
- [ ] Syntax highlighting works for `.kt` files
- [ ] Syntax highlighting works for `.md` files
- [ ] Auto-save fires within 10 seconds of last change
- [ ] Crash recovery dialog appears on relaunch after simulated kill
- [ ] Read-only mode fully blocks editing
- [ ] Dark and light themes both render correctly
- [ ] App runs on API 29 — Sony Xperia SO-51A (primary test device, Android 10)
- [ ] App runs on API 35 — Android 15 (emulator or secondary device)
- [ ] Layout looks correct on 21:9 aspect ratio (Xperia SO-51A screen — 6.5 inch, tall and narrow)

---

## 15. Submission Checklist

### Code & Build
- [ ] Source code hosted on GitHub (public or shared with instructor)
- [ ] README.md in repo with: setup instructions, architecture overview, and library attributions
- [ ] Signed APK uploaded to GitHub Releases (or shared link)
- [ ] App installs and runs correctly from the APK

### Technical Report
- [ ] How **syntax highlighting** was implemented (regex, span styles, keyword file)
- [ ] How **delta versioning** works (patch creation, storage, chain-apply reconstruction)
- [ ] How **crash recovery** works (auto-save interval, .tmp file, restore dialog)
- [ ] How **Markdown preview** was implemented (Markwon integration)
- [ ] How the **diff viewer** renders changes (java-diff-utils hunk parsing)
- [ ] Database schema diagram or description
- [ ] Architecture diagram (MVVM layers)
- [ ] Screenshots of key screens

### Demo Video (≤ 25 minutes)
- [ ] All three members appear on camera and introduce themselves
- [ ] Member 1 presents and demos: editor, file ops, syntax highlighting, undo/redo, find/replace
- [ ] Member 2 presents and demos: save → version created, version history, diff view, rollback, read-only
- [ ] Member 3 presents and demos: crash recovery (live kill & restore), theme switch, Markdown preview, settings
- [ ] Video is clear and audible
- [ ] Video link shared with submission

---

## 📎 Quick Reference — Important File Paths

| File/Path                             | Purpose                              |
|---------------------------------------|--------------------------------------|
| `assets/kotlin_keywords.txt`          | Kotlin keyword list for highlighter  |
| `internal_storage/<name>.txt`         | Actual file content (base version)   |
| `cache/recovery/<name>.tmp`           | Auto-save crash recovery buffer      |
| `Room DB: versions.patch_text`        | Delta patch strings for each version |
| `DataStore: preferences`              | Settings (theme, font size, etc.)    |

---

> **"Build things worth showing, and show the things you built."**
> — Good luck, and Yoroshiku Onegaishimasu! 🎌

---
*Document version 1.0 — IS2205 Quill Mini-Project*

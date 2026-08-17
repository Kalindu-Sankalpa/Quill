# 📄 Technical Documentation — Quill Mobile Editor
**Course:** IS2205 — Mobile Application Design and Development (Mini-Project)  
**App Name:** Quill — A Developer's Mobile Text & Code Editor  
**Target Platform:** Android (Native Kotlin + Jetpack Compose)  
**Min SDK:** API 29 (Android 10) | **Target SDK:** API 35 (Android 15)  
**Repository Architecture:** MVVM + Clean Repository Pattern (Manual DI)

---

## 🛠 Executive Summary

This technical documentation details the architectural design, algorithmic implementation, database schemas, and state management mechanics of **Quill**, a native Android mobile text and code editor built for developers and technical writers. 

Quill satisfies all core functional requirements of the IS2205 specification:
1. **Delta-Based Version Control System**: Non-duplicating incremental snapshot engine powered by `java-diff-utils` and Room SQLite persistence.
2. **Crash-Resilient Local History & Auto-Save**: Background coroutine buffer caching to prevent data loss.
3. **Advanced Syntax Highlighting**: Real-time Kotlin and Markdown tokenization via Compose `VisualTransformation`.
4. **Markdown Preview & Line-by-Line Diff Viewer**: High-performance UI rendering using Markwon and custom Compose `LazyColumn` diff components.

---

## 1. System Architecture & Component Design

Quill follows the **MVVM (Model-View-ViewModel)** pattern combined with a clean **Repository pattern**.

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                          UI LAYER (Jetpack Compose)                         │
│   ┌──────────────┐   ┌────────────────┐   ┌─────────────────────────────┐   │
│   │ EditorScreen │   │ QuillDrawer    │   │ VersionHistory / DiffViewer │   │
│   └──────┬───────┘   └───────┬────────┘   └──────────────┬──────────────┘   │
└──────────┼───────────────────┼───────────────────────────┼──────────────────┘
           │                   │ StateFlow                 │
┌──────────▼───────────────────▼───────────────────────────▼──────────────────┐
│                      VIEWMODEL / DOMAIN LAYER                               │
│  EditorViewModel                                                            │
│  ├── UndoRedoManager (In-Memory ArrayDeque Stack)                           │
│  ├── DeltaEngine (java-diff-utils Patch / Reconstruct)                      │
│  └── Syntax Highlighters (KotlinHighlighter, MarkdownHighlighter)           │
└──────────┬───────────────────┬───────────────────────────┬──────────────────┘
           │                   │                           │
┌──────────▼───────────────────▼───────────────────────────▼──────────────────┐
│                          REPOSITORY LAYER                                   │
│  FileRepository   │   VersionRepository   │   SettingsRepository            │
│  (Disk I/O)       │   (Room DAO Access)   │   (DataStore Preferences)       │
└──────────┬───────────────────┬───────────────────────────┬──────────────────┘
           │                   │                           │
┌──────────▼───────┐   ┌───────▼───────────┐   ┌───────────▼──────────────────┐
│ Internal Storage │   │ Room SQLite DB    │   │ Cache Recovery Directory     │
│ (Base .kt/.md)   │   │ (files, versions) │   │ (cache/recovery/*.tmp)       │
└──────────────────┘   └───────────────────┘   └──────────────────────────────┘
```

### Dependency Injection (`AppContainer`)
To eliminate third-party annotation processor fragility on Android Gradle Plugin 9 / Kotlin 2.2+, the app employs **Manual Dependency Injection** via `AppContainer` (`di/AppContainer.kt`). The container acts as an application-scoped singleton providing lazy instances of:
- `QuillDatabase` & DAOs (`FileDao`, `VersionDao`)
- `FileRepository`, `VersionRepository`, `SettingsRepository`
- `CrashRecoveryManager`

---

## 2. Storage Mechanics & File Lifecycle

### 2.1 File Storage Dual-Layer Strategy
Quill uses a hybrid storage model dividing text content and version metadata:
- **Base Content File**: Stored directly on app-internal storage (`/data/data/com.quill.editor/files/`).
- **Metadata Database**: Stored in a Room SQLite database (`QuillDatabase.kt`).

```sql
-- Files metadata table schema
CREATE TABLE files (
    id          INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
    name        TEXT NOT NULL,
    path        TEXT NOT NULL UNIQUE,
    encoding    TEXT NOT NULL DEFAULT 'UTF-8',
    isReadOnly  INTEGER NOT NULL DEFAULT 0,
    lastOpened  INTEGER,
    createdAt   INTEGER NOT NULL
);
```

### 2.2 File Lifecycle Operations
1. **New File**: Instantiates an in-memory buffer with default filename `Untitled.kt` / `Untitled.md`. Prompts for location and name upon first save.
2. **Open File**: Integrates with Android's System Storage Access Framework (SAF) via `ActivityResultContracts.OpenDocument()`. Imported files copy their initial contents into app-internal storage and register an entry in the `files` table.
3. **Save**: Writes the active buffer to internal storage and triggers the delta versioning pipeline.
4. **Read-Only Lock**: Controlled via `isReadOnly` column in Room. When enabled, `CodeEditor` disables input interactions (`enabled = false`) and displays a lock visual indicator in the top control dock.

---

## 3. Delta-Based Version Control Engine

### 3.1 Non-Duplicating Incremental Storage Algorithm
The spec mandates that version snapshots **must not duplicate full file contents**. Quill implements a unified delta patch pipeline powered by `io.github.java-diff-utils:java-diff-utils:4.12`.

```
Version 1 (BASE): Full text saved to internal storage file (.kt / .md)
                 Room entry: VersionEntity(fileId=1, versionNumber=1, patchText=null, isBase=true)

Version 2:       Diff(v1, v2) -> Unified Diff Patch String
                 Room entry: VersionEntity(fileId=1, versionNumber=2, patchText="@@ -1,3 +1,4 @@...", isBase=false)

Version 3:       Diff(v2, v3) -> Unified Diff Patch String
                 Room entry: VersionEntity(fileId=1, versionNumber=3, patchText="@@ -2,1 +2,3 @@...", isBase=false)
```

```kotlin
// VersionEntity.kt schema
@Entity(tableName = "versions")
data class VersionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val fileId: Long,
    val versionNumber: Int,
    val label: String? = null,
    val patchText: String? = null, // NULL for base version (v1)
    val isBase: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
```

### 3.2 Patch Creation & Applying (`DeltaEngine.kt`)
- **Patch Creation**:
  ```kotlin
  fun createPatch(originalText: String, revisedText: String): String {
      val originalLines = originalText.lines()
      val revisedLines = revisedText.lines()
      val patch = DiffUtils.diff(originalLines, revisedLines)
      val unifiedDiff = UnifiedDiffUtils.generateUnifiedDiff(
          "original", "revised", originalLines, patch, 3
      )
      return unifiedDiff.joinToString("\n")
  }
  ```
- **Chain-Apply Reconstruction Formula**:
  To reconstruct target version $V_N$ from base content $V_1$:
  $$V_N = \text{applyPatch}(\dots \text{applyPatch}(\text{applyPatch}(V_1, P_2), P_3)\dots, P_N)$$

  ```kotlin
  fun reconstructVersion(baseContent: String, patches: List<String>): String {
      var currentContent = baseContent
      for (patch in patches) {
          if (!patch.isNullOrBlank()) {
              currentContent = applyPatch(currentContent, patch)
          }
      }
      return currentContent
  }
  ```

---

## 4. Advanced Syntax Highlighting & Rendering

### 4.1 Tokenization via Compose `VisualTransformation`
Instead of heavy background thread state emission which introduces keystroke lag, Quill utilizes Jetpack Compose's `VisualTransformation` to execute single-pass regex tokenization synchronously during layout rendering in `CodeEditor.kt`.

### 4.2 Kotlin Syntax Rules (`KotlinHighlighter.kt`)
Keywords are dynamically loaded from `assets/kotlin_keywords.txt` into an in-memory `Set<String>`.

| Token Type | Matching Logic / Regex | Style Applied |
|---|---|---|
| **Keywords** | Word boundary match against `kotlin_keywords.txt` | Bold, Teal `#00897B` |
| **Strings** | `"[^"\\]*(?:\\.[^"\\]*)*"` | Secondary Green `#4CAF50` |
| **Comments** | `//.*` or `/\*[\s\S]*?\*/` | Italic Gray `#9E9E9E` |
| **Annotations** | `@[A-Za-z0-9_]+` | Tertiary Amber `#FFB300` |
| **Numbers** | `\b\d+(\.\d+)?\b` | Orange Accent `#FB8C00` |

### 4.3 Markdown Syntax Rules (`MarkdownHighlighter.kt`)
- Headings (`#`, `##`, `###`): Bold + enlarged font scale
- Bold (`**text**`): `FontWeight.Bold`
- Italic (`*text*`): `FontStyle.Italic`
- Inline Code (`` `code` ``): Monospace font family with background surface tint
- Links (`[title](url)`): Primary color + Underline

---

## 5. Preview & Comparison Mechanics

### 5.1 Markdown Preview Panel (`MarkdownPreview.kt`)
Quill integrates the open-source `io.noties.markwon:core:4.6.2` library for rendering Markdown elements.
- Embedded inside Compose using `AndroidView(factory = { TextView(it) }, update = { markwon.setMarkdown(it, text) })`.
- Accessible via a full-screen preview view mode toggled directly from the bottom control dock.

### 5.2 Line-by-Line Visual Diff Viewer (`DiffViewerScreen.kt`)
- Generates line delta hunks between any two selected versions $V_A$ and $V_B$.
- Rendered using Compose `LazyColumn`:
  - **Inserted Lines**: Light green background chip (`#1B5E20` tint) with `+` prefix.
  - **Deleted Lines**: Light red background chip (`#B71C1C` tint) with `-` prefix and strikethrough.
  - **Unchanged Lines**: Standard muted theme typography.

---

## 6. Local History, Fault Tolerance & Crash Recovery

### 6.1 Periodic Background Auto-Save Loop
To guarantee fault tolerance against sudden crashes, force kills, or OS memory reclaim:
1. `EditorViewModel` maintains an active IO Coroutine job firing every $N$ seconds (configurable in settings, default 10 seconds).
2. The active buffer text is serialized to app cache storage: `/data/data/com.quill.editor/cache/recovery/<filename>.tmp`.

### 6.2 Application Launch Recovery Check (`CrashRecoveryManager.kt`)
Upon app startup:
1. The app checks if a matching `.tmp` buffer exists for the last opened file.
2. If detected, an explicit recovery prompt dialog is presented to the user:
   - **Restore**: Replaces active editor buffer with `.tmp` contents.
   - **Discard**: Deletes `.tmp` cache file.
3. Successful user-initiated saves explicitly clean up the active `.tmp` file.

### 6.3 In-Memory Session Undo / Redo (`UndoRedoManager.kt`)
- Maintains an bounded `ArrayDeque<String>` stack capturing editor states up to a maximum capacity (e.g., 50 states).
- Provides instant $O(1)$ stack pushing, undoing, and redoing without triggering disk writes.

---

## 7. Testing & Verification Summary

The project includes 17 pure JVM unit tests passing in `./gradlew testDebugUnitTest`:

| Test Suite | Test Count | Key Scenarios Verified |
|---|---|---|
| **`DeltaEngineTest`** | 7 | Single char diff, multi-line diff, patch apply round-trip, 5-version chain reconstruction, rollback integrity |
| **`UndoRedoManagerTest`** | 5 | Push state, undo, redo, empty stack resilience, max capacity truncation |
| **`HighlighterTest`** | 5 | Kotlin keyword detection, string literal protection against false keywords, Markdown heading styling |

---

## 8. Group Member Roles & Responsibilities

| Team Member          | Assigned Module | Key Delivered Components |
|----------------------|---|---|
| **Chaluka Kavinka**  | **Module A — Editor Engine & Syntax Highlighting** | `CodeEditor`, `KotlinHighlighter`, `MarkdownHighlighter`, `UndoRedoManager`, SAF File I/O |
| **Tehan Jayaweera**  | **Module B — Delta Version Control System** | `DeltaEngine`, Room DB entities (`FileEntity`, `VersionEntity`), `VersionRepository`, `DiffViewerScreen` |
| **Kalindu Sankalpa** | **Module C — UX, Recovery & Markdown Preview** | `CrashRecoveryManager`, `MarkdownPreview`, `SettingsScreen`, `DataStore` preferences, Material 3 theming |

---
*Document Version 1.0 — Produced for IS2205 Mini-Project Submission*

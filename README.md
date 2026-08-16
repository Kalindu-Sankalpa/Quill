# ✒ Quill — A Developer's Mobile Text & Code Editor

Quill is a native Android text/code editor built for the **IS2205** mini-project. It combines a
focused Markdown/Kotlin editing surface, syntax highlighting, a **delta-based version-control
system**, and a **crash-resilient auto-save engine**, all wrapped in Material 3.

> Status: **builds and runs**. `./gradlew assembleDebug` produces an installable APK; 17 JVM unit
> tests pass (`./gradlew testDebugUnitTest`).

---

## Features implemented

| Area | What's included |
|------|-----------------|
| **Editor** | `BasicTextField` surface, word-wrap toggle (horizontal scroll when off), optional line-number gutter, adjustable font size |
| **Markdown toolbar** | Scrollable formatting strip — H1/H2/H3, bold, italic, strikethrough, inline code, bullet/numbered lists, quote, link — that wraps the selection or prefixes the line |
| **Syntax highlighting** | Kotlin (keywords from `assets/kotlin_keywords.txt`, strings, comments, numbers, annotations) and Markdown (headings, bold, italic, inline/fenced code, links) via a single-pass tokenizer + `VisualTransformation` |
| **Undo / Redo** | Bounded in-memory `ArrayDeque` stack (`UndoRedoManager`) |
| **Find / Replace** | Match counter, find-next, replace-all |
| **File ops** | New, Save, Save As, Open (system file picker / SAF import), read-only lock |
| **Version control** | Delta engine (`java-diff-utils`): v1 = base on disk, v2+ = unified-diff patches in Room. Chain-apply reconstruction, restore, rename, delete-newest |
| **Diff viewer** | Colored line-by-line diff (green insert / red delete) |
| **Crash recovery** | Coroutine auto-save to `cache/recovery/*.tmp` every N seconds; restore/discard dialog on launch |
| **Theming** | Material 3 static teal/amber palette + dynamic color on Android 12+; dark / light / system |
| **Markdown preview** | Markwon-rendered `TextView` inside Compose (`AndroidView`), opened **full-screen** (toggle from the dock) |
| **Settings** | Theme, dynamic color, font size, word wrap, line numbers, auto-save interval, encoding — persisted in DataStore |
| **Navigation** | Slide-out drawer (recent files) + Navigation-Compose graph |
| **UI shell** | Edge-to-edge with a themed (contrast-off) system navigation bar; Telegram-style **floating circular top controls** and a **floating capsule dock** drawn over the editor |

---

## Architecture (MVVM + Repository, manual DI)

```
UI (Compose)            EditorScreen · VersionHistory · DiffViewer · Settings · Drawer
      │  StateFlow
ViewModel               EditorViewModel
      │
Repository              FileRepository · VersionRepository · SettingsRepository · CrashRecoveryManager
      │
Data / Domain           Room (files, versions) · Internal storage · DataStore · DeltaEngine · Highlighters
```

- **`domain/`** — pure, framework-free logic (`DeltaEngine`, `UndoRedoManager`, highlighter
  tokenizers). This is what the unit tests target.
- **`data/`** — Room entities/DAOs, repositories, DataStore settings, crash recovery.
- **`ui/`** — Compose screens, the `EditorViewModel`, theme.
- **`di/AppContainer`** — lightweight manual dependency injection (see note below).

---

## Tech stack

| Layer | Choice |
|-------|--------|
| Language / UI | Kotlin 2.2.10, Jetpack Compose (Material 3, BOM 2024.12.01) |
| Build | Gradle 9.4.1 (wrapper), **Android Gradle Plugin 9.2.1** |
| Min / Target / Compile SDK | 29 / 35 / 35 |
| Persistence | Room 2.7.1 (KSP), DataStore Preferences, app-internal storage |
| Versioning | `io.github.java-diff-utils:java-diff-utils:4.12` |
| Markdown | `io.noties.markwon:core / ext-tables / ext-strikethrough:4.6.2` |
| Async | Kotlin Coroutines + Flow |

### Toolchain notes (important)

This project targets the **AGP 9 / built-in-Kotlin** toolchain:

- **AGP 9 compiles Kotlin itself.** We do **not** apply `org.jetbrains.kotlin.android`; only the
  Compose compiler plugin and KSP are applied (both pinned to Kotlin 2.2.10).
- `gradle.properties` sets `android.disallowKotlinSourceSets=false` so KSP (2.2.10-2.0.2) can
  register Room's generated sources under built-in Kotlin.
- `jvmTarget` defaults from `compileOptions.targetCompatibility` (Java 11).

---

## Deviations from the original project plan

These were deliberate, to keep the build robust on the current toolchain — each is easy to swap
back:

1. **Manual DI (`AppContainer`) instead of Hilt.** Hilt's Gradle plugin is the biggest
   compatibility risk on AGP 9 / Kotlin 2.2 and isn't required to demonstrate clean MVVM.
2. **Recent files derived from the `files` table** (ordered by `lastOpened`) rather than a separate
   `recent_files` table — avoids a redundant table.
3. **System `Monospace`/sans fonts** instead of downloadable JetBrains Mono / Inter — removes a
   runtime font-download dependency.
4. **Synchronous highlighting** via `VisualTransformation` (efficient single-pass regex) rather than
   a debounced background `StateFlow`. Fine for typical file sizes; the debounce approach is a
   documented future enhancement.
5. Markwon **syntax-highlight (Prism4j)** extension and the optional **ktfmt** formatter are not
   included.

---

## Building & running

### In Android Studio
Open the project and let it sync (it uses its bundled JDK). Run the `app` configuration.

### Command line
The build needs a JDK 17+ (Android Studio bundles one as its "JBR"):

```bash
export JAVA_HOME=/path/to/android-studio/jbr      # e.g. /snap/android-studio/current/jbr
./gradlew assembleDebug        # -> app/build/outputs/apk/debug/app-debug.apk
./gradlew testDebugUnitTest    # run the 17 unit tests
./gradlew installDebug         # install on a connected device / emulator
```

`local.properties` must point `sdk.dir` at your Android SDK.

---

## Testing

`./gradlew testDebugUnitTest` runs pure-JVM tests for the critical logic:

- **`DeltaEngineTest`** (7) — patch round-trip, empty/edge files, single-char change, **5-version
  chain reconstruction**, and **rollback integrity** (reconstruct v3 exactly).
- **`UndoRedoManagerTest`** (5) — undo/redo, empty-stack, redo invalidation, capacity bound.
- **`HighlighterTest`** (5) — Kotlin keyword/string/comment/number/annotation detection (keywords
  inside strings are *not* mis-highlighted) and Markdown tokens.

---

## Library attributions

- [java-diff-utils](https://github.com/java-diff-utils/java-diff-utils) — Apache-2.0
- [Markwon](https://github.com/noties/Markwon) — Apache-2.0
- AndroidX Jetpack (Compose, Room, DataStore, Navigation, Lifecycle) — Apache-2.0

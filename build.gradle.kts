// Top-level build file — plugin versions declared here, applied in :app.
// Toolchain chosen to match this machine's cached Gradle 9.4.1 + JBR 21.
// AGP 9 ships built-in Kotlin support (enabled by default), so we do NOT apply
// org.jetbrains.kotlin.android — AGP compiles Kotlin itself. We still apply the
// Compose compiler plugin and KSP, both pinned to the same Kotlin version (2.2.10)
// that AGP 9.2.1's built-in Kotlin uses.
plugins {
    id("com.android.application") version "9.3.1" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.2.10" apply false
    id("com.google.devtools.ksp") version "2.3.6" apply false
}

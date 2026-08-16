package com.quill.editor.ui.theme

import androidx.compose.ui.graphics.Color

// ---- Brand palette: deep teal primary + warm amber accent (developer-friendly) ----

// Light scheme roles
val md_primary = Color(0xFF006A6B)
val md_onPrimary = Color(0xFFFFFFFF)
val md_primaryContainer = Color(0xFF6FF6F8)
val md_onPrimaryContainer = Color(0xFF002020)

val md_secondary = Color(0xFF7D5700)
val md_onSecondary = Color(0xFFFFFFFF)
val md_secondaryContainer = Color(0xFFFFDEA6)
val md_onSecondaryContainer = Color(0xFF271900)

val md_tertiary = Color(0xFF525E7D)
val md_onTertiary = Color(0xFFFFFFFF)
val md_tertiaryContainer = Color(0xFFDAE2FF)
val md_onTertiaryContainer = Color(0xFF0E1B37)

val md_error = Color(0xFFBA1A1A)
val md_onError = Color(0xFFFFFFFF)
val md_errorContainer = Color(0xFFFFDAD6)
val md_onErrorContainer = Color(0xFF410002)

val md_background = Color(0xFFFAFDFC)
val md_onBackground = Color(0xFF191C1C)
val md_surface = Color(0xFFFAFDFC)
val md_onSurface = Color(0xFF191C1C)
val md_surfaceVariant = Color(0xFFDAE5E3)
val md_onSurfaceVariant = Color(0xFF3F4948)
val md_outline = Color(0xFF6F7978)
val md_surfaceContainerHigh = Color(0xFFE7ECEB)
val md_surfaceContainerHighest = Color(0xFFE1E6E5)

// Dark scheme roles
val md_primary_dark = Color(0xFF4CDADB)
val md_onPrimary_dark = Color(0xFF003737)
val md_primaryContainer_dark = Color(0xFF004F50)
val md_onPrimaryContainer_dark = Color(0xFF6FF6F8)

val md_secondary_dark = Color(0xFFF6BD48)
val md_onSecondary_dark = Color(0xFF422C00)
val md_secondaryContainer_dark = Color(0xFF5E4100)
val md_onSecondaryContainer_dark = Color(0xFFFFDEA6)

val md_tertiary_dark = Color(0xFFBAC6EA)
val md_onTertiary_dark = Color(0xFF24304D)
val md_tertiaryContainer_dark = Color(0xFF3B4664)
val md_onTertiaryContainer_dark = Color(0xFFDAE2FF)

val md_error_dark = Color(0xFFFFB4AB)
val md_onError_dark = Color(0xFF690005)
val md_errorContainer_dark = Color(0xFF93000A)
val md_onErrorContainer_dark = Color(0xFFFFDAD6)

val md_background_dark = Color(0xFF191C1C)
val md_onBackground_dark = Color(0xFFE0E3E2)
val md_surface_dark = Color(0xFF101414)
val md_onSurface_dark = Color(0xFFE0E3E2)
val md_surfaceVariant_dark = Color(0xFF3F4948)
val md_onSurfaceVariant_dark = Color(0xFFBEC9C7)
val md_outline_dark = Color(0xFF899392)
val md_surfaceContainerHigh_dark = Color(0xFF272B2B)
val md_surfaceContainerHighest_dark = Color(0xFF323736)

// ---- Syntax-highlighting token colors (shared by both schemes for consistency) ----
object SyntaxColors {
    val keyword = Color(0xFF00838F)      // teal
    val keywordDark = Color(0xFF4DD0E1)
    val string = Color(0xFF2E7D32)       // green
    val stringDark = Color(0xFF81C784)
    val comment = Color(0xFF9E9E9E)      // muted gray
    val commentDark = Color(0xFF7A8785)
    val annotation = Color(0xFF7E57C2)   // purple
    val annotationDark = Color(0xFFB39DDB)
    val number = Color(0xFFEF6C00)       // orange
    val numberDark = Color(0xFFFFB74D)
    val heading = Color(0xFF006A6B)
    val headingDark = Color(0xFF4CDADB)
    val link = Color(0xFF1565C0)
    val linkDark = Color(0xFF90CAF9)
}

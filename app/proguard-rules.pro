# Default ProGuard/R8 rules for Quill.
# Minification is disabled for release in this project, so these are mostly a placeholder.

# Keep Room generated code
-keep class androidx.room.** { *; }

# java-diff-utils is pure Java reflection-free; nothing special needed.

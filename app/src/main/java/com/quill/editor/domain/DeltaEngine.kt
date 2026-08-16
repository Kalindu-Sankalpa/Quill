package com.quill.editor.domain

import com.github.difflib.DiffUtils
import com.github.difflib.UnifiedDiffUtils
import com.github.difflib.patch.DeltaType
import com.github.difflib.patch.Patch

/** A single rendered diff line and how it changed between two versions. */
enum class DiffLineType { EQUAL, INSERT, DELETE }

data class DiffLine(
    val type: DiffLineType,
    val text: String,
    val oldLineNumber: Int?,
    val newLineNumber: Int?,
)

/**
 * Delta / patch engine backing the version-control feature.
 *
 * Storage model (see project plan §B1): version 1 is the full base content on disk; every later
 * version stores only the *unified diff* from the previous version. Reconstructing version N means
 * starting from the base and chain-applying patches 2..N. This keeps the database free of full-file
 * duplication.
 */
object DeltaEngine {

    private const val CONTEXT_SIZE = 3

    /** Build a unified-diff patch string that turns [originalText] into [revisedText]. */
    fun createPatch(originalText: String, revisedText: String): String {
        val originalLines = originalText.lines()
        val revisedLines = revisedText.lines()
        val patch: Patch<String> = DiffUtils.diff(originalLines, revisedLines)
        val unified = UnifiedDiffUtils.generateUnifiedDiff(
            "previous",
            "current",
            originalLines,
            patch,
            CONTEXT_SIZE,
        )
        return unified.joinToString("\n")
    }

    /** Apply a unified-diff [patchString] to [originalText], returning the revised text. */
    fun applyPatch(originalText: String, patchString: String): String {
        if (patchString.isBlank()) return originalText
        val originalLines = originalText.lines()
        val parsed: Patch<String> = UnifiedDiffUtils.parseUnifiedDiff(patchString.lines())
        val result = parsed.applyTo(originalLines)
        return result.joinToString("\n")
    }

    /** Start from [baseText] and chain-apply [orderedPatches] (v2..vN) to reconstruct a version. */
    fun reconstruct(baseText: String, orderedPatches: List<String>): String {
        var content = baseText
        for (patch in orderedPatches) {
            content = applyPatch(content, patch)
        }
        return content
    }

    /**
     * Produce a line-by-line diff between [oldText] and [newText] for the diff viewer.
     * EQUAL lines appear once; changed regions emit DELETE lines then INSERT lines.
     */
    fun computeDiffLines(oldText: String, newText: String): List<DiffLine> {
        val oldLines = oldText.lines()
        val newLines = newText.lines()
        val patch = DiffUtils.diff(oldLines, newLines)

        val result = ArrayList<DiffLine>()
        var oldIndex = 0
        var newIndex = 0

        for (delta in patch.deltas.sortedBy { it.source.position }) {
            val srcPos = delta.source.position
            // Emit unchanged lines up to this delta.
            while (oldIndex < srcPos) {
                result += DiffLine(DiffLineType.EQUAL, oldLines[oldIndex], oldIndex + 1, newIndex + 1)
                oldIndex++
                newIndex++
            }
            when (delta.type) {
                DeltaType.DELETE -> {
                    delta.source.lines.forEach {
                        result += DiffLine(DiffLineType.DELETE, it, oldIndex + 1, null)
                        oldIndex++
                    }
                }

                DeltaType.INSERT -> {
                    delta.target.lines.forEach {
                        result += DiffLine(DiffLineType.INSERT, it, null, newIndex + 1)
                        newIndex++
                    }
                }

                DeltaType.CHANGE -> {
                    delta.source.lines.forEach {
                        result += DiffLine(DiffLineType.DELETE, it, oldIndex + 1, null)
                        oldIndex++
                    }
                    delta.target.lines.forEach {
                        result += DiffLine(DiffLineType.INSERT, it, null, newIndex + 1)
                        newIndex++
                    }
                }

                DeltaType.EQUAL -> {
                    delta.source.lines.forEach {
                        result += DiffLine(DiffLineType.EQUAL, it, oldIndex + 1, newIndex + 1)
                        oldIndex++
                        newIndex++
                    }
                }
            }
        }
        // Trailing unchanged lines.
        while (oldIndex < oldLines.size) {
            result += DiffLine(DiffLineType.EQUAL, oldLines[oldIndex], oldIndex + 1, newIndex + 1)
            oldIndex++
            newIndex++
        }
        return result
    }
}

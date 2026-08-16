package com.quill.editor.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** Tests the delta/patch engine — the storage-critical part of the version-control feature. */
class DeltaEngineTest {

    @Test
    fun createThenApply_roundTrips() {
        val original = "fun main() {\n    println(\"Hello\")\n}"
        val revised = "fun main() {\n    println(\"Hello, world\")\n}"

        val patch = DeltaEngine.createPatch(original, revised)
        val result = DeltaEngine.applyPatch(original, patch)

        assertEquals(revised, result)
    }

    @Test
    fun applyEmptyPatch_returnsOriginal() {
        val original = "unchanged\ncontent"
        val patch = DeltaEngine.createPatch(original, original)
        assertEquals(original, DeltaEngine.applyPatch(original, patch))
    }

    @Test
    fun reconstruct_chainOfFiveVersions_matchesEachSave() {
        val versions = listOf(
            "v1 line",
            "v1 line\nv2 added",
            "v1 line changed\nv2 added",
            "v1 line changed\nv2 added\nv3 added",
            "v1 line changed\nv2 added\nv3 added\nv4 added",
        )

        // Build forward patches v(n-1) -> v(n), exactly like the Save flow.
        val patches = (1 until versions.size).map { i ->
            DeltaEngine.createPatch(versions[i - 1], versions[i])
        }

        // Reconstruct every version from the base + first k patches.
        versions.forEachIndexed { index, expected ->
            val reconstructed = DeltaEngine.reconstruct(versions[0], patches.take(index))
            assertEquals("version ${index + 1} should reconstruct exactly", expected, reconstructed)
        }
    }

    @Test
    fun rollbackIntegrity_reconstructV3_matchesOriginalV3Save() {
        // Priority-1 scenario from the plan: create 5 versions, roll back to v3, assert exact match.
        val v1 = "alpha\nbeta\ngamma"
        val v2 = "alpha\nbeta changed\ngamma"
        val v3 = "alpha\nbeta changed\ngamma\ndelta"
        val v4 = "alpha\nbeta changed\ngamma\ndelta\nepsilon"
        val v5 = "alpha\nbeta changed\ngamma\ndelta\nepsilon\nzeta"

        val p2 = DeltaEngine.createPatch(v1, v2)
        val p3 = DeltaEngine.createPatch(v2, v3)
        val p4 = DeltaEngine.createPatch(v3, v4)
        val p5 = DeltaEngine.createPatch(v4, v5)

        val reconstructedV3 = DeltaEngine.reconstruct(v1, listOf(p2, p3))
        assertEquals(v3, reconstructedV3)

        // And the full chain still yields v5.
        assertEquals(v5, DeltaEngine.reconstruct(v1, listOf(p2, p3, p4, p5)))
    }

    @Test
    fun emptyBaseFile_edgeCase() {
        val base = ""
        val next = "first line added"
        val patch = DeltaEngine.createPatch(base, next)
        assertEquals(next, DeltaEngine.applyPatch(base, patch))
    }

    @Test
    fun singleCharacterChange() {
        val a = "val x = 1"
        val b = "val x = 2"
        val patch = DeltaEngine.createPatch(a, b)
        assertEquals(b, DeltaEngine.applyPatch(a, patch))
    }

    @Test
    fun computeDiffLines_marksInsertAndDelete() {
        val old = "keep\nremove me\nkeep2"
        val new = "keep\nkeep2\nadded"
        val diff = DeltaEngine.computeDiffLines(old, new)

        assertTrue(diff.any { it.type == DiffLineType.DELETE && it.text == "remove me" })
        assertTrue(diff.any { it.type == DiffLineType.INSERT && it.text == "added" })
        assertTrue(diff.any { it.type == DiffLineType.EQUAL && it.text == "keep" })
    }
}

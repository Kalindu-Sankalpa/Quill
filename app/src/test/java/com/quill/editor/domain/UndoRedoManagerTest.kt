package com.quill.editor.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class UndoRedoManagerTest {

    @Test
    fun undo_returnsPreviousState() {
        val manager = UndoRedoManager<String>()
        manager.record("a")
        manager.record("ab")

        assertTrue(manager.canUndo)
        assertEquals("ab", manager.undo("abc"))
        assertEquals("a", manager.undo("ab"))
    }

    @Test
    fun undoPastEmpty_returnsNull() {
        val manager = UndoRedoManager<String>()
        assertNull(manager.undo("current"))
        assertFalse(manager.canUndo)
    }

    @Test
    fun redo_reappliesUndoneState() {
        val manager = UndoRedoManager<String>()
        manager.record("a")
        val undone = manager.undo("ab")
        assertEquals("a", undone)

        assertTrue(manager.canRedo)
        assertEquals("ab", manager.redo("a"))
    }

    @Test
    fun recordingClearsRedoStack() {
        val manager = UndoRedoManager<String>()
        manager.record("a")
        manager.undo("ab")
        assertTrue(manager.canRedo)

        manager.record("aX")
        assertFalse("recording a new edit invalidates redo history", manager.canRedo)
    }

    @Test
    fun capacityIsBounded() {
        val manager = UndoRedoManager<Int>(capacity = 3)
        repeat(10) { manager.record(it) }

        // Only the last 3 recorded states remain undoable.
        var count = 0
        var value = 100
        while (manager.canUndo) {
            value = manager.undo(value)!!
            count++
        }
        assertEquals(3, count)
    }
}

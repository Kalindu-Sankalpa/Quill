package com.quill.editor.domain

/**
 * A generic bounded undo/redo stack.
 *
 * Kept generic (not tied to Compose's TextFieldValue) so the core logic is plain-JVM
 * unit-testable. The editor uses `UndoRedoManager<TextFieldValue>`.
 */
class UndoRedoManager<T>(private val capacity: Int = 100) {

    private val undoStack = ArrayDeque<T>()
    private val redoStack = ArrayDeque<T>()

    val canUndo: Boolean get() = undoStack.isNotEmpty()
    val canRedo: Boolean get() = redoStack.isNotEmpty()

    /** Record [previous] as an undoable state. Clears the redo history (new edit branch). */
    fun record(previous: T) {
        undoStack.addLast(previous)
        while (undoStack.size > capacity) undoStack.removeFirst()
        redoStack.clear()
    }

    /** Move one step back. Pushes [current] onto redo and returns the restored state, or null. */
    fun undo(current: T): T? {
        val previous = undoStack.removeLastOrNull() ?: return null
        redoStack.addLast(current)
        return previous
    }

    /** Move one step forward. Pushes [current] onto undo and returns the restored state, or null. */
    fun redo(current: T): T? {
        val next = redoStack.removeLastOrNull() ?: return null
        undoStack.addLast(current)
        return next
    }

    fun clear() {
        undoStack.clear()
        redoStack.clear()
    }
}

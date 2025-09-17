package com.example.dancetrainer.logic

import com.example.dancetrainer.data.Move
import kotlin.random.Random

class DanceConductor {
    var currentSequence: List<Move> = emptyList()
        private set

    fun reset() {
        currentSequence = emptyList()
    }

    /** Returns a random move from a provided list; null if none. */
    fun nextSingleMoveFrom(moves: List<Move>): Move? {
        if (moves.isEmpty()) return null
        return moves[Random.nextInt(moves.size)]
    }

    fun nextSingleMove(): Move? = null // kept for backward calls; prefer nextSingleMoveFrom()

    /** Generate a random sequence [3..10] from provided moves; empty if none. */
    fun generateRandomSequenceFrom(moves: List<Move>, length: Int? = null) {
        if (moves.isEmpty()) { currentSequence = emptyList(); return }
        val n = length ?: (3..10).random()
        currentSequence = List(n) { moves.random() }
    }

    fun generateRandomSequence() { currentSequence = emptyList() }

    /** Pick a stored sequence: for now stub as empty; real impl should load from storage. */
    fun pickStoredSequence() { currentSequence = emptyList() }
}

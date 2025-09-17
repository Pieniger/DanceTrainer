package com.example.dancetrainer.logic

import com.example.dancetrainer.data.*
import kotlin.random.Random

class DanceConductor {
    var currentSequence: List<Move> = emptyList()
        private set

    fun reset() {
        currentSequence = emptyList()
    }

    fun nextSingleMove(): Move? {
        // pick a random move that has at least one connection
        return sampleMoves().randomOrNull()
    }

    fun generateRandomSequence() {
        val moves = sampleMoves()
        val length = Random.nextInt(3, 10)
        currentSequence = List(length) { moves.random() }
    }

    fun pickStoredSequence() {
        // load from storage (stub for now, needs context in actual call)
        currentSequence = listOf(
            Move("1", "Sample Stored Move 1"),
            Move("2", "Sample Stored Move 2")
        )
    }

    private fun sampleMoves(): List<Move> {
        return listOf(
            Move("1", "Step Left"),
            Move("2", "Step Right"),
            Move("3", "Spin"),
            Move("4", "Jump")
        )
    }
}

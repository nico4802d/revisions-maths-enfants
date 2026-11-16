package com.nico.revisionsmaths.core

import com.nico.revisionsmaths.data.model.FixedTable
import com.nico.revisionsmaths.data.model.Operation
import com.nico.revisionsmaths.data.model.OperationType
import com.nico.revisionsmaths.data.model.Question
import kotlin.random.Random

object QuestionGenerator {

    private const val MAX_UNIQUE_ATTEMPTS = 50

    fun generate(
        age: Int,
        type: OperationType,
        count: Int,
        rng: Random = Random.Default
    ): List<Question> {
        val seen = mutableSetOf<String>()
        val questions = mutableListOf<Question>()
        while (questions.size < count) {
            val op = when (type) {
                OperationType.ADDITION -> Operation.ADDITION
                OperationType.MULTIPLICATION -> Operation.MULTIPLICATION
                OperationType.MIXED ->
                    if (rng.nextBoolean()) Operation.ADDITION else Operation.MULTIPLICATION
            }
            var question = generateQuestion(age, op, rng)
            var attempts = 0
            while (question.key() in seen && attempts < MAX_UNIQUE_ATTEMPTS) {
                question = generateQuestion(age, op, rng)
                attempts++
            }
            seen.add(question.key())
            questions.add(question)
        }
        return questions
    }

    private fun Question.key(): String = "$a|$b|${op.name}"

    fun generateQuestion(age: Int, op: Operation, rng: Random = Random.Default): Question = when {
        age <= 6 -> when (op) {
            Operation.ADDITION -> {
                val a = rng.nextInt(0, 11)
                val b = rng.nextInt(0, 11 - a)
                Question(a, b, op, a + b)
            }
            Operation.MULTIPLICATION -> {
                val a = rng.nextInt(1, 4)
                val b = rng.nextInt(1, 4)
                Question(a, b, op, a * b)
            }
        }
        age == 7 -> when (op) {
            Operation.ADDITION -> {
                val a = rng.nextInt(1, 20)
                val b = rng.nextInt(1, 21 - a)
                Question(a, b, op, a + b)
            }
            Operation.MULTIPLICATION -> {
                val a = rng.nextInt(2, 6)
                val b = rng.nextInt(1, 6)
                Question(a, b, op, a * b)
            }
        }
        age == 8 -> when (op) {
            Operation.ADDITION -> {
                val a = rng.nextInt(1, 51)
                val b = rng.nextInt(1, 51)
                Question(a, b, op, a + b)
            }
            Operation.MULTIPLICATION -> {
                val a = rng.nextInt(2, 10)
                val b = rng.nextInt(2, 10)
                Question(a, b, op, a * b)
            }
        }
        else -> when (op) {
            Operation.ADDITION -> {
                val a = rng.nextInt(10, 90)
                val b = rng.nextInt(10, 90)
                Question(a, b, op, a + b)
            }
            Operation.MULTIPLICATION -> {
                val a = rng.nextInt(2, 11)
                val b = rng.nextInt(2, 11)
                Question(a, b, op, a * b)
            }
        }
    }

    fun generateForTable(
        fixedTable: FixedTable,
        count: Int,
        rng: Random = Random.Default
    ): List<Question> {
        val pool = (1..10).shuffled(rng).toMutableList()
        return List(count) {
            if (pool.isEmpty()) {
                pool.addAll((1..10).shuffled(rng))
            }
            val b = pool.removeAt(0)
            val a = fixedTable.table
            when (fixedTable.op) {
                Operation.MULTIPLICATION -> Question(a, b, fixedTable.op, a * b)
                Operation.ADDITION -> Question(a, b, fixedTable.op, a + b)
            }
        }
    }

    fun questionForTable(fixedTable: FixedTable, rng: Random = Random.Default): Question {
        val a = fixedTable.table
        val b = rng.nextInt(1, 11)
        return when (fixedTable.op) {
            Operation.MULTIPLICATION -> Question(a, b, fixedTable.op, a * b)
            Operation.ADDITION -> Question(a, b, fixedTable.op, a + b)
        }
    }

    fun difficultyHint(age: Int): String = when {
        age <= 6 -> "Additions jusqu'à 10, petites multiplications"
        age == 7 -> "Additions jusqu'à 20, tables de 2 à 5"
        age == 8 -> "Additions jusqu'à 100, tables de 2 à 9"
        else -> "Additions à deux chiffres, tables de 2 à 10"
    }
}

package com.nico.revisionsmaths.data.model

enum class OperationType(val label: String) {
    ADDITION("Addition"),
    MULTIPLICATION("Multiplication"),
    MIXED("Mixte");

    companion object {
        fun fromName(name: String): OperationType = entries.firstOrNull { it.name == name } ?: MIXED
    }
}

enum class Operation(val symbol: String) {
    ADDITION("+"),
    MULTIPLICATION("×");
}

data class Question(val a: Int, val b: Int, val op: Operation, val answer: Int) {
    val text: String get() = "$a ${op.symbol} $b"
}

data class QuizConfig(val operationType: OperationType, val questionCount: Int)

package com.nico.revisionsmaths.data.model

enum class OperationType(val label: String) {
    ADDITION("Addition"),
    MULTIPLICATION("Multiplication"),
    MIXED("Mixte");

    companion object {
        fun fromName(name: String): OperationType = entries.firstOrNull { it.name == name } ?: MIXED
    }
}

enum class Operation(val symbol: String, val label: String) {
    ADDITION("+", "addition"),
    MULTIPLICATION("×", "multiplication");
}

data class Question(val a: Int, val b: Int, val op: Operation, val answer: Int) {
    val text: String get() = "$a ${op.symbol} $b"
}

/** Mode « table fixe » : toutes les questions dans une table précise (ex. table des 8). */
data class FixedTable(val op: Operation, val table: Int) {

    fun encode(): String = (if (op == Operation.MULTIPLICATION) "M" else "A") + table

    fun description(): String = "Table d'${op.label} des $table"

    companion object {
        fun decode(value: String?): FixedTable? {
            if (value.isNullOrBlank() || value == "-") return null
            val op = when (value.first()) {
                'M' -> Operation.MULTIPLICATION
                'A' -> Operation.ADDITION
                else -> return null
            }
            val table = value.drop(1).toIntOrNull() ?: return null
            if (table < 1 || table > 12) return null
            return FixedTable(op, table)
        }
    }
}

data class QuizConfig(
    val operationType: OperationType,
    val questionCount: Int,
    val fixedTable: FixedTable? = null
)

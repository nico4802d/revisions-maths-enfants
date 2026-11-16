package com.nico.revisionsmaths.core

/**
 * Convertit une réponse parlée ou saisie en nombre.
 * Accepte les chiffres ("56") et les nombres écrits en lettres
 * ("cinquante-six", "quatre-vingt-dix-neuf", "cent quatre"...).
 */
object FrenchNumberParser {

    private val units = mapOf(
        "zéro" to 0, "zero" to 0,
        "un" to 1, "une" to 1, "deux" to 2, "trois" to 3, "quatre" to 4,
        "cinq" to 5, "six" to 6, "sept" to 7, "huit" to 8, "neuf" to 9,
        "dix" to 10, "onze" to 11, "douze" to 12, "treize" to 13,
        "quatorze" to 14, "quinze" to 15, "seize" to 16
    )

    private val tens = mapOf(
        "vingt" to 20, "vingts" to 20, "trente" to 30, "quarante" to 40,
        "cinquante" to 50, "soixante" to 60, "soixantes" to 60,
        "cent" to 100, "cents" to 100, "mille" to 1000, "milles" to 1000
    )

    fun parse(input: String): Int? {
        val trimmed = input.trim().lowercase()
        if (trimmed.isEmpty()) return null

        Regex("\\d+").find(trimmed)?.let { return it.value.toInt() }

        val tokens = trimmed
            .replace(Regex("[^a-zàâäéèêëîïôöûùüç\\s-]"), "")
            .replace("-", " ")
            .split(Regex("\\s+"))
            .filter { it.isNotBlank() && it != "et" }

        val known = tokens.filter { it in units || it in tens }
        if (known.isEmpty()) return null

        var total = 0
        var current = 0
        var i = 0

        while (i < known.size) {
            val token = known[i]

            if (token == "quatre" && i + 1 < known.size && known[i + 1] in setOf("vingt", "vingts")) {
                current = 80
                if (i + 2 < known.size && known[i + 2] == "dix") {
                    current = 90
                    i++
                }
                i += 2
                continue
            }

            when {
                token in units -> current += units.getValue(token)
                token == "mille" || token == "milles" -> {
                    current = (if (current == 0) 1 else current) * 1000
                    total += current
                    current = 0
                }
                token == "cent" || token == "cents" -> {
                    current = if (current == 0) 100 else current * 100
                }
                token in tens -> {
                    if (current == 0) current = tens.getValue(token)
                    else if (current % 100 == 0 && current >= 100) current += tens.getValue(token)
                    else current += tens.getValue(token)
                }
            }
            i++
        }

        val result = total + current
        return if (result in 0..99999) result else null
    }
}

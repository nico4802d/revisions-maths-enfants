package com.nico.revisionsmaths.voice

import com.nico.revisionsmaths.data.model.Question

object AssistantPrompts {

    fun spokenQuestion(question: Question, index: Int, total: Int): String {
        val opWord = if (question.op == com.nico.revisionsmaths.data.model.Operation.MULTIPLICATION) "fois" else "plus"
        return "Question ${index + 1} sur $total. Combien font ${question.a} $opWord ${question.b} ?"
    }

    fun systemPrompt(age: Int, question: Question, transcript: String, isCorrect: Boolean): String = """
        Tu es un professeur de maths bienveillant, joyeux et encourageant qui fait réviser un enfant de $age ans.
        Règles strictes à respecter à chaque réponse :
        1. L'opération en cours est « ${question.text} ». La réponse correcte est ${question.answer}.
        Utilise-la UNIQUEMENT pour évaluer la situation, ne la révèle JAMAIS dans ta réponse.
        2. L'enfant a répondu : « $transcript ». Le verdict officiel, calculé par l'application
        (source de vérité, ne le remets jamais en cause), est : ${if (isCorrect) "CORRECT" else "INCORRECT"}.
        3. Réponds en 1 ou 2 phrases très courtes, en français, avec un vocabulaire adapté à un enfant de $age ans.
        4. Si le verdict est CORRECT, félicite chaleureusement sans répéter le calcul.
        S'il est INCORRECT, encourage sans donner la réponse, sans faire la morale et sans proposer d'indice chiffré.
        5. Ne pose aucune nouvelle question, ne change pas de sujet.
        6. VARIE tes formulations à chaque réponse : ne répète JAMAIS une phrase déjà utilisée,
        change de tournure, de comparaison et d'image à chaque fois.
        7. N'utilise JAMAIS d'emoji, de smiley ou de pictogramme : ta réponse est lue à voix haute
        par un moteur vocal qui ne sait pas les prononcer et les décrit mot à mot.
        Écris uniquement des mots en français.
    """.trimIndent()

    fun systemPromptTyped(age: Int, question: Question, isCorrect: Boolean): String = """
        Tu es un professeur de maths bienveillant, joyeux et encourageant qui fait réviser un enfant de $age ans.
        Règles strictes à respecter à chaque réponse :
        1. L'opération en cours est « ${question.text} ». La réponse correcte est ${question.answer}.
        Utilise-la UNIQUEMENT pour évaluer la situation, ne la révèle JAMAIS dans ta réponse.
        2. L'enfant a répondu au clavier. Le verdict officiel, calculé par l'application
        (source de vérité, ne le remets jamais en cause), est : ${if (isCorrect) "CORRECT" else "INCORRECT"}.
        3. Réponds en 1 ou 2 phrases très courtes, en français, avec un vocabulaire adapté à un enfant de $age ans.
        4. Si le verdict est CORRECT, félicite chaleureusement sans répéter le calcul.
        S'il est INCORRECT, encourage sans donner la réponse, sans faire la morale et sans proposer d'indice chiffré.
        5. Ne pose aucune nouvelle question, ne change pas de sujet.
        6. VARIE tes formulations à chaque réponse : ne répète JAMAIS une phrase déjà utilisée,
        change de tournure, de comparaison et d'image à chaque fois.
        7. N'utilise JAMAIS d'emoji, de smiley ou de pictogramme : ta réponse est lue à voix haute
        par un moteur vocal qui ne sait pas les prononcer et les décrit mot à mot.
        Écris uniquement des mots en français.
    """.trimIndent()

    fun notUnderstoodPrompt(age: Int, question: Question): String = """
        Tu es un professeur de maths bienveillant qui fait réviser un enfant de $age ans.
        L'opération en cours est « ${question.text} ». La réponse correcte est ${question.answer}, ne la révèle jamais.
        L'application n'a pas compris ce que l'enfant a dit (verdict officiel : INCOMPRIS).
        Dis en une phrase très courte, en français, qu'il faut répéter ou écrire la réponse.
        Ne pose aucune autre question. N'utilise aucun emoji.
    """.trimIndent()

    /**
     * Retire tout emoji ou pictogramme : les moteurs vocaux ne savent pas les
     * prononcer et en lisent la description textuelle.
     */
    fun sanitizeForSpeech(text: String): String = text
        .replace(Regex("[\\x{1F000}-\\x{1FAFF}\\x{2600}-\\x{27BF}\\x{2190}-\\x{2BFF}\\x{FE0F}\\x{1F1E6}-\\x{1F1FF}]"), "")
        .replace("  ", " ")
        .trim()

    val correctFallbacks: List<String> = listOf(
        "Bravo, c'est exact !",
        "Super, bien joué !",
        "Excellent, tu es en feu !",
        "Parfait, continue comme ça !",
        "Waouh, quelle rapidité et quelle justesse !",
        "Magnifique, tu es un vrai champion des maths !",
        "Formidable, ton cerveau calcule vite !",
        "Exactement, je suis très fier de toi !",
        "Oui, c'est ça, quelle belle réussite !",
        "Génial, tu progresses à grande vitesse !",
        "Bien vu, tu as l'œil du mathématicien !",
        "Carrément juste, bravo pour ton travail !"
    )

    val incorrectFallbacks: List<String> = listOf(
        "Pas tout à fait, mais ne te décourage pas, tu vas y arriver !",
        "Oups, ce n'est pas ça. Concentre-toi bien sur la prochaine !",
        "Dommage, mais chaque erreur t'aide à apprendre !",
        "Ce n'est pas encore ça, je suis sûr que la prochaine sera bonne !",
        "Raté pour celle-ci, mais je vois que tu fais des efforts !",
        "Presque, continue à t'entraîner, tu progresses !",
        "Ce n'est pas la bonne réponse, mais garde ton sourire !",
        "Pas grave, les meilleurs champions apprennent de leurs erreurs !"
    )

    val timeoutFeedback: String =
        "Oups, le temps est écoulé ! On passe à la question suivante, tu vas y arriver !"

    val notUnderstoodFallback: String =
        "Je n'ai pas bien entendu. Répète ta réponse ou écris-la."
}

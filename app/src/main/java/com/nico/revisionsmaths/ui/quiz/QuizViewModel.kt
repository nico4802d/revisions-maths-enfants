package com.nico.revisionsmaths.ui.quiz

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.nico.revisionsmaths.RevisionsApp
import com.nico.revisionsmaths.core.FrenchNumberParser
import com.nico.revisionsmaths.core.QuestionGenerator
import com.nico.revisionsmaths.data.db.Profile
import com.nico.revisionsmaths.data.db.QuizResult
import com.nico.revisionsmaths.data.model.Operation
import com.nico.revisionsmaths.data.model.Question
import com.nico.revisionsmaths.data.model.QuizConfig
import com.nico.revisionsmaths.voice.AssistantPrompts
import com.nico.revisionsmaths.voice.AudioPlayer
import com.nico.revisionsmaths.voice.NativeTts
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class QuizPhase { ASKING, ANSWERING, FEEDBACK, FINISHED }

data class QuizUiState(
    val phase: QuizPhase = QuizPhase.ASKING,
    val questionIndex: Int = 0,
    val questionCount: Int = 0,
    val question: Question? = null,
    val lastCorrect: Boolean? = null,
    val score: Int = 0,
    val assistantMessage: String? = null,
    val isSpeaking: Boolean = false,
    val error: String? = null,
    val xpGained: Int = 0,
    val finished: Boolean = false,
    val timeLeft: Int? = null
)

/**
 * Version 1 : les questions et les feedbacks sont lus à voix haute (TTS),
 * les réponses sont saisies au clavier (activé par défaut).
 */
class QuizViewModel(
    application: Application,
    private val profile: Profile,
    private val config: QuizConfig
) : AndroidViewModel(application) {

    private val container = (application as RevisionsApp).container
    private val nativeTts = NativeTts(application)
    private val audioPlayer = AudioPlayer()

    private val _state = MutableStateFlow(QuizUiState(questionCount = config.questionCount))
    val state: StateFlow<QuizUiState> = _state.asStateFlow()

    private var questions: List<Question> = emptyList()
    private val answers = mutableListOf<Pair<Operation, Boolean>>()
    private var startTime = 0L
    private var pipelineJob: Job? = null
    private var timerJob: Job? = null

    init {
        start()
    }

    private fun start() {
        questions = config.fixedTable?.let {
            QuestionGenerator.generateForTable(it, config.questionCount)
        } ?: QuestionGenerator.generate(profile.age, config.operationType, config.questionCount)
        startTime = System.currentTimeMillis()
        askCurrent()
    }

    private fun askCurrent() {
        val index = _state.value.questionIndex
        val question = questions.getOrNull(index) ?: return finishQuiz()
        pipelineJob = viewModelScope.launch {
            _state.update {
                it.copy(
                    question = question,
                    lastCorrect = null,
                    assistantMessage = null,
                    error = null,
                    timeLeft = null,
                    phase = QuizPhase.ASKING,
                    isSpeaking = true
                )
            }
            speak(AssistantPrompts.spokenQuestion(question, index, questions.size))
            _state.update { it.copy(isSpeaking = false, phase = QuizPhase.ANSWERING) }
            startAnswerTimer()
        }
    }

    fun repeatQuestion() {
        val index = _state.value.questionIndex
        val question = _state.value.question ?: return
        if (_state.value.phase == QuizPhase.FINISHED) return
        pipelineJob?.cancel()
        cancelAnswerTimer()
        pipelineJob = viewModelScope.launch {
            _state.update { it.copy(isSpeaking = true, error = null) }
            speak(AssistantPrompts.spokenQuestion(question, index, questions.size))
            _state.update { it.copy(isSpeaking = false, phase = QuizPhase.ANSWERING) }
            startAnswerTimer()
        }
    }

    fun submitManualAnswer(input: String) {
        val current = _state.value
        if (current.phase !in listOf(QuizPhase.ASKING, QuizPhase.ANSWERING)) return
        val parsed = FrenchNumberParser.parse(input)
        if (parsed == null) {
            _state.update { it.copy(error = "Écris un nombre s'il te plaît 😊") }
            return
        }
        cancelAnswerTimer()
        if (current.isSpeaking) {
            nativeTts.stopSpeaking()
            audioPlayer.stop()
        }
        pipelineJob?.cancel()
        pipelineJob = viewModelScope.launch { evaluate(parsed) }
    }

    private fun startAnswerTimer() {
        timerJob?.cancel()
        val limit = container.settings.answerTimeLimit
        _state.update { it.copy(timeLeft = limit) }
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1_000)
                val current = _state.value.timeLeft ?: break
                if (current <= 1) {
                    _state.update { it.copy(timeLeft = 0) }
                    onAnswerTimeout()
                    break
                }
                _state.update { it.copy(timeLeft = current - 1) }
            }
        }
    }

    private fun cancelAnswerTimer() {
        timerJob?.cancel()
        _state.update { it.copy(timeLeft = null) }
    }

    private fun onAnswerTimeout() {
        if (_state.value.phase != QuizPhase.ANSWERING) return
        pipelineJob?.cancel()
        pipelineJob = viewModelScope.launch {
            val question = _state.value.question ?: return@launch
            answers.add(question.op to false)
            _state.update { it.copy(lastCorrect = false, timeLeft = null, phase = QuizPhase.FEEDBACK) }
            val message = AssistantPrompts.timeoutFeedback
            _state.update { it.copy(assistantMessage = message, isSpeaking = true) }
            speak(message)
            _state.update { it.copy(isSpeaking = false) }
            nextQuestion()
        }
    }

    private suspend fun evaluate(value: Int) {
        val question = _state.value.question ?: return
        val isCorrect = value == question.answer
        answers.add(question.op to isCorrect)
        _state.update {
            it.copy(
                lastCorrect = isCorrect,
                score = if (isCorrect) it.score + 1 else it.score,
                error = null,
                phase = QuizPhase.FEEDBACK
            )
        }
        val feedback = assistantFeedback(question, isCorrect)
        _state.update { it.copy(assistantMessage = feedback, isSpeaking = true) }
        speak(feedback)
        _state.update { it.copy(isSpeaking = false) }
        nextQuestion()
    }

    private suspend fun nextQuestion() {
        if (_state.value.questionIndex + 1 < questions.size) {
            delay(800)
            _state.update { it.copy(questionIndex = it.questionIndex + 1) }
            askCurrent()
        } else {
            finishQuiz()
        }
    }

    private suspend fun assistantFeedback(question: Question, isCorrect: Boolean): String {
        if (container.aiClient.isConfigured) {
            val reply = container.aiClient.chat(
                AssistantPrompts.systemPromptTyped(profile.age, question, isCorrect),
                if (isCorrect) "L'enfant a donné la bonne réponse." else "L'enfant a donné une mauvaise réponse."
            )
            if (!reply.isNullOrBlank()) return reply
        }
        return if (isCorrect) AssistantPrompts.correctFallbacks.random()
        else AssistantPrompts.incorrectFallback
    }

    private suspend fun speak(text: String) {
        if (container.aiClient.isConfigured) {
            val audio = container.aiClient.speech(text)
            if (audio != null) {
                audioPlayer.play(getApplication(), audio)
                return
            }
        }
        nativeTts.speak(text)
    }

    private fun finishQuiz() {
        pipelineJob = viewModelScope.launch {
            val current = _state.value
            val xpGained = answers.mapNotNull { (op, correct) ->
                if (correct) {
                    if (op == Operation.MULTIPLICATION) 10 else 5
                } else {
                    null
                }
            }.sum()
            val durationSeconds = ((System.currentTimeMillis() - startTime) / 1000).toInt()
            container.database.profileDao().update(profile.copy(xp = profile.xp + xpGained))
            container.database.quizResultDao().insert(
                QuizResult(
                    profileId = profile.id,
                    date = System.currentTimeMillis(),
                    operationType = config.operationType.name,
                    totalQuestions = questions.size,
                    correctCount = current.score,
                    durationSeconds = durationSeconds,
                    xpGained = xpGained
                )
            )
            _state.update {
                it.copy(phase = QuizPhase.FINISHED, finished = true, xpGained = xpGained)
            }
        }
    }

    override fun onCleared() {
        pipelineJob?.cancel()
        timerJob?.cancel()
        nativeTts.shutdown()
        audioPlayer.stop()
        super.onCleared()
    }
}

class QuizViewModelFactory(
    private val application: Application,
    private val profile: Profile,
    private val config: QuizConfig
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(QuizViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return QuizViewModel(application, profile, config) as T
        }
        throw IllegalArgumentException("ViewModel inconnu : ${modelClass.name}")
    }
}

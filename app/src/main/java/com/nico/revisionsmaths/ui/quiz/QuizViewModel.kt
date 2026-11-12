package com.nico.revisionsmaths.ui.quiz

import android.Manifest
import android.app.Application
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
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
import com.nico.revisionsmaths.voice.VoiceController
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class QuizPhase { ASKING, LISTENING, CHECKING, FEEDBACK, FINISHED }

data class QuizUiState(
    val phase: QuizPhase = QuizPhase.ASKING,
    val questionIndex: Int = 0,
    val questionCount: Int = 0,
    val question: Question? = null,
    val transcript: String? = null,
    val lastCorrect: Boolean? = null,
    val score: Int = 0,
    val assistantMessage: String? = null,
    val isSpeaking: Boolean = false,
    val isListening: Boolean = false,
    val error: String? = null,
    val xpGained: Int = 0,
    val finished: Boolean = false,
    val timeLeft: Int? = null,
    val micRetry: Boolean = false
)

class QuizViewModel(
    application: Application,
    private val profile: Profile,
    private val config: QuizConfig
) : AndroidViewModel(application) {

    private val container = (application as RevisionsApp).container
    private val voiceController = VoiceController(application)
    private val nativeTts = NativeTts(application)
    private val audioPlayer = AudioPlayer()

    private val _state = MutableStateFlow(QuizUiState(questionCount = config.questionCount))
    val state: StateFlow<QuizUiState> = _state.asStateFlow()

    private var questions: List<Question> = emptyList()
    private val answers = mutableListOf<Pair<Operation, Boolean>>()
    private var startTime = 0L
    private var pipelineJob: Job? = null
    private var timerJob: Job? = null
    private var sttRetries = 0

    init {
        start()
    }

    private fun start() {
        questions = QuestionGenerator.generate(profile.age, config.operationType, config.questionCount)
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
                    transcript = null,
                    lastCorrect = null,
                    assistantMessage = null,
                    error = null,
                    timeLeft = null,
                    phase = QuizPhase.ASKING,
                    isSpeaking = true
                )
            }
            speak(AssistantPrompts.spokenQuestion(question, index, questions.size))
            _state.update { it.copy(isSpeaking = false, phase = QuizPhase.LISTENING, isListening = true) }
            startAnswerTimer()
            startListeningForAnswer()
        }
    }

    fun repeatQuestion() {
        val index = _state.value.questionIndex
        val question = _state.value.question ?: return
        if (_state.value.phase == QuizPhase.FINISHED) return
        pipelineJob?.cancel()
        cancelAnswerTimer()
        voiceController.stopListening()
        pipelineJob = viewModelScope.launch {
            _state.update { it.copy(isSpeaking = true, isListening = false, error = null) }
            speak(AssistantPrompts.spokenQuestion(question, index, questions.size))
            _state.update { it.copy(isSpeaking = false, phase = QuizPhase.LISTENING, isListening = true) }
            startAnswerTimer()
            startListeningForAnswer()
        }
    }

    fun startVoiceAnswer() {
        val current = _state.value
        if (current.phase !in listOf(QuizPhase.LISTENING, QuizPhase.CHECKING)) return
        if (current.isSpeaking) return
        if (current.timeLeft == null) startAnswerTimer()
        _state.update { it.copy(phase = QuizPhase.LISTENING, isListening = true, error = null) }
        startListeningForAnswer()
    }

    private fun startListeningForAnswer() {
        if (!hasMicPermission()) {
            _state.update {
                it.copy(
                    isListening = false,
                    phase = QuizPhase.CHECKING,
                    error = "Autorise le micro pour répondre à la voix, ou écris ta réponse."
                )
            }
            return
        }
        voiceController.startListening(
            onResult = { transcript ->
                sttRetries = 0
                _state.update { it.copy(micRetry = false) }
                handleTranscript(transcript ?: "")
            },
            onError = { errorCode -> handleSttError(errorCode) }
        )
    }

    private fun handleSttError(errorCode: Int) {
        if (errorCode in VoiceController.TRANSIENT_ERRORS && sttRetries < MAX_STT_RETRIES) {
            sttRetries++
            _state.update { it.copy(isListening = false, micRetry = true) }
            viewModelScope.launch {
                delay(STT_RETRY_DELAY_MS)
                if (_state.value.phase == QuizPhase.LISTENING) {
                    _state.update { it.copy(isListening = true) }
                    startListeningForAnswer()
                } else {
                    _state.update { it.copy(micRetry = false) }
                }
            }
        } else {
            sttRetries = 0
            _state.update {
                it.copy(
                    isListening = false,
                    micRetry = false,
                    phase = QuizPhase.CHECKING,
                    error = "🎙️ ${VoiceController.friendlyMessage(errorCode)} " +
                        "Écris ta réponse ou appuie sur Parler pour réessayer."
                )
            }
        }
    }

    private fun handleTranscript(transcript: String) {
        cancelAnswerTimer()
        _state.update { it.copy(isListening = false, transcript = transcript, phase = QuizPhase.CHECKING) }
        val parsed = FrenchNumberParser.parse(transcript)
        if (parsed == null) {
            pipelineJob = viewModelScope.launch { evaluateNotUnderstood(transcript) }
        } else {
            pipelineJob?.cancel()
            pipelineJob = viewModelScope.launch { evaluate(parsed, transcript) }
        }
    }

    fun submitManualAnswer(input: String) {
        val current = _state.value
        if (current.phase !in listOf(QuizPhase.LISTENING, QuizPhase.CHECKING)) return
        val parsed = FrenchNumberParser.parse(input) ?: return
        cancelAnswerTimer()
        voiceController.stopListening()
        _state.update { it.copy(isListening = false) }
        pipelineJob?.cancel()
        pipelineJob = viewModelScope.launch { evaluate(parsed, input) }
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
        if (_state.value.phase !in listOf(QuizPhase.LISTENING, QuizPhase.CHECKING)) return
        voiceController.stopListening()
        pipelineJob?.cancel()
        pipelineJob = viewModelScope.launch {
            val question = _state.value.question ?: return@launch
            answers.add(question.op to false)
            _state.update {
                it.copy(
                    lastCorrect = false,
                    transcript = null,
                    isListening = false,
                    timeLeft = null,
                    phase = QuizPhase.FEEDBACK
                )
            }
            val message = AssistantPrompts.timeoutFeedback
            _state.update { it.copy(assistantMessage = message, isSpeaking = true) }
            speak(message)
            _state.update { it.copy(isSpeaking = false) }
            if (_state.value.questionIndex + 1 < questions.size) {
                delay(800)
                _state.update { it.copy(questionIndex = it.questionIndex + 1) }
                askCurrent()
            } else {
                finishQuiz()
            }
        }
    }

    private suspend fun evaluate(value: Int, transcript: String) {
        val question = _state.value.question ?: return
        val isCorrect = value == question.answer
        answers.add(question.op to isCorrect)
        _state.update {
            it.copy(
                lastCorrect = isCorrect,
                score = if (isCorrect) it.score + 1 else it.score,
                phase = QuizPhase.FEEDBACK
            )
        }
        val feedback = assistantFeedback(question, transcript, isCorrect)
        _state.update { it.copy(assistantMessage = feedback, isSpeaking = true) }
        speak(feedback)
        _state.update { it.copy(isSpeaking = false) }
        if (_state.value.questionIndex + 1 < questions.size) {
            delay(800)
            _state.update { it.copy(questionIndex = it.questionIndex + 1) }
            askCurrent()
        } else {
            finishQuiz()
        }
    }

    private suspend fun evaluateNotUnderstood(transcript: String) {
        val question = _state.value.question ?: return
        val message = if (container.aiClient.isConfigured) {
            container.aiClient.chat(
                AssistantPrompts.notUnderstoodPrompt(profile.age, question),
                transcript
            ) ?: AssistantPrompts.notUnderstoodFallback
        } else {
            AssistantPrompts.notUnderstoodFallback
        }
        _state.update { it.copy(assistantMessage = message, isSpeaking = true, phase = QuizPhase.CHECKING) }
        speak(message)
        _state.update { it.copy(isSpeaking = false) }
        if (_state.value.timeLeft == null) startAnswerTimer()
    }

    private suspend fun assistantFeedback(question: Question, transcript: String, isCorrect: Boolean): String {
        if (container.aiClient.isConfigured) {
            val reply = container.aiClient.chat(
                AssistantPrompts.systemPrompt(profile.age, question, transcript, isCorrect),
                transcript
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
                it.copy(phase = QuizPhase.FINISHED, finished = true, xpGained = xpGained, isListening = false)
            }
        }
    }

    private fun hasMicPermission(): Boolean = ContextCompat.checkSelfPermission(
        getApplication(),
        Manifest.permission.RECORD_AUDIO
    ) == PackageManager.PERMISSION_GRANTED

    override fun onCleared() {
        pipelineJob?.cancel()
        timerJob?.cancel()
        voiceController.stopListening()
        nativeTts.shutdown()
        audioPlayer.stop()
        super.onCleared()
    }

    companion object {
        const val MAX_STT_RETRIES = 3
        const val STT_RETRY_DELAY_MS = 800L
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

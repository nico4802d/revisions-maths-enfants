package com.nico.revisionsmaths.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.nico.revisionsmaths.RevisionsApp
import com.nico.revisionsmaths.data.db.Profile
import com.nico.revisionsmaths.data.model.QuizConfig
import com.nico.revisionsmaths.ui.nav.Routes
import com.nico.revisionsmaths.ui.quiz.QuizPhase
import com.nico.revisionsmaths.ui.quiz.QuizUiState
import com.nico.revisionsmaths.ui.quiz.QuizViewModel
import com.nico.revisionsmaths.ui.quiz.QuizViewModelFactory
import com.nico.revisionsmaths.ui.theme.Green
import com.nico.revisionsmaths.ui.theme.Orange
import com.nico.revisionsmaths.ui.theme.Pink
import com.nico.revisionsmaths.ui.theme.Purple
import com.nico.revisionsmaths.ui.theme.PurpleLight
import com.nico.revisionsmaths.ui.theme.Red
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizScreen(
    navController: NavHostController,
    profileId: Long,
    config: QuizConfig
) {
    val context = LocalContext.current
    val app = context.applicationContext as RevisionsApp

    var profile by remember { mutableStateOf<Profile?>(null) }
    LaunchedEffect(profileId) {
        profile = withContext(Dispatchers.IO) { app.container.database.profileDao().getById(profileId) }
    }

    val currentProfile = profile
    if (currentProfile == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val viewModel: QuizViewModel = viewModel(
        key = "${currentProfile.id}-${config.operationType.name}-${config.questionCount}",
        factory = QuizViewModelFactory(app, currentProfile, config)
    )
    val state by viewModel.state.collectAsState()

    LaunchedEffect(state.finished) {
        if (state.finished) {
            navController.navigate(Routes.results(currentProfile.id)) {
                popUpTo(Routes.HOME)
            }
        }
    }

    QuizContent(
        state = state,
        profileName = currentProfile.name,
        onBack = { navController.popBackStack() },
        onRepeat = viewModel::repeatQuestion,
        onManualAnswer = viewModel::submitManualAnswer
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun QuizContent(
    state: QuizUiState,
    profileName: String,
    onBack: () -> Unit,
    onRepeat: () -> Unit,
    onManualAnswer: (String) -> Unit
) {
    var manualInput by remember { mutableStateOf("") }
    val answerFocusRequester = remember { FocusRequester() }

    LaunchedEffect(state.phase, state.questionIndex) {
        if (state.phase in listOf(QuizPhase.ASKING, QuizPhase.ANSWERING)) {
            answerFocusRequester.requestFocus()
        }
    }

    LaunchedEffect(state.phase, state.questionIndex) {
        if (state.phase == QuizPhase.FEEDBACK && manualInput.isNotBlank()) {
            manualInput = ""
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("🎯 Quiz — $profileName") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                ),
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quitter")
                    }
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 14.dp, vertical = 10.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Question ${state.questionIndex + 1} / ${state.questionCount}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.weight(1f))
                Text(
                    "⭐ Score : ${state.score}",
                    style = MaterialTheme.typography.titleMedium,
                    color = Pink,
                    fontWeight = FontWeight.Bold
                )
            }
            LinearProgressIndicator(
                progress = { state.questionIndex.toFloat() / state.questionCount.coerceAtLeast(1) },
                modifier = Modifier.fillMaxWidth(),
                color = Purple,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            state.question?.let { question ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(listOf(Purple, PurpleLight)),
                            RoundedCornerShape(20.dp)
                        )
                        .padding(vertical = 20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "${question.text} = ?",
                        fontSize = 40.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        color = Color.White
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                state.timeLeft?.let { seconds ->
                    Surface(
                        shape = CircleShape,
                        color = when {
                            seconds <= 3 -> Red
                            seconds <= 6 -> Orange
                            else -> Green
                        }
                    ) {
                        Text(
                            "⏳ $seconds s",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            OutlinedButton(
                onClick = onRepeat,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Orange)
            ) {
                Icon(Icons.Default.Replay, contentDescription = null)
                Spacer(Modifier.width(4.dp))
                Text("🔊 Répéter la question")
            }

            state.assistantMessage?.let { message ->
                Text(
                    message,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            when (state.lastCorrect) {
                                true -> Color(0xFFE8F5E9)
                                false -> Color(0xFFFFF3E0)
                                null -> MaterialTheme.colorScheme.surfaceVariant
                            },
                            RoundedCornerShape(12.dp)
                        )
                        .padding(10.dp)
                )
            }

            state.error?.let { error ->
                Text(
                    error,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }

            if (state.isSpeaking) {
                Text("🔊 L'assistant parle…", fontWeight = FontWeight.Medium)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = manualInput,
                    onValueChange = { manualInput = it },
                    label = { Text("Ta réponse") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            if (manualInput.isNotBlank() &&
                                state.phase in listOf(QuizPhase.ASKING, QuizPhase.ANSWERING)
                            ) {
                                onManualAnswer(manualInput)
                                manualInput = ""
                            }
                        }
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .focusRequester(answerFocusRequester)
                )
                Button(
                    onClick = {
                        onManualAnswer(manualInput)
                        manualInput = ""
                    },
                    enabled = manualInput.isNotBlank() &&
                        state.phase in listOf(QuizPhase.ASKING, QuizPhase.ANSWERING),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Green,
                        contentColor = Color.White
                    )
                ) {
                    Text("OK", fontWeight = FontWeight.Bold)
                }
            }

            Spacer(Modifier.height(12.dp))
        }
    }
}

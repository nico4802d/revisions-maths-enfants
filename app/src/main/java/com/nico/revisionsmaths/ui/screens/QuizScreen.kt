package com.nico.revisionsmaths.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
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

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }

    LaunchedEffect(Unit) {
        val granted = ContextCompat.checkSelfPermission(
            context, Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
        if (!granted) permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
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
        onVoiceAnswer = viewModel::startVoiceAnswer,
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
    onVoiceAnswer: () -> Unit,
    onManualAnswer: (String) -> Unit
) {
    var manualInput by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Quiz — $profileName") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quitter")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Question ${state.questionIndex + 1} / ${state.questionCount}",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(Modifier.weight(1f))
                Text(
                    "Score : ${state.score}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            LinearProgressIndicator(
                progress = { state.questionIndex.toFloat() / state.questionCount.coerceAtLeast(1) },
                modifier = Modifier.fillMaxWidth()
            )

            state.question?.let { question ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "${question.text} = ?",
                            fontSize = 52.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    }
                }
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
                        .padding(12.dp)
                )
            }

            state.transcript?.let { transcript ->
                Text(
                    "Tu as dit : « $transcript »",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            state.error?.let { error ->
                Text(
                    error,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }

            when {
                state.isSpeaking -> Text("🔊 L'assistant parle…")
                state.isListening -> Text("🎙️ Je t'écoute, dis ta réponse !")
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(onClick = onRepeat, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Default.Replay, contentDescription = null)
                    Spacer(Modifier.padding(2.dp))
                    Text("Répéter")
                }
                Button(
                    onClick = onVoiceAnswer,
                    enabled = !state.isSpeaking &&
                        state.phase in listOf(QuizPhase.LISTENING, QuizPhase.CHECKING),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Mic, contentDescription = null)
                    Spacer(Modifier.padding(2.dp))
                    Text("Parler")
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = manualInput,
                    onValueChange = { manualInput = it },
                    label = { Text("Ou écris ta réponse") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
                Button(
                    onClick = {
                        onManualAnswer(manualInput)
                        manualInput = ""
                    },
                    enabled = manualInput.isNotBlank() &&
                        state.phase in listOf(QuizPhase.LISTENING, QuizPhase.CHECKING)
                ) {
                    Text("OK")
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

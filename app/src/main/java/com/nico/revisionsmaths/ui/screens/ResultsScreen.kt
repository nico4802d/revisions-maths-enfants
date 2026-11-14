package com.nico.revisionsmaths.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.nico.revisionsmaths.RevisionsApp
import com.nico.revisionsmaths.data.db.Profile
import com.nico.revisionsmaths.data.db.QuizResult
import com.nico.revisionsmaths.ui.nav.Routes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultsScreen(navController: NavHostController, profileId: Long) {
    val container = (LocalContext.current.applicationContext as RevisionsApp).container
    var data by remember { mutableStateOf<Pair<Profile, QuizResult>?>(null) }

    LaunchedEffect(profileId) {
        withContext(Dispatchers.IO) {
            val profile = container.database.profileDao().getById(profileId) ?: return@withContext
            val result = container.database.quizResultDao().latestForProfile(profileId)
                ?: return@withContext
            data = profile to result
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("🏆 Résultats") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = androidx.compose.ui.graphics.Color.White
                )
            )
        }
    ) { padding ->
        val loaded = data
        if (loaded == null) {
            Spacer(Modifier.fillMaxSize())
        } else {
            val (profile, result) = loaded
            val percentage = if (result.totalQuestions > 0) {
                result.correctCount * 100 / result.totalQuestions
            } else 0
            val isGreatScore = percentage >= 80

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (isGreatScore) {
                    Text("🎊 🎉 🎊", fontSize = 40.sp)
                    Text(
                        "Félicitations ${profile.name} !",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                    Text(
                        "⭐ ⭐ ⭐ ⭐ ⭐",
                        fontSize = 28.sp
                    )
                    Text(
                        "Quelle belle note ! Petite surprise : tu es officiellement " +
                            "un champion des maths aujourd'hui !",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                } else {
                    Icon(
                        Icons.Default.EmojiEvents,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.height(64.dp)
                    )
                    Text(
                        "Bien joué ${profile.name} !",
                        style = MaterialTheme.typography.headlineSmall
                    )
                }
                Text(
                    "${result.correctCount} / ${result.totalQuestions}",
                    fontSize = 56.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (percentage >= 70) MaterialTheme.colorScheme.tertiary
                    else MaterialTheme.colorScheme.primary
                )
                Text("$percentage % de bonnes réponses")

                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "Niveau ${profile.level} · ${profile.xp} XP",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { profile.levelProgress },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "+${result.xpGained} XP gagnés · Encore ${profile.xpToNextLevel} XP " +
                                "pour le niveau ${profile.level + 1}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(Modifier.weight(1f))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { navController.navigate(Routes.config(profile.id)) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Refaire un quiz")
                    }
                    OutlinedButton(
                        onClick = {
                            navController.popBackStack(Routes.HOME, inclusive = false)
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Accueil")
                    }
                }
            }
        }
    }
}

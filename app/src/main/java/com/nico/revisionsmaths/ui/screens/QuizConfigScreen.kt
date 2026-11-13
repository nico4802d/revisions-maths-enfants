package com.nico.revisionsmaths.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.nico.revisionsmaths.RevisionsApp
import com.nico.revisionsmaths.core.QuestionGenerator
import com.nico.revisionsmaths.data.db.Profile
import com.nico.revisionsmaths.data.model.FixedTable
import com.nico.revisionsmaths.data.model.Operation
import com.nico.revisionsmaths.data.model.OperationType
import com.nico.revisionsmaths.ui.nav.Routes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private val QUESTION_COUNTS = listOf(5, 10, 15, 20)
private val TABLES = (1..10).toList()

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizConfigScreen(navController: NavHostController, profileId: Long) {
    val container = (LocalContext.current.applicationContext as RevisionsApp).container
    var profile by remember { mutableStateOf<Profile?>(null) }
    LaunchedEffect(profileId) {
        profile = withContext(Dispatchers.IO) { container.database.profileDao().getById(profileId) }
    }

    var operationType by remember { mutableStateOf(OperationType.MIXED) }
    var questionCount by remember { mutableIntStateOf(10) }
    var fixedTable by remember { mutableStateOf<FixedTable?>(null) }

    val currentProfile = profile

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("⚙️ Quiz — ${currentProfile?.name ?: ""}") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                ),
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
                    }
                }
            )
        }
    ) { padding ->
        if (currentProfile == null) {
            Spacer(Modifier.fillMaxSize())
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Column {
                    Text("Type de révision", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OperationType.entries.forEach { type ->
                            FilterChip(
                                selected = fixedTable == null && operationType == type,
                                onClick = {
                                    operationType = type
                                    fixedTable = null
                                },
                                label = { Text(type.label) }
                            )
                        }
                    }
                }

                Column {
                    Text(
                        "Ou réviser une table précise 📌",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = fixedTable?.op == Operation.MULTIPLICATION,
                            onClick = {
                                fixedTable = FixedTable(
                                    Operation.MULTIPLICATION,
                                    fixedTable?.table ?: 8
                                )
                            },
                            label = { Text("× Multiplication") }
                        )
                        FilterChip(
                            selected = fixedTable?.op == Operation.ADDITION,
                            onClick = {
                                fixedTable = FixedTable(
                                    Operation.ADDITION,
                                    fixedTable?.table ?: 8
                                )
                            },
                            label = { Text("＋ Addition") }
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        TABLES.forEach { table ->
                            FilterChip(
                                selected = fixedTable?.table == table,
                                onClick = {
                                    fixedTable = FixedTable(
                                        fixedTable?.op ?: Operation.MULTIPLICATION,
                                        table
                                    )
                                },
                                label = { Text("$table") }
                            )
                        }
                    }
                    if (fixedTable != null) {
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "Toutes les questions seront dans la ${fixedTable!!.description()}.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Column {
                    Text("Nombre de questions", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        QUESTION_COUNTS.forEach { count ->
                            FilterChip(
                                selected = questionCount == count,
                                onClick = { questionCount = count },
                                label = { Text("$count") }
                            )
                        }
                    }
                }

                Column {
                    Text(
                        if (fixedTable != null) "Mode table fixe"
                        else "Difficulté adaptée (${currentProfile.age} ans)",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        if (fixedTable != null) {
                            "Tirage aléatoire uniquement dans la table choisie, " +
                                "pour une révision ciblée."
                        } else {
                            QuestionGenerator.difficultyHint(currentProfile.age)
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(Modifier.weight(1f))

                Button(
                    onClick = {
                        navController.navigate(
                            Routes.quiz(currentProfile.id, operationType, questionCount, fixedTable)
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Commencer le quiz")
                }
            }
        }
    }
}

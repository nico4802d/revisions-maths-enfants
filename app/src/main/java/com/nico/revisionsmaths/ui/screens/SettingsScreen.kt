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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.nico.revisionsmaths.RevisionsApp
import com.nico.revisionsmaths.data.AiProvider

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavHostController) {
    val container = (LocalContext.current.applicationContext as RevisionsApp).container

    var provider by remember { mutableStateOf(container.settings.aiProvider) }
    var apiKey by remember { mutableStateOf(container.settings.apiKey ?: "") }
    var saved by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Réglages") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Column {
                Text("Assistant IA (optionnel)", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(4.dp))
                Text(
                    "Sans clé API, l'application fonctionne en mode vocal complet avec la " +
                        "voix native Android (questions lues, réponses écoutées, feedbacks simples). " +
                        "Avec une clé API, l'assistant génère des feedbacks personnalisés.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Column {
                Text("Fournisseur", style = MaterialTheme.typography.titleSmall)
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AiProvider.entries.forEach { p ->
                        FilterChip(
                            selected = provider == p,
                            onClick = { provider = p },
                            label = { Text(p.label) }
                        )
                    }
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    if (provider.supportsTts) {
                        "Ce fournisseur propose aussi une voix IA naturelle."
                    } else {
                        "Ce fournisseur n'a pas d'API vocale : la voix native Android " +
                            "sera utilisée pour parler."
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            OutlinedTextField(
                value = apiKey,
                onValueChange = { apiKey = it; saved = false },
                label = { Text("Clé API ${provider.label}") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = {
                    container.settings.aiProvider = provider
                    container.settings.apiKey = apiKey.trim().ifBlank { null }
                    saved = true
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Enregistrer")
            }

            if (saved) {
                Text(
                    "Réglages enregistrés ✓",
                    color = MaterialTheme.colorScheme.tertiary
                )
            }

            TextButton(
                onClick = {
                    container.settings.apiKey = null
                    apiKey = ""
                    saved = false
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Supprimer la clé API")
            }

            Text(
                "La clé est stockée de façon chiffrée sur cet appareil et n'est jamais " +
                    "envoyée ailleurs que vers l'API du fournisseur choisi.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

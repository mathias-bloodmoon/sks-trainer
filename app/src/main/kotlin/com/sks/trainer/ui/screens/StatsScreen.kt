package com.sks.trainer.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sks.trainer.R
import com.sks.trainer.data.StatsManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val statsManager = remember { StatsManager(context) }
    
    // Wir nutzen State für die Statistiken, damit das UI bei einem Reset aktualisiert wird
    var userStats by remember { mutableStateOf(statsManager.loadStats()) }
    
    // State für den Bestätigungs-Dialog
    var showResetDialog by remember { mutableStateOf(false) }
    var categoryToReset by remember { mutableStateOf<String?>(null) } // null bedeutet "Alle"

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = R.string.home_statistik)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = stringResource(id = R.string.content_desc_back))
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(id = R.string.stats_global_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f) // Zwingt den Text, den Platz einzunehmen und den Button nach rechts zu schieben
                    )
                    IconButton(
                        onClick = {
                            categoryToReset = null
                            showResetDialog = true
                        },
                        // Verschiebt den Button visuell um 12dp nach rechts, um das interne Padding auszugleichen
                        modifier = Modifier.offset(x = 12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Delete, 
                            contentDescription = stringResource(id = R.string.content_desc_delete),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                
                val globalTotal = userStats.globalStats.totalTestQuestions
                val globalCorrect = userStats.globalStats.correctAnswers
                val globalPercent = if (globalTotal > 0) (globalCorrect.toDouble() / globalTotal * 100).toInt() else 0

                StatRow(stringResource(id = R.string.stats_learning_interactions), userStats.globalStats.learningInteractions.toString())
                StatRow(stringResource(id = R.string.stats_tests_taken), userStats.globalStats.testsTaken.toString())
                StatRow(
                    stringResource(id = R.string.stats_success_rate), 
                    stringResource(id = R.string.stats_success_rate_value, globalPercent, globalCorrect, globalTotal)
                )
            }
            
            if (userStats.categoryStats.isNotEmpty()) {
                item {
                    Divider(
                        modifier = Modifier.padding(vertical = 16.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                        thickness = 2.dp
                    )
                    Text(
                        text = stringResource(id = R.string.stats_details_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
                
                val categories = userStats.categoryStats.keys.toList().sorted()
                items(categories) { category ->
                    val catStats = userStats.categoryStats[category]!!
                    val total = catStats.totalTestQuestions
                    val correct = catStats.correctAnswers
                    val percent = if (total > 0) (correct.toDouble() / total * 100).toInt() else 0

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = category,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground,
                                modifier = Modifier.weight(1f) // Zwingt den Text, den Platz einzunehmen und den Button nach rechts zu schieben
                            )
                            IconButton(
                                onClick = {
                                    categoryToReset = category
                                    showResetDialog = true
                                },
                                // Verschiebt den Button visuell um 12dp nach rechts, um das interne Padding auszugleichen
                                modifier = Modifier.offset(x = 12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Delete, 
                                    contentDescription = stringResource(id = R.string.content_desc_delete),
                                    tint = MaterialTheme.colorScheme.primary,
                                    // size(24.dp) entfernt, da dies der Standard für Icons in IconButtons ist
                                )
                            }
                        }
                        
                        StatRow(stringResource(id = R.string.stats_learning_interactions), catStats.learningInteractions.toString())
                        StatRow(stringResource(id = R.string.stats_tests_taken), catStats.testsTaken.toString())
                        StatRow(
                            stringResource(id = R.string.stats_success_rate_short), 
                            stringResource(id = R.string.stats_success_rate_value_short, percent, correct, total)
                        )
                        
                        Divider(
                            modifier = Modifier.padding(top = 16.dp),
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                        )
                    }
                }
            } else {
                item {
                    Spacer(modifier = Modifier.height(32.dp))
                    Text(
                        text = stringResource(id = R.string.stats_empty_message),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }

    // Reset Bestätigungs-Dialog
    if (showResetDialog) {
        val categoryName = categoryToReset ?: stringResource(id = R.string.stats_global_title)
        
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text(stringResource(id = R.string.stats_reset_title)) },
            text = { Text(stringResource(id = R.string.stats_reset_message, categoryName)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (categoryToReset == null) {
                            statsManager.resetAllStats()
                        } else {
                            statsManager.resetCategoryStats(categoryToReset!!)
                        }
                        userStats = statsManager.loadStats() // UI aktualisieren
                        showResetDialog = false
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(stringResource(id = R.string.stats_reset_yes))
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text(stringResource(id = R.string.stats_reset_no))
                }
            }
        )
    }
}

@Composable
fun StatRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label, 
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = value, 
            style = MaterialTheme.typography.bodyLarge, 
            fontWeight = FontWeight.Bold, 
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

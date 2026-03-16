package com.sks.trainer.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.sks.trainer.R
import com.sks.trainer.data.QuestionRepository
import com.sks.trainer.data.StatsManager
import com.sks.trainer.model.SksQuestion
import com.sks.trainer.util.SpeechRecognizerHelper
import com.sks.trainer.util.TextSimilarity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TestScreen(
    category: String,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val repository = remember { QuestionRepository(context) }
    val statsManager = remember { StatsManager(context) }
    val questions = remember { repository.getRandomQuestions(category) }

    var currentIndex by remember { mutableIntStateOf(0) }
    var recognizedText by remember { mutableStateOf("") }
    var similarityScore by remember { mutableStateOf<Int?>(null) }
    var isListening by remember { mutableStateOf(false) }
    var showResults by remember { mutableStateOf(false) }
    var correctCount by remember { mutableIntStateOf(0) }

    val speechHelper = remember {
        SpeechRecognizerHelper(
            context = context,
            onResult = { 
                recognizedText = it
                isListening = false
            },
            onError = { 
                isListening = false
                // Handle error (e.g., toast or message)
            }
        )
    }

    DisposableEffect(Unit) {
        onDispose { speechHelper.destroy() }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (it) {
            isListening = true
            speechHelper.startListening()
        }
    }

    if (showResults) {
        TestResultsScreen(
            correct = correctCount,
            total = questions.size,
            onClose = onBack
        )
        return
    }

    if (questions.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Keine Fragen gefunden.")
        }
        return
    }

    val currentQuestion = questions[currentIndex]

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Test: $category") },
                actions = {
                    Text(
                        text = "${currentIndex + 1}/${questions.size}",
                        modifier = Modifier.padding(end = 16.dp),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.4f),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = currentQuestion.question,
                        style = MaterialTheme.typography.headlineSmall,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // User Answer Section
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.3f),
                contentAlignment = Alignment.TopCenter
            ) {
                if (recognizedText.isNotEmpty()) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Ihre Antwort:",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = recognizedText,
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(8.dp)
                        )
                        
                        if (similarityScore != null) {
                            Text(
                                text = stringResource(id = R.string.score_label, similarityScore!!),
                                style = MaterialTheme.typography.titleLarge,
                                color = if (similarityScore!! >= 70) 
                                    MaterialTheme.colorScheme.primary 
                                else 
                                    MaterialTheme.colorScheme.error,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                    }
                }
            }

            // Controls
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (similarityScore == null) {
                    Button(
                        onClick = {
                            if (isListening) {
                                speechHelper.stopListening()
                                isListening = false
                            } else {
                                val hasPermission = ContextCompat.checkSelfPermission(
                                    context, Manifest.permission.RECORD_AUDIO
                                ) == PackageManager.PERMISSION_GRANTED
                                
                                if (hasPermission) {
                                    recognizedText = ""
                                    isListening = true
                                    speechHelper.startListening()
                                } else {
                                    permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            if (isListening) Icons.Default.Stop else Icons.Default.Mic,
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (isListening) "Stoppen" else "Antwort einsprechen")
                    }

                    if (recognizedText.isNotEmpty() && !isListening) {
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Button(
                                onClick = {
                                    recognizedText = ""
                                    isListening = true
                                    speechHelper.startListening()
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                            ) {
                                Text(stringResource(id = R.string.btn_record_again), fontSize = 12.sp)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    val score = TextSimilarity.calculateSimilarity(recognizedText, currentQuestion.answer)
                                    similarityScore = score
                                    if (score >= 70) correctCount++
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(stringResource(id = R.string.btn_confirm))
                            }
                        }
                    }
                } else {
                    // Score revealed, show correct answer and "Next"
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Richtige Antwort:", style = MaterialTheme.typography.labelSmall)
                            Text(currentQuestion.answer, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                    
                    Button(
                        onClick = {
                            if (currentIndex < questions.size - 1) {
                                currentIndex++
                                recognizedText = ""
                                similarityScore = null
                            } else {
                                statsManager.recordTestResult(correctCount, questions.size)
                                showResults = true
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(id = R.string.btn_next))
                    }
                }
            }
        }
    }
}

@Composable
fun TestResultsScreen(
    correct: Int,
    total: Int,
    onClose: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(id = R.string.test_finished),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(id = R.string.correct_answers, correct, total),
            style = MaterialTheme.typography.titleLarge
        )
        val percent = (correct.toDouble() / total * 100).toInt()
        Text(
            text = "$percent%",
            style = MaterialTheme.typography.displayLarge,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Black
        )
        Spacer(modifier = Modifier.height(48.dp))
        Button(onClick = onClose, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(id = R.string.btn_back_home))
        }
    }
}

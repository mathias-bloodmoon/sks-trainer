package com.sks.trainer.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.sks.trainer.R
import com.sks.trainer.data.QuestionRepository
import com.sks.trainer.data.StatsManager
import com.sks.trainer.util.SpeechRecognizerHelper
import com.sks.trainer.util.TextSimilarity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TestScreen(
    category: String,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    
    val repository = remember { QuestionRepository(context) }
    val statsManager = remember { StatsManager(context) }
    val questions = remember { repository.getRandomQuestions(category) }

    var currentIndex by remember { mutableIntStateOf(0) }
    var fullRecognizedText by remember { mutableStateOf("") }
    var currentPartialText by remember { mutableStateOf("") }
    var similarityScore by remember { mutableStateOf<Int?>(null) }
    var isPressing by remember { mutableStateOf(false) }
    var showResults by remember { mutableStateOf(false) }
    var correctCount by remember { mutableIntStateOf(0) }
    
    var rmsLevel by remember { mutableFloatStateOf(0f) }

    val recognizedText = buildString {
        if (fullRecognizedText.isNotEmpty()) append(fullRecognizedText)
        if (fullRecognizedText.isNotEmpty() && currentPartialText.isNotEmpty()) append(" ")
        if (currentPartialText.isNotEmpty()) append(currentPartialText)
    }

    val speechHelper = remember {
        SpeechRecognizerHelper(
            context = context,
            onPartialResult = { currentPartialText = it },
            onResult = { 
                if (it.isNotBlank()) {
                    fullRecognizedText = if (fullRecognizedText.isEmpty()) it else "$fullRecognizedText $it"
                }
                currentPartialText = ""
            },
            onError = { /* Ignoriere Timeouts */ },
            onVolumeChanged = { rmsLevel = it }
        )
    }

    DisposableEffect(Unit) {
        onDispose { speechHelper.destroy() }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (!isGranted) { /* Optional Toast */ }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )
    
    val volumeScale = (1f + (rmsLevel.coerceIn(0f, 10f) / 15f))

    if (showResults) {
        TestResultsScreen(correct = correctCount, total = questions.size, onClose = onBack)
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

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.3f),
                contentAlignment = Alignment.TopCenter
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    if (isPressing) {
                        Text(
                            text = "Ich höre zu...",
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    } else if (recognizedText.isNotEmpty()) {
                        Text(text = "Ihre Antwort:", style = MaterialTheme.typography.labelMedium)
                    }
                    
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
                            color = if (similarityScore!! >= 70) Color(0xFF4CAF50) else MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (similarityScore == null) {
                    Text(
                        text = if (isPressing) "Loslassen zum Beenden" else "Halten zum Sprechen",
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(140.dp)
                            .pointerInput(Unit) {
                                awaitPointerEventScope {
                                    while (true) {
                                        awaitFirstDown()
                                        
                                        val hasPermission = ContextCompat.checkSelfPermission(
                                            context, Manifest.permission.RECORD_AUDIO
                                        ) == PackageManager.PERMISSION_GRANTED

                                        if (hasPermission) {
                                            isPressing = true
                                            fullRecognizedText = ""
                                            currentPartialText = ""
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            speechHelper.startListening()

                                            waitForUpOrCancellation()
                                            
                                            isPressing = false
                                            rmsLevel = 0f
                                            speechHelper.stopListening()
                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        } else {
                                            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                            waitForUpOrCancellation()
                                        }
                                    }
                                }
                            }
                    ) {
                        if (isPressing) {
                            Box(
                                modifier = Modifier
                                    .size(80.dp)
                                    .scale(pulseScale * volumeScale)
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.25f), CircleShape)
                            )
                        }
                        
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape)
                                .background(if (isPressing) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = "Sprechen",
                                tint = Color.White,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    }

                    if (recognizedText.isNotEmpty() && !isPressing) {
                        Button(
                            onClick = {
                                val score = TextSimilarity.calculateSimilarity(recognizedText, currentQuestion.answer)
                                similarityScore = score
                                if (score >= 70) correctCount++
                            },
                            modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
                        ) {
                            Text(stringResource(id = R.string.btn_confirm))
                        }
                    }
                } else {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
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
                                fullRecognizedText = ""
                                currentPartialText = ""
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
fun TestResultsScreen(correct: Int, total: Int, onClose: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Test Beendet", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "Richtige Antworten: $correct / $total", style = MaterialTheme.typography.titleLarge)
        val percent = (correct.toDouble() / total * 100).toInt()
        Text(text = "$percent%", style = MaterialTheme.typography.displayLarge, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Black)
        Spacer(modifier = Modifier.height(48.dp))
        Button(onClick = onClose, modifier = Modifier.fillMaxWidth()) {
            Text("Zurück zum Start")
        }
    }
}

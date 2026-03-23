package com.sks.trainer.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
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
    val haptic = LocalHapticFeedback.current
    
    val repository = remember { QuestionRepository(context) }
    val statsManager = remember { StatsManager(context) }
    
    val questions = rememberSaveable(category) { repository.getRandomQuestions(category) }

    var currentIndex by rememberSaveable { mutableIntStateOf(0) }
    var fullRecognizedText by rememberSaveable { mutableStateOf("") }
    var currentPartialText by rememberSaveable { mutableStateOf("") } 
    var similarityScore by rememberSaveable { mutableStateOf<Int?>(null) }
    var isPressing by remember { mutableStateOf(false) } 
    var showResults by rememberSaveable { mutableStateOf(false) }
    var correctCount by rememberSaveable { mutableIntStateOf(0) }
    
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
        if (!isGranted) { 
            // Optionaler Toast / Hinweis
        }
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
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Zurück")
                    }
                },
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
            // GEMEINSAMER SCROLL-BEREICH FÜR ALLES
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                
                // 1. KARTEN-BOX: PRÜFUNGSFRAGE
                OutlinedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
                    colors = CardDefaults.outlinedCardColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.onSurface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp), 
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "PRÜFUNGSFRAGE",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Text(
                                text = currentQuestion.question,
                                style = MaterialTheme.typography.headlineSmall, // Zentrale Font Size
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                // 2. KARTEN-BOX: IHRE ANTWORT
                if (isPressing || recognizedText.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(24.dp))
                    OutlinedCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
                        colors = CardDefaults.outlinedCardColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                            contentColor = MaterialTheme.colorScheme.onSurface
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp), 
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = if (isPressing && recognizedText.isEmpty()) "ICH HÖRE ZU..." else "IHRE ANTWORT",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                
                                Text(
                                    text = recognizedText,
                                    // SELBE FONT SIZE WIE DIE PRÜFUNGSFRAGE
                                    style = MaterialTheme.typography.headlineSmall, 
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }

                // 3. KARTEN-BOX: RICHTIGE ANTWORT
                if (similarityScore != null) {
                    Spacer(modifier = Modifier.height(24.dp))
                    OutlinedCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
                        colors = CardDefaults.outlinedCardColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                            contentColor = MaterialTheme.colorScheme.onSurface
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp), 
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "RICHTIGE ANTWORT",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                
                                Text(
                                    text = currentQuestion.answer,
                                    // SELBE FONT SIZE WIE DIE PRÜFUNGSFRAGE
                                    style = MaterialTheme.typography.headlineSmall, 
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
                
                // Etwas Platz am Ende des scrollbaren Bereichs, damit es nicht direkt am Button klebt
                Spacer(modifier = Modifier.height(16.dp))
            } 

            // FESTER BEREICH UNTEN: Mic Button links, Text darunter. Bestätigen/Nächste Button rechts.
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween // Schiebt das Mic nach links und den Button nach rechts
            ) {
                // Linke Seite: Entweder Mikrofon oder das kalkulierte Ergebnis
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.weight(0.4f) // Nimmt etwa 40% des Platzes unten ein
                ) {
                    if (similarityScore == null) {
                        // Noch nicht bestätigt -> Zeige Mikrofon
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(88.dp)
                                .pointerInput(Unit) {
                                    awaitPointerEventScope {
                                        while (true) {
                                            awaitFirstDown()
                                            
                                            val hasPermission = ContextCompat.checkSelfPermission(
                                                context, Manifest.permission.RECORD_AUDIO
                                            ) == PackageManager.PERMISSION_GRANTED

                                            if (hasPermission) {
                                                isPressing = true
                                                if (similarityScore != null) {
                                                    // Wenn bereits eine Punktzahl existiert, starten wir eine neue Antwort für dieselbe Frage
                                                    similarityScore = null 
                                                }
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
                                        .size(64.dp)
                                        .scale(pulseScale * volumeScale)
                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.25f), CircleShape)
                                )
                            }
                            
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .background(if (isPressing) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Mic,
                                    contentDescription = "Sprechen",
                                    tint = Color.White,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }
                        
                        // Text unterhalb des Mikrofons
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (isPressing) "Loslassen zum Beenden" else "Halten zum Sprechen",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                            textAlign = TextAlign.Center
                        )
                    } else {
                        // Wurde bestätigt -> Zeige Ergebnis anstelle des Mikrofons an
                        Text(
                            text = stringResource(id = R.string.score_label, similarityScore!!),
                            style = MaterialTheme.typography.titleLarge,
                            // Die Farbcodierung wurde entfernt, der Text ist nun immer schwarz
                            color = MaterialTheme.colorScheme.onBackground,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                // Rechte Seite: Bestätigen oder Nächste-Button
                Box(
                    modifier = Modifier.weight(0.6f), // Nimmt die restlichen 60% des Platzes ein
                    contentAlignment = Alignment.CenterEnd // Richtet den Button rechts aus
                ) {
                    if (similarityScore == null) {
                        // Zeige den Bestätigen-Button an, sobald man gesprochen hat
                        Button(
                            onClick = {
                                val score = TextSimilarity.calculateSimilarity(recognizedText, currentQuestion.answer)
                                similarityScore = score
                                if (score >= 70) correctCount++
                            },
                            enabled = recognizedText.isNotEmpty() && !isPressing,
                            shape = MaterialTheme.shapes.medium,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 16.dp)
                                .height(56.dp) // Button etwas höher machen für bessere Bedienbarkeit
                        ) {
                            Text(stringResource(id = R.string.btn_confirm), fontWeight = FontWeight.Bold)
                        }
                    } else {
                        // Zeige den Nächste-Button an, wenn die Auswertung da ist
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
                            shape = MaterialTheme.shapes.medium,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 16.dp)
                                .height(56.dp)
                        ) {
                            Text(stringResource(id = R.string.btn_next), fontWeight = FontWeight.Bold)
                        }
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
        Button(
            onClick = onClose, 
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier.fillMaxWidth().height(48.dp)
        ) {
            Text("Zurück zum Start", fontWeight = FontWeight.Bold)
        }
    }
}

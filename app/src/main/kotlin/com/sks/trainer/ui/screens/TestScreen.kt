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
import androidx.compose.material.icons.filled.ThumbDown
import androidx.compose.material.icons.filled.ThumbUp
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.sks.trainer.R
import com.sks.trainer.data.QuestionRepository
import com.sks.trainer.data.StatsManager
import com.sks.trainer.util.SpeechRecognizerHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TestScreen(
    category: String,
    bookmarksOnly: Boolean = false,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    
    val repository = remember { QuestionRepository(context) }
    val statsManager = remember { StatsManager(context) }
    
    val questions = rememberSaveable(category, bookmarksOnly) { 
        repository.getRandomQuestions(category, bookmarksOnly) 
    }

    var currentIndex by rememberSaveable { mutableIntStateOf(0) }
    var fullRecognizedText by rememberSaveable { mutableStateOf("") }
    var currentPartialText by rememberSaveable { mutableStateOf("") } 
    var isPressing by remember { mutableStateOf(false) } 
    var correctCount by rememberSaveable { mutableIntStateOf(0) }
    
    var needsConfirmation by rememberSaveable { mutableStateOf(false) }
    var needsEvaluation by rememberSaveable { mutableStateOf(false) }
    
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

    LaunchedEffect(recognizedText, isPressing) {
        if (recognizedText.isNotBlank() && !isPressing && !needsEvaluation) {
            needsConfirmation = true
        } else if (recognizedText.isBlank()) {
            needsConfirmation = false
        }
    }

    DisposableEffect(Unit) {
        onDispose { speechHelper.destroy() }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ -> }

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

    if (questions.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = if (bookmarksOnly) stringResource(id = R.string.no_bookmarks_found) else stringResource(id = R.string.no_questions_found),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(16.dp)
                )
                Button(onClick = onBack) {
                    Text(stringResource(id = R.string.btn_back_home))
                }
            }
        }
        return
    }

    if (currentIndex >= questions.size) {
        TestResultsScreen(correct = correctCount, total = questions.size, onClose = onBack)
        return
    }

    val currentQuestion = questions[currentIndex]

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column(modifier = Modifier.fillMaxWidth()) {
                        val title = stringResource(id = R.string.title_test_category, category)
                        
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        
                        if (bookmarksOnly) {
                            Text(
                                text = stringResource(id = R.string.category_bookmarks),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = stringResource(id = R.string.content_desc_back))
                    }
                },
                actions = {
                    Box(
                        modifier = Modifier
                            .background(
                                color = Color(0xFFF0F0F0),
                                shape = MaterialTheme.shapes.small
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                            .align(Alignment.CenterVertically)
                    ) {
                        Text(
                            text = "${currentIndex + 1} / ${questions.size}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
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
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
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
                                text = stringResource(id = R.string.test_question_label),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            if (currentQuestion.questionImage != null) {
                                AssetImage(
                                    fileName = currentQuestion.questionImage,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(max = 180.dp)
                                        .padding(bottom = 16.dp)
                                )
                            }

                            Text(
                                text = currentQuestion.question,
                                style = MaterialTheme.typography.headlineSmall, 
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

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
                                    text = if (isPressing && recognizedText.isEmpty()) stringResource(id = R.string.test_listening) else stringResource(id = R.string.test_your_answer),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                
                                Text(
                                    text = recognizedText,
                                    style = MaterialTheme.typography.headlineSmall, 
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }

                if (needsEvaluation) {
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
                                    text = stringResource(id = R.string.test_correct_answer),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                
                                if (currentQuestion.answerImage != null) {
                                    AssetImage(
                                        fileName = currentQuestion.answerImage,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .heightIn(max = 180.dp)
                                            .padding(bottom = 16.dp)
                                    )
                                }

                                Text(
                                    text = currentQuestion.answer,
                                    style = MaterialTheme.typography.headlineSmall, 
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
            } 

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (!needsEvaluation) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(0.4f) 
                    ) {
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
                                                fullRecognizedText = ""
                                                currentPartialText = ""
                                                needsConfirmation = false
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
                                    contentDescription = stringResource(id = R.string.content_desc_speak),
                                    tint = Color.White,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (isPressing) stringResource(id = R.string.test_release_to_stop) else stringResource(id = R.string.test_hold_to_speak),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                            textAlign = TextAlign.Center
                        )
                    }

                    Box(
                        modifier = Modifier.weight(0.6f), 
                        contentAlignment = Alignment.CenterEnd 
                    ) {
                        Button(
                            onClick = {
                                needsEvaluation = true
                            },
                            enabled = needsConfirmation,
                            shape = MaterialTheme.shapes.medium,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 16.dp)
                                .height(56.dp)
                        ) {
                            Text(stringResource(id = R.string.btn_confirm), fontWeight = FontWeight.Bold)
                        }
                    }
                } else {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = stringResource(id = R.string.test_evaluate_answer),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Button(
                                onClick = {
                                    if (currentIndex < questions.size - 1) {
                                        currentIndex++
                                        fullRecognizedText = ""
                                        needsConfirmation = false
                                        needsEvaluation = false
                                    } else {
                                        statsManager.recordTestResult(category, correctCount, questions.size)
                                        currentIndex++ // Trigger the end screen
                                    }
                                },
                                shape = MaterialTheme.shapes.medium,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(56.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.error
                                )
                            ) {
                                Icon(Icons.Default.ThumbDown, contentDescription = stringResource(id = R.string.content_desc_wrong), tint = Color.White)
                            }

                            Button(
                                onClick = {
                                    correctCount++
                                    if (currentIndex < questions.size - 1) {
                                        currentIndex++
                                        fullRecognizedText = ""
                                        needsConfirmation = false
                                        needsEvaluation = false
                                    } else {
                                        statsManager.recordTestResult(category, correctCount, questions.size)
                                        currentIndex++ // Trigger the end screen
                                    }
                                },
                                shape = MaterialTheme.shapes.medium,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(56.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF4CAF50)
                                )
                            ) {
                                Icon(Icons.Default.ThumbUp, contentDescription = stringResource(id = R.string.content_desc_correct), tint = Color.White)
                            }
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
        Text(text = stringResource(id = R.string.test_finished), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = stringResource(id = R.string.correct_answers, correct, total), style = MaterialTheme.typography.titleLarge)
        val percent = if (total > 0) (correct.toDouble() / total * 100).toInt() else 0
        Text(text = "$percent%", style = MaterialTheme.typography.displayLarge, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Black)
        Spacer(modifier = Modifier.height(48.dp))
        Button(
            onClick = onClose, 
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier.fillMaxWidth().height(48.dp)
        ) {
            Text(stringResource(id = R.string.btn_back_home), fontWeight = FontWeight.Bold)
        }
    }
}

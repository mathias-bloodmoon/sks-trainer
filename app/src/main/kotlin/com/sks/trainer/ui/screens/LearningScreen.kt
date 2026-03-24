package com.sks.trainer.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.sks.trainer.R
import com.sks.trainer.data.QuestionRepository
import com.sks.trainer.data.StatsManager
import com.sks.trainer.model.SksQuestion
import com.sks.trainer.ui.theme.SksBlueLight

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LearningScreen(
    category: String,
    bookmarksOnly: Boolean = false,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val repository = remember { QuestionRepository(context) }
    val statsManager = remember { StatsManager(context) }
    
    // rememberSaveable sorgt dafür, dass die Daten bei Rotation erhalten bleiben
    val questions = remember(category, bookmarksOnly) { repository.getQuestions(category, bookmarksOnly) }
    var currentIndex by rememberSaveable { mutableIntStateOf(0) }
    var isFlipped by rememberSaveable { mutableStateOf(false) }

    if (questions.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = if (bookmarksOnly) 
                        stringResource(id = R.string.no_bookmarks_found)
                    else stringResource(id = R.string.no_questions_found),
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

    // Falls currentIndex durch Filter-Wechsel ungültig wird (Sicherheitscheck)
    if (currentIndex >= questions.size) {
        currentIndex = 0
    }

    val currentQuestion = questions[currentIndex]
    var isBookmarked by remember(currentQuestion.id) { mutableStateOf(statsManager.isBookmarked(currentQuestion.id)) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = category,
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
            Text(
                text = stringResource(id = R.string.question_progress, currentIndex + 1, questions.size),
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Flashcard(
                question = currentQuestion,
                isFlipped = isFlipped,
                isBookmarked = isBookmarked,
                onFlip = { 
                    isFlipped = !isFlipped 
                    if (isFlipped) statsManager.incrementLearning(currentQuestion.category)
                },
                onBookmarkToggle = {
                    statsManager.toggleBookmark(currentQuestion.id)
                    isBookmarked = !isBookmarked
                },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(
                    onClick = {
                        if (currentIndex > 0) {
                            currentIndex--
                            isFlipped = false
                        }
                    },
                    enabled = currentIndex > 0,
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.height(48.dp)
                ) {
                    Text(stringResource(id = R.string.btn_previous), fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = {
                        if (currentIndex < questions.size - 1) {
                            currentIndex++
                            isFlipped = false
                        } else {
                            onBack()
                        }
                    },
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.height(48.dp)
                ) {
                    Text(
                        text = if (currentIndex < questions.size - 1) stringResource(id = R.string.btn_next_question) else stringResource(id = R.string.btn_finish),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun Flashcard(
    question: SksQuestion,
    isFlipped: Boolean,
    isBookmarked: Boolean,
    onFlip: () -> Unit,
    onBookmarkToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val rotation by animateFloatAsState(
        targetValue = if (isFlipped) 180f else 0f,
        label = "cardRotation"
    )

    val cardColor = if (rotation > 90f) SksBlueLight else MaterialTheme.colorScheme.primary

    Card(
        modifier = modifier.clickable { onFlip() },
        colors = CardDefaults.cardColors(
            containerColor = cardColor,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Oberer statischer Bereich
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Links: "Frage" oder "Antwort"
                Text(
                    text = if (rotation <= 90f) stringResource(id = R.string.flashcard_question) 
                           else stringResource(id = R.string.flashcard_answer),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary
                )

                // Rechts: Favoriten-Funktion
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable(onClick = { onBookmarkToggle() })
                ) {
                    Text(
                        text = stringResource(id = R.string.content_desc_bookmark),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                        modifier = Modifier.padding(end = 4.dp)
                    )
                    Icon(
                        imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                        contentDescription = null,
                        tint = if (isBookmarked) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Divider(
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.1f),
                thickness = 1.dp,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            // Unterer Bereich der sich dreht
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        rotationY = rotation
                        cameraDistance = 12f * density
                    },
                contentAlignment = Alignment.Center
            ) {
                if (rotation <= 90f) {
                    // Vorderseite (Frage)
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        if (question.questionImage != null) {
                            AssetImage(
                                fileName = question.questionImage,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 200.dp)
                                    .padding(bottom = 16.dp)
                            )
                        }
                        Text(
                            text = question.question,
                            style = MaterialTheme.typography.titleMedium,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    // Rückseite (Antwort)
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer { rotationY = 180f }
                            .verticalScroll(rememberScrollState())
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        if (question.answerImage != null) {
                            AssetImage(
                                fileName = question.answerImage,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 200.dp)
                                    .padding(bottom = 16.dp)
                            )
                        }
                        Text(
                            text = question.answer,
                            style = MaterialTheme.typography.titleMedium,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

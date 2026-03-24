package com.sks.trainer.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.sks.trainer.R
import com.sks.trainer.data.QuestionRepository

/**
 * Screen to select a category for learning or testing.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategorySelectionScreen(
    mode: String,
    onCategorySelected: (String, Boolean) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val repository = remember { QuestionRepository(context) }

    val categories = listOf(
        stringResource(id = R.string.category_navigation),
        stringResource(id = R.string.category_recht),
        stringResource(id = R.string.category_wetter),
        stringResource(id = R.string.category_seemannschaft)
    ).let {
        if (mode == "test") it + stringResource(id = R.string.category_random) else it
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        if (mode == "lernen") stringResource(id = R.string.title_category_lernen) 
                        else stringResource(id = R.string.title_category_test)
                    ) 
                },
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
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            items(categories) { category ->
                CategorySection(
                    category = category,
                    allCount = repository.getQuestionCount(category),
                    bookmarkCount = repository.getBookmarkCount(category),
                    onSelectAll = { onCategorySelected(category, false) },
                    onSelectBookmarks = { onCategorySelected(category, true) }
                )
            }
        }
    }
}

@Composable
fun CategorySection(
    category: String,
    allCount: Int,
    bookmarkCount: Int,
    onSelectAll: () -> Unit,
    onSelectBookmarks: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = category,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
        )
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min), // Stellt sicher, dass beide Buttons gleich hoch sind
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Button für Alle Fragen
            Button(
                onClick = onSelectAll,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .heightIn(min = 64.dp),
                shape = MaterialTheme.shapes.medium,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text(
                    text = stringResource(id = R.string.btn_all_questions, allCount),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }

            // Button für Gemerkte Fragen
            Button(
                onClick = onSelectBookmarks,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .heightIn(min = 64.dp),
                enabled = bookmarkCount > 0,
                shape = MaterialTheme.shapes.medium,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text(
                    text = stringResource(id = R.string.btn_bookmarked_questions, bookmarkCount),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

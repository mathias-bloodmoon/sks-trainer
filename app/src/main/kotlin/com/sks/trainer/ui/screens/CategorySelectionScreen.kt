package com.sks.trainer.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.sks.trainer.R

/**
 * Screen to select a category for learning or testing.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategorySelectionScreen(
    mode: String,
    onCategorySelected: (String) -> Unit,
    onBack: () -> Unit
) {
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
                title = { Text(if (mode == "lernen") "Lernen: Thema wählen" else "Test: Thema wählen") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Zurück")
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(categories) { category ->
                Card(
                    onClick = { onCategorySelected(category) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = category,
                        modifier = Modifier.padding(24.dp),
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }
    }
}

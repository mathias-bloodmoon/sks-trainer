package com.sks.trainer.ui.screens

import android.text.method.LinkMovementMethod
import android.widget.TextView
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.text.HtmlCompat
import com.sks.trainer.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LegalInfoScreen(onBack: () -> Unit) {
    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf(
        stringResource(id = R.string.legal_imprint_title),
        stringResource(id = R.string.legal_privacy_title),
        stringResource(id = R.string.legal_licenses_title)
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = R.string.title_legal_info)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = stringResource(id = R.string.content_desc_back)
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            TabRow(selectedTabIndex = selectedTabIndex) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = { 
                            Text(
                                text = title, 
                                maxLines = 2, 
                                modifier = Modifier.padding(vertical = 8.dp)
                            ) 
                        }
                    )
                }
            }

            val scrollState = rememberScrollState()
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(16.dp)
            ) {
                val currentTextRes = when (selectedTabIndex) {
                    0 -> R.string.legal_imprint_text
                    1 -> R.string.legal_privacy_text
                    2 -> R.string.legal_licenses_text
                    else -> R.string.legal_imprint_text
                }
                
                HtmlText(htmlResId = currentTextRes)
            }
        }
    }
}

@Composable
fun HtmlText(htmlResId: Int) {
    val htmlString = stringResource(id = htmlResId)
    val textColor = MaterialTheme.colorScheme.onBackground.toArgb()
    val linkColor = MaterialTheme.colorScheme.primary.toArgb()

    AndroidView(
        modifier = Modifier.fillMaxWidth(),
        factory = { ctx ->
            TextView(ctx).apply {
                movementMethod = LinkMovementMethod.getInstance()
                setTextColor(textColor)
                setLinkTextColor(linkColor)
                textSize = 16f
            }
        },
        update = { textView ->
            textView.text = HtmlCompat.fromHtml(htmlString, HtmlCompat.FROM_HTML_MODE_COMPACT)
            textView.setTextColor(textColor)
            textView.setLinkTextColor(linkColor)
        }
    )
}

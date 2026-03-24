package com.sks.trainer.ui.screens

import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sks.trainer.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToLernen: () -> Unit,
    onNavigateToTest: () -> Unit,
    onNavigateToStats: () -> Unit,
    onNavigateToLegalInfo: () -> Unit
) {
    val context = LocalContext.current
    val shareText = stringResource(id = R.string.share_text)
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState), 
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        Text(
            text = stringResource(id = R.string.app_name),
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 48.dp, top = 32.dp)
        )

        Row(modifier = Modifier.fillMaxWidth()) {
            HomeButton(
                text = stringResource(id = R.string.home_lernen),
                iconPainter = painterResource(id = R.drawable.icon_home_lernen), 
                modifier = Modifier.weight(1f),
                onClick = onNavigateToLernen
            )
            Spacer(modifier = Modifier.width(16.dp))
            HomeButton(
                text = stringResource(id = R.string.home_test),
                iconPainter = painterResource(id = R.drawable.icon_home_test), 
                modifier = Modifier.weight(1f),
                onClick = onNavigateToTest
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(modifier = Modifier.fillMaxWidth()) {
            HomeButton(
                text = stringResource(id = R.string.home_statistik),
                iconPainter = painterResource(id = R.drawable.icon_home_statistik), 
                modifier = Modifier.weight(1f),
                onClick = onNavigateToStats
            )
            Spacer(modifier = Modifier.width(16.dp))
            HomeButton(
                text = stringResource(id = R.string.home_share),
                iconPainter = painterResource(id = R.drawable.icon_home_empfehlen), 
                modifier = Modifier.weight(1f),
                onClick = {
                    val sendIntent: Intent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_TEXT, shareText)
                        type = "text/plain"
                    }
                    val shareIntent = Intent.createChooser(sendIntent, null)
                    context.startActivity(shareIntent)
                }
            )
        }
        
        Spacer(modifier = Modifier.height(48.dp))

        TextButton(onClick = onNavigateToLegalInfo) {
            Text(
                text = stringResource(id = R.string.title_legal_info),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeButton(
    text: String,
    iconPainter: Painter,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    OutlinedCard(
        onClick = onClick,
        modifier = modifier.height(160.dp),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
        colors = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = iconPainter,
                contentDescription = null,
                modifier = Modifier.size(72.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = text,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

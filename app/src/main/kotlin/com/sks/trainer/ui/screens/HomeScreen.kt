package com.sks.trainer.ui.screens

import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sks.trainer.R

@Composable
fun HomeScreen(
    onNavigateToLernen: () -> Unit,
    onNavigateToTest: () -> Unit,
    onNavigateToStats: () -> Unit
) {
    val context = LocalContext.current
    val shareText = stringResource(id = R.string.share_text)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(id = R.string.app_name),
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        Row(modifier = Modifier.fillMaxWidth()) {
            HomeButton(
                text = stringResource(id = R.string.home_lernen),
                icon = Icons.Default.School,
                modifier = Modifier.weight(1f),
                onClick = onNavigateToLernen
            )
            Spacer(modifier = Modifier.width(16.dp))
            HomeButton(
                text = stringResource(id = R.string.home_test),
                icon = Icons.Default.Timer,
                modifier = Modifier.weight(1f),
                onClick = onNavigateToTest
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(modifier = Modifier.fillMaxWidth()) {
            HomeButton(
                text = stringResource(id = R.string.home_statistik),
                icon = Icons.Default.BarChart,
                modifier = Modifier.weight(1f),
                onClick = onNavigateToStats
            )
            Spacer(modifier = Modifier.width(16.dp))
            HomeButton(
                text = stringResource(id = R.string.home_share),
                icon = Icons.Default.Share,
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
    }
}

@Composable
fun HomeButton(
    text: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .height(120.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = text,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp
            )
        }
    }
}

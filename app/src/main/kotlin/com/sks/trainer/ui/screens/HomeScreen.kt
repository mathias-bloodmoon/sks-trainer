package com.sks.trainer.ui.screens

import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
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
        // App Icon (geändert von Icon zu Image ohne Einfärbung)
        Image(
            painter = painterResource(id = R.drawable.ic_sks_lighthouse),
            contentDescription = null,
            modifier = Modifier.size(120.dp).padding(bottom = 16.dp)
        )
        
        Text(
            text = stringResource(id = R.string.app_name),
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 32.dp)
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
    }
}

@Composable
fun HomeButton(
    text: String,
    iconPainter: Painter,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .height(160.dp), // Höhe erhöht, damit das große Icon genug Platz hat
        shape = RoundedCornerShape(12.dp),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp, pressedElevation = 0.dp), // Kein Schatten für transparenten Look
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent, // Pinker Hintergrund entfernt
            contentColor = MaterialTheme.colorScheme.onBackground // Textfarbe passt sich an Light/Dark Mode an
        )
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = iconPainter,
                contentDescription = null,
                modifier = Modifier.size(80.dp) // Icons doppelt so groß (von 40 auf 80)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = text,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp
            )
        }
    }
}

package com.sks.trainer.ui.screens

import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
    
    // Wir merken uns den Scroll-Zustand
    val scrollState = rememberScrollState()

    // .verticalScroll(scrollState) macht die Spalte scrollbar, wenn der Platz nicht ausreicht!
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
            modifier = Modifier.padding(bottom = 32.dp, top = 32.dp) // Oben auch etwas Padding, falls man scrollt
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
        
        // Etwas Platz am Ende der scrollbaren Liste
        Spacer(modifier = Modifier.height(32.dp))
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
                fontSize = 18.sp
            )
        }
    }
}

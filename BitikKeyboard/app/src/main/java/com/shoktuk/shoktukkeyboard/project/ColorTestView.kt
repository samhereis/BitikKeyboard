package com.shoktuk.shoktukkeyboard.ui.test

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.shoktuk.shoktukkeyboard.ui.theme.ShoktukKeyboardTheme

@Composable
fun ColorTestView() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors()[0]), contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                onClick = { },
                colors = ButtonDefaults.buttonColors(containerColor = colors()[1]),
                modifier = Modifier
                    .padding(16.dp)
                    .scale(2f)
            ) {
                Text("Test Colors", color = MaterialTheme.colorScheme.onSurface)
            }

            Button(
                onClick = { },
                colors = ButtonDefaults.buttonColors(containerColor = colors()[4]),
                modifier = Modifier
                    .padding(16.dp)
                    .scale(2f)
            ) {
                Text("Test Colors", color = MaterialTheme.colorScheme.onSurface)
            }

            Button(
                onClick = { },
                colors = ButtonDefaults.buttonColors(containerColor = colors()[5]),
                modifier = Modifier
                    .padding(16.dp)
                    .scale(2f)
            ) {
                Text("Test Colors", color = MaterialTheme.colorScheme.onSurface)
            }

            Button(
                onClick = { },
                colors = ButtonDefaults.buttonColors(containerColor = colors()[6]),
                modifier = Modifier
                    .padding(16.dp)
                    .scale(2f)
            ) {
                Text("Test Colors")
            }
        }
    }
}

@Composable
fun colors(): List<Color> = listOf(
    MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.5f),
    MaterialTheme.colorScheme.surfaceContainerHigh,
    MaterialTheme.colorScheme.onSurface,
    MaterialTheme.colorScheme.onSurface,
    MaterialTheme.colorScheme.onPrimary,
    MaterialTheme.colorScheme.errorContainer,
    MaterialTheme.colorScheme.surfaceTint
)

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun ColorTestView_Preview() {
    ShoktukKeyboardTheme {
        ColorTestView()
    }
}
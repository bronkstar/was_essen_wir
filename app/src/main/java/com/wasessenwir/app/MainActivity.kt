package com.wasessenwir.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.wasessenwir.app.ui.theme.WasEssenWirTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WasEssenWirTheme {
                WasEssenWirApp()
            }
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun WasEssenWirApp() {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text(text = "was_essen_wir") })
        }
    ) { padding ->
        Text(
            text = "App-Setup abgeschlossen.",
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun WasEssenWirAppPreview() {
    WasEssenWirTheme {
        WasEssenWirApp()
    }
}

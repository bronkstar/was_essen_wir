package com.wasessenwir.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.res.stringResource
import androidx.compose.foundation.layout.height
import androidx.compose.ui.unit.dp
import com.wasessenwir.app.ui.AppViewModel
import com.wasessenwir.app.ui.screens.AppScreen
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
    val viewModel: AppViewModel = viewModel()
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.app_name),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = androidx.compose.ui.graphics.Color.Transparent),
                modifier = Modifier.height(64.dp)
            )
        }
    ) { padding ->
        AppScreen(viewModel = viewModel, modifier = Modifier.padding(padding))
    }
}

@Preview(showBackground = true)
@Composable
private fun WasEssenWirAppPreview() {
    WasEssenWirTheme {
        WasEssenWirApp()
    }
}

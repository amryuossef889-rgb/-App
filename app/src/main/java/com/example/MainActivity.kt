package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.navigation.AppNavigation
import com.example.ui.screens.settings.SettingsViewModel
import com.example.ui.theme.SunnahTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as SunnahApplication

        setContent {
            val settingsViewModel: SettingsViewModel = viewModel(
                factory = SettingsViewModel.provideFactory(this, app.settingsRepository)
            )
            val settings by settingsViewModel.settings.collectAsState()

            SunnahTheme(
                themeMode = settings.themeMode,
                fontSize = settings.fontSize
            ) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AppNavigation(settingsViewModel = settingsViewModel)
                }
            }
        }
    }
}

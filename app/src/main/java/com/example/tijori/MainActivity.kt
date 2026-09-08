package com.example.tijori

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.tijori.ui.screen.HomeScreen
import com.example.tijori.ui.screen.LoginScreen
import com.example.tijori.ui.theme.TijoriTheme

private enum class Screen { HOME, LOGIN }

class MainActivity : ComponentActivity() {
    private val webClientId = "343233879164-a41eqdtau7rjtp67tk02n86pgnpiqppq.apps.googleusercontent.com"

    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TijoriTheme {
                var currentScreen by remember { mutableStateOf(Screen.HOME) }

                when (currentScreen) {
                    Screen.HOME -> HomeScreen(
                        modifier = Modifier.fillMaxSize(),
                        onNavigateToLogin = { currentScreen = Screen.LOGIN }
                    )
                    Screen.LOGIN -> LoginScreen(
                        modifier = Modifier.fillMaxSize(),
                        webClientId,
                        onNavigateBack = { currentScreen = Screen.HOME },
                        onSignInSuccess = {

                        }
                    )
                }
            }
        }
    }
}


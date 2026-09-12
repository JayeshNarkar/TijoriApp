package com.example.tijori

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.tijori.data.entities.ThemeMode
import com.example.tijori.ui.screen.LoginScreen
import com.example.tijori.ui.screen.MainScreen
import com.example.tijori.ui.theme.TijoriTheme
import com.example.tijori.ui.viewmodel.AppConfigDBViewModel
import com.example.tijori.ui.viewmodel.AppConfigLoadState
import com.example.tijori.ui.viewmodel.UserDBViewModel
import com.example.tijori.ui.viewmodel.UserLoadState
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val webClientId =
        "343233879164-tem1irtfn2onhkhj917f0r70up4pjaoo.apps.googleusercontent.com"

    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        var isReady = false
        splashScreen.setKeepOnScreenCondition { !isReady }

        setContent {
            TijoriTheme {
                val viewModel: UserDBViewModel = hiltViewModel()
                val userState by viewModel.currentUserState.collectAsState()

                val configViewModel: AppConfigDBViewModel = hiltViewModel()
                val configState by configViewModel.configState.collectAsState()

                LaunchedEffect(userState, configState) {
                    if (userState is UserLoadState.Loaded && configState is AppConfigLoadState.Loaded) {
                        isReady = true
                    }
                }

                val systemDark = isSystemInDarkTheme()
                val darkTheme =
                    when ((configState as? AppConfigLoadState.Loaded)?.config?.themeMode) {
                        ThemeMode.LIGHT -> false
                        ThemeMode.DARK -> true
                        ThemeMode.SYSTEM, null -> systemDark
                    }

                TijoriTheme(darkTheme = darkTheme) {
                    when (val state = userState) {
                        is UserLoadState.Loading -> {
                            // Splash screen is still covering the window; render nothing.
                        }

                        is UserLoadState.Loaded -> {
                            if (state.user != null) {
                                MainScreen(modifier = Modifier.fillMaxSize())
                            } else {
                                LoginScreen(
                                    modifier = Modifier.fillMaxSize(),
                                    webClientId = webClientId
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
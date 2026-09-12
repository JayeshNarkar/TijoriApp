package com.example.tijori.ui.screen

import android.content.Context
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.credentials.GetCredentialRequest
import com.example.tijori.SignInHelper.signIn
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import kotlinx.coroutines.launch
import com.example.tijori.R
import com.example.tijori.generateSecureRandomNonce
import com.example.tijori.ui.viewmodel.UserDBViewModel
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel

//@Preview
@RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun LoginScreen(
    modifier: Modifier = Modifier,
    webClientId: String = "",
    viewModel: UserDBViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var isSigningIn by remember { mutableStateOf(false) }
    val isDarkTheme = isSystemInDarkTheme()

    Scaffold(
        modifier = modifier
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Button(
                onClick = {
                    if (!isSigningIn) {
                        isSigningIn = true
                        Log.d("LoginINFO", "Webclientid: $webClientId")
                        val signInWithGoogleOption: GetSignInWithGoogleOption =
                            GetSignInWithGoogleOption.Builder(serverClientId = webClientId)
                                .setNonce(generateSecureRandomNonce()).build()

                        val request: GetCredentialRequest = GetCredentialRequest.Builder()
                            .addCredentialOption(signInWithGoogleOption).build()
                        coroutineScope.launch {
                            signIn(request, context, onSuccess = { userDetails ->
                                val (email, displayName, profilePictureURI) = userDetails


                                viewModel.addUser(
                                    email, displayName, profilePictureURI
                                )
                            }, onFailure = { errorMessage ->
                                Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                            })
                            isSigningIn = false
                        }
                    }
                },
                enabled = !isSigningIn,
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, Color(0xFFDADCE0)),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 6.dp),
                modifier = Modifier
                    .align(Alignment.Center)
                    .height(48.dp)
            ) {
                if (isSigningIn) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp), strokeWidth = 2.dp
                    )
                } else {
                    Image(
                        painter = painterResource(
                            id = if (isDarkTheme) R.drawable.google_logo_dark else R.drawable.google_logo_light
                        ), contentDescription = "Google Logo", modifier = Modifier.size(25.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Sign in with Google", style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
@Preview
@Composable
private fun LoginScreenPreview() {
    LoginScreen()
}
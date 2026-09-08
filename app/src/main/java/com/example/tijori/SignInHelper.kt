package com.example.tijori

import android.content.Context
import android.os.Build
import androidx.compose.ui.res.painterResource
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialCustomException
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import androidx.credentials.exceptions.NoCredentialException
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.credentials.GetCredentialRequest
import java.security.SecureRandom
import java.util.Base64

object SignInHelper {
    private const val TAG = "SignInHelper";
    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    suspend fun signIn(request: GetCredentialRequest, context: Context): Exception? {
        val credentialManager = CredentialManager.create(context)
        val failureMessage = "Sign in failed!"
        //using delay() here helps prevent NoCredentialException when the BottomSheet Flow is triggered
        //on the initial running of our app
        delay(250)
        return try {
            // The getCredential is called to request a credential from Credential Manager.
            val result = credentialManager.getCredential(
                request = request,
                context = context,
            )
            Log.i(TAG, result.toString())

            val credential = result.credential
            if (credential is CustomCredential &&
                credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                Log.i(TAG, "Signed in as: ${googleIdTokenCredential.id}")
            }

            Toast.makeText(context, "Sign in successful!", Toast.LENGTH_SHORT).show()
            Log.i(TAG, "(☞ﾟヮﾟ)☞  Sign in Successful!  ☜(ﾟヮﾟ☜)")
            null
        } catch (e: GoogleIdTokenParsingException) {
            Toast.makeText(context, failureMessage, Toast.LENGTH_SHORT).show()
            Log.e(TAG, failureMessage + ": Issue with parsing received GoogleIdToken", e)
            e
        } catch (e: NoCredentialException) {
            Toast.makeText(context, failureMessage, Toast.LENGTH_SHORT).show()
            Log.e(TAG, failureMessage + ": No credentials found", e)
            e
        } catch (e: GetCredentialCancellationException) {
            Toast.makeText(context, "Sign-in cancelled", Toast.LENGTH_SHORT).show()
            Log.e(TAG, failureMessage + ": Sign-in was cancelled", e)
            e
        } catch (e: GetCredentialCustomException) {
            Toast.makeText(context, failureMessage, Toast.LENGTH_SHORT).show()
            Log.e(TAG, failureMessage + ": Issue with custom credential request", e)
            e
        } catch (e: GetCredentialException) {
            Toast.makeText(context, failureMessage, Toast.LENGTH_SHORT).show()
            Log.e(TAG, failureMessage + ": Failure getting credentials", e)
            e
        }
    }
}

fun generateSecureRandomNonce(byteLength: Int = 32): String {
    val randomBytes = ByteArray(byteLength)
    SecureRandom.getInstanceStrong().nextBytes(randomBytes)
    return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes)
}
package com.example.tijori

import android.content.Context
import android.os.Build
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialCustomException
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import androidx.credentials.exceptions.NoCredentialException
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.credentials.GetCredentialRequest
import java.security.SecureRandom
import java.util.Base64

data class UserDetails(
    val email: String,
    val displayName: String?,
    var profilePictureUri: String
)


object SignInHelper {
    private const val TAG = "SignInHelper";

    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    suspend fun signIn(
        request: GetCredentialRequest,
        context: Context,
        onSuccess: (UserDetails) -> Unit,
        onFailure: (String) -> Unit
    ) {
        val credentialManager = CredentialManager.create(context)
        val failureMessage = "Sign in failed!"
        try {
            val result = credentialManager.getCredential(
                request = request,
                context = context,
            )
            Log.i(TAG, result.toString())

            val credential = result.credential
            if (credential is CustomCredential &&
                credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
            ) {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                onSuccess(
                    UserDetails(
                        googleIdTokenCredential.id,
                        googleIdTokenCredential.displayName,
                        googleIdTokenCredential.profilePictureUri.toString()
                    )
                )
            } else {
                onFailure("Error with credentials.")
            }
        } catch (e: GoogleIdTokenParsingException) {
            val errorMsg = "$failureMessage: Issue with parsing received GoogleIdToken"
            Toast.makeText(context, failureMessage, Toast.LENGTH_SHORT).show()
            Log.e(TAG, errorMsg, e)
            onFailure(errorMsg)
        } catch (e: NoCredentialException) {
            val errorMsg = "$failureMessage: No credentials found"
            Toast.makeText(context, failureMessage, Toast.LENGTH_SHORT).show()
            Log.e(TAG, errorMsg, e)
            onFailure(errorMsg)
        } catch (e: GetCredentialCancellationException) {
            val errorMsg = "Sign-in cancelled"
            Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT).show()
            Log.e(TAG, "$failureMessage: Sign-in was cancelled", e)
            onFailure(errorMsg)
        } catch (e: GetCredentialCustomException) {
            val errorMsg = "$failureMessage: Issue with custom credential request"
            Toast.makeText(context, failureMessage, Toast.LENGTH_SHORT).show()
            Log.e(TAG, errorMsg, e)
            onFailure(errorMsg)
        } catch (e: GetCredentialException) {
            val errorMsg = "$failureMessage: Failure getting credentials"
            Toast.makeText(context, failureMessage, Toast.LENGTH_SHORT).show()
            Log.e(TAG, errorMsg, e)
            onFailure(errorMsg)
        }
    }
}

fun generateSecureRandomNonce(byteLength: Int = 32): String {
    val randomBytes = ByteArray(byteLength)
    SecureRandom.getInstanceStrong().nextBytes(randomBytes)
    return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes)
}


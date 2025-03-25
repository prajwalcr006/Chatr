package com.prajwalcr.chatr.googleSignIn

import android.content.Context
import android.content.Intent
import android.content.IntentSender
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.BeginSignInRequest.GoogleIdTokenRequestOptions
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.firebase.Firebase
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import com.prajwalcr.chatr.BuildConfig
import com.prajwalcr.chatr.ui.screens.SignInViewModel
import com.prajwalcr.domain.model.SignInResult
import com.prajwalcr.domain.model.UserData
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.tasks.await

class GoogleAuthUiClient(
    private val context: Context,
    private val oneTapClient: SignInClient,
    private val signInViewModel: SignInViewModel
) {
    private val auth = Firebase.auth

    suspend fun signIn(): IntentSender? {
        return try {
            val result = oneTapClient.beginSignIn(
                buildSignInRequest()
            ).await()
            result?.pendingIntent?.intentSender
        } catch (ex: Exception) {
            ex.printStackTrace()
            if (ex is CancellationException) throw ex
            null
        }
    }

    private fun buildSignInRequest(): BeginSignInRequest {
        return BeginSignInRequest.builder().setGoogleIdTokenRequestOptions(
            GoogleIdTokenRequestOptions.builder()
                .setSupported(true)
                .setServerClientId(BuildConfig.FIREBASE_WEB_CLIENT_ID)
                .setFilterByAuthorizedAccounts(false)
                .build()
        ).setAutoSelectEnabled(true)
            .build()
    }

    private fun signInWithIntent(intent: Intent): SignInResult {
        signInViewModel.resetState()

        val cred = oneTapClient.getSignInCredentialFromIntent(intent)
        val googleIdToken = cred.googleIdToken
        val googleCred = GoogleAuthProvider.getCredential(googleIdToken,null)

        return try {
            val user = auth.signInWithCredential(googleCred).await().user

            SignInResult (
                userData = UserData(

                )
            )
        } catch (ex: Exception) {
            ex.printStackTrace()
            if (ex is CancellationException) throw ex
            SignInResult(

            )
        }
    }
}
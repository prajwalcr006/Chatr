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
import com.prajwalcr.domain.model.UserData
import com.prajwalcr.domain.utils.Resource
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.tasks.await

class GoogleAuthUiClient(
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

    suspend fun signInWithIntent(intent: Intent): Resource<UserData> {
        signInViewModel.resetState()

        val cred = oneTapClient.getSignInCredentialFromIntent(intent)
        val googleIdToken = cred.googleIdToken
        val googleCred = GoogleAuthProvider.getCredential(googleIdToken,null)

        return try {
            val user = auth.signInWithCredential(googleCred).await().user
            val userData = user?.let {
                UserData(
                    email = it.email.toString(),
                    userId = it.uid,
                    userName = it.displayName.toString(),
                    profileUrl = it.photoUrl.toString().substring(0,it.photoUrl.toString().length - 6)
                )
            }
            Resource.Success(userData)
        } catch (ex: Exception) {
            ex.printStackTrace()
            if (ex is CancellationException) throw ex
            Resource.Error(ex)
        }
    }

    fun getSignInDetails(): UserData? = auth.currentUser?.run {
        UserData(
            email = email.toString(),
            userId = uid,
            userName = displayName.toString(),
            profileUrl = photoUrl.toString().substring(0,photoUrl.toString().length - 6)
        )

    }
}
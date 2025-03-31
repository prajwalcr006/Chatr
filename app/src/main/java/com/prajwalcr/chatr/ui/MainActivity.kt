package com.prajwalcr.chatr.ui

import android.app.ComponentCaller
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.auth.api.identity.Identity
import com.prajwalcr.chatr.googleSignIn.GoogleAuthUiClient
import com.prajwalcr.chatr.ui.screens.SignInScreen
import com.prajwalcr.chatr.ui.screens.SignInViewModel
import com.prajwalcr.domain.utils.Resource
import com.prajwalcr.dummy.ui.theme.ChatrTheme
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class MainActivity : ComponentActivity(), KoinComponent {

    private val signInViewModel: SignInViewModel by inject()
    private val googleAuthUiClient: GoogleAuthUiClient by lazy {
        GoogleAuthUiClient(
            oneTapClient = Identity.getSignInClient(applicationContext),
            signInViewModel = signInViewModel
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            ChatrTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->

                    val launcher =
                        rememberLauncherForActivityResult(ActivityResultContracts.StartIntentSenderForResult(),
                            onResult = { result ->
                                if (result.resultCode == RESULT_OK) {
                                    lifecycleScope.launch {
                                        val signInResult = googleAuthUiClient.signInWithIntent(
                                            intent = result.data ?: return@launch
                                        )
                                        when (signInResult) {
                                            is Resource.Success -> {
                                                Log.i("cptn","${signInResult.data}")
                                            }
                                            is Resource.Error -> {

                                            }

                                            is Resource.Loading -> TODO()
                                        }
                                    }
                                }
                            }
                        )

                    SignInScreen(onSignInClick = {
                        lifecycleScope.launch {
                            val signInIntentSender = googleAuthUiClient.signIn()
                            launcher.launch(
                                IntentSenderRequest.Builder(
                                    signInIntentSender ?: return@launch
                                ).build()
                            )
                        }
                    })
                }
            }
        }
    }


}

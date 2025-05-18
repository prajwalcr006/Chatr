package com.prajwalcr.chatr.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.android.gms.auth.api.identity.Identity
import com.prajwalcr.chatr.googleSignIn.GoogleAuthUiClient
import com.prajwalcr.chatr.ui.navigation.ChatScreenRoute
import com.prajwalcr.chatr.ui.navigation.HomeScreenRoute
import com.prajwalcr.chatr.ui.navigation.SignInScreenRoute
import com.prajwalcr.chatr.ui.navigation.SplashScreenRoute
import com.prajwalcr.chatr.ui.screens.chat.ChatScreen
import com.prajwalcr.chatr.ui.screens.signin.SignInScreen
import com.prajwalcr.chatr.ui.screens.signin.SignInViewModel
import com.prajwalcr.chatr.ui.screens.home.HomeScreen
import com.prajwalcr.domain.utils.Resource
import com.prajwalcr.dummy.ui.theme.ChatrTheme
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import timber.log.Timber

class MainActivity : ComponentActivity(), KoinComponent {

    companion object {
        const val TAG = "MainActivity"
        const val CHANNEL_ID = "channelId"
    }

    private val signInViewModel: SignInViewModel by inject()
    private val mainViewModel: MainViewModel by inject()
    private val googleAuthUiClient: GoogleAuthUiClient by lazy {
        GoogleAuthUiClient(
            oneTapClient = Identity.getSignInClient(applicationContext),
            signInViewModel = signInViewModel
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //enableEdgeToEdge()

        setContent {
            ChatrTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->

                    Box {
                        val appState by mainViewModel.appState.collectAsState()
                        val navController = rememberNavController()
                        NavHost(navController, startDestination = SplashScreenRoute) {
                            composable<SplashScreenRoute> {
                                LaunchedEffect(Unit) {
                                    val data = googleAuthUiClient.getSignInDetails()
                                    if (data != null) {
                                        navController.navigate(HomeScreenRoute)
                                    } else {
                                        navController.navigate(SignInScreenRoute)
                                    }
                                }
                            }

                            composable<SignInScreenRoute> {
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
                                                            mainViewModel.onSignInResult(signInResult.data)
                                                            signInResult.data?.let {
                                                                //TODO: CHANGE IT TO PERSISTENT CACHE

                                                                mainViewModel.sendUserDetailsToFirebase(it)
                                                            }
                                                        }
                                                        is Resource.Error -> {
                                                            Timber.tag(TAG).e("User sign in failed!! message: ${signInResult.error}")
                                                        }

                                                        is Resource.Loading -> {

                                                        }
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

                            composable<HomeScreenRoute> {
                                HomeScreen(navController)
                            }

                            composable<ChatScreenRoute> { navBackStackEntry ->
                                val channelId = navBackStackEntry.arguments?.getString(CHANNEL_ID) ?: return@composable
                                ChatScreen(channelId)
                            }
                        }

                        LaunchedEffect(appState) {
                            if (appState.isSignedIn) {
                                navController.navigate(HomeScreenRoute)
                            }
                        }
                    }
                }
            }
        }
    }
}

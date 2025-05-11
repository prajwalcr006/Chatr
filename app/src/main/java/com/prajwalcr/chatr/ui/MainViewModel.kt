package com.prajwalcr.chatr.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.prajwalcr.domain.model.AppState
import com.prajwalcr.domain.model.UserData
import com.prajwalcr.domain.repository.caching.CacheStore
import com.prajwalcr.domain.usecase.user.SetUserDataToFirebaseUserCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber

class MainViewModel(
    private val setUserDataToFirebaseUserCase: SetUserDataToFirebaseUserCase,
): ViewModel() {

    private val _appState: MutableStateFlow<AppState> = MutableStateFlow(AppState())
    val appState: StateFlow<AppState> = _appState

    fun onSignInResult(signInResult: UserData?) {
        _appState.update {
            AppState(
                isSignedIn = signInResult != null,
                userData = signInResult
            )
        }
    }

    fun sendUserDetailsToFirebase(userData: UserData) {
        viewModelScope.launch {
            try {
                setUserDataToFirebaseUserCase(userData)
                //TODO: LOOK FOR CACHE EXPIRY
               // inMemoryCacheStore.store(SIGNED_IN_USER_DATA, userData, Duration.INFINITE)
            } catch (ex: Exception) {
                Timber.e("Exception in setting user data to firestore. EX: $ex")
            }
        }
    }
}
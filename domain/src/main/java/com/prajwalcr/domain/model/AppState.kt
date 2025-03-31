package com.prajwalcr.domain.model

data class AppState(
    val isSignedIn: Boolean = false,
    val userData: UserData? = null
)

package com.prajwalcr.chatr.ui.navigation

import kotlinx.serialization.Serializable

@Serializable
object SignInScreenRoute

@Serializable
object HomeScreenRoute

@Serializable
object SplashScreenRoute

@Serializable
data class ChatScreenRoute(val channelId: String)
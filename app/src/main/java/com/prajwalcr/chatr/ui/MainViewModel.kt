package com.prajwalcr.chatr.ui

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.prajwalcr.domain.model.AppState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class MainViewModel: ViewModel() {

    private val _appState: MutableStateFlow<AppState> = MutableStateFlow(AppState())
    val appState: StateFlow<AppState> = _appState
}
package com.prajwalcr.chatr.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.prajwalcr.domain.model.Channel
import com.prajwalcr.domain.usecase.GetChannelsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber

class HomeViewModel(
    private val getChannelsUseCase: GetChannelsUseCase
): ViewModel() {

    private val _channelList: MutableStateFlow<List<Channel>> = MutableStateFlow(mutableListOf())
    val channelList: StateFlow<List<Channel>> = _channelList

    init {
        getChannelList()
    }

    private fun getChannelList() {
        viewModelScope.launch {
            try {
                val channelList = getChannelsUseCase()
                Timber.i("Channel list is $channelList")
                _channelList.update {
                    channelList
                }
            } catch (ex: Exception) {
                Timber.e("Exception in getting channel list. EX: $ex")
            }
        }
    }
}
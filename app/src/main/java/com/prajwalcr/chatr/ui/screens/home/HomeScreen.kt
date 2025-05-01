package com.prajwalcr.chatr.ui.screens.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.prajwalcr.domain.model.Channel
import org.koin.androidx.compose.koinViewModel

@Composable
fun HomeScreen(navController: NavController) {
    val homeViewModel: HomeViewModel = koinViewModel()
    val channels = homeViewModel.channelList.collectAsState()

    Scaffold {
        Box(
            modifier = Modifier
                .padding(it)
                .fillMaxSize()
        ) {
            LazyColumn {
                items(channels.value) { channel ->
                    Column {
                        Text(text = channel.key ?: "")
                    }
                }
            }
        }
    }
}
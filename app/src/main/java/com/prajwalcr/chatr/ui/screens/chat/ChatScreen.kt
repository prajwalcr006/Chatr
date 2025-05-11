package com.prajwalcr.chatr.ui.screens.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHost
import com.prajwalcr.domain.model.Message
import com.prajwalcr.domain.model.UserData
import com.prajwalcr.domain.repository.FirebaseAuthRepository
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

@Composable
fun ChatScreen(navHost: NavHost, channelId: String) {

    val messageText = remember {
        mutableStateOf("")
    }

    val chatViewModel: ChatViewModel = koinViewModel()
    val firebaseAuthRepository: FirebaseAuthRepository = koinInject()
    val messageList= chatViewModel.messages.collectAsState()
    var currentUserData: UserData? = null

    LaunchedEffect(channelId) {
        chatViewModel.listenForMessages(channelId)
        currentUserData = firebaseAuthRepository.getUserData()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn {
            items(messageList.value) {
                ChatBubble(message = it, currentUser = currentUserData!!)
            }
        }

        Row(
            modifier = Modifier
                .fillMaxSize()
                .background(color = Color.Gray)
                .padding(8.dp)
                .align(Alignment.BottomCenter)
        ) {
            TextField(
                value = messageText.value,
                onValueChange = { messageText.value = it },
                modifier = Modifier.weight(1f)
            )

            IconButton(
                onClick = {
                    chatViewModel.sendMessage(
                        channelId,
                        messageText.value
                    )
                    messageText.value = ""
                },
            ) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = "Send"
                )
            }
        }
    }
}

@Composable
fun ChatBubble(message: Message, currentUser: UserData) {
    val isCurrentUser = message.senderId == currentUser.userId
    val bubbleColor = if (isCurrentUser) {
        Color.Red
    } else {
        Color.Green
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 4.dp)
    ) {
        val alignment = if (isCurrentUser) {Alignment.CenterStart} else {Alignment.CenterEnd}

        Box(
            contentAlignment = alignment
        ) {
            Box(
                modifier = Modifier.background(
                    color = bubbleColor,
                    shape = RoundedCornerShape(8.dp)
                )
            ) {
                Text(
                    text = message.text,
                    color = Color.White,
                    modifier = Modifier.padding(8.dp)
                )
            }
        }
    }
}
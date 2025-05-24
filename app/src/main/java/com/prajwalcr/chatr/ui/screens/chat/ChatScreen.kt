package com.prajwalcr.chatr.ui.screens.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.prajwalcr.chatr.ui.BlueBlack
import com.prajwalcr.chatr.ui.rememberImeState
import com.prajwalcr.domain.model.Message
import com.prajwalcr.domain.model.UserData
import com.prajwalcr.domain.repository.FirebaseAuthRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject
import timber.log.Timber

@Composable
fun ChatScreen(channelId: String) {

    val currentKeyBoard = LocalSoftwareKeyboardController.current

    val messageText = remember {
        mutableStateOf("")
    }

    val imeState = rememberImeState()

    val listState = rememberLazyListState()

    var currentUserData = remember {
        mutableStateOf<UserData?>(null)
    }

    val chatViewModel: ChatViewModel = koinViewModel()
    val firebaseAuthRepository: FirebaseAuthRepository = koinInject()
    val messageList= chatViewModel.messages.collectAsState()

    LaunchedEffect(channelId) {
        currentUserData.value = firebaseAuthRepository.getUserData()
        chatViewModel.listenForMessages(channelId)
    }

    LaunchedEffect(messageList.value) {
        if ((messageList.value.isNotEmpty()) && (messageList.value.last().senderId == currentUserData.value?.userId)) {
            listState.animateScrollToItem(messageList.value.lastIndex)
        }
    }

    LaunchedEffect(imeState.value) {
        if (imeState.value) {
            snapshotFlow { listState.layoutInfo.viewportEndOffset }
                .distinctUntilChanged()
                .collect {
                    val lastVisibleItem = listState.layoutInfo.visibleItemsInfo.lastOrNull()
                    if (lastVisibleItem?.index != messageList.value.lastIndex) {
                        listState.animateScrollToItem(messageList.value.lastIndex)
                    }
                }
        }
    }

    Column(modifier = Modifier
        .fillMaxSize()
        .imePadding()
        .background(BlueBlack)
    ) {
        LazyColumn(
            modifier = Modifier.padding(20.dp)
                .weight(1f),
            state = listState
        ) {
            Timber.d("messageList.value = ${messageList.value}")  //keep this line
            items(messageList.value) { message ->
                currentUserData.value?.let { userData ->
                    ChatBubble(message = message, currentUser = userData)
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(color = Color.Gray)
                .padding(8.dp)
        ) {
            TextField(
                value = messageText.value,
                onValueChange = { messageText.value = it },
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(
                    onDone = {
                        currentKeyBoard?.hide()
                    }
                )
            )
            IconButton(
                onClick = {
                    if (messageText.value.isNotEmpty()) {
                        chatViewModel.sendMessage(
                            channelId,
                            messageText.value
                        )
                        messageText.value = ""
                    }
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

@Preview
@Composable
fun PreviewChatBubble() {
    ChatBubble(
        message = Message(
            messageId = "123",
            text = "I am loki of asgard and I am assigned with glorious purpose and test ",
            senderId = "124",
            senderName = "Prajwal",
            profileUrl = "https://lh3.googleusercontent.com/a/ACg8ocKgVHpTYdQ859-zWqmnK5IO6eHa-LO4wJHZ2wadVo9Bgdxu3w"
        ),
        UserData(
            email = "email",
            userName = "Prajwal",
            userId = "123",
            profileUrl = "https://lh3.googleusercontent.com/a/ACg8ocKgVHpTYdQ859-zWqmnK5IO6eHa-LO4wJHZ2wadVo9Bgdxu3w"
        )
    )
}


@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun ChatBubble(message: Message, currentUser: UserData) {
    val isCurrentUser = message.senderId == currentUser.userId
    val bubbleColor = if (isCurrentUser) {
        Color.Green
    } else {
        Color.Red
    }
    val arrangement = if (isCurrentUser) {Arrangement.End} else {Arrangement.Start}

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 4.dp),
        horizontalArrangement = arrangement,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (!isCurrentUser) {
            Spacer(Modifier.padding(3.dp))
            GlideImage(
                model = message.profileUrl,
                contentDescription = null,
                modifier = Modifier.size(40.dp)
                    .padding(6.dp)
                    .clip(RoundedCornerShape(5.dp))
                    .align(Alignment.CenterVertically),
                contentScale = ContentScale.Crop
            )
        }

        ChatBubbleText(bubbleColor, message)
    }
}

@Composable
fun ChatBubbleText(bubbleColor: Color, message: Message) {
    Box(
        modifier = Modifier.background(
            color = bubbleColor,
            shape = RoundedCornerShape(8.dp)
        )
            .widthIn(max = LocalConfiguration.current.screenWidthDp.dp * 0.7f)
    ) {
        Text(
            text = message.text,
            color = Color.White,
            modifier = Modifier.padding(8.dp)
        )
    }
}
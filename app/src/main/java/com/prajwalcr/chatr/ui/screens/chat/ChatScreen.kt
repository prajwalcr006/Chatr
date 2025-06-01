package com.prajwalcr.chatr.ui.screens.chat

import android.content.Context
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.lazy.LazyListState
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.bumptech.glide.integration.compose.placeholder
import com.prajwalcr.chatr.BuildConfig
import com.prajwalcr.chatr.R
import com.prajwalcr.chatr.ui.BlueBlack
import com.prajwalcr.chatr.ui.customDarkGray
import com.prajwalcr.chatr.ui.customPurple
import com.prajwalcr.chatr.ui.rememberImeState
import com.prajwalcr.chatr.ui.textMessageColor
import com.prajwalcr.domain.model.Message
import com.prajwalcr.domain.model.UserData
import com.prajwalcr.domain.repository.FirebaseAuthRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject
import timber.log.Timber
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.jar.Manifest

@Composable
fun ChatScreen(channelId: String) {

    val messageText = remember {
        mutableStateOf("")
    }

    val imeState = rememberImeState()

    val listState = rememberLazyListState()

    val currentUserData = remember {
        mutableStateOf<UserData?>(null)
    }

    val chatViewModel: ChatViewModel = koinViewModel()
    val firebaseAuthRepository: FirebaseAuthRepository = koinInject()
    val messageList= chatViewModel.messages.collectAsState()
    val imageUrl = chatViewModel.publicImageUrl.collectAsState()

    LaunchedEffect(channelId) {
        currentUserData.value = firebaseAuthRepository.getUserData()
        chatViewModel.listenForMessages(channelId)
    }

    LaunchedEffect(messageList.value) {
        if ((messageList.value.isNotEmpty()) && (messageList.value.last().senderId == currentUserData.value?.userId)) {
            listState.animateScrollToItem(messageList.value.lastIndex)
        }
    }

    LaunchedEffect(imageUrl.value) {
        if (imageUrl.value.isNotEmpty()) {
            chatViewModel.sendMessage(
                messageContent = imageUrl.value,
                channelId = channelId,
                isImage = true
            )
        }
    }

    LaunchedEffect(imeState.value) {
        if (imeState.value) {
            snapshotFlow { listState.layoutInfo.viewportEndOffset }
                .distinctUntilChanged()
                .collect {
                    if (listState.isScrolledToEnd()) {
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

        SendMessageTextBox(messageText, chatViewModel, channelId)
    }
}

fun createImageFile(context: Context): File {
    val timeStamp = SimpleDateFormat("yyyymmdd_hhmmss").format(Date())

    val fileName = "JPEG_${timeStamp}_"

    val imageFile = File.createTempFile(
        fileName,
        ".jpg",
        context.externalCacheDir
    )

    return imageFile
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun SendMessageTextBox(
    messageText: MutableState<String>,
    chatViewModel: ChatViewModel,
    channelId: String) {
    val currentKeyBoard = LocalSoftwareKeyboardController.current

    val showSourceAlert = remember {
        mutableStateOf(false)
    }

    val context = LocalContext.current

    val file = createImageFile(context)

    val imageUri = FileProvider.getUriForFile(
        context,
        BuildConfig.APPLICATION_ID+".provider",
        file
    )

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) {
        chatViewModel.uploadImage(imageUri)
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            chatViewModel.uploadImage(it)
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) {
        if (it) {
            Timber.d("Permission granted")
            cameraLauncher.launch(imageUri)
        } else {
            Toast.makeText(context, "Please provide camera permission", Toast.LENGTH_SHORT).show()
        }
    }

    if (showSourceAlert.value) {
        ShowCameraAlertDialog(
            onCameraClick = {
                showSourceAlert.value = false
                val permissionCheck = ContextCompat.checkSelfPermission(context, android.Manifest.permission.CAMERA)
                if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
                    cameraLauncher.launch(imageUri)
                } else {
                    permissionLauncher.launch(android.Manifest.permission.CAMERA)
                }
            },
            onGalaryClick = {
                showSourceAlert.value = false
                galleryLauncher.launch("image/*")
            }
        )
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = customDarkGray)
            .size(100.dp)
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        TextField(
            value = messageText.value,
            placeholder = {
                Text(
                    text = "Type your messages...",
                    color = textMessageColor,
                    fontStyle = FontStyle.Italic
                )
            },
            onValueChange = { messageText.value = it },
            modifier = Modifier
                .weight(1f),
            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(
                onDone = {
                    currentKeyBoard?.hide()
                }
            ),
            colors = TextFieldDefaults.colors().copy(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedContainerColor = Color.DarkGray,
                unfocusedContainerColor = Color.DarkGray,
                focusedPlaceholderColor = Color.Gray,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                cursorColor = customPurple
            ),
            shape = RoundedCornerShape(30.dp),

            leadingIcon = {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Spacer(Modifier.padding(5.dp))
                    IconButton(
                        onClick = {showSourceAlert.value = true}
                    ) {
                        GlideImage(
                            model =  R.drawable.attachment,
                            contentDescription = null,
                            modifier = Modifier.size(40.dp)
                                .align(Alignment.CenterVertically)
                                .padding(5.dp),
                        )
                    }
                }
            },

            trailingIcon = {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
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
                        GlideImage(
                            model =  R.drawable.send_icon,
                            contentDescription = null,
                            modifier = Modifier.size(40.dp)
                                .align(Alignment.CenterVertically)
                                .padding(5.dp)
                        )

                        Spacer(Modifier.padding(8.dp))
                    }
                    Spacer(Modifier.padding(5.dp))
                }
            }
        )
    }
}

fun LazyListState.isScrolledToEnd(): Boolean {
    val lastVisible = layoutInfo.visibleItemsInfo.lastOrNull() ?: return false
    val totalItemsCount = layoutInfo.totalItemsCount
    return lastVisible.index >= totalItemsCount - 1
}

//@Preview
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
        customPurple
    } else {
        customDarkGray
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
                contentScale = ContentScale.Crop,
                failure = placeholder(R.drawable.default_user)
            )
        }

        if (message.imageUrl != null) {
            Timber.tag("cptn").d("message.imageUrl = ${message.imageUrl}")
            ChatBubbleImage(customPurple, message.imageUrl!!, isCurrentUser)
        } else {
            Timber.tag("cptn").d("message text = ${message.text}")
            ChatBubbleText(bubbleColor, message)
        }
    }
}

@Composable
fun ShowCameraAlertDialog(onCameraClick: () -> Unit, onGalaryClick: () -> Unit) {
    AlertDialog(
        onDismissRequest = {  },
        dismissButton = {
            TextButton(
                onClick = onCameraClick
            ) {
                Text(text = "Select from Camera")
            }
        },
        confirmButton = {
            TextButton(
                onClick = onGalaryClick
            ) {
                Text(text = "Select from Galary")
            }
        },
        title = {
            Text(text = "Source selection")
        },
        text = {
            Text("Would you like to select from camera or galary?")
        }
    )
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun ChatBubbleImage(bubbleColor: Color, imageUrl: String, isCurrentUser: Boolean) {

    val glideModifier = if (isCurrentUser) {
        Modifier.size(200.dp)
            .background(bubbleColor)
            .padding(6.dp)
            .clip(RoundedCornerShape(1.dp))
    } else {
        Modifier.size(200.dp)
            .padding(6.dp)
            .clip(RoundedCornerShape(1.dp))
    }

    GlideImage(
        model = imageUrl,
        contentDescription = null,
        modifier = glideModifier,
        contentScale = ContentScale.Crop,
        loading = placeholder(R.drawable.chat_image_loader)
    )
}

//@Preview
@Composable
fun PreviewChatBubbleImage() {
    ChatBubbleImage(
        customPurple,
        "data:image/jpeg;base64,/9j/4AAQSkZJRgABAQAAAQABAAD/2wCEAAkGBxIQEBUSEBIVFhUVGRgVFhYYFxgWGBgYGBgYFhgVGRgYHSggGB0mHRgYITEhJSkrLi4uGB8zODMtNygtLisBCgoKDg0OGxAQGi0lHyYvNS0vLTUtLS0tLS0tLTItKy0tLS0tLS0tLS0tLS0tLS0tLy0tLS0tLS0tLS0tLS0tLf/AABEIAPYAzQMBIgACEQEDEQH/xAAcAAAABwEBAAAAAAAAAAAAAAAAAgMFBgcIBAH/xABPEAACAQIDBAcCCAgLBwUAAAABAhEAAwQSIQUxQVEGBxMiYXGBMpEUI0JSYoKhsiQlM3JzkrHBFTRjg5Ojs8LR4fAINUNTVKK0dMPS0/H/xAAaAQACAwEBAAAAAAAAAAAAAAAABAIDBQEG/8QAJREAAgIBBAICAwEBAAAAAAAAAAECEQMEEiExIkEyURMjM3Hw/9oADAMBAAIRAxEAPwBxihFHihFbJ54JFexRor2KACRQijxQy0AEihFKZaEUAEihFHy0IoAJFCKUihFcATihFKRQigBG04ZQymQwBBG4giQfdR4pr6MXPimsnfYuPa+orHsz+pHup4iuRdqyUo7XQTA7Y7HEG1lU57ZdZG4qwDR+sn21IbW3gygXB3o3ncarnbmIybTwIB9oXlI8GAA+0D3VKIqvapN2Xb5Y0q9oUxOIZ950kkDlRLOYnKDv4V5FegVZRRbux+wXRu46sxeG4eJ5TRcFsDMSHuRxMcQKRt9ILq2hbEQBE8a5F2hdDZgx8uHuqqsnPIxuwquB5k2EyFu7qJOmnDzouFxaXTDPrTbj8ebiAGCd503eFcdtspkDXxrn4rXPZJ6ja6XRJdpYUMkZojWOdRV1EmN1d2L2g91QG4cq44qeOLiuSrNNTdoLFCKPFCKsKQkUIo8UIoAJFexR4oRQASKEUeKEUAEihFKRQig6EihFHihFABIoRSkV5Ca9qYtIrXbzcrVsS/qdEHHvTwqMpbVbJQi5SSQx2LLWsa3dIXFIL9otCC52YCXsuYiYTs7knTKrxvp7Sy7CVXOOdtrd77LLMQPEiqV6QdLr2K2h8PUm3cVg1uDOQI3xagbgAsAjcdTxrt2vsu3iLDbT2cVthCDicOGyth3YwHtSZa0zHSNVJjhoitRJGpLSQkuSS44/CNuWVU5ls2Xdo1AOS4wJPiTbHqKm8VWfV5imN1799rl29iGFhD3rjvkC3nJjvHVLCzr7dS3beP7d/gWHYG4+l0qQextzDNcI0DRICb5PeygQ1uLKuX7b6F8+B+KXSXY74DFreTPb1QkhW4MAYzDwkGDxGvGumK8w2HW2ioghUAVRyAEAUplppWIur4CRQilIoRQAnFCKUivYoATihFKRQigAmWvctHy0IoAJloZaUil8Jg3ukhBJAmuN0dSb4RyZa9y0tdslCVYQRvFEiu2cCRQijxXoFACcV7FdF3CuollIHPhSUVyzrTXYSKEUeKEV04JsQASTAGpJ3ADjUG61NuCzY+Aq8XrpS7ihGqoAWs4cngRo7Dm41gRUw2vtS3g7L4q6AUsxlQmO1vkZrVjy+W3JVGhBqiNs57jfCLt0XXvOzOwn2iqXGBkDUdpBA0EaUlqMlvajS0eKlvY59F8XgWtvhsfbgOc1vEKO9aYiO9GpTQaa8dNZEbOhInw040YJpPu1HDfRKSjDa217HaJr0OXGYq2uF7W9awfeLG2MguMzKpUuB3z3l0JMAbtKtDY2xbGDTJYQKDqTvZjzZjqagHVdtFrtx7d66SLVtRatblC9orO6qNCQQpJ3wWJ0Bq0op7SLxbfZna2Utyj6CRQijxQinBEJFCKPFCK4AWKEUeKEUAEihFHivYoAJFCKe7WwWBOcgACRB31zYnCrDEArEQDxNQWSLfBY8UkrZz7NwvaXAsTxI5gcKkyYENlbDxbyyG018R51ydGbSEGd4M/6NSEWwAQBG+l82Tyod02JOFsY8VaWy5JUNm35tSfGmbE2BBbKBT1tRNxLbhBJMRFNlu/buCAWccTbVrn3Aa5GVcsnkxpppI59nbO7VWI+T/oCjWti3jByRxp+2Xft2rYXJdnefiL3/wAKNd2oDOVbw4R2F7dznJXXnd8FcdJHar7G/ad/4gWypUrv86ZEwrESN0E+6n3F3C4PxdwxqZXsx6tdyj7ZpnudKMHY/jG0MHbyggrbYX7gB8RuPhkNdWVRQT07nLk5stHwOFbEXOztRI9tt4tjmeZ5LxjgASGvGdZmwkHea9iSOHZsQfJLmS2PQCmnb/W5YfZ+IGzbPYuYSXa3bcG4CDcS2hJaFWJkZSVrktTa4QQ0dO2yF9bnSO3iMQMLhWJw+FLIOIe4T8ZeLfKJaRPgSPapibZN65sq3ft2ma3bxGIW66qSEm3hipeNw0Op0qO0/wDRDpXiNm3S9m5cCkHNbDAI5ghSysrKQDHCYkAiaUH6oYkAnUxoeE6wYHqYFFqwB0g2PtD/AHjg2wl478Rg9EJ3y9hpA8SJJmunA9WrXXW7snH4PFhSGCMclzTUBrLgiPzj6UBZXuBxb2Li3bZhkMg/uI4g7iOIJrQ+xr7XbCMylSVUlTMglQSpnUlSSpnWVO7dUB2F1X4rDYpbmPS2EUzbTMr9q41BKqdLa6M0xMBd7aWbbt5QBrpxOpPMk8Sd801pk+WIa2SpR9nmWhlpSKEU2Z4nlr2KPFCKACRQy0pFCKAE8texR4oRQBIsZbO+df3VzMUMh9QeG6l8ZeObXfGojdXBcadYpNM12kPGFbu5LYURr6UvcxzMwt2QMxXMzNqqCYHdBlmJmBIHdOukFgtuQdDrTn0fBY3naJLhBHzVRTH6zv76rmicX9CHSHEYXAYe5jMYTcFsTLwzFvkpbTRFYmBoBzJ0mqV2x15bQusRhrdqwnAZe1f1Zu6fRa6P9oXpGbuKt4FD3LAFy4Od1xoD+ahH65qpAarbLEianrY2z/1p/obH/wBdd2C65drW4z3bd3Xc9lAI87eU1XrOTpw3xwnn9gotcO0ixdt9Zq7TtC1tTAo4Uko9i49l0JESobOGPg0iopt61gk0wrXmJCt3yjKMwDFSVAJYTB0iZ30y17FB0MFHP/Kig0dLpCsoiGidAd2o13j0pOgBfBYbtXCBkWZ7zsEUQCdWOg3R5kUjHGlruHyoj5kOeTAYFlymO+u9Z3id4pJ0IiRvEjy50Ae5hljL3pnNPCN0fvpy6Kuq4/CswBUX7JYHUEC4pII46U1UpDIwMFTow4HWGVh9hBoA0pgkC2bd0XFvW2VVF9XzqDAm25k9mcxI5E8iYrris6bM27iMBfZ8HiG10JAOS4OT23EMNTowq3ehnS74cQi2LltjosW3bDt5MoZsOfA5k0GqU1izpcMz8+lbe6JLstDLXt5uz0vK1o/TED0f2G9CaMjBhKkEcxqKZUk+hFxa7QWKGWjxXsV04J5aGWlIoRQATLQy0eKEUAOKuTq2vnRLwAGgrrOUmOdIXQdRoKUo1rRzJzp26PXF7B3zCO0vSZ07lxlOvhlj0rhxOFYoqoYa4wRDxEglmH5qhmg78sVGukXSzDrsnai4SAMKxwoy87oS2XEn57XNeOQmoTkShH2Z86R7UOMxd/EtM3bjuJ3gEkqvoIHpTfXqRImYnWN8cYryNdKpLz1InWY4xvpfFYkOAAiKBAEDvECdWPEmdTpXrYqbItdnb7rF+0C/GHMAMrNxURoOZNctACpvHJkB7s5iNPaEid0jQ0lUkXoxcTZ1vaRQ3LLXLlp1BKlCBFtyQD3SxI81A+UKjoSd2p10EyI1k+lAHt66zsWckk6kneaJRkIBBIkTqN0+FFoAFKBGaWgkDVjqQJManhJohPh/nRkaNJMHfHHlQB4JYgakmAOJ5AVNNmdAL15hav4i1avZc/YQbl9U7oHaBdE0ywrNIncKfOoPZNu7i71+4oZrCp2c6gM5bveYCketQfB7Xv4faHwjvG8LrM6mQWLMc6EROsketQyOW17OyLb9EmxvV1jMJ8ZhrysQOAa04/NYyoP1weHGoxiOkO0EYo+LxalSVKteugqRoVIJ0M8KvfpXtQ4XA3r6qSyp3REwzQoLDkC0nyNUBicU+KyZu9dEW8xiXGgTMx+UIyyeGXlqnoNVPUQcppKmRxyclyduE6Z7StMCmOxUjgb1xh6qxIPqKesF1oY1TN9MPf8ApPaCXI4xds5GHvNQehT9k2ky5tidaGFukLdD4c8nY3rXD/igdpb1n2lcabxvqfYPFpdUFCDIDbwZB3MCpIZT85SRoddKy/2JyZ+AOU+BIkT5wfcaf+iXS29gHAlmskyyT7M73tzorfY0QwPC+GZrsVy6WMuY8M0TFCKSwd/tFB8FaQCAyuoZHWfksCD4ajeDXRFOJ2rRmNNOmEihFHy17loODnewptgMTOmopvuPmNKXb7NvpGKrUPsYlm9ROTpv0g+BYLE4pT3rSDDWOfa3QpZ+RiV/om51mfDbTe3ZvWVPcvhO0BHG2+dSPLX9Y1Y/XXtgm3hcICdDfxL+Je9cW37lDfrVVVIy7NWHQYJIJ0013xxA0576LQo2kcZ+yK4SC127L2XcxLFLIDMIhcwDMWZUCoCe8xLDQeNcmUzEazEcZ5RWguqrq7XCfhF4Ht2USSINhWUTaWDpdYHvNvVTAgkkBxsmXQzo3bw+yreBuhboCsl8e0rO5LXVniAzFZ+jzFUR1k9XVzZTvcQNcwrn4q4D+TJPsXRHKQCIB08q05bQKAqgAAQANAANwAomJw6XUa3cVXRwVZWAZWB0IIOhFSogmYnoVeXTbqQDE3dluFnU4e42n83cP7G9/Cqs2t0Ox+Fnt8JeSN7ZCUA+cbglI9aiTsYK9Bo7KACCdQYEQQefeB8NImZoPaIVWMQ0xDAnTTUAyvrvoOk46pOmFvZuJdb4i1fCqzjUoVJysRxXvGffwq3NudBcLi8VZ2hZC9opFwwR2d8ASrEie8DBzDfEHmM347su0PYZ+z0jOAGmBmnLpvmPCKf+j3TjGbPRUwt1gJLMj99JJOiqdFG4kiDPGozhuTX2RcbL8xWENxGt3LFwhwVZSsggiCJUlePOqX6VdEV2ThHGIYNexF0CwgILLZt5iXcjQMSbYgTuOvJ9t9dGMEKbWFcsAQ47RACRqGBbgfKq+2/tK/jnbGYm6ruzZMuYBlAEgLb3rbExPOeM0tpdJHT3tb5IQhtGzsmy5spyzlzQYmJyzumOFem0AgfMurEZdcwgA5jpuM/YaUs41lQ2z3kMnIxbKrEAdoFBAzwInlSVkqJLCdDA8ToJMiI3+gHGmy0fujWDXEDG2hA/B7l+2N/esMt3SdfyYuL9ao8BUu6pkD7XsIdzrftkcw+Hur++okQANZmd0f61oA0V1e4s3sBhXPDDpa/ob2ItA+4L9lSeKg/VBczbOtj5vaD+tZv3mp3FPYvgjH1K/awkUIo8UIqwoCxXoFGivQKAM69Ym0X/AISZlYgpbsqhG8BrKu32u3vqJE08dMD+G3fqD3W1FcN/41l7NSbjAl1VQBOvsKg3ZQJ03zWczeXCBs3Bi85U3bdsBWbNcJC90E5dAdTuA5muSlLLKCcylhBGhiCQQpmOB1jjFS3q56InH3e0uqfg9tgGAJXtnPeWwrcJAzMR7KgniKOwbrlkt6meg+d1xt9Rm9rDowBygGPhTKeRkIDvYFtyzV92LKooVRoPUknUkniSZJPEk1GMEPg7pdOsd25lWBkIAhEG5UKpA4Kp3kmZNhsQl1A9t1dGEqykMpHMEaGrJQceGUQyrIrQrQoUKiTBUK291pbNwV57F57va2zDqtptDv3tAOhBkc6mlZy/2gtl9ltRbwGmItKxPN0m2f8AtCVxnUiQ7d60dh4gk3Nltebdme1ZU/r5iwqvdu7V2RdBOH2ddsmfk4sz5lHtOI8iKiNCok6PTHClMQ6s0omQad2SdwAJk66mT60lS1shRmlSTmXKQSRoIbdHExrMr5SHRGhQoUAGSJGaYnWNTHGBXRtKzaR4s3e1SFObIU1IBZcpPAyJ4xNc4InUacgY+0zXhoAl/VCCdt4OPnt7hbcn7KYhasvibovXTbSbpDKmeWElFiRoTAnhNTbDYkbK2HZxFkRi9oG9aFwnvWrCNlfsh8kt3ZO/vTwEVwqEyQCYEnwEgSeWpA9aDhbPVXtm3bSzYle0KX7p+dlRpZPGUzvG+bC8DVvRWXOi21jhMdh8SSYtXELcZQEB18ikitP4FcqZP+WWtjmVQkIT5rlPrTOCXoQ1sOpCkV7FHihFMCIWKSvsQAFEuxyou6WMn0AAJJ4AE8K6IrzAXERLmNvMFtojFCdwtDVrv140390LHtGoTntRbhx75V6M29YeyblvbF/DKC75raoANXzImWB4yKjDo9typlXUlSNxBGhB5cRT30z6TXNo465izKyQLYGhRF9gSOPEnmTTI6sYY65idZkk6TPHjSJsnf0d2M+NxC2LZidXcglbaDVrjRwA4cSQBqRV/wCAxGC2Zbt2GYoUSETKzuqEyXfICA7kZm8QAJCio31b7FTC7Pv4lCty/ZcdugOgKqGyMfm284cx8tG35FNGtbKfG3ixYiJNx4GrGIXXlp5DSmdPC+RPUtvxfQ87b6SJiQLOEYlSV7Z4ZCLZuKhQTBBJYAnkdOJHnQpGXEYhLTtbCBCuU6SWeZDSHn6U8SIOtNV/o/8ABmzJcLENaz6AAI15AJPEkgQPCeFPPQz+O4ofRT7Ger3H0xa9sPEnWE2xBCYgBSYAuDS2xOgGpm2xPAkjUAMTpTvTAyggggEHQg6gg7wa8wt98Pos3LXzJl0H8mT7S/QP1ToFNE8Vcotxam+JkgqEdaXRfC7Sw6pfvJYvW5Ni47ZVzPA7NidCGKjdqIBHIzLDYhLqB7bBlO4jw0IPIg6EHUEUTH4O3ftPZvIHt3AVdTuIP+t9UjZj/bmxb+ButZxVkq8d065SJnOjDRwRpP7xTcbcayCJiR/gYMa8fGpr1m9EcRsm72Qd3wdwlrBJJUHeUYbg45jeNeYEJS4QCsmDEgGASN0jjx99QLQldVrCTYe7Dd1kWe7l7wc6yZnu6QI3zGk8ytBBG8a067Q2havYe2CLvboWBYsOy7IywVViQcxY741roDZl0nxAj9/2V4wjQiD/AIxH+vGvCaNcfMZJJMAEkzu0HoBA9K4AbFYZ7TZLilWgGDyZQyn1BB9a69hbGvY7EJh8Mhe45gDgBxZjwUDUmuFiTqZPifcP2fZV9dT2w2wmB7Ud3EY0Zg0Am1hlMK+umZiSQOPdMEKa6lbojKSirZH+t7o98G2dggGzDCEYOR7LOU7S44HCXBXzU1UrRJiY4TvjxrWeP2JZ2ns1sFd7pCqrR7Vu6olbgneJ1n5QJ5msv7T2diMFinw90Zbtksh1ERBMgneCpkeYoaoIu0ceOsNbYW7ls23UDMDMme8GIO7usN3ACtLdBsYb+Dt3CZLLZzH6Qw9kMfeDWZ8Tbho724HvjKZygnSTxmOYitD9URnZlvzn0gL/AHTVmH5C+s/mTKKEUeKEU2ZZzX7XaMtkf8SS/hbWM/vlU03Z54VBP9oHpGLWGtbPtEBr3xlwCBFpD3V8MzD+rqxtm6Ylp42xH1XOb7y+4Vmzre2g1/bOKJMi2wsqOQtgKQPrZj5k0rlfJp6WKULIcTNHsXijBlMMuoPI8CPEUbF4c23KFkaI1Rg6mQDow0O+kapGyx+ovbIs7ROFua2sYhtMDuzKGZCfTMv16tTZ+Haxa7G1Zcm2WtzBhmVipcseBIzSedZ96PhrWPwzWjLC7YdOEkuhA0J46VqjTtr5XdnHlItoGA8JHvzUxp5uL4E9YvFMj22sD2WCYEyxuWWdvnN21rdyGgAHIU29DR+G4r81fvtUh6VL+CP+daPuu26j/Q3+P4n8wffNX2Jr+bJploRRor2KLKTmNt0Y3LJAc+0p9i5GkNG4xoHGo09oCKd8BjVvLmWQQcrodGRuKsPtBGhBBBIINcMUhetsrC7a/KKII3C4u/s295yt8knkWBrnC+UM4M7jxLoL072Db2hs+9YuDXKXttElLiAlGH7DzBI41kRY1mfDzkb/AEmtq4TELeRXTVW56EcCpHAgyCOBBFY32oWtvcw8kJbut3d/eXuEzv4UqzTicQUmTy1PvA/eKLXtBTrXCQvfCAplObugsNfa1ldQPDdPnSAMUa6QWJUELJgEyQOAJgT5xRKAHXo1sw4zF2cPJAdwGM+zbEs7a/NQMfStSbNsZVzZcpaIX5ltRFu1HDKsSN2YseNUj1IbLFzEXbzD2QlldNJuFnf+qtXF/nKvyKvwr2IayfUTnvWmzC5bIFxdxO5hxRo3qfsOoqAdc/RhdoYP+EcOpF7DAi+mmbs11YN4p7QO4qSRIy1Y8VzXj2bi4BmDlbVxN4dXYKDHzlLT4rmHKJZI2rKtNmcXtfRkrH37tx899nZ2C955JKhQFMneMoEeFX11NYsHApbJiAQs7yRcusfCcjWzHLWqh6xNijBbQu2kJNo9+yTP5M7k11GQhkIO4oacerfph8BuizeP4NcY5yPatu2QLfXTehQacQW0OlUQltdj+XH+SFGkIoRSODvZ1MxmU5Wy+ydAQy/RZSrDwYV0RTidmO006YlbOXEWj84XLXvUXP8A2qyz02QttXGgCScViAAOJN59K1JjWyqLm7s2VyeSgjOf1C/vrOPXDhey21ihEBmVx9dFYkepNLZezS0buFENIilMRYa22VomAdCDowDDUEjcRXiOB8kGCDrO4fJIB3H91FuNJJgCSTA3DwE8KqGyV9VuB7XadpyuZcOGxLDn2QzIPW5kHrWlsLZyIFJk72PzmJlm9WJPrVR9SOy7eS5fTMe0e1aOYAEG0vwi7lg6rn7Aa6++riimMK4sztZK5KI09KF/BLn1T7nU/uqN9Dz+Mb452yfdcH+NSbpSPwO75D7y1GOiI/Gd79E39olXLplEf5snUUIo0UIrllIWhRor2KLAT2R3b95BuYW7v1mzW2/s1PmxrKXTS1l2jjByxN8e641atw2mKHjaf/te3H3j76yz1hiNrY3/ANRd++aVydmtpncEMAjSfXyoNEmN3Cd9FoztPADwH+dQGDou5TZQrbgqWV3zTnJ1UZT7MAHUb650SZ3aAnUgbvPefCi0obg17o1AHHfIObfvMRy1OlAF79RmDy4IPp3musfU27aT5dlc95qzoqC9TllVwFvI2YG2NYjXt8VIjwOk8YqexTOP4mTqXeRhIpJRmxFpeWe6fHKBbA990H6tdEUlYfJiVLbri5FPJlJfL9ZZP83412b4I4K/IrMzdau2Di9q4kj2LdxrSCNwSEb0LKzebHnUSFPPTPZ1zC7QxNm97a3WJPMMc6t6qwPrTMGjdSpsF69S3SU37IsXDL2Ys+JtHM1k/UIuJ/OWxwqwts7VGGySs5s3GN0eHjWeuqe8ybQkbuxvMf5pDfWfr2kPpVqdLekS4hkGHBKpmlmlQc2WIETw4gb6vxttUZ+oxr8if2WFcthlKsJBBBHgdDVB9eWDb4Rh8QxEtZFl+bXLL3Edh4aL7xzrQFVJ187OnDLcA/J3A4PJbq9ncgcg1ux63KMvKI6OVSr7KS7mT5WfN4Zcse+ZpfZmzbuJcpZXMwVnIlV7qDMxliBoBSN2yAqsGBDTpIzAiJkcBJ05xSSmDNUGkaL6nMNGz7TFQCy3LpgAAm7eZJgfRw6e+rAion1Xj8XWd0hLa6CN6C7+26al0UzDiJkah3kY0dKV/A735s+4g1F+iI/Gl39C/wDapUs6Tj8CxH6Nz7hNRPomfxrcH8i/9pbqxdM5H4MnsV7FexQio2UnkUIo0UIos6c1r+N2/wBDe+/h6y11huG2tjSP+ouj3ORWprf8ctfor338PWUOkbG5j8W4GYdveuHll7Unz41Rk7NTS/zQ0s8gCBoI046kyee/7KMFyPDqdDqu4+U8KIa8qsZBQoUKANGdSTTs1PAEf1+IP76sKKrDqFxIbBMnzWK+qsXb7LqVaFMQfBkahfsZ5SOMw/aIVBg6FW+aykMjRxhgDHhS9CpFK4dlJ9feyu0TDbSVcpacPfG/LcQsQPGCLiz9EVT6hYMkg6ZREg66yZ00861J002N8LweNwoEm7b+EWv0trLmA5A5bX67VlilpKmbOKW6KZNOq6zN/FNujCXVB5Nea3hx/amp7b2ZbjvLnPNoPuG4elRHqxtZcPirhHt3MPZB8u1vt9tpPeKmfamtLQ404ORm6+b3qKLZqNdYWx/heBuWwJYqVHm0ZNeA7QWiTyU1JqTv2Q6MjbmBU+REGlXyiuEtskzH2H2bduW7lxEJW1lFw6d3O2VRG8ydNK5SI0NSvrE2Bcw2MuvkIt3GzhgO7nae0QeVxbgHgtROl2bKdmnOqO+Lmy7ZB3ZV/Ut20/ux6VM6q/qGxwbBtan2GZV8YOcnw/Kj9U8qtGr49GTnjWRjX0o/iWI/RXPumoh0U/3s/wCguf2lupj0lH4FiP0Nz7hqH9FT+Nm8bNz79s1YumEfgywqFChUSqgUKFCgKODFYxbN1rzkBbOHu3HPIZkbX+jPurH7MSZJknUmtA9cW3Ow2ddVT38bc7BdYbsLE9oY4jtMw8rorPtLzfJrYI7YJBkaCDpoZ1AI9QdDXjGTP+X2V5TxsToxisaM2HtyubJmLIgzROUZiMxjWFk1EuGnhXoiAZ1ndGkaQZnXjpFWBtPq2v28FcxlxVVbNuCtlWOZkORnuG8VZSGktlUjumIqvKAstvqE2jlu3bJI3o8fRebb/wBZ8HH/AO1etZc6sNo9htFPpq6nxZR2tsfr27YrUYq7G+DO1cakmChXtCpipy3+7esOPnlCfosjafrrbPpWVuney/gm0sVYAgJdfKOSMc6D9VhWqdobkPK7Z+26gP2E1QfXThrA2tiWuO63GSwyBUDKx7PK2Ylhl9ld07zyqrJ2aGkfgdfQrD9ns6wJ/KvexBHGJWxb/sr3vp9FGubOGHFm3xSxasmNNbRdH/re2P1q6cKNPWtfSKsKM3WSvKy06FChWaSKl68tis+GN5J+KYXSJOqNFq5p9F+yP88/jNEVr/pBhEu2SLom2QVu8PinBS5r4Bs/mgrKHSDZD4LFXsNd9q05QndIHssByIgjzqma5NPTT3Q/wnvULtREx74W8qsmJTQMAw7S3LKYIgd0uPMitB/wRh/+ns/0af4VjzYl68mJsthp7YXENoDeXzDKPGTFbOtzAzRMCY3TxjwriLZIiu2lS3h8UttQtt7N9lAAUB7atbuqANBuUgce+ajfRcfjY/oX+9bp523iM+AdTB7Rb+ItOu4Iy3brAmdGGbIeYueYDN0ZP4287T/3DTGP4sTzRSbLEoUKFcEwVz7QvFLZKxnMKk7i7kKgPhmInwmuioz0124MHh72IJH4PbLJO44i6DbsjxgFiRyZTXG6RZihukkUd1xbZF/aJsWzNnBqMMmsyV/KMfHNI+oKg1eu5YksSSTJJ1JJ3knjXiiTA3mlzXO/YWybmMvrYtRLSSx0VEUS9xjwVVBJ8q1H0R2Dbwdi2iLlCLlQEQwUwWd/5RyAzcu6u5ZMQ6p+hHwS12l9fjWg3Z3qQQyWB4KYZ/phV/4ZmzathH2IanLfiji+CrcN/DXBNu+hMHiGXsroA5DuHzuGsj4y09h7mHuEjJcKuBr3kJUn9ta7xrZDbu/McZvzH7jSeQkP9Ss39c2y/g22cRAhbuW+v84Jc/rh6jNcl+mlcCK7ExYs4mzdO63ctufJWDH9la62V+RQEyVUIT4p3D9oNY6ZyYngIrXHRW9nwqNz7/8ASfG/367jIateKY70jfvFSqqpZnJAEgRAJJJPDhpO8UtXHh7wzPiHkLbzWlHyvaHaOR45Vgb4E65gBY3SFMUN8qFfgV1ypuMqhWVsiy05TIlzHGDAXhvNZ66zMWt3pK2eCiXcPbI8FFvMPfmrSli+txQ9tgysJVgQQQeII31nTp50Za30lRIJTF3rV5TqdLjgXNeEMH8hFVPk0YRUFwT/AKUW8l20GPe7C3m8WzXCx9SSa4sPurt6x8UiYlAx17Iabye+/AUxWse5HdQAfSOvuWY99a+GcY4lb/6zEywlKTaLhoUKFZxceMoIIIkHQg7iOVUX13dHSBbxigk2yMNfOpJAGbD3SeMp3Cx+UkVetcmJUpcW6EzDRLigSSs5kcDiUbXyZt5gVGStF+nybJclVdTHV7dsn+EsVaIcA/BrLd06iDdaRoYJCg8500NW6+1FRS923dtoBJdlBAG8lshJUDiSABzqLdJukAw7Zrq3FZvZUQGYCYzLbxSyBuzEVCrnSHEYy4tu65FmCwsgllBAES79+5rJ7xgctBRDFJ8jryJk92rZtvhr/ZiLS28Q4J0L3HR1zKN4VVZlBI72adYBMb2BiEt7UDXGVQbTiWIAmEMSfI0ybRwy9jc7o9h+A+aaG0f42nkfuCrow2qhecW3yW9YxKXPybq0b8rBv2UrUL6A/lb3iifYX/xqa1CSp0KzjtdBXcKCzGAASTyA1JqhevDbZJs4OSDri7400e4Mtm230ktAD601dW27qhMrkBGlrpO4WbYL3SfAqMn1xWUeku12xuLvYl5m67PB4LPdX0WB6VTkfoc0kOHIbatPqf6GG66426vE/BgQCAVMPiSDoQh0Uay8cFJqHdBujR2hiQjZhZtw95gJOWQotrwNx2IVRzM7ga1BsfZ4sWwMqqYUZV9lFUQlpPoqNPElm3sa5GNlufLsjx2deHsrbUIggKIG8+pJ1J4knU0pQoVcZgS/ZDoyNqrAqR4EQapDr6whuW8FiyAWy3MNeYf8y027yzdrHlV1YnGpb9o66aDU66CeW+q76xsD8I2Vjky62biYy1PBWJV49BcY/nVCa4sZ0sqlRnqtVdXFzNs+zP8Ay7P24eyf31lWtT9Wn+77X5mH/wDEw9Qx9jGr+BKq4Lq2rWIS7cIVWDglmhc+VSGgnKGyowzb4EV30KtasQhPbKzgwu0MPazOcQrZ++wTvoCeK5AYEQPGJ3kkxXrF6RWDatDD3F7VnPfHduLbSGIB9pQbhtHhMeFTO1hLa6rbRT4KB+wVD+sG1mvYcaaJeP22RpUsWLdNJl89V4vgrtLTuSQDrqXeQT4695j5++ndNn6d52J8DkHoBr7ya6lsKN+tHLA7q1FhgjOnmlLotahQoVlDAKFChQBTvWC8bQvk8Bb93ZIY+0++mHZnfvWydxZhHCMrb+e4GhQp1fBf4NY/Q77Tw6yQFEdjeMRxGSD6SffXu10z4i0NNRx/RzQoVV7LZdoV+DZAoWAS0SNODcqdtgo4xVmbjEZxpLRuPjQoUSiqYTXDD9dG1msYG9l33TbwgPzQwN+96Mq219DWeKFCkJdl2FVBGmerHoumCwyjQsIdyPlXWQEtrwVWyKPFzveBOKFCro9Gdlbc3YK58ZixaAJBJOg8/E0KFSStlTI/etniYlpgbtWn1ot7CC64suSVxNu/hWGm65aZ82gBkdlH1jXlCrJrxZ3A/wBkTLbrBI5aVqjq6SMBbH8nh/8AxMPQoUpDs0dV8CT0KFCrjOBUM6eH46x+jvfes0KFW4P6IjP4sjBXnrXq0KFawqf/2Q==",
        true
    )
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
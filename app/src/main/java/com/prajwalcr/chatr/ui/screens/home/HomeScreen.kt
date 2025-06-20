package com.prajwalcr.chatr.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.prajwalcr.chatr.ui.BlueBlack
import com.prajwalcr.chatr.ui.customDarkGray
import com.prajwalcr.chatr.ui.navigation.ChatScreenRoute
import org.koin.androidx.compose.koinViewModel
import timber.log.Timber

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController) {
    val homeViewModel: HomeViewModel = koinViewModel()
    val channels = homeViewModel.channelList.collectAsState()
    val showAddChannelDialog = remember {
        mutableStateOf(false)
    }
    val sheetState = rememberModalBottomSheetState()

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                modifier = Modifier.
                width(140.dp)
                    .height(64.dp)
                    .clip(RoundedCornerShape(16.dp))
                ,
                onClick = {
                    showAddChannelDialog.value = true
                },
                containerColor = Color.Blue,
            ) {
                Text(text = "Add Channel",
                color= Color.White)
            }
        }
    ) {
        Box(
            modifier = Modifier
                .padding(it)
                .fillMaxSize()
                .background(color = BlueBlack)
        ) {
            Column {
                Spacer(Modifier.fillMaxWidth().padding(5.dp))

                Text(
                    text = "Message",
                    color = Color.DarkGray,
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    style = TextStyle(
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black
                    )
                )

                Spacer(Modifier.fillMaxWidth().padding(5.dp))

                TextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .clip(RoundedCornerShape(20.dp)),
                    value = "",
                    onValueChange = {},
                    placeholder = {
                        Text(
                            text = "Search...",
                        )
                    },
                    colors = TextFieldDefaults.colors().copy(
                        focusedTextColor = customDarkGray,
                        unfocusedTextColor = customDarkGray,
                        focusedContainerColor = customDarkGray,
                        unfocusedContainerColor = customDarkGray,
                        focusedPlaceholderColor = Color.Gray,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                    ),
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Filled.Search,
                            contentDescription = null
                        )
                    }
                )

                Spacer(Modifier.fillMaxWidth().padding(10.dp))

                LazyColumn(
                    modifier = Modifier.weight(1f)
                ) {
                    items(channels.value) { channel ->
                        Column(
                            modifier = Modifier.padding(8.dp)
                        ) {
                            ChannelItem(channel.value ?: "") {
                                channel.key?.let { channelId ->
                                    navController.navigate(ChatScreenRoute(channelId))
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddChannelDialog.value) {
        Timber.i("Showing Add Channel Dialog")
        ModalBottomSheet(
            onDismissRequest = { showAddChannelDialog.value = false},
            sheetState = sheetState
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .onFocusEvent { focusState ->
                        // You can add additional logic here based on focus events if needed.
                        // For example, you can check if focusState.isFocused is true or false.
                    },
            ) {
                AddChannelDialog { channelName ->
                    Timber.i("Sending channel name to view model. Channel Name: $channelName")
                    homeViewModel.addChannel(channelName)
                    showAddChannelDialog.value = false
                }
            }
        }
    }
}

@Composable
private fun AddChannelDialog(onChannelAdded: (String) -> Unit) {
    val channelName = remember {
        mutableStateOf("")
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally, // Center horizontally
        verticalArrangement = Arrangement.Center // Center vertically
    ) {
        Text(text = "Add Channel")
        Spacer(modifier = Modifier.padding(8.dp))
        TextField(
            value = channelName.value,
            onValueChange = {
                channelName.value = it
            },
            label = {
                Text("Enter Channel Name")
            }
        )
        Spacer(modifier = Modifier.padding(8.dp))
        Button(onClick = {
            onChannelAdded(channelName.value)
        }) {
            Text(text = "Add Channel")
        }
    }
}

@Composable
fun ChannelItem(channelName: String, onChannelClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.Gray)
            .padding(8.dp)
            .clickable { onChannelClick() }
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(CircleShape)
                .background(Color.Yellow.copy(alpha = 0.3f))
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = channelName[0].toUpperCase().toString(),
                color = Color.White,
                style = TextStyle(fontSize = 30.sp, textAlign = TextAlign.Center),
            )
        }

        Column(
            modifier = Modifier.fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = channelName,
                color = Color.White,
                style = TextStyle(
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            )
        }
    }
}

@Preview
@Composable
fun PreviewItem() {
    ChannelItem("Test channel") { }
}

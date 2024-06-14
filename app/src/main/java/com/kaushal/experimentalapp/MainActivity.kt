package com.kaushal.experimentalapp

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.clearText
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Send
import androidx.compose.material.icons.outlined.Send
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.Content
import com.google.ai.client.generativeai.type.ResponseStoppedException
import com.google.ai.client.generativeai.type.TextPart
import com.kaushal.experimentalapp.ui.theme.ExperimentalAppTheme
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.haze
import dev.chrisbanes.haze.hazeChild
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.UUID

enum class UserType {
    UserOne,
    UserTwo
}

@Immutable
data class MessageModel(
    val id: String,
    val userType: UserType,
    val message: String
)


class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val chatViewModel: ChatViewModel by viewModels()
            ExperimentalAppTheme {
                val messageList = chatViewModel.messageList
                val message = rememberTextFieldState()
                val listState = rememberLazyListState()
                val scope = rememberCoroutineScope()
                val screenWidth = LocalConfiguration.current.screenWidthDp.dp
                val hazeState = remember {
                    HazeState()
                }
                Scaffold(
                    modifier = Modifier
                        .fillMaxSize(),
                    topBar = {
                        Box(
                            modifier = Modifier
                                .hazeChild(
                                    hazeState,
                                    shape = RoundedCornerShape(
                                        bottomStart = 10.dp,
                                        bottomEnd = 10.dp
                                    )
                                )
                                .statusBarsPadding()
                                .height(56.dp)
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Text(
                                text = "Gemini Chat Bot",
                                fontSize = 20.sp,
                                style = TextStyle(
//                                    fontFamily = FontFamily.SansSerif,
                                    fontWeight = FontWeight.W900,
                                    color = Color.White,
                                    shadow = Shadow(
                                        blurRadius = 60f,
                                        color = Color.Black
                                    )
                                )
                            )
                        }
                    },
                    bottomBar = {
                        Row(
                            Modifier
                                .hazeChild(
                                    hazeState,
                                    shape = RoundedCornerShape(
                                        topStart = 10.dp,
                                        topEnd = 10.dp
                                    )
                                )
                                .imePadding()
                                .navigationBarsPadding()
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            BasicTextField(
                                state = message,
                                modifier = Modifier
                                    .weight(1f),
                                decorator = { innerTextField ->
                                    Box(
                                        modifier = Modifier
                                            .padding(15.dp)
                                            .height(
                                                48.dp
                                            )
                                            .background(
                                                Color.White,
                                                shape = RoundedCornerShape(10.dp)
                                            )
                                            .padding(15.dp, 0.dp),
                                        contentAlignment = Alignment.CenterStart
                                    ) {
                                        if (message.text.isEmpty()) {
                                            Text(
                                                text = "Enter message...",
                                                color = Color.Gray,
                                                fontSize = 14.sp,
                                                fontFamily = FontFamily.SansSerif,
                                                lineHeight = 16.sp
                                            )
                                        }
                                        innerTextField()
                                    }
                                },
                                textStyle = TextStyle(
                                    color = Color.DarkGray,
                                    fontSize = 14.sp,
                                    fontFamily = FontFamily.SansSerif,
                                    lineHeight = 16.sp
                                ),
                                lineLimits = TextFieldLineLimits.SingleLine
                            )
                            IconButton(
                                onClick = {
                                    if (message.text.trim()
                                            .isNotEmpty() && chatViewModel.messageList.lastOrNull()?.message != "......"
                                    ) {
                                        scope.launch {
                                            chatViewModel.addMessage(message.text.toString()) {
                                                launch {
                                                    listState.scrollToItem(
                                                        0
                                                    )
                                                }
                                            }
                                            message.clearText()
                                            listState.scrollToItem(
                                                0
                                            )
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .shadow(
                                        5.dp,
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                    .background(
                                        Color.White,
                                        shape = RoundedCornerShape(10.dp)
                                )
                            ) {
                                Image(
                                    imageVector = Icons.AutoMirrored.Outlined.Send,
                                    contentDescription = ""
                                )
                            }
                            Spacer(modifier = Modifier.width(20.dp))
                        }
                    },
                    containerColor = Color.Transparent
                ) { innerPadding ->
                    Box(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.wallpaper_image),
                            contentScale = ContentScale.Crop,
                            contentDescription = null,
                            modifier = Modifier
                                .haze(
                                    hazeState,
                                    backgroundColor = Color.White,
                                    tint = Color.White.copy(alpha = .3f),
                                    blurRadius = 30.dp,
                                    noiseFactor = .1f
                                )
                                .fillMaxSize()
                        )
                    }
                    LazyColumn(
                        Modifier
                            .fillMaxSize(),
                        contentPadding = PaddingValues(
                            top = innerPadding.calculateTopPadding() + 10.dp,
                            bottom = innerPadding.calculateBottomPadding() + 10.dp
                        ),
                        state = listState,
                        verticalArrangement = Arrangement.spacedBy(
                            10.dp,
                            alignment = Alignment.Top
                        ),
                        reverseLayout = true
                    ) {
                        items(
                            messageList.toList().asReversed(),
                            key = { item ->
                                item.id
                            }
                        ) { message ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .animateItem()
                            ) {
                                Text(
                                    text = message.message,
                                    modifier = Modifier
                                        .widthIn(
                                            max = screenWidth * .85f
                                        )
                                        .align(
                                            if (message.userType == UserType.UserOne) Alignment.CenterStart else Alignment.CenterEnd
                                        )
                                        .padding(horizontal = 15.dp)
                                        .background(
                                            if (message.userType == UserType.UserOne) Color.Cyan else Color.White,
                                            shape = RoundedCornerShape(10.dp)
                                        )
                                        .shadow(
                                            15.dp,
                                            shape = RoundedCornerShape(10.dp),
                                            ambientColor = if (message.userType == UserType.UserOne) Color.Cyan else Color.White,
                                            spotColor = if (message.userType == UserType.UserOne) Color.Cyan else Color.White
                                        )
                                        .padding(12.dp),
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 13.sp,
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

class ChatViewModel : ViewModel() {
    private val generativeMessageModel = GenerativeModel(
        modelName = "gemini-pro",
        apiKey = "AIzaSyA9wJntsM6pDo4_K5-bOARSZUddyTj4OUM"
    )
    var messageList = mutableStateListOf<MessageModel>()
        private set

    fun addMessage(message: String, onChatResponse: () -> Unit) {
        messageList.add(
            MessageModel(UUID.randomUUID().toString().slice(0..6), UserType.UserTwo, message)
        )

        try {
            viewModelScope.launch(Dispatchers.IO) {
                messageList.add(
                    MessageModel(
                        UUID.randomUUID().toString().slice(0..6),
                        UserType.UserOne,
                        "......"
                    )
                )
                delay(100)
                onChatResponse()
                val response = generativeMessageModel.startChat(
                    messageList.map {
                        Content(
                            role = if (it.userType == UserType.UserOne) "model" else "user",
                            parts = listOf(TextPart(it.message))
                        )
                    }
                ).sendMessage(message)
                messageList.removeLast()

                println(
                    response.text.toString()
                )
                messageList.add(
                    MessageModel(
                        UUID.randomUUID().toString().slice(0..6),
                        UserType.UserOne,
                        response.text.toString()
                    )
                )
                delay(100)
                onChatResponse()
            }
        } catch (e: Exception) {
            viewModelScope.launch {

                println(
                    e.message.toString()
                )
                messageList.add(
                    MessageModel(
                        UUID.randomUUID().toString().slice(0..6),
                        UserType.UserOne,
                        e.localizedMessage ?: "Something went wrong..."
                    )
                )
                delay(100)
                onChatResponse()
            }
        } catch (e: ResponseStoppedException) {
            viewModelScope.launch {
                println(
                    e.message.toString()
                )
                messageList.add(
                    MessageModel(
                        UUID.randomUUID().toString().slice(0..6),
                        UserType.UserOne,
                        e.localizedMessage ?: "Something went wrong..."
                    )
                )
                delay(100)
                onChatResponse()
            }
        }

    }


}
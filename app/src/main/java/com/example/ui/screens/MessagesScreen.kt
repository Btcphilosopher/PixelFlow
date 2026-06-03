package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.model.DirectMessage
import com.example.ui.PixelViewModel
import com.example.ui.theme.PixelPrimary

@Composable
fun MessagesScreen(
    viewModel: PixelViewModel,
    modifier: Modifier = Modifier
) {
    val messages by viewModel.directMessages.collectAsState(initial = emptyList())
    val isTyping by viewModel.isTyping.collectAsState()

    // Screen-level state: null means inbox list, value means active chat thread with user
    var activeChatUser by remember { mutableStateOf<String?>(null) }
    var activeChatAvatar by remember { mutableStateOf("") }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        if (activeChatUser == null) {
            // Inbox summary overview list
            Column(modifier = Modifier.fillMaxSize()) {
                Surface(
                    modifier = Modifier.fillMaxWidth().statusBarsPadding(),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 2.dp,
                    shadowElevation = 2.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Direct Messages",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }

                // Hardcoded Inbox conversations but linked to database
                val inboxRooms = listOf(
                    InboxItem("sophia_travels", "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=150&auto=format&fit=crop&q=80", "Active 3m ago"),
                    InboxItem("liam_design", "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=150&auto=format&fit=crop&q=80", "Active 1h ago"),
                    InboxItem("elena.style", "https://images.unsplash.com/photo-1438761681033-6461ffad8d80?w=150&auto=format&fit=crop&q=80", "Active yesterday")
                )

                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(inboxRooms) { room ->
                        InboxRowItem(
                            item = room,
                            onClick = {
                                activeChatUser = room.username
                                activeChatAvatar = room.avatar
                            }
                        )
                        Divider(
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                }
            }
        } else {
            // Active chat thread dialog console
            ChatThreadLayout(
                username = activeChatUser!!,
                avatar = activeChatAvatar,
                messages = messages,
                isTyping = isTyping,
                onSendMessage = { text -> viewModel.sendDirectMessage(text) },
                onBack = { activeChatUser = null }
            )
        }
    }
}

data class InboxItem(
    val username: String,
    val avatar: String,
    val activeTimeSnippet: String
)

@Composable
fun InboxRowItem(
    item: InboxItem,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp)
            .testTag("inbox_item_${item.username}"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = item.avatar,
            contentDescription = null,
            modifier = Modifier
                .size(54.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.username,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = item.activeTimeSnippet,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.outlineVariant,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
fun ChatThreadLayout(
    username: String,
    avatar: String,
    messages: List<DirectMessage>,
    isTyping: Boolean,
    onSendMessage: (String) -> Unit,
    onBack: () -> Unit
) {
    var messageText by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize()) {
        // Chat header block
        Surface(
            modifier = Modifier.fillMaxWidth().statusBarsPadding(),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 4.dp,
            tonalElevation = 2.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack, modifier = Modifier.testTag("chat_back_button")) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Return to Inbox")
                }
                Spacer(modifier = Modifier.width(4.dp))
                AsyncImage(
                    model = avatar,
                    contentDescription = null,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = username,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Online creator",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // Messages scrolling log
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .testTag("chat_messages_log"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(messages) { msg ->
                val isMe = msg.senderUsername == "user_me"
                // Match sender context roughly
                val shouldDisplay = isMe || msg.senderUsername.equals(username, ignoreCase = true)

                if (shouldDisplay) {
                    ChatBubble(msg = msg, isMe = isMe)
                }
            }

            // Real-time animated typing indicator
            if (isTyping) {
                item {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        AsyncImage(
                            model = avatar,
                            contentDescription = null,
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "typing...",
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                fontWeight = FontWeight.Bold,
                                color = PixelPrimary
                            )
                        }
                    }
                }
            }
        }

        // Send Input Bar Box
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Photo attachments mock button
                IconButton(onClick = { 
                    // Autofills a scenic photo link attachment
                    onSendMessage("Sent reference image: sunset view 🌅") 
                }) {
                    Icon(
                        imageVector = Icons.Default.AddPhotoAlternate,
                        contentDescription = "Attach visual photo",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                TextField(
                    value = messageText,
                    onValueChange = { messageText = it },
                    placeholder = { Text("Send direct chat...", fontSize = 13.sp) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("chat_input_text_field"),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.background,
                        unfocusedContainerColor = MaterialTheme.colorScheme.background,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    shape = RoundedCornerShape(24.dp)
                )

                Spacer(modifier = Modifier.width(6.dp))

                Button(
                    onClick = {
                        if (messageText.trim().isNotEmpty()) {
                            onSendMessage(messageText)
                            messageText = ""
                        }
                    },
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier.testTag("chat_send_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Post message",
                        modifier = Modifier.size(16.dp),
                        tint = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun ChatBubble(msg: DirectMessage, isMe: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
    ) {
        if (!isMe) {
            AsyncImage(
                model = msg.senderAvatar,
                contentDescription = null,
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(8.dp))
        }

        Column(horizontalAlignment = if (isMe) Alignment.End else Alignment.Start) {
            Surface(
                color = if (isMe) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(
                    topStart = 16.dp,
                    topEnd = 16.dp,
                    bottomStart = if (isMe) 16.dp else 2.dp,
                    bottomEnd = if (isMe) 2.dp else 16.dp
                ),
                tonalElevation = 2.dp
            ) {
                Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                    if (msg.imageUrl != null) {
                        AsyncImage(
                            model = msg.imageUrl,
                            contentDescription = null,
                            modifier = Modifier
                                .size(width = 160.dp, height = 110.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .padding(bottom = 4.dp),
                            contentScale = ContentScale.Crop
                        )
                    }
                    if (msg.text.isNotEmpty()) {
                        Text(
                            text = msg.text,
                            fontSize = 12.sp,
                            color = if (isMe) Color.White else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Read", // simulated
                fontSize = 8.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }
    }
}

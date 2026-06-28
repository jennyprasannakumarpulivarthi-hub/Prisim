package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.Message
import com.example.data.model.User
import com.example.ui.theme.SleekCyan
import com.example.ui.theme.TechPurple
import com.example.ui.widgets.MediaRenderer
import com.example.ui.viewmodel.PrisimViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun MessageScreen(
    viewModel: PrisimViewModel,
    innerPadding: PaddingValues
) {
    val activePartner by viewModel.activeChatPartner.collectAsStateWithLifecycle()
    val allUsers by viewModel.allUsers.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
    ) {
        if (activePartner == null) {
            // Conversational Inbox Screen
            ConversationInbox(viewModel, allUsers)
        } else {
            // Active Conversation screen
            ActiveChat(viewModel, activePartner!!, currentUser)
        }
    }
}

@Composable
fun ConversationInbox(viewModel: PrisimViewModel, users: List<User>) {
    val filteredUsers = remember(users) { users.filter { !it.isMe } }

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "Collaborators Chat",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(16.dp),
            color = MaterialTheme.colorScheme.onSurface
        )

        if (filteredUsers.isEmpty()) {
            Box(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text("Your collaborator list is empty.", color = MaterialTheme.colorScheme.tertiary)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(filteredUsers) { user ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.openChatWith(user) }
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box {
                            MediaRenderer(user.profilePicture, modifier = Modifier.size(52.dp), isAvatar = true)
                            // Simulated online indicator badge
                            Box(
                                modifier = Modifier
                                    .size(14.dp)
                                    .align(Alignment.BottomEnd)
                                    .clip(CircleShape)
                                    .background(Color(0xFF10B981)) // Green online
                                    .background(Color.White.copy(alpha = 0.2f))
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = user.displayName,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                if (user.isVerified) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "Verified",
                                        tint = SleekCyan,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = user.bio,
                                style = MaterialTheme.typography.bodySmall,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                color = MaterialTheme.colorScheme.tertiary
                            )
                        }

                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = "Open chat",
                            tint = MaterialTheme.colorScheme.tertiary
                        )
                    }
                    Divider(color = MaterialTheme.colorScheme.surfaceVariant, modifier = Modifier.padding(horizontal = 16.dp))
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ActiveChat(
    viewModel: PrisimViewModel,
    partner: User,
    currentUser: User?
) {
    val messages by viewModel.activeChatMessages.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    var textInput by remember { mutableStateOf("") }
    var isTyping by remember { mutableStateOf(false) }

    // Scroll to latest message on load or message size update
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    // Typing simulation on message submit
    var submittedMessagesCount by remember { mutableStateOf(0) }
    LaunchedEffect(submittedMessagesCount) {
        if (submittedMessagesCount > 0) {
            isTyping = true
            delay(1500)
            isTyping = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("active_chat_layout")
    ) {
        // Chat Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .statusBarsPadding()
                .padding(horizontal = 8.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { viewModel.closeChat() }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back to list")
            }
            MediaRenderer(partner.profilePicture, modifier = Modifier.size(40.dp), isAvatar = true)
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = partner.displayName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = if (isTyping) "typing collaboration specs..." else "online",
                    fontSize = 11.sp,
                    color = if (isTyping) SleekCyan else Color(0xFF10B981),
                    fontWeight = FontWeight.Medium
                )
            }
            IconButton(onClick = { /* Simulated Call */ }) {
                Icon(Icons.Default.VideoCall, contentDescription = "Video Session")
            }
        }

        Divider(color = MaterialTheme.colorScheme.surfaceVariant)

        // Conversation List
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            items(messages, key = { it.id }) { message ->
                val isMe = message.senderId == "me"
                var showMsgMenu by remember { mutableStateOf(false) }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
                ) {
                    Column(
                        horizontalAlignment = if (isMe) Alignment.End else Alignment.Start
                    ) {
                        // Main Speech Bubble
                        Box(
                            modifier = Modifier
                                .clip(
                                    RoundedCornerShape(
                                        topStart = 16.dp,
                                        topEnd = 16.dp,
                                        bottomStart = if (isMe) 16.dp else 2.dp,
                                        bottomEnd = if (isMe) 2.dp else 16.dp
                                    )
                                )
                                .background(
                                    if (isMe) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.surfaceVariant
                                )
                                .combinedClickable(
                                    onClick = {},
                                    onLongClick = { showMsgMenu = true }
                                )
                                .padding(horizontal = 14.dp, vertical = 10.dp)
                                .widthIn(max = 260.dp)
                        ) {
                            Column {
                                // Content media attachments if any
                                if (message.mediaUrl != null) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(140.dp)
                                            .padding(bottom = 6.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                    ) {
                                        MediaRenderer(message.mediaUrl, modifier = Modifier.fillMaxSize())
                                    }
                                }

                                if (message.mediaType == "VOICE") {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(vertical = 4.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.PlayArrow,
                                            contentDescription = null,
                                            tint = if (isMe) Color.White else SleekCyan,
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        // Simple voice waveform lines
                                        Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                            repeat(12) { index ->
                                                val h = listOf(6, 12, 18, 14, 8, 16, 20, 10, 12, 18, 14, 4)[index].dp
                                                Box(
                                                    modifier = Modifier
                                                        .width(2.dp)
                                                        .height(h)
                                                        .background(if (isMe) Color.White.copy(alpha = 0.8f) else SleekCyan)
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "0:12",
                                            fontSize = 10.sp,
                                            color = if (isMe) Color.White else MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                } else {
                                    Text(
                                        text = message.content,
                                        color = if (isMe) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        }

                        // Message reaction display
                        if (message.reactions.isNotEmpty()) {
                            Box(
                                modifier = Modifier
                                    .offset(y = (-6).dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.surface)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(text = message.reactions, fontSize = 10.sp)
                            }
                        }

                        // Read receipt checkmark
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = 2.dp)
                        ) {
                            Text(
                                text = "10:42 AM",
                                fontSize = 9.sp,
                                color = MaterialTheme.colorScheme.tertiary
                            )
                            if (isMe) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Read",
                                    tint = SleekCyan,
                                    modifier = Modifier.size(10.dp)
                                )
                            }
                        }
                    }
                }

                // Emoji reaction sheet / delete menu
                DropdownMenu(
                    expanded = showMsgMenu,
                    onDismissRequest = { showMsgMenu = false }
                ) {
                    DropdownMenuItem(
                        text = {
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                listOf("❤️", "👍", "😮", "🚀").forEach { emoji ->
                                    Text(
                                        text = emoji,
                                        fontSize = 20.sp,
                                        modifier = Modifier.clickable {
                                            viewModel.reactToMessage(message.id, emoji)
                                            showMsgMenu = false
                                        }
                                    )
                                }
                            }
                        },
                        onClick = {}
                    )
                    if (isMe) {
                        Divider()
                        DropdownMenuItem(
                            text = { Text("Delete Message", color = Color.Red) },
                            onClick = {
                                viewModel.deleteMessage(message.id)
                                showMsgMenu = false
                            },
                            leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Red) }
                        )
                    }
                }
            }
        }

        // Typing Indicator Row
        AnimatedVisibility(visible = isTyping) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "${partner.displayName} is typing specifications...",
                    fontSize = 11.sp,
                    color = SleekCyan,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Input Tray / Toolbar
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Media attachment button
                IconButton(
                    onClick = {
                        // Simulate sending a mock layout photo
                        viewModel.sendMessage(
                            receiverId = partner.uid,
                            content = "Project Wireframe Spec attached.",
                            mediaUrl = "post_design_1",
                            mediaType = "PHOTO"
                        )
                    }
                ) {
                    Icon(Icons.Default.AttachFile, contentDescription = "Share wireframe", tint = SleekCyan)
                }

                // Voice Recording Simulator Button
                IconButton(
                    onClick = {
                        viewModel.sendMessage(
                            receiverId = partner.uid,
                            content = "Voice specification memo.",
                            mediaType = "VOICE"
                        )
                    }
                ) {
                    Icon(Icons.Default.Mic, contentDescription = "Record voice memo", tint = SleekCyan)
                }

                // Input Field
                OutlinedTextField(
                    value = textInput,
                    onValueChange = { textInput = it },
                    placeholder = { Text("Collaborate securely...") },
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 4.dp)
                        .testTag("chat_text_input"),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SleekCyan,
                        unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                )

                // Send Button
                IconButton(
                    onClick = {
                        if (textInput.isNotEmpty()) {
                            viewModel.sendMessage(partner.uid, textInput)
                            textInput = ""
                            submittedMessagesCount++
                        }
                    },
                    colors = IconButtonDefaults.iconButtonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Send message",
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
    }
}

package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.SleekCyan
import com.example.ui.widgets.MediaRenderer
import com.example.ui.viewmodel.PrisimViewModel
import kotlinx.coroutines.delay

@Composable
fun MomentsViewer(viewModel: PrisimViewModel) {
    val activeUser by viewModel.activeStoryUser.collectAsState()
    val moments by viewModel.moments.collectAsState()
    
    if (activeUser == null) return

    val userMoments = remember(activeUser, moments) {
        moments.filter { it.userId == activeUser!!.uid }
    }

    if (userMoments.isEmpty()) {
        viewModel.closeStoryViewer()
        return
    }

    var currentMomentIndex by remember { mutableStateOf(0) }
    var replyText by remember { mutableStateOf("") }
    var progress by remember { mutableStateOf(0f) }

    // Auto-advance logic
    LaunchedEffect(currentMomentIndex, activeUser) {
        progress = 0f
        val durationMs = 4000L
        val stepMs = 50L
        val totalSteps = durationMs / stepMs

        for (step in 1..totalSteps) {
            delay(stepMs)
            progress = step.toFloat() / totalSteps
        }

        if (currentMomentIndex < userMoments.lastIndex) {
            currentMomentIndex++
        } else {
            viewModel.closeStoryViewer()
        }
    }

    val activeMoment = userMoments[currentMomentIndex]

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .statusBarsPadding()
            .navigationBarsPadding()
            .testTag("moments_viewer")
    ) {
        // Full screen graphics
        MediaRenderer(
            mediaName = activeMoment.mediaUrl,
            modifier = Modifier.fillMaxSize()
        )

        // Story Top Controls & Indicators
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .padding(16.dp)
        ) {
            // Horizontal segment progress indicators
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                userMoments.forEachIndexed { index, _ ->
                    val segmentProgress = when {
                        index < currentMomentIndex -> 1f
                        index == currentMomentIndex -> progress
                        else -> 0f
                    }
                    LinearProgressIndicator(
                        progress = { segmentProgress },
                        modifier = Modifier
                            .weight(1f)
                            .height(3.dp)
                            .clip(CircleShape),
                        color = SleekCyan,
                        trackColor = Color.White.copy(alpha = 0.3f),
                    )
                }
            }

            // User Info
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                MediaRenderer(activeUser!!.profilePicture, modifier = Modifier.size(36.dp), isAvatar = true)
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = activeUser!!.displayName,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        if (activeUser!!.isVerified) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Verified",
                                tint = SleekCyan,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                    Text(
                        text = if (activeMoment.isHighlight) "Story Highlight" else "Active Moment",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 11.sp
                    )
                }
                IconButton(onClick = { viewModel.closeStoryViewer() }) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close story viewer",
                        tint = Color.White
                    )
                }
            }
        }

        // Left/Right click detectors for manual tapping
        Row(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clickable {
                        if (currentMomentIndex > 0) {
                            currentMomentIndex--
                        }
                    }
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clickable {
                        if (currentMomentIndex < userMoments.lastIndex) {
                            currentMomentIndex++
                        } else {
                            viewModel.closeStoryViewer()
                        }
                    }
            )
        }

        // Bottom Story Replies Section
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = replyText,
                onValueChange = { replyText = it },
                placeholder = { Text("Reply to Moment...", color = Color.White.copy(alpha = 0.6f)) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedContainerColor = Color.White.copy(alpha = 0.15f),
                    unfocusedContainerColor = Color.White.copy(alpha = 0.10f),
                    focusedBorderColor = SleekCyan,
                    unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                ),
                modifier = Modifier
                    .weight(1f)
                    .testTag("moment_reply_input"),
                shape = RoundedCornerShape(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = {
                    if (replyText.isNotEmpty()) {
                        // Send reply as a collaborative chat message!
                        viewModel.sendMessage(
                            receiverId = activeUser!!.uid,
                            content = "Replied to your Moment: \"$replyText\""
                        )
                        replyText = ""
                        viewModel.closeStoryViewer()
                    }
                },
                colors = IconButtonDefaults.iconButtonColors(containerColor = SleekCyan)
            ) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = "Send Reply",
                    tint = Color.Black
                )
            }
        }
    }
}

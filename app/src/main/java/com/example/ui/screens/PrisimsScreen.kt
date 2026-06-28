package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.Prisim
import com.example.data.model.User
import com.example.ui.theme.SleekCyan
import com.example.ui.widgets.MediaRenderer
import com.example.ui.viewmodel.PrisimViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrisimsScreen(
    viewModel: PrisimViewModel,
    innerPadding: PaddingValues
) {
    val prisims by viewModel.prisims.collectAsStateWithLifecycle()
    val allUsers by viewModel.allUsers.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()

    val lazyListState = rememberLazyListState()
    var showAddPrisimDialog by remember { mutableStateOf(false) }
    var activeCommentsPrisimId by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .background(Color.Black)
    ) {
        if (prisims.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("No Prisim video highlights available.", color = Color.White)
            }
        } else {
            // Full screen vertical pager list
            LazyColumn(
                state = lazyListState,
                modifier = Modifier.fillMaxSize()
            ) {
                itemsIndexed(prisims, key = { _, p -> p.id }) { index, prisim ->
                    val author = allUsers.find { it.uid == prisim.userId } ?: currentUser
                    if (author != null) {
                        Box(
                            modifier = Modifier
                                .fillParentMaxSize()
                                .background(Color.Black)
                        ) {
                            // Fullscreen media card
                            MediaRenderer(
                                mediaName = prisim.videoUrl,
                                modifier = Modifier.fillMaxSize()
                            )

                            // Overlay Controls / Metadata
                            PrisimOverlayControls(
                                prisim = prisim,
                                author = author,
                                viewModel = viewModel,
                                onOpenComments = { activeCommentsPrisimId = prisim.id }
                            )
                        }
                    }
                }
            }
        }

        // Add Prisim floating button
        FloatingActionButton(
            onClick = { showAddPrisimDialog = true },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
                .testTag("add_prisim_fab"),
            containerColor = SleekCyan,
            contentColor = Color.Black
        ) {
            Icon(Icons.Default.VideoCall, contentDescription = "Create Prisim")
        }

        // Add Prisim Dialog
        if (showAddPrisimDialog) {
            AddPrisimDialog(
                onDismiss = { showAddPrisimDialog = false },
                onSubmit = { caption, music ->
                    val randomVideo = listOf("video_interaction_demo", "video_compose_animation").random()
                    viewModel.createPrisim(randomVideo, caption, music)
                    showAddPrisimDialog = false
                }
            )
        }

        // Comments dialog
        if (activeCommentsPrisimId != null) {
            // Reusing comment dialogue
            CommentsDialog(
                postId = activeCommentsPrisimId!!,
                viewModel = viewModel,
                onDismiss = { activeCommentsPrisimId = null }
            )
        }
    }
}

@Composable
fun PrisimOverlayControls(
    prisim: Prisim,
    author: User,
    viewModel: PrisimViewModel,
    onOpenComments: () -> Unit
) {
    // Rotation Animation for the Lofi Vinyl Disc
    val infiniteTransition = rememberInfiniteTransition(label = "VinylDisc")
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "DiscRotation"
    )

    var isPaused by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable { isPaused = !isPaused }
    ) {
        // Play/Pause Overlay visual indicator
        AnimatedVisibility(
            visible = isPaused,
            enter = fadeIn() + scaleIn(),
            exit = fadeOut() + scaleOut(),
            modifier = Modifier.align(Alignment.Center)
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Paused",
                    tint = Color.White,
                    modifier = Modifier.size(36.dp)
                )
            }
        }

        // Bottom and Right Content Overlays
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f))
                    )
                )
                .padding(16.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            // Bottom Metadata
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    MediaRenderer(author.profilePicture, modifier = Modifier.size(36.dp), isAvatar = true)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "@${author.username}",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    if (author.isVerified) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Verified",
                            tint = SleekCyan,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    // Quick simulated follow
                    Text(
                        text = if (author.isMe) "" else "Collaborate",
                        color = SleekCyan,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable {
                            viewModel.toggleFollowUser(author.uid)
                        }
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = prisim.caption,
                    color = Color.White,
                    fontSize = 13.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Scrolling Music Marquee
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = "Background music",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = prisim.musicTitle,
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Right Action Controls Panel
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.width(56.dp)
            ) {
                // Like Button
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    IconButton(
                        onClick = { viewModel.toggleLikePrisim(prisim.id) },
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = Color.Black.copy(alpha = 0.4f)
                        )
                    ) {
                        Icon(
                            imageVector = if (prisim.isLikedByMe) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = "Like Prisim",
                            tint = if (prisim.isLikedByMe) Color.Red else Color.White,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                    Text(
                        text = "${prisim.likesCount}",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Comment Button
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    IconButton(
                        onClick = onOpenComments,
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = Color.Black.copy(alpha = 0.4f)
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.ChatBubbleOutline,
                            contentDescription = "Prisim feedback thread",
                            tint = Color.White,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                    Text(
                        text = "Reply",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Save button
                IconButton(
                    onClick = { /* Save simulator */ },
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = Color.Black.copy(alpha = 0.4f)
                    )
                ) {
                    Icon(
                        imageVector = Icons.Outlined.BookmarkBorder,
                        contentDescription = "Save Prisim highlight",
                        tint = Color.White,
                        modifier = Modifier.size(26.dp)
                    )
                }

                // Share button
                IconButton(
                    onClick = { /* Share simulator */ },
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = Color.Black.copy(alpha = 0.4f)
                    )
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Send,
                        contentDescription = "Share",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Spinning Lofi Music Disc
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .rotate(rotationAngle)
                        .clip(CircleShape)
                        .background(Color.DarkGray),
                    contentAlignment = Alignment.Center
                ) {
                    // Outer vinyl lines
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawCircle(color = Color.Black, radius = size.minDimension / 2, style = Stroke(width = 3.dp.toPx()))
                        drawCircle(color = Color.White, radius = size.minDimension / 6)
                    }
                }
            }
        }
    }
}

@Composable
fun AddPrisimDialog(
    onDismiss: () -> Unit,
    onSubmit: (caption: String, music: String) -> Unit
) {
    var caption by remember { mutableStateOf("") }
    var music by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Publish Prisim Highlight Video", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Publish short, immersive micro-interactions or terminal demos to inspire other Prisimers.", fontSize = 12.sp, color = MaterialTheme.colorScheme.tertiary)
                OutlinedTextField(
                    value = caption,
                    onValueChange = { caption = it },
                    label = { Text("Prisim Caption") },
                    placeholder = { Text("E.g., Micro-interactions review...") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = music,
                    onValueChange = { music = it },
                    label = { Text("Background Soundtrack Title") },
                    placeholder = { Text("E.g., Tech Beats - Chill Lofi") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (caption.isNotEmpty()) {
                        onSubmit(caption, music.ifEmpty { "Dynamic Sound - Original Audio" })
                    }
                },
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Publish Prisim")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.Comment
import com.example.data.model.Post
import com.example.data.model.User
import com.example.ui.theme.SleekCyan
import com.example.ui.theme.TechPurple
import com.example.ui.widgets.MediaRenderer
import com.example.ui.viewmodel.PrisimViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun FeedScreen(
    viewModel: PrisimViewModel,
    innerPadding: PaddingValues
) {
    val posts by viewModel.posts.collectAsStateWithLifecycle()
    val moments by viewModel.moments.collectAsStateWithLifecycle()
    val allUsers by viewModel.allUsers.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()

    var showAddPostDialog by remember { mutableStateOf(false) }
    var showAddMomentDialog by remember { mutableStateOf(false) }
    var activeCommentPostId by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            // Section 1: Tray of Moments (Stories)
            item {
                Text(
                    text = "Moments",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 16.dp, top = 12.dp, bottom = 4.dp),
                    color = MaterialTheme.colorScheme.onSurface
                )
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // "Add Moment" Button
                    item {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.clickable { showAddMomentDialog = true }
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surfaceVariant),
                                contentAlignment = Alignment.Center
                            ) {
                                MediaRenderer("avatar_placeholder", modifier = Modifier.size(66.dp), isAvatar = true)
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .align(Alignment.BottomEnd)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.Add,
                                        contentDescription = "Add Moment",
                                        tint = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Your Moment",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                            )
                        }
                    }

                    // Seeded Users Moments
                    val usersWithMoments = allUsers.filter { user ->
                        moments.any { it.userId == user.uid }
                    }
                    items(usersWithMoments) { user ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.clickable { viewModel.openStoryViewer(user) }
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.linearGradient(
                                            colors = listOf(SleekCyan, TechPurple)
                                        )
                                    )
                                    .padding(3.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                MediaRenderer(user.profilePicture, modifier = Modifier.size(66.dp), isAvatar = true)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = user.displayName.substringBefore(" "),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
                Divider(color = MaterialTheme.colorScheme.surfaceVariant, thickness = 1.dp)
            }

            // Section 2: Timeline of Posts
            val visiblePosts = posts.filter { !it.isReported }
            if (visiblePosts.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(64.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Default.Feed,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.tertiary
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No collaborative updates yet.",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Be the first to post a design review or engineering milestone!",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.padding(horizontal = 24.dp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            } else {
                items(visiblePosts, key = { it.id }) { post ->
                    val author = allUsers.find { it.uid == post.userId } ?: currentUser
                    if (author != null) {
                        PostCard(
                            post = post,
                            author = author,
                            viewModel = viewModel,
                            onOpenComments = { activeCommentPostId = post.id }
                        )
                    }
                }
            }
        }

        // Floating Action Button to post new milestone/update
        FloatingActionButton(
            onClick = { showAddPostDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("add_post_fab"),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ) {
            Icon(Icons.Default.Create, contentDescription = "New Milestone Post")
        }

        // --- Post Creation Dialog ---
        if (showAddPostDialog) {
            AddPostDialog(
                onDismiss = { showAddPostDialog = false },
                onSubmit = { caption, tag, location ->
                    // Simulate selecting custom beautiful design canvas illustrations
                    val randomImage = listOf("post_design_1", "post_design_2", "post_code_1").random()
                    viewModel.createPost(caption, listOf(randomImage), location)
                    showAddPostDialog = false
                }
            )
        }

        // --- Moment Creation Dialog ---
        if (showAddMomentDialog) {
            AddMomentDialog(
                onDismiss = { showAddMomentDialog = false },
                onSubmit = { content ->
                    // Save as a moment
                    val randomMoment = listOf("moment_design_sprint", "moment_code_review", "moment_scrum_board").random()
                    viewModel.createMoment(randomMoment, isVideo = false)
                    showAddMomentDialog = false
                }
            )
        }

        // --- Comments Sheet Dialog ---
        if (activeCommentPostId != null) {
            CommentsDialog(
                postId = activeCommentPostId!!,
                viewModel = viewModel,
                onDismiss = { activeCommentPostId = null }
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PostCard(
    post: Post,
    author: User,
    viewModel: PrisimViewModel,
    onOpenComments: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }
    val images = remember(post.imageUrls) { post.imageUrls.split(",") }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 12.dp)
            .testTag("post_card_${post.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(28.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                MediaRenderer(author.profilePicture, modifier = Modifier.size(42.dp), isAvatar = true)
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = author.displayName,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (author.isVerified) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Verified Project Lead",
                                tint = SleekCyan,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    post.location?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.tertiary
                        )
                    }
                }
                Box {
                    IconButton(onClick = { menuExpanded = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Options")
                    }
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false }
                    ) {
                        if (author.isMe) {
                            DropdownMenuItem(
                                text = { Text("Delete Post") },
                                onClick = {
                                    viewModel.deletePost(post.id)
                                    menuExpanded = false
                                },
                                leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null) }
                            )
                        } else {
                            DropdownMenuItem(
                                text = { Text("Report Post") },
                                onClick = {
                                    viewModel.reportPost(post.id)
                                    menuExpanded = false
                                },
                                leadingIcon = { Icon(Icons.Default.Warning, contentDescription = null) }
                            )
                        }
                    }
                }
            }

            // Image Carousel with indicators
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .background(Color.Black)
            ) {
                val pagerState = rememberPagerState(pageCount = { images.size })
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize()
                ) { page ->
                    MediaRenderer(images[page], modifier = Modifier.fillMaxSize())
                }

                // Page indicators if more than 1 image
                if (images.size > 1) {
                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(12.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.Black.copy(alpha = 0.5f))
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        repeat(images.size) { index ->
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (pagerState.currentPage == index) SleekCyan else Color.White.copy(alpha = 0.5f)
                                    )
                            )
                        }
                    }
                }
            }

            // Action Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { viewModel.toggleLikePost(post.id) }) {
                    Icon(
                        imageVector = if (post.isLikedByMe) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = "Like Post",
                        tint = if (post.isLikedByMe) Color.Red else MaterialTheme.colorScheme.onSurface
                    )
                }
                IconButton(onClick = onOpenComments) {
                    Icon(Icons.Outlined.ChatBubbleOutline, contentDescription = "Comment")
                }
                IconButton(onClick = { /* Share Simulation */ }) {
                    Icon(Icons.Outlined.Send, contentDescription = "Share")
                }
                Spacer(modifier = Modifier.weight(1f))
                IconButton(onClick = { viewModel.toggleSavePost(post.id) }) {
                    Icon(
                        imageVector = if (post.isSavedByMe) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                        contentDescription = "Save Post",
                        tint = if (post.isSavedByMe) SleekCyan else MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // Likes Count & Caption
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
            ) {
                Text(
                    text = "${post.likesCount} likes",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                
                // Caption with clickable hashtags
                Row(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = author.displayName + " ",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = post.caption,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                
                // Comment quick view
                Text(
                    text = "View all comments",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.clickable { onOpenComments() }
                )
            }
        }
    }
}

@Composable
fun AddPostDialog(
    onDismiss: () -> Unit,
    onSubmit: (caption: String, tag: String, location: String?) -> Unit
) {
    var caption by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var projectTag by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Publish Project Milestone", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Share code updates, designs, and architectural designs with other Prisimers.", fontSize = 12.sp, color = MaterialTheme.colorScheme.tertiary)
                OutlinedTextField(
                    value = caption,
                    onValueChange = { caption = it },
                    label = { Text("Milestone Caption") },
                    placeholder = { Text("E.g. Just shipped my Jetpack Compose layout!...") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("Location (Optional)") },
                    placeholder = { Text("E.g. San Francisco Office") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = projectTag,
                    onValueChange = { projectTag = it },
                    label = { Text("Collaboration Tags") },
                    placeholder = { Text("E.g. #Kotlin #Material3") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (caption.isNotEmpty()) {
                        onSubmit(caption + " " + projectTag, projectTag, location.ifEmpty { null })
                    }
                },
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Post Milestone")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun AddMomentDialog(
    onDismiss: () -> Unit,
    onSubmit: (content: String) -> Unit
) {
    var content by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Post a Moment (Story)", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("A Moment expires in 24 hours and is tracked clearly among your collaborators.", fontSize = 12.sp, color = MaterialTheme.colorScheme.tertiary)
                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("What is happening now?") },
                    placeholder = { Text("E.g., Reviewing PR for the login sheet...") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (content.isNotEmpty()) {
                        onSubmit(content)
                    }
                },
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Share Moment")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun CommentsDialog(
    postId: String,
    viewModel: PrisimViewModel,
    onDismiss: () -> Unit
) {
    val commentsFlow = remember(postId) { viewModel.getCommentsForPost(postId) }
    val comments by commentsFlow.collectAsStateWithLifecycle(initialValue = emptyList())
    val allUsers by viewModel.allUsers.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    var myCommentText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Discussion Board", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp)
            ) {
                if (comments.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No collaborative feedback yet. Share a review!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.tertiary
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(comments) { comment ->
                            val user = allUsers.find { it.uid == comment.userId } ?: currentUser
                            if (user != null) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    MediaRenderer(user.profilePicture, modifier = Modifier.size(32.dp), isAvatar = true)
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = user.displayName,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = comment.content,
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = myCommentText,
                        onValueChange = { myCommentText = it },
                        placeholder = { Text("Write constructive comment...") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = {
                            if (myCommentText.isNotEmpty()) {
                                viewModel.addComment(postId, myCommentText)
                                myCommentText = ""
                            }
                        },
                        colors = IconButtonDefaults.iconButtonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Default.Send, contentDescription = "Add comment", tint = MaterialTheme.colorScheme.onPrimary)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

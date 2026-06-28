package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.SleekCyan
import com.example.ui.theme.TechPurple
import com.example.ui.viewmodel.PrisimViewModel
import com.example.ui.widgets.MediaRenderer

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Fetch our globally configured central repository
        val app = application as PrisimApplication
        val repository = app.repository

        setContent {
            // Retrieve viewmodel reactively tied to application life cycle
            val viewModel: PrisimViewModel by viewModels {
                PrisimViewModel.provideFactory(app, repository)
            }

            val isDark by viewModel.isDarkMode.collectAsStateWithLifecycle()

            MyApplicationTheme(darkTheme = isDark) {
                val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    if (currentUser == null) {
                        // User needs authentication
                        AuthScreen(viewModel = viewModel)
                    } else {
                        // Main collaborative platform scaffold
                        MainScaffold(viewModel = viewModel)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScaffold(viewModel: PrisimViewModel) {
    val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val notifications by viewModel.notifications.collectAsStateWithLifecycle()

    var showNotificationsDialog by remember { mutableStateOf(false) }
    var inAdminDashboard by remember { mutableStateOf(false) }

    val unreadNotificationsCount = remember(notifications) {
        notifications.count { !it.isRead }
    }

    if (inAdminDashboard) {
        AdminDashboardScreen(
            viewModel = viewModel,
            onBack = { inAdminDashboard = false }
        )
    } else {
        Scaffold(
            topBar = {
                // Top App Bar visible on standard social screens
                TopAppBar(
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { viewModel.selectTab(0) }
                        ) {
                            PrisimLogo(modifier = Modifier.size(28.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Prisim",
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = (-0.5).sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    },
                    actions = {
                        // Notifications quick-access icon with badge
                        Box(
                            modifier = Modifier
                                .padding(end = 6.dp)
                                .clickable { showNotificationsDialog = true }
                                .padding(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Notifications,
                                contentDescription = "Alert notifications",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                            if (unreadNotificationsCount > 0) {
                                Box(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .clip(CircleShape)
                                        .background(Color.Red)
                                        .align(Alignment.TopEnd),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "$unreadNotificationsCount",
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        // Collaborative Messaging Shortcut
                        IconButton(
                            onClick = { viewModel.selectTab(1) } // Inbox/Chat tab
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.ChatBubbleOutline,
                                contentDescription = "Messaging",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    )
                )
            },
            bottomBar = {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
                ) {
                    NavigationBarItem(
                        selected = selectedTab == 0,
                        onClick = { viewModel.selectTab(0) },
                        icon = { Icon(if (selectedTab == 0) Icons.Default.Home else Icons.Outlined.Home, contentDescription = "Feed") },
                        label = { Text("Feed") }
                    )
                    NavigationBarItem(
                        selected = selectedTab == 1,
                        onClick = { viewModel.selectTab(1) },
                        icon = { Icon(if (selectedTab == 1) Icons.Default.ChatBubble else Icons.Outlined.ChatBubbleOutline, contentDescription = "Collaboration Chat") },
                        label = { Text("Collab") }
                    )
                    NavigationBarItem(
                        selected = selectedTab == 2,
                        onClick = { viewModel.selectTab(2) },
                        icon = { Icon(if (selectedTab == 2) Icons.Default.PlayArrow else Icons.Outlined.PlayArrow, contentDescription = "Prisims Reels") },
                        label = { Text("Prisims") }
                    )
                    NavigationBarItem(
                        selected = selectedTab == 3,
                        onClick = { viewModel.selectTab(3) },
                        icon = { Icon(Icons.Default.Search, contentDescription = "Search Discover") },
                        label = { Text("Search") }
                    )
                    NavigationBarItem(
                        selected = selectedTab == 4,
                        onClick = { viewModel.selectTab(4) },
                        icon = { Icon(if (selectedTab == 4) Icons.Default.Person else Icons.Outlined.Person, contentDescription = "Profile Page") },
                        label = { Text("Profile") }
                    )
                }
            },
            modifier = Modifier.testTag("main_scaffold_root")
        ) { innerPadding ->
            // Crossfade tab switches for seamless visuals
            Crossfade(targetState = selectedTab, label = "TabCrossfade") { targetTab ->
                when (targetTab) {
                    0 -> FeedScreen(viewModel = viewModel, innerPadding = innerPadding)
                    1 -> MessageScreen(viewModel = viewModel, innerPadding = innerPadding)
                    2 -> PrisimsScreen(viewModel = viewModel, innerPadding = innerPadding)
                    3 -> SearchScreen(viewModel = viewModel, innerPadding = innerPadding)
                    4 -> ProfileScreen(
                        viewModel = viewModel,
                        innerPadding = innerPadding,
                        onNavigateToAdmin = { inAdminDashboard = true }
                    )
                }
            }
        }

        // --- Moments story viewer overlay ---
        MomentsViewer(viewModel = viewModel)

        // --- Notifications Dialog Overlay ---
        if (showNotificationsDialog) {
            NotificationsOverlay(
                notifications = notifications,
                onDismiss = {
                    showNotificationsDialog = false
                    currentUser?.uid?.let { viewModel.banUser("read_notifications") /* simulate or mark read */ }
                }
            )
        }
    }
}

@Composable
fun NotificationsOverlay(
    notifications: List<com.example.data.model.Notification>,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Collaborative Feed", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 350.dp)
            ) {
                if (notifications.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No notifications yet.", color = MaterialTheme.colorScheme.tertiary)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(notifications) { notification ->
                            val iconColor = when (notification.type) {
                                "LIKE" -> Color.Red
                                "COMMENT" -> SleekCyan
                                "FOLLOW" -> TechPurple
                                else -> MaterialTheme.colorScheme.primary
                            }
                            val icon = when (notification.type) {
                                "LIKE" -> Icons.Default.Favorite
                                "COMMENT" -> Icons.Default.ChatBubble
                                "FOLLOW" -> Icons.Default.PersonAdd
                                else -> Icons.Default.Notifications
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(iconColor.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = null,
                                        tint = iconColor,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = notification.content,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss, shape = RoundedCornerShape(12.dp)) {
                Text("Dismiss All")
            }
        }
    )
}

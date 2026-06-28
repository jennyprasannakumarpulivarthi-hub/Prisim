package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.Post
import com.example.data.model.User
import com.example.ui.theme.SleekCyan
import com.example.ui.theme.TechPurple
import com.example.ui.widgets.MediaRenderer
import com.example.ui.viewmodel.PrisimViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: PrisimViewModel,
    innerPadding: PaddingValues,
    onNavigateToAdmin: () -> Unit
) {
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val posts by viewModel.posts.collectAsStateWithLifecycle()

    var showEditProfileDialog by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    var activeTab by remember { mutableStateOf(0) } // 0: Grid Posts, 1: Saved Posts, 2: Tagged

    if (currentUser == null) return

    val myPosts = remember(posts, currentUser) {
        posts.filter { it.userId == currentUser!!.uid }
    }

    val savedPosts = remember(posts) {
        posts.filter { it.isSavedByMe }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .testTag("profile_screen_layout")
    ) {
        // Upper Profile Header Info
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Profile avatar with gradient ring
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(SleekCyan)
                        .padding(3.dp),
                    contentAlignment = Alignment.Center
                ) {
                    MediaRenderer(currentUser!!.profilePicture, modifier = Modifier.size(74.dp), isAvatar = true)
                }

                Spacer(modifier = Modifier.width(20.dp))

                // Stats row
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    ProfileStatColumn(count = myPosts.size, label = "Milestones")
                    ProfileStatColumn(count = currentUser!!.prisimersCount, label = "Prisimers")
                    ProfileStatColumn(count = currentUser!!.prisimingCount, label = "Prisiming")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Text Info
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = currentUser!!.displayName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (currentUser!!.isVerified) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Verified Lead",
                        tint = SleekCyan,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            Text(
                text = "@${currentUser!!.username}",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.tertiary
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = currentUser!!.bio,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f)
            )
            if (currentUser!!.website.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Link, contentDescription = null, modifier = Modifier.size(14.dp), tint = SleekCyan)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = currentUser!!.website,
                        color = SleekCyan,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable { /* Simulate browse */ }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Primary buttons (Edit Profile, Settings)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { showEditProfileDialog = true },
                    modifier = Modifier.weight(1f).testTag("edit_profile_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Text("Edit Profile", color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
                }
                IconButton(
                    onClick = { showSettingsDialog = true },
                    modifier = Modifier.testTag("settings_button"),
                    colors = IconButtonDefaults.iconButtonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Icon(Icons.Default.Settings, contentDescription = "Settings", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        // Horizontal Profile tabs
        TabRow(
            selectedTabIndex = activeTab,
            containerColor = Color.Transparent,
            divider = {}
        ) {
            Tab(
                selected = activeTab == 0,
                onClick = { activeTab = 0 },
                icon = { Icon(Icons.Default.GridOn, contentDescription = "Posts") }
            )
            Tab(
                selected = activeTab == 1,
                onClick = { activeTab = 1 },
                icon = { Icon(Icons.Default.BookmarkBorder, contentDescription = "Saved") }
            )
            Tab(
                selected = activeTab == 2,
                onClick = { activeTab = 2 },
                icon = { Icon(Icons.Default.Label, contentDescription = "Tagged") }
            )
        }

        // Tab Content Grid
        Box(modifier = Modifier.weight(1f)) {
            when (activeTab) {
                0 -> ProfilePostsGrid(posts = myPosts)
                1 -> ProfilePostsGrid(posts = savedPosts)
                2 -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No collaborative tags referencing you yet.", color = MaterialTheme.colorScheme.tertiary)
                }
            }
        }
    }

    // --- Edit Profile overlay ---
    if (showEditProfileDialog) {
        EditProfileOverlay(
            user = currentUser!!,
            onDismiss = { showEditProfileDialog = false },
            onSubmit = { dName, bio, web, gen, bday ->
                viewModel.updateProfile(dName, bio, web, gen, bday)
                showEditProfileDialog = false
            }
        )
    }

    // --- Settings Overlay ---
    if (showSettingsDialog) {
        SettingsDialog(
            viewModel = viewModel,
            user = currentUser!!,
            onDismiss = { showSettingsDialog = false },
            onNavigateToAdmin = {
                showSettingsDialog = false
                onNavigateToAdmin()
            }
        )
    }
}

@Composable
fun ProfileStatColumn(count: Int, label: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "$count",
            fontSize = 18.sp,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.tertiary
        )
    }
}

@Composable
fun ProfilePostsGrid(posts: List<Post>) {
    if (posts.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("No milestones posted yet.", color = MaterialTheme.colorScheme.tertiary)
        }
    } else {
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(1.dp),
            horizontalArrangement = Arrangement.spacedBy(1.dp),
            verticalArrangement = Arrangement.spacedBy(1.dp)
        ) {
            items(posts, key = { it.id }) { post ->
                val primaryImage = post.imageUrls.split(",").first()
                Box(
                    modifier = Modifier
                        .aspectRatio(1f)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    MediaRenderer(primaryImage, modifier = Modifier.fillMaxSize())
                }
            }
        }
    }
}

@Composable
fun EditProfileOverlay(
    user: User,
    onDismiss: () -> Unit,
    onSubmit: (displayName: String, bio: String, website: String, gender: String, birthday: String) -> Unit
) {
    var displayName by remember { mutableStateOf(user.displayName) }
    var bio by remember { mutableStateOf(user.bio) }
    var website by remember { mutableStateOf(user.website) }
    var gender by remember { mutableStateOf(user.gender) }
    var birthday by remember { mutableStateOf(user.birthday) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Collaborator Profile", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = displayName,
                    onValueChange = { displayName = it },
                    label = { Text("Display Name") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = bio,
                    onValueChange = { bio = it },
                    label = { Text("Bio / Specialty") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = website,
                    onValueChange = { website = it },
                    label = { Text("Portfolio Website") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = gender,
                    onValueChange = { gender = it },
                    label = { Text("Gender (Optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = birthday,
                    onValueChange = { birthday = it },
                    label = { Text("Birthday (Optional)") },
                    placeholder = { Text("YYYY-MM-DD") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSubmit(displayName, bio, website, gender, birthday)
                },
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Save Profile")
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
fun SettingsDialog(
    viewModel: PrisimViewModel,
    user: User,
    onDismiss: () -> Unit,
    onNavigateToAdmin: () -> Unit
) {
    val isDark by viewModel.isDarkMode.collectAsStateWithLifecycle()
    val isNotifEnabled by viewModel.isNotificationsEnabled.collectAsStateWithLifecycle()
    val language by viewModel.selectedLanguage.collectAsStateWithLifecycle()

    var showLanguageMenu by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Settings & Privacy", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Dark Mode Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.DarkMode, contentDescription = null, tint = SleekCyan)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Dark Mode Theme")
                    }
                    Switch(
                        checked = isDark,
                        onCheckedChange = { viewModel.toggleDarkMode() }
                    )
                }

                // Notifications Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Notifications, contentDescription = null, tint = SleekCyan)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Push Notifications")
                    }
                    Switch(
                        checked = isNotifEnabled,
                        onCheckedChange = { viewModel.toggleNotifications() }
                    )
                }

                // Language Selection Selector
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showLanguageMenu = true },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Language, contentDescription = null, tint = SleekCyan)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Selected Language")
                    }
                    Text(
                        text = language,
                        fontWeight = FontWeight.Bold,
                        color = SleekCyan,
                        fontSize = 14.sp
                    )
                }

                // Muted/Blocked lists visualization
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Block, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Blocked & Muted Users (0)", color = MaterialTheme.colorScheme.tertiary, fontSize = 14.sp)
                }

                // Admin Dashboard trigger (visible to verified/owner profiles)
                if (user.isVerified) {
                    Divider()
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigateToAdmin() },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = TechPurple)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Admin Moderation Dashboard", fontWeight = FontWeight.Bold, color = TechPurple)
                        }
                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TechPurple)
                    }
                }

                Divider()

                // Delete account & Logout buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            viewModel.logout()
                            onDismiss()
                        }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.ExitToApp, contentDescription = null, tint = Color.Red)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Log Out", color = Color.Red, fontWeight = FontWeight.Bold)
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            viewModel.deleteAccount()
                            onDismiss()
                        }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.DeleteForever, contentDescription = null, tint = Color.Red)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Delete Professional Account", color = Color.Red, fontSize = 13.sp)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close Settings")
            }
        }
    )

    // Language dropdown selection
    if (showLanguageMenu) {
        AlertDialog(
            onDismissRequest = { showLanguageMenu = false },
            title = { Text("Select Language") },
            text = {
                Column {
                    listOf("English", "Español", "Français", "Deutsch").forEach { lang ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.setLanguage(lang)
                                    showLanguageMenu = false
                                }
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(lang, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            },
            confirmButton = {}
        )
    }
}

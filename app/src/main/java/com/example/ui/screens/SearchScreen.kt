package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import com.example.data.model.User
import com.example.ui.theme.SleekCyan
import com.example.ui.widgets.MediaRenderer
import com.example.ui.viewmodel.PrisimViewModel

@Composable
fun SearchScreen(
    viewModel: PrisimViewModel,
    innerPadding: PaddingValues
) {
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val recentSearches by viewModel.recentSearches.collectAsStateWithLifecycle()
    val allUsers by viewModel.allUsers.collectAsStateWithLifecycle()
    val posts by viewModel.posts.collectAsStateWithLifecycle()

    val filteredUsers = remember(searchQuery, allUsers) {
        if (searchQuery.isEmpty()) emptyList()
        else allUsers.filter {
            it.displayName.contains(searchQuery, ignoreCase = true) ||
            it.username.contains(searchQuery, ignoreCase = true)
        }
    }

    val filteredPosts = remember(searchQuery, posts) {
        if (searchQuery.isEmpty()) emptyList()
        else posts.filter {
            it.caption.contains(searchQuery, ignoreCase = true) ||
            (it.location?.contains(searchQuery, ignoreCase = true) ?: false)
        }
    }

    val trendingTopics = listOf(
        "DesignSystems", "ComposeAnimations", "KotlinMultiplatform",
        "Material3", "OfflineFirst", "CleanArchitecture"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .padding(horizontal = 16.dp)
    ) {
        // Search Input Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.updateSearchQuery(it) },
            placeholder = { Text("Search collaborators or project tags...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = SleekCyan) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear text")
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
                .testTag("global_search_input"),
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = SleekCyan,
                unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant
            )
        )

        if (searchQuery.isEmpty()) {
            // Default discover layout
            Text(
                text = "Recent Searches",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(vertical = 6.dp)
            )

            // Horizontal row of chips
            if (recentSearches.isEmpty()) {
                Text(
                    text = "No recent search logs.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            } else {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    items(recentSearches) { log ->
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .clickable {
                                    viewModel.updateSearchQuery(log)
                                    viewModel.addRecentSearch(log)
                                }
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                Icons.Default.History,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = log,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Delete",
                                modifier = Modifier
                                    .size(14.dp)
                                    .clickable { viewModel.removeRecentSearch(log) },
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                        }
                    }
                }
            }

            // Trending tech keywords
            Text(
                text = "Trending Collaborative Topics",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(trendingTopics) { topic ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                viewModel.updateSearchQuery(topic)
                                viewModel.addRecentSearch(topic)
                            },
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(SleekCyan.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.TrendingUp,
                                    contentDescription = "Trending",
                                    tint = SleekCyan,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Text(
                                    text = "#$topic",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Active discussion on milestones",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.tertiary
                                )
                            }
                        }
                    }
                }
            }
        } else {
            // Results lists
            Text(
                text = "Search Results",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                // Users results
                if (filteredUsers.isNotEmpty()) {
                    item {
                        Text(
                            text = "Collaborators (${filteredUsers.size})",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = SleekCyan,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                    items(filteredUsers) { user ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.openChatWith(user) }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            MediaRenderer(user.profilePicture, modifier = Modifier.size(44.dp), isAvatar = true)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = user.displayName,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    if (user.isVerified) {
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = "Verified",
                                            tint = SleekCyan,
                                            modifier = Modifier.size(12.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = "@${user.username}",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.tertiary
                                )
                            }
                        }
                    }
                }

                // Posts/Hashtags results
                if (filteredPosts.isNotEmpty()) {
                    item {
                        Text(
                            text = "Milestones (${filteredPosts.size})",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = SleekCyan,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                    items(filteredPosts) { post ->
                        val postAuthor = allUsers.find { it.uid == post.userId }
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.selectTab(0) }, // Go back to Home Feed tab
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = postAuthor?.displayName ?: "Prisimer",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = post.caption,
                                    fontSize = 12.sp,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }

                if (filteredUsers.isEmpty() && filteredPosts.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(48.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No collaborators or tags match \"$searchQuery\"", color = MaterialTheme.colorScheme.tertiary)
                        }
                    }
                }
            }
        }
    }
}

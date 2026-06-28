package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey val uid: String,
    val username: String,
    val displayName: String,
    val bio: String,
    val profilePicture: String,
    val website: String = "",
    val gender: String = "",
    val birthday: String = "",
    val prisimersCount: Int = 0,
    val prisimingCount: Int = 0,
    val isVerified: Boolean = false,
    val isMuted: Boolean = false,
    val isBlocked: Boolean = false,
    val isMe: Boolean = false
)

@Entity(tableName = "posts")
data class Post(
    @PrimaryKey val id: String,
    val userId: String,
    val caption: String,
    val imageUrls: String, // Comma-separated list of URLs
    val videoUrl: String? = null,
    val location: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val likesCount: Int = 0,
    val isLikedByMe: Boolean = false,
    val isSavedByMe: Boolean = false,
    val isReported: Boolean = false
)

@Entity(tableName = "comments")
data class Comment(
    @PrimaryKey val id: String,
    val postId: String,
    val userId: String,
    val content: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "moments")
data class Moment(
    @PrimaryKey val id: String,
    val userId: String,
    val mediaUrl: String,
    val isVideo: Boolean = false,
    val timestamp: Long = System.currentTimeMillis(),
    val isHighlight: Boolean = false
)

@Entity(tableName = "prisims")
data class Prisim(
    @PrimaryKey val id: String,
    val userId: String,
    val videoUrl: String,
    val musicTitle: String,
    val caption: String,
    val likesCount: Int = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val isLikedByMe: Boolean = false,
    val isSavedByMe: Boolean = false
)

@Entity(tableName = "messages")
data class Message(
    @PrimaryKey val id: String,
    val senderId: String,
    val receiverId: String,
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val mediaUrl: String? = null,
    val mediaType: String? = null, // "PHOTO", "VIDEO", "VOICE"
    val isRead: Boolean = false,
    val reactions: String = "" // Comma-separated reactions e.g. "❤️,👍"
)

@Entity(tableName = "notifications")
data class Notification(
    @PrimaryKey val id: String,
    val userId: String, // Receiver of notification
    val triggerUserId: String, // User who triggered notification
    val type: String, // "LIKE", "COMMENT", "FOLLOW", "MESSAGE", "MENTION", "REPLY"
    val content: String,
    val referenceId: String, // Post ID or Message ID
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)

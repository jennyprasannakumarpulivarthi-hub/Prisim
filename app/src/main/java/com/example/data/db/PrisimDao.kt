package com.example.data.db

import androidx.room.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface PrisimDao {
    // --- Users ---
    @Query("SELECT * FROM users")
    fun getAllUsers(): Flow<List<User>>

    @Query("SELECT * FROM users WHERE uid = :uid LIMIT 1")
    fun getUserById(uid: String): Flow<User?>

    @Query("SELECT * FROM users WHERE uid = :uid LIMIT 1")
    suspend fun getUserByIdSync(uid: String): User?

    @Query("SELECT * FROM users WHERE isMe = 1 LIMIT 1")
    fun getMyProfile(): Flow<User?>

    @Query("SELECT * FROM users WHERE isMe = 1 LIMIT 1")
    suspend fun getMyProfileSync(): User?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsers(users: List<User>)

    @Update
    suspend fun updateUser(user: User)

    // --- Posts ---
    @Query("SELECT * FROM posts WHERE isReported = 0 ORDER BY timestamp DESC")
    fun getAllPosts(): Flow<List<Post>>

    @Query("SELECT * FROM posts WHERE userId = :userId ORDER BY timestamp DESC")
    fun getPostsByUserId(userId: String): Flow<List<Post>>

    @Query("SELECT * FROM posts WHERE isSavedByMe = 1 ORDER BY timestamp DESC")
    fun getSavedPosts(): Flow<List<Post>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPost(post: Post)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPosts(posts: List<Post>)

    @Update
    suspend fun updatePost(post: Post)

    @Delete
    suspend fun deletePost(post: Post)

    @Query("DELETE FROM posts WHERE id = :id")
    suspend fun deletePostById(id: String)

    // --- Comments ---
    @Query("SELECT * FROM comments WHERE postId = :postId ORDER BY timestamp ASC")
    fun getCommentsByPostId(postId: String): Flow<List<Comment>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComment(comment: Comment)

    @Delete
    suspend fun deleteComment(comment: Comment)

    // --- Moments ---
    @Query("SELECT * FROM moments ORDER BY timestamp DESC")
    fun getAllMoments(): Flow<List<Moment>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMoment(moment: Moment)

    // --- Prisims ---
    @Query("SELECT * FROM prisims ORDER BY timestamp DESC")
    fun getAllPrisims(): Flow<List<Prisim>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPrisim(prisim: Prisim)

    @Update
    suspend fun updatePrisim(prisim: Prisim)

    // --- Messages ---
    @Query("SELECT * FROM messages WHERE (senderId = :user1 AND receiverId = :user2) OR (senderId = :user2 AND receiverId = :user1) ORDER BY timestamp ASC")
    fun getMessagesBetween(user1: String, user2: String): Flow<List<Message>>

    @Query("SELECT * FROM messages ORDER BY timestamp DESC")
    fun getAllMessages(): Flow<List<Message>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: Message)

    @Query("DELETE FROM messages WHERE id = :id")
    suspend fun deleteMessageById(id: String)

    @Update
    suspend fun updateMessage(message: Message)

    // --- Notifications ---
    @Query("SELECT * FROM notifications ORDER BY timestamp DESC")
    fun getAllNotifications(): Flow<List<Notification>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: Notification)

    @Query("UPDATE notifications SET isRead = 1 WHERE userId = :userId")
    suspend fun markNotificationsAsRead(userId: String)
}

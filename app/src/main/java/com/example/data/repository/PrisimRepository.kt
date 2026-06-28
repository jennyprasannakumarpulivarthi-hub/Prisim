package com.example.data.repository

import android.content.Context
import com.example.data.db.PrisimDao
import com.example.data.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

class PrisimRepository(
    private val dao: PrisimDao,
    private val context: Context
) {
    private val repositoryScope = CoroutineScope(Dispatchers.IO)

    // Current logged-in user session state
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    // Exposed Flows from DAO
    val allUsers: Flow<List<User>> = dao.getAllUsers()
    val allPosts: Flow<List<Post>> = dao.getAllPosts()
    val allMoments: Flow<List<Moment>> = dao.getAllMoments()
    val allPrisims: Flow<List<Prisim>> = dao.getAllPrisims()
    val allMessages: Flow<List<Message>> = dao.getAllMessages()
    val allNotifications: Flow<List<Notification>> = dao.getAllNotifications()

    init {
        // Load existing "me" user if logged in, otherwise seed database
        repositoryScope.launch {
            val myProfile = dao.getMyProfileSync()
            if (myProfile != null) {
                _currentUser.value = myProfile
            } else {
                seedInitialData()
            }
        }
    }

    // --- Authentication ---
    suspend fun loginWithEmail(email: String, username: String): Boolean = withContext(Dispatchers.IO) {
        val uid = UUID.randomUUID().toString()
        val formattedUsername = username.lowercase().replace(" ", "_")
        val newUser = User(
            uid = uid,
            username = formattedUsername,
            displayName = username,
            bio = "Product Innovator & Collaborator | Building on Prisim.",
            profilePicture = "avatar_placeholder",
            website = "https://prisim.collab",
            prisimersCount = 142,
            prisimingCount = 89,
            isVerified = true,
            isMe = true
        )
        dao.insertUser(newUser)
        _currentUser.value = newUser
        
        // Add welcome notification
        addSystemNotification(
            userId = uid,
            type = "MESSAGE",
            content = "Welcome to Prisim! Let's collaborate and share moments with other Prisimers.",
            refId = uid
        )
        true
    }

    suspend fun loginWithGoogle(displayName: String, email: String): Boolean = withContext(Dispatchers.IO) {
        val uid = UUID.randomUUID().toString()
        val username = displayName.lowercase().replace(" ", "_")
        val newUser = User(
            uid = uid,
            username = username,
            displayName = displayName,
            bio = "Creative Developer | Google collaborator.",
            profilePicture = "avatar_placeholder",
            website = "https://github.com/$username",
            prisimersCount = 320,
            prisimingCount = 112,
            isVerified = true,
            isMe = true
        )
        dao.insertUser(newUser)
        _currentUser.value = newUser
        
        addSystemNotification(
            userId = uid,
            type = "FOLLOW",
            content = "Welcome via Google! Your profile has been verified automatically.",
            refId = uid
        )
        true
    }

    suspend fun loginWithPhone(phoneNumber: String): Boolean = withContext(Dispatchers.IO) {
        val uid = UUID.randomUUID().toString()
        val newUser = User(
            uid = uid,
            username = "user_${phoneNumber.takeLast(4)}",
            displayName = "Prisimer ${phoneNumber.takeLast(4)}",
            bio = "Mobile collaborator | Verified via Phone.",
            profilePicture = "avatar_placeholder",
            website = "",
            prisimersCount = 12,
            prisimingCount = 4,
            isVerified = false,
            isMe = true
        )
        dao.insertUser(newUser)
        _currentUser.value = newUser
        true
    }

    suspend fun logout() = withContext(Dispatchers.IO) {
        val me = dao.getMyProfileSync()
        if (me != null) {
            // Delete current user from DB to simulate session end
            dao.deletePostById("me") // Clear temp items or just set isMe to false
            // We can just delete our user profile or remove isMe flag
            dao.updateUser(me.copy(isMe = false))
        }
        _currentUser.value = null
    }

    suspend fun deleteAccount() = withContext(Dispatchers.IO) {
        val me = dao.getMyProfileSync()
        if (me != null) {
            dao.deletePostById("me")
            // Remove from user table
            // Delete all data or just clear currentUser flow
        }
        _currentUser.value = null
    }

    // --- Profiles ---
    fun getUserFlow(userId: String): Flow<User?> {
        return dao.getUserById(userId)
    }

    suspend fun updateProfile(
        displayName: String,
        bio: String,
        website: String,
        gender: String,
        birthday: String
    ) = withContext(Dispatchers.IO) {
        val me = dao.getMyProfileSync() ?: return@withContext
        val updated = me.copy(
            displayName = displayName,
            bio = bio,
            website = website,
            gender = gender,
            birthday = birthday
        )
        dao.updateUser(updated)
        _currentUser.value = updated
    }

    suspend fun toggleFollow(userId: String) = withContext(Dispatchers.IO) {
        val target = dao.getUserByIdSync(userId) ?: return@withContext
        val me = dao.getMyProfileSync() ?: return@withContext

        val isNowFollowing = target.isMuted // Use a flag or logic, let's toggle a state or simulate count
        val newFollowersCount = if (isNowFollowing) target.prisimersCount - 1 else target.prisimersCount + 1
        val updatedTarget = target.copy(
            prisimersCount = newFollowersCount.coerceAtLeast(0),
            isMuted = !isNowFollowing // Just a simulated toggle for demo follow state
        )
        dao.updateUser(updatedTarget)

        val updatedMe = me.copy(
            prisimingCount = if (isNowFollowing) me.prisimingCount - 1 else me.prisimingCount + 1
        )
        dao.updateUser(updatedMe)
        _currentUser.value = updatedMe

        if (!isNowFollowing) {
            // Add notification
            addSystemNotification(
                userId = userId,
                type = "FOLLOW",
                content = "${me.displayName} started following (Prisiming) you!",
                refId = me.uid
            )
        }
    }

    // --- Posts ---
    suspend fun createPost(caption: String, imageUrls: List<String>, location: String? = null) = withContext(Dispatchers.IO) {
        val me = _currentUser.value ?: return@withContext
        val newPost = Post(
            id = UUID.randomUUID().toString(),
            userId = me.uid,
            caption = caption,
            imageUrls = imageUrls.joinToString(","),
            location = location,
            timestamp = System.currentTimeMillis()
        )
        dao.insertPost(newPost)
    }

    suspend fun toggleLikePost(postId: String) = withContext(Dispatchers.IO) {
        // Retrieve post
        allPosts.first().find { it.id == postId }?.let { post ->
            val me = _currentUser.value ?: return@withContext
            val isLiked = post.isLikedByMe
            val newLikes = if (isLiked) post.likesCount - 1 else post.likesCount + 1
            val updated = post.copy(
                isLikedByMe = !isLiked,
                likesCount = newLikes.coerceAtLeast(0)
            )
            dao.updatePost(updated)

            if (!isLiked && post.userId != me.uid) {
                addSystemNotification(
                    userId = post.userId,
                    type = "LIKE",
                    content = "${me.displayName} liked your post.",
                    refId = postId
                )
            }
        }
    }

    suspend fun toggleSavePost(postId: String) = withContext(Dispatchers.IO) {
        allPosts.first().find { it.id == postId }?.let { post ->
            val updated = post.copy(isSavedByMe = !post.isSavedByMe)
            dao.updatePost(updated)
        }
    }

    suspend fun deletePost(postId: String) = withContext(Dispatchers.IO) {
        dao.deletePostById(postId)
    }

    suspend fun reportPost(postId: String) = withContext(Dispatchers.IO) {
        allPosts.first().find { it.id == postId }?.let { post ->
            dao.updatePost(post.copy(isReported = true))
        }
    }

    // --- Comments ---
    fun getCommentsForPost(postId: String): Flow<List<Comment>> {
        return dao.getCommentsByPostId(postId)
    }

    suspend fun addComment(postId: String, content: String) = withContext(Dispatchers.IO) {
        val me = _currentUser.value ?: return@withContext
        val newComment = Comment(
            id = UUID.randomUUID().toString(),
            postId = postId,
            userId = me.uid,
            content = content,
            timestamp = System.currentTimeMillis()
        )
        dao.insertComment(newComment)

        // Find post to notify owner
        allPosts.first().find { it.id == postId }?.let { post ->
            if (post.userId != me.uid) {
                addSystemNotification(
                    userId = post.userId,
                    type = "COMMENT",
                    content = "${me.displayName} commented: \"$content\"",
                    refId = postId
                )
            }
        }
    }

    // --- Moments ---
    suspend fun createMoment(mediaUrl: String, isVideo: Boolean) = withContext(Dispatchers.IO) {
        val me = _currentUser.value ?: return@withContext
        val newMoment = Moment(
            id = UUID.randomUUID().toString(),
            userId = me.uid,
            mediaUrl = mediaUrl,
            isVideo = isVideo,
            timestamp = System.currentTimeMillis()
        )
        dao.insertMoment(newMoment)
    }

    // --- Prisims ---
    suspend fun createPrisim(videoUrl: String, caption: String, musicTitle: String) = withContext(Dispatchers.IO) {
        val me = _currentUser.value ?: return@withContext
        val newPrisim = Prisim(
            id = UUID.randomUUID().toString(),
            userId = me.uid,
            videoUrl = videoUrl,
            musicTitle = musicTitle,
            caption = caption,
            likesCount = 0,
            timestamp = System.currentTimeMillis()
        )
        dao.insertPrisim(newPrisim)
    }

    suspend fun toggleLikePrisim(prisimId: String) = withContext(Dispatchers.IO) {
        allPrisims.first().find { it.id == prisimId }?.let { prisim ->
            val me = _currentUser.value ?: return@withContext
            val isLiked = prisim.isLikedByMe
            val updated = prisim.copy(
                isLikedByMe = !isLiked,
                likesCount = if (isLiked) prisim.likesCount - 1 else prisim.likesCount + 1
            )
            dao.updatePrisim(updated)

            if (!isLiked && prisim.userId != me.uid) {
                addSystemNotification(
                    userId = prisim.userId,
                    type = "LIKE",
                    content = "${me.displayName} liked your Prisim video.",
                    refId = prisimId
                )
            }
        }
    }

    // --- Messages / Collaboration ---
    fun getMessagesWith(otherUserId: String): Flow<List<Message>> {
        val me = _currentUser.value?.uid ?: ""
        return dao.getMessagesBetween(me, otherUserId)
    }

    suspend fun sendMessage(
        receiverId: String,
        content: String,
        mediaUrl: String? = null,
        mediaType: String? = null
    ) = withContext(Dispatchers.IO) {
        val me = _currentUser.value ?: return@withContext
        val newMessage = Message(
            id = UUID.randomUUID().toString(),
            senderId = me.uid,
            receiverId = receiverId,
            content = content,
            timestamp = System.currentTimeMillis(),
            mediaUrl = mediaUrl,
            mediaType = mediaType,
            isRead = false
        )
        dao.insertMessage(newMessage)

        // Add a notification
        addSystemNotification(
            userId = receiverId,
            type = "MESSAGE",
            content = "New collaborative message from ${me.displayName}: \"${content.take(30)}\"",
            refId = me.uid
        )

        // Simulation: Send automatic smart collaborative response from seed accounts!
        simulateResponseIfNeeded(receiverId, content)
    }

    suspend fun deleteMessage(messageId: String) = withContext(Dispatchers.IO) {
        dao.deleteMessageById(messageId)
    }

    suspend fun addMessageReaction(messageId: String, reaction: String) = withContext(Dispatchers.IO) {
        // Simple update
        dao.getAllMessages().first().find { it.id == messageId }?.let { message ->
            dao.updateMessage(message.copy(reactions = reaction))
        }
    }

    private suspend fun simulateResponseIfNeeded(receiverId: String, userMessage: String) {
        // If user is messaging a seeded account, trigger a smart collaboration response!
        val replier = dao.getUserByIdSync(receiverId) ?: return
        if (replier.isMe) return

        val replyContent = when {
            userMessage.lowercase().contains("hello") || userMessage.lowercase().contains("hey") -> {
                "Hi there! Glad to connect on Prisim. Let's collaborate on this amazing design system project!"
            }
            userMessage.lowercase().contains("project") || userMessage.lowercase().contains("milestone") || userMessage.lowercase().contains("collab") -> {
                "I just reviewed the project board on my Moments! The latest Figma mockup is beautiful. Let me know when you want to deploy the next sprint."
            }
            userMessage.lowercase().contains("code") || userMessage.lowercase().contains("issue") || userMessage.lowercase().contains("bug") -> {
                "I see it! Let's schedule a deep dive. I'll push my updates directly and show the solution in my next Prisim highlight video."
            }
            else -> {
                "That looks perfect! Let me sync with the engineering team and review the requirements. Prisim is scaling great!"
            }
        }

        // Simulate short typing delay
        kotlinx.coroutines.delay(1500)

        val simMessage = Message(
            id = UUID.randomUUID().toString(),
            senderId = receiverId,
            receiverId = _currentUser.value?.uid ?: "",
            content = replyContent,
            timestamp = System.currentTimeMillis()
        )
        dao.insertMessage(simMessage)
    }

    // --- Admin Dashboard / Moderation ---
    suspend fun banUser(userId: String) = withContext(Dispatchers.IO) {
        dao.getUserByIdSync(userId)?.let { user ->
            dao.updateUser(user.copy(isBlocked = true))
        }
    }

    // --- Helpers ---
    private suspend fun addSystemNotification(
        userId: String,
        type: String,
        content: String,
        refId: String
    ) {
        val notification = Notification(
            id = UUID.randomUUID().toString(),
            userId = userId,
            triggerUserId = "system",
            type = type,
            content = content,
            referenceId = refId,
            timestamp = System.currentTimeMillis()
        )
        dao.insertNotification(notification)
    }

    // --- Database Seeding ---
    private suspend fun seedInitialData() {
        val defaultMe = User(
            uid = "me",
            username = "privateuserx533",
            displayName = "Sarah Jenkins",
            bio = "Product Designer & Innovator | Collaborating on next-gen interfaces with Prisimers.",
            profilePicture = "avatar_placeholder",
            website = "https://sarahjenkins.design",
            prisimersCount = 345,
            prisimingCount = 210,
            isVerified = true,
            isMe = true
        )
        dao.insertUser(defaultMe)
        _currentUser.value = defaultMe

        val creator1 = User(
            uid = "alex_innovate",
            username = "alex_innovate",
            displayName = "Alex Rivera",
            bio = "Product Designer & Visionary | Crafting high-fidelity mobile systems. Let's innovate!",
            profilePicture = "avatar_alex",
            website = "https://alexrivera.io",
            prisimersCount = 1240,
            prisimingCount = 450,
            isVerified = true
        )
        val creator2 = User(
            uid = "sophia_codes",
            username = "sophia_codes",
            displayName = "Sophia Chen",
            bio = "Senior Android Engineer | Passionate about Jetpack Compose, architectural design, & performance.",
            profilePicture = "avatar_sophia",
            website = "https://sophiachen.dev",
            prisimersCount = 890,
            prisimingCount = 312,
            isVerified = true
        )
        val creator3 = User(
            uid = "marcus_pm",
            username = "marcus_pm",
            displayName = "Marcus Vance",
            bio = "Lead Project Architect | Scaling tech collaborations. Prisiming for progress.",
            profilePicture = "avatar_marcus",
            website = "",
            prisimersCount = 421,
            prisimingCount = 612,
            isVerified = false
        )
        dao.insertUsers(listOf(creator1, creator2, creator3))

        // Seed some Moments (Stories)
        val moments = listOf(
            Moment(
                id = "m1",
                userId = "alex_innovate",
                mediaUrl = "moment_design_sprint",
                timestamp = System.currentTimeMillis() - 3600000
            ),
            Moment(
                id = "m2",
                userId = "sophia_codes",
                mediaUrl = "moment_code_review",
                timestamp = System.currentTimeMillis() - 7200000
            ),
            Moment(
                id = "m3",
                userId = "marcus_pm",
                mediaUrl = "moment_scrum_board",
                timestamp = System.currentTimeMillis() - 10800000,
                isHighlight = true
            )
        )
        moments.forEach { dao.insertMoment(it) }

        // Seed some Posts
        val posts = listOf(
            Post(
                id = "p1",
                userId = "alex_innovate",
                caption = "Excited to launch the initial design guidelines for our new collaborative system! Built purely using Material 3 guidelines and focused on high-contrast, premium dark accents. Thoughts? #DesignSystem #PrisimCollaboration #UIUX",
                imageUrls = "post_design_1,post_design_2",
                location = "San Francisco, CA",
                timestamp = System.currentTimeMillis() - 14400000,
                likesCount = 142,
                isLikedByMe = true,
                isSavedByMe = false
            ),
            Post(
                id = "p2",
                userId = "sophia_codes",
                caption = "Clean architecture in Jetpack Compose is a lifesaver. Refactored the entire navigation state today to use robust, type-safe routes. Offline-first database updates are completely smooth. #AndroidDev #Kotlin #Compose",
                imageUrls = "post_code_1",
                location = "Seattle, WA",
                timestamp = System.currentTimeMillis() - 28800000,
                likesCount = 98,
                isLikedByMe = false,
                isSavedByMe = true
            )
        )
        dao.insertPosts(posts)

        // Seed some Prisims (Reels)
        val prisims = listOf(
            Prisim(
                id = "pr1",
                userId = "alex_innovate",
                videoUrl = "video_interaction_demo",
                musicTitle = "Lofi Vibes - Study Beats",
                caption = "Smooth prototype transitions in Action! Creating micro-interactions that spark joy ✨.",
                likesCount = 312,
                timestamp = System.currentTimeMillis() - 18000000
            ),
            Prisim(
                id = "pr2",
                userId = "sophia_codes",
                videoUrl = "video_compose_animation",
                musicTitle = "Synthwave Neon - Cyberpunk",
                caption = "Check out this custom canvas particle animation. Fast, lightweight, and fully integrated with Compose dynamic themes! 🚀",
                likesCount = 245,
                timestamp = System.currentTimeMillis() - 36000000
            )
        )
        prisims.forEach { dao.insertPrisim(it) }

        // Seed initial conversations/messages
        val initialMessages = listOf(
            Message(
                id = "msg1",
                senderId = "alex_innovate",
                receiverId = "me",
                content = "Hey Sarah! Have you checked out the latest project specs?",
                timestamp = System.currentTimeMillis() - 40000000
            ),
            Message(
                id = "msg2",
                senderId = "me",
                receiverId = "alex_innovate",
                content = "Yes Alex! The Figma flow is extremely clean. I love how you structured the moments layout.",
                timestamp = System.currentTimeMillis() - 35000000
            ),
            Message(
                id = "msg3",
                senderId = "alex_innovate",
                receiverId = "me",
                content = "Awesome! Let me know if we need any custom illustrations.",
                timestamp = System.currentTimeMillis() - 30000000
            )
        )
        initialMessages.forEach { dao.insertMessage(it) }

        // Seed some notifications
        val initialNotifications = listOf(
            Notification(
                id = "n1",
                userId = "me",
                triggerUserId = "alex_innovate",
                type = "LIKE",
                content = "Alex Rivera liked your post.",
                referenceId = "p1",
                timestamp = System.currentTimeMillis() - 5000000
            ),
            Notification(
                id = "n2",
                userId = "me",
                triggerUserId = "sophia_codes",
                type = "FOLLOW",
                content = "Sophia Chen started following (Prisiming) you.",
                referenceId = "sophia_codes",
                timestamp = System.currentTimeMillis() - 12000000
            ),
            Notification(
                id = "n3",
                userId = "me",
                triggerUserId = "marcus_pm",
                type = "COMMENT",
                content = "Marcus Vance commented on your post: 'Brilliant work!'",
                referenceId = "p1",
                timestamp = System.currentTimeMillis() - 15000000
            )
        )
        initialNotifications.forEach { dao.insertNotification(it) }
    }
}

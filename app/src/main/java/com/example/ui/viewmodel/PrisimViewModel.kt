package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.PrisimApplication
import com.example.data.model.*
import com.example.data.repository.PrisimRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class PrisimViewModel(
    application: Application,
    private val repository: PrisimRepository
) : AndroidViewModel(application) {

    // --- Active Session ---
    val currentUser: StateFlow<User?> = repository.currentUser

    // --- Content flows ---
    val posts: StateFlow<List<Post>> = repository.allPosts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val moments: StateFlow<List<Moment>> = repository.allMoments
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val prisims: StateFlow<List<Prisim>> = repository.allPrisims
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val notifications: StateFlow<List<Notification>> = repository.allNotifications
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allUsers: StateFlow<List<User>> = repository.allUsers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- UI/Navigation States ---
    private val _selectedTab = MutableStateFlow(0) // 0: Feed, 1: Chat, 2: Prisims, 3: Search, 4: Profile
    val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

    private val _activeChatPartner = MutableStateFlow<User?>(null)
    val activeChatPartner: StateFlow<User?> = _activeChatPartner.asStateFlow()

    private val _activeStoryUser = MutableStateFlow<User?>(null) // User whose moments are being viewed
    val activeStoryUser: StateFlow<User?> = _activeStoryUser.asStateFlow()

    // --- Settings States ---
    private val _isDarkMode = MutableStateFlow(true) // Default to sleek tech dark mode!
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    private val _isNotificationsEnabled = MutableStateFlow(true)
    val isNotificationsEnabled: StateFlow<Boolean> = _isNotificationsEnabled.asStateFlow()

    private val _selectedLanguage = MutableStateFlow("English")
    val selectedLanguage: StateFlow<String> = _selectedLanguage.asStateFlow()

    // --- Search & Filtering States ---
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _recentSearches = MutableStateFlow(listOf("design_sprint", "alex_innovate", "kotlin_tips", "composables"))
    val recentSearches: StateFlow<List<String>> = _recentSearches.asStateFlow()

    // --- Active chat messages ---
    val activeChatMessages: StateFlow<List<Message>> = _activeChatPartner
        .flatMapLatest { partner ->
            if (partner == null) flowOf(emptyList())
            else repository.getMessagesWith(partner.uid)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Admin state ---
    val reportedPosts: StateFlow<List<Post>> = repository.allPosts
        .map { list -> list.filter { it.isReported } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Navigation ---
    fun selectTab(index: Int) {
        _selectedTab.value = index
        if (index != 1) {
            _activeChatPartner.value = null // reset active chat when leaving message tab
        }
    }

    fun openChatWith(user: User) {
        _activeChatPartner.value = user
        _selectedTab.value = 1 // Go to Chat/Collaboration tab
    }

    fun closeChat() {
        _activeChatPartner.value = null
    }

    fun openStoryViewer(user: User) {
        _activeStoryUser.value = user
    }

    fun closeStoryViewer() {
        _activeStoryUser.value = null
    }

    // --- Auth actions ---
    fun login(email: String, username: String) {
        viewModelScope.launch {
            repository.loginWithEmail(email, username)
        }
    }

    fun loginGoogle(name: String, email: String) {
        viewModelScope.launch {
            repository.loginWithGoogle(name, email)
        }
    }

    fun loginPhone(phone: String) {
        viewModelScope.launch {
            repository.loginWithPhone(phone)
        }
    }

    fun logout() {
        viewModelScope.launch {
            repository.logout()
            _selectedTab.value = 0
            _activeChatPartner.value = null
        }
    }

    fun deleteAccount() {
        viewModelScope.launch {
            repository.deleteAccount()
            _selectedTab.value = 0
            _activeChatPartner.value = null
        }
    }

    // --- Profiles ---
    fun toggleFollowUser(userId: String) {
        viewModelScope.launch {
            repository.toggleFollow(userId)
        }
    }

    fun updateProfile(displayName: String, bio: String, website: String, gender: String, birthday: String) {
        viewModelScope.launch {
            repository.updateProfile(displayName, bio, website, gender, birthday)
        }
    }

    // --- Post actions ---
    fun createPost(caption: String, imageUrls: List<String>, location: String? = null) {
        viewModelScope.launch {
            repository.createPost(caption, imageUrls, location)
        }
    }

    fun toggleLikePost(postId: String) {
        viewModelScope.launch {
            repository.toggleLikePost(postId)
        }
    }

    fun toggleSavePost(postId: String) {
        viewModelScope.launch {
            repository.toggleSavePost(postId)
        }
    }

    fun deletePost(postId: String) {
        viewModelScope.launch {
            repository.deletePost(postId)
        }
    }

    fun reportPost(postId: String) {
        viewModelScope.launch {
            repository.reportPost(postId)
        }
    }

    fun addComment(postId: String, content: String) {
        viewModelScope.launch {
            repository.addComment(postId, content)
        }
    }

    fun getCommentsForPost(postId: String): Flow<List<Comment>> {
        return repository.getCommentsForPost(postId)
    }

    // --- Moments (Stories) actions ---
    fun createMoment(mediaUrl: String, isVideo: Boolean) {
        viewModelScope.launch {
            repository.createMoment(mediaUrl, isVideo)
        }
    }

    // --- Prisims (Reels) actions ---
    fun createPrisim(videoUrl: String, caption: String, musicTitle: String) = viewModelScope.launch {
        repository.createPrisim(videoUrl, caption, musicTitle)
    }

    fun toggleLikePrisim(prisimId: String) = viewModelScope.launch {
        repository.toggleLikePrisim(prisimId)
    }

    // --- Messages / Collaboration ---
    fun sendMessage(receiverId: String, content: String, mediaUrl: String? = null, mediaType: String? = null) {
        viewModelScope.launch {
            repository.sendMessage(receiverId, content, mediaUrl, mediaType)
        }
    }

    fun deleteMessage(messageId: String) {
        viewModelScope.launch {
            repository.deleteMessage(messageId)
        }
    }

    fun reactToMessage(messageId: String, reaction: String) {
        viewModelScope.launch {
            repository.addMessageReaction(messageId, reaction)
        }
    }

    // --- Admin actions ---
    fun banUser(userId: String) {
        viewModelScope.launch {
            repository.banUser(userId)
        }
    }

    fun deleteReportedContent(postId: String) {
        viewModelScope.launch {
            repository.deletePost(postId)
        }
    }

    // --- Settings & Preferences ---
    fun toggleDarkMode() {
        _isDarkMode.value = !_isDarkMode.value
    }

    fun toggleNotifications() {
        _isNotificationsEnabled.value = !_isNotificationsEnabled.value
    }

    fun setLanguage(lang: String) {
        _selectedLanguage.value = lang
    }

    // --- Search ---
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun addRecentSearch(search: String) {
        val current = _recentSearches.value.toMutableList()
        current.remove(search)
        current.add(0, search)
        _recentSearches.value = current.take(6)
    }

    fun removeRecentSearch(search: String) {
        val current = _recentSearches.value.toMutableList()
        current.remove(search)
        _recentSearches.value = current
    }

    // --- Factory ---
    companion object {
        fun provideFactory(
            application: Application,
            repository: PrisimRepository
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return PrisimViewModel(application, repository) as T
            }
        }
    }
}

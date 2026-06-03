package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.*
import com.example.data.repository.PixelRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class PixelViewModel(
    application: Application,
    private val repository: PixelRepository
) : AndroidViewModel(application) {

    // Current Screen / Tab Configuration
    // "home", "explore", "create", "messages", "notifications", "profile"
    private val _currentTab = MutableStateFlow("home")
    val currentTab: StateFlow<String> = _currentTab.asStateFlow()

    fun navigateToTab(tabName: String) {
        _currentTab.value = tabName
    }

    // Interactive States
    private val _selectedPostForComments = MutableStateFlow<Post?>(null)
    val selectedPostForComments: StateFlow<Post?> = _selectedPostForComments.asStateFlow()

    fun selectPostForComments(post: Post?) {
        _selectedPostForComments.value = post
    }

    // Active Story Mode
    private val _activeStoryUser = MutableStateFlow<String?>(null) // Username list
    val activeStoryUser: StateFlow<String?> = _activeStoryUser.asStateFlow()

    fun viewStoryForUser(username: String?) {
        _activeStoryUser.value = username
        if (username != null) {
            // Mark all stories for this user as viewed
            viewModelScope.launch {
                val storiesSnapshot = repository.allStories.first()
                storiesSnapshot.filter { it.username == username }.forEach {
                    repository.markStoryAsViewed(it.storyId)
                }
            }
        }
    }

    // Search and Categorization
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun selectCategory(category: String) {
        _selectedCategory.value = category
    }

    // Algorithmic Sorting Mode ("chronological" vs "algorithmic")
    private val _feedRankingMode = MutableStateFlow("algorithmic")
    val feedRankingMode: StateFlow<String> = _feedRankingMode.asStateFlow()

    fun toggleRankingMode() {
        _feedRankingMode.value = if (_feedRankingMode.value == "algorithmic") "chronological" else "algorithmic"
    }

    // Posts stream with Algorithmic Logic applied
    val posts: StateFlow<List<Post>> = combine(
        repository.allPosts,
        _feedRankingMode,
        _searchQuery,
        _selectedCategory
    ) { rawPosts, mode, query, category ->
        var filtered = rawPosts

        // Apply category filter
        if (category != "All") {
            filtered = filtered.filter { it.category.equals(category, ignoreCase = true) }
        }

        // Apply query search (hashtag or caption)
        if (query.isNotEmpty()) {
            filtered = filtered.filter {
                it.caption.contains(query, ignoreCase = true) ||
                it.location.contains(query, ignoreCase = true) ||
                it.category.contains(query, ignoreCase = true) ||
                it.username.contains(query, ignoreCase = true)
            }
        }

        // Apply ranking mode
        if (mode == "algorithmic") {
            // Algorithmic scoring formula: engagement density (likes + comments weighted) combined with slight recency
            filtered.sortedByDescending { post ->
                val engagementScore = (post.likesCount * 3.5) + (post.commentsCount * 7.0)
                val recencyPenalty = (System.currentTimeMillis() - post.timestamp) / (1000 * 60 * 60) // hours elapsed
                engagementScore - (recencyPenalty * 1.5) // prioritize high interaction, degrade slowly over time
            }
        } else {
            // Chronological
            filtered.sortedByDescending { it.timestamp }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Comments list for selected post
    val commentsForSelectedPost: StateFlow<List<Comment>> = _selectedPostForComments
        .flatMapLatest { post ->
            if (post == null) flowOf(emptyList())
            else repository.getCommentsForPost(post.id)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Stories list
    val stories: StateFlow<List<Story>> = repository.allStories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Conversations inbox summary
    val directMessages: StateFlow<List<DirectMessage>> = repository.allMessages
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Notifications
    val notifications: StateFlow<List<Notification>> = repository.allNotifications
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Photo Collections
    val collections: StateFlow<List<PhotoCollection>> = repository.allCollections
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Post creation preview draft
    val postDraftCaption = MutableStateFlow("")
    val postDraftTag = MutableStateFlow("Travel")
    val postDraftLocation = MutableStateFlow("")
    val postDraftImageUrl = MutableStateFlow("")

    fun resetPostDraft() {
        postDraftCaption.value = ""
        postDraftTag.value = "Travel"
        postDraftLocation.value = ""
        postDraftImageUrl.value = ""
    }

    // --- Actions ---

    // Like Action (with fast ripple/Haptic feeling)
    fun toggleLike(post: Post) {
        viewModelScope.launch {
            val isLiking = !post.isLiked
            val updatedPost = post.copy(
                isLiked = isLiking,
                likesCount = post.likesCount + (if (isLiking) 1 else -1)
            )
            repository.updatePost(updatedPost)

            // Trigger visual notification of liking
            if (isLiking) {
                repository.insertNotification(
                    Notification(
                        type = "like",
                        actorName = "user_me",
                        actorAvatar = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=150&auto=format&fit=crop&q=80",
                        contentSnippet = "liked your post in ${post.location.ifEmpty { "the feed" }}",
                        associatedPostId = post.id
                    )
                )
            }
        }
    }

    // Save Action
    fun toggleSave(post: Post) {
        viewModelScope.launch {
            val isSaving = !post.isSaved
            val updatedPost = post.copy(isSaved = isSaving)
            repository.updatePost(updatedPost)

            // If saved, add to "Saved Posts" collection implicitly or create one
            val allCols = repository.allCollections.first()
            val savedCol = allCols.find { it.name == "Saved Posts" }
            if (isSaving) {
                if (savedCol == null) {
                    repository.insertCollection(
                        PhotoCollection(
                            name = "Saved Posts",
                            coverImageUrl = post.imageUrl,
                            postIdsString = "${post.id}"
                        )
                    )
                } else {
                    val currentIds = savedCol.getPostIds().toMutableList()
                    if (!currentIds.contains(post.id)) {
                        currentIds.add(post.id)
                        repository.updateCollection(
                            savedCol.copy(
                                coverImageUrl = post.imageUrl,
                                postIdsString = currentIds.joinToString(",")
                            )
                        )
                    }
                }
            } else {
                if (savedCol != null) {
                    val currentIds = savedCol.getPostIds().toMutableList()
                    currentIds.remove(post.id)
                    repository.updateCollection(
                        savedCol.copy(
                            postIdsString = currentIds.joinToString(",")
                        )
                    )
                }
            }
        }
    }

    // Comments actions
    fun addComment(text: String) {
        val activePost = _selectedPostForComments.value ?: return
        if (text.trim().isEmpty()) return

        viewModelScope.launch {
            val comment = Comment(
                postId = activePost.id,
                username = "user_me",
                userAvatar = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=150&auto=format&fit=crop&q=80",
                text = text
            )
            repository.insertComment(comment)

            // Auto notify owner
            repository.insertNotification(
                Notification(
                    type = "comment",
                    actorName = "user_me",
                    actorAvatar = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=150&auto=format&fit=crop&q=80",
                    contentSnippet = "commented on your post: \"${text.take(30)}...\"",
                    associatedPostId = activePost.id
                )
            )

            // Refresh selected post snapshot comments count to prevent click delay state sync
            val refPost = repository.getPostById(activePost.id)
            if (refPost != null) {
                _selectedPostForComments.value = refPost
            }
        }
    }

    // Post creation
    fun publishPost(): Boolean {
        val imageUrl = postDraftImageUrl.value.ifEmpty {
            // Default elegant image fallback
            "https://images.unsplash.com/photo-1516035069371-29a1b244cc32?w=600&auto=format&fit=crop&q=80"
        }
        val caption = postDraftCaption.value.ifEmpty { "Fresh lens on life! 🌟 #lens" }
        val category = postDraftTag.value
        val location = postDraftLocation.value.ifEmpty { "Pixel Studio" }

        viewModelScope.launch {
            val newPost = Post(
                username = "user_me",
                userAvatar = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=150&auto=format&fit=crop&q=80",
                imageUrl = imageUrl,
                caption = caption,
                location = location,
                category = category,
                likesCount = 0,
                commentsCount = 0,
                isLiked = false,
                isSaved = false
            )
            repository.insertPost(newPost)

            // Add notification to confirm
            repository.insertNotification(
                Notification(
                    type = "mention",
                    actorName = "PixelFlow System",
                    actorAvatar = "https://images.unsplash.com/photo-1628157582853-a796fa650a6a?w=150&auto=format&fit=crop&q=80",
                    contentSnippet = "Your beautiful photo was published successfully to the global feed!"
                )
            )
        }
        resetPostDraft()
        navigateToTab("home")
        return true
    }

    // Story creation
    fun publishStory(imagePath: String): Boolean {
        val url = imagePath.ifEmpty {
            "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?w=600&auto=format&fit=crop&q=80"
        }
        viewModelScope.launch {
            repository.insertStory(
                Story(
                    username = "user_me",
                    userAvatar = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=150&auto=format&fit=crop&q=80",
                    imageUrl = url
                )
            )
        }
        return true
    }

    // Messaging Dynamic Response Simulator
    private val _isTyping = MutableStateFlow(false)
    val isTyping: StateFlow<Boolean> = _isTyping.asStateFlow()

    fun sendDirectMessage(text: String, imageUri: String? = null) {
        if (text.trim().isEmpty() && imageUri == null) return

        viewModelScope.launch {
            val msg = DirectMessage(
                senderUsername = "user_me",
                senderAvatar = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=150&auto=format&fit=crop&q=80",
                text = text,
                imageUrl = imageUri
            )
            repository.insertMessage(msg)

            // Trigger Simulated Creator Reply typing indicator
            _isTyping.value = true
            delay(1500)
            _isTyping.value = false

            val responses = listOf(
                "Wow, that's such a cool photo observation!",
                "Thanks, I'm absolutely loving these new lighting settings! Check out my Paris collection.",
                "Yes! Let's definitely collaborate on the Travel theme next week. 🌍✨",
                "Appreciate the kind words! Sending positive vibes your way.",
                "Let's shoot some amazing architectural layouts soon!"
            )
            val randomReply = responses.random()

            repository.insertMessage(
                DirectMessage(
                    senderUsername = "sophia_travels",
                    senderAvatar = "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=150&auto=format&fit=crop&q=80",
                    text = randomReply
                )
            )

            // Add push notification simulation
            repository.insertNotification(
                Notification(
                    type = "message",
                    actorName = "sophia_travels",
                    actorAvatar = "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=150&auto=format&fit=crop&q=80",
                    contentSnippet = "sent you a direct message: \"$randomReply\""
                )
            )
        }
    }

    // Creating Curated Custom Profile Folder Collections
    fun createNewCollection(name: String, coverUrl: String) {
        if (name.trim().isEmpty()) return
        viewModelScope.launch {
            repository.insertCollection(
                PhotoCollection(
                    name = name,
                    coverImageUrl = coverUrl.ifEmpty { "https://images.unsplash.com/photo-1453728013993-6d66e9c9123a?w=600&auto=format&fit=crop&q=80" },
                    postIdsString = ""
                )
            )
        }
    }

    // Clear notifications in ViewModelScope coroutine
    fun clearAllNotifications() {
        viewModelScope.launch {
            repository.clearAllNotifications()
        }
    }

    // Mark single story as viewed
    fun markStoryAsViewed(storyId: Int) {
        viewModelScope.launch {
            repository.markStoryAsViewed(storyId)
        }
    }
}

class PixelViewModelFactory(
    private val application: Application,
    private val repository: PixelRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PixelViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PixelViewModel(application, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

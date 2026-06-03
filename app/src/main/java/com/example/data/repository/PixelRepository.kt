package com.example.data.repository

import com.example.data.local.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

class PixelRepository(
    private val postDao: PostDao,
    private val commentDao: CommentDao,
    private val storyDao: StoryDao,
    private val messageDao: MessageDao,
    private val collectionDao: CollectionDao,
    private val notificationDao: NotificationDao
) {
    // Posts
    val allPosts: Flow<List<Post>> = postDao.getAllPostsFlow()
    
    fun getPostsByCategory(category: String): Flow<List<Post>> = postDao.getPostsByCategoryFlow(category)
    
    fun searchPosts(query: String): Flow<List<Post>> = postDao.searchPostsFlow(query)
    
    suspend fun getPostById(postId: Int): Post? = postDao.getPostById(postId)

    suspend fun insertPost(post: Post): Long = postDao.insertPost(post)

    suspend fun updatePost(post: Post) = postDao.updatePost(post)

    suspend fun deletePost(post: Post) = postDao.deletePost(post)

    // Comments
    fun getCommentsForPost(postId: Int): Flow<List<Comment>> = commentDao.getCommentsForPostFlow(postId)

    suspend fun insertComment(comment: Comment): Long {
        // Also update post comments count
        val post = postDao.getPostById(comment.postId)
        if (post != null) {
            postDao.updatePost(post.copy(commentsCount = post.commentsCount + 1))
        }
        return commentDao.insertComment(comment)
    }

    // Stories
    val allStories: Flow<List<Story>> = storyDao.getAllStoriesFlow()

    suspend fun insertStory(story: Story): Long = storyDao.insertStory(story)

    suspend fun markStoryAsViewed(storyId: Int) = storyDao.markAsViewed(storyId)

    // Messages
    val allMessages: Flow<List<DirectMessage>> = messageDao.getAllMessagesFlow()

    suspend fun insertMessage(message: DirectMessage): Long = messageDao.insertMessage(message)

    // Collections
    val allCollections: Flow<List<PhotoCollection>> = collectionDao.getAllCollectionsFlow()

    suspend fun getCollectionById(id: Int): PhotoCollection? = collectionDao.getCollectionById(id)

    suspend fun insertCollection(collection: PhotoCollection): Long = collectionDao.insertCollection(collection)

    suspend fun updateCollection(collection: PhotoCollection) = collectionDao.updateCollection(collection)

    suspend fun deleteCollection(collection: PhotoCollection) = collectionDao.deleteCollection(collection)

    // Notifications
    val allNotifications: Flow<List<Notification>> = notificationDao.getAllNotificationsFlow()

    suspend fun insertNotification(notification: Notification): Long = notificationDao.insertNotification(notification)

    suspend fun clearAllNotifications() = notificationDao.clearAllNotifications()
}

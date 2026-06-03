package com.example.data.local

import androidx.room.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface PostDao {
    @Query("SELECT * FROM posts ORDER BY timestamp DESC")
    fun getAllPostsFlow(): Flow<List<Post>>

    @Query("SELECT * FROM posts WHERE id = :postId LIMIT 1")
    suspend fun getPostById(postId: Int): Post?

    @Query("SELECT * FROM posts WHERE category = :category ORDER BY timestamp DESC")
    fun getPostsByCategoryFlow(category: String): Flow<List<Post>>

    @Query("SELECT * FROM posts WHERE caption LIKE '%' || :query || '%' OR location LIKE '%' || :query || '%' OR category LIKE '%' || :query || '%'")
    fun searchPostsFlow(query: String): Flow<List<Post>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPost(post: Post): Long

    @Update
    suspend fun updatePost(post: Post)

    @Delete
    suspend fun deletePost(post: Post)
}

@Dao
interface CommentDao {
    @Query("SELECT * FROM comments WHERE postId = :postId ORDER BY timestamp ASC")
    fun getCommentsForPostFlow(postId: Int): Flow<List<Comment>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComment(comment: Comment): Long
}

@Dao
interface StoryDao {
    @Query("SELECT * FROM stories ORDER BY timestamp DESC")
    fun getAllStoriesFlow(): Flow<List<Story>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStory(story: Story): Long

    @Query("UPDATE stories SET isViewed = 1 WHERE storyId = :storyId")
    suspend fun markAsViewed(storyId: Int)
}

@Dao
interface MessageDao {
    @Query("SELECT * FROM messages ORDER BY timestamp ASC")
    fun getAllMessagesFlow(): Flow<List<DirectMessage>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: DirectMessage): Long
}

@Dao
interface CollectionDao {
    @Query("SELECT * FROM collections ORDER BY collectionId DESC")
    fun getAllCollectionsFlow(): Flow<List<PhotoCollection>>

    @Query("SELECT * FROM collections WHERE collectionId = :id LIMIT 1")
    suspend fun getCollectionById(id: Int): PhotoCollection?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCollection(collection: PhotoCollection): Long

    @Update
    suspend fun updateCollection(collection: PhotoCollection)

    @Delete
    suspend fun deleteCollection(collection: PhotoCollection)
}

@Dao
interface NotificationDao {
    @Query("SELECT * FROM notifications ORDER BY timestamp DESC")
    fun getAllNotificationsFlow(): Flow<List<Notification>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: Notification): Long

    @Query("DELETE FROM notifications")
    suspend fun clearAllNotifications()
}

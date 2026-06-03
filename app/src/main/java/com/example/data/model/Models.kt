package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "posts")
data class Post(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val username: String,
    val userAvatar: String,
    val imageUrl: String,
    val caption: String,
    val location: String = "",
    val category: String = "Photography",
    val likesCount: Int = 0,
    val commentsCount: Int = 0,
    val isLiked: Boolean = false,
    val isSaved: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "comments")
data class Comment(
    @PrimaryKey(autoGenerate = true) val commentId: Int = 0,
    val postId: Int,
    val username: String,
    val userAvatar: String,
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "stories")
data class Story(
    @PrimaryKey(autoGenerate = true) val storyId: Int = 0,
    val username: String,
    val userAvatar: String,
    val imageUrl: String,
    val isViewed: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "messages")
data class DirectMessage(
    @PrimaryKey(autoGenerate = true) val messageId: Int = 0,
    val senderUsername: String,
    val senderAvatar: String,
    val text: String = "",
    val imageUrl: String? = null,
    val isRead: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "collections")
data class PhotoCollection(
    @PrimaryKey(autoGenerate = true) val collectionId: Int = 0,
    val name: String,
    val coverImageUrl: String,
    val postIdsString: String = "" // Comma separated list of post ids in this collection
) {
    fun getPostIds(): List<Int> {
        if (postIdsString.isEmpty()) return emptyList()
        return postIdsString.split(",").mapNotNull { it.toIntOrNull() }
    }
}

@Entity(tableName = "notifications")
data class Notification(
    @PrimaryKey(autoGenerate = true) val notificationId: Int = 0,
    val type: String, // "like", "comment", "follow", "mention", "message"
    val actorName: String,
    val actorAvatar: String,
    val contentSnippet: String,
    val associatedPostId: Int? = null,
    val timestamp: Long = System.currentTimeMillis()
)

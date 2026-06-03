package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        Post::class,
        Comment::class,
        Story::class,
        DirectMessage::class,
        PhotoCollection::class,
        Notification::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun postDao(): PostDao
    abstract fun commentDao(): CommentDao
    abstract fun storyDao(): StoryDao
    abstract fun messageDao(): MessageDao
    abstract fun collectionDao(): CollectionDao
    abstract fun notificationDao(): NotificationDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "pixelflow_database"
                )
                .addCallback(AppDatabaseCallback(scope))
                .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class AppDatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {

        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    populateDatabase(database)
                }
            }
        }

        suspend fun populateDatabase(db: AppDatabase) {
            val postDao = db.postDao()
            val commentDao = db.commentDao()
            val storyDao = db.storyDao()
            val messageDao = db.messageDao()
            val collectionDao = db.collectionDao()
            val notificationDao = db.notificationDao()

            // 1. Prepopulation Stories
            val stories = listOf(
                Story(username = "sophia_travels", userAvatar = "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=150&auto=format&fit=crop&q=80", imageUrl = "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?w=600&auto=format&fit=crop&q=80", isViewed = false),
                Story(username = "liam_design", userAvatar = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=150&auto=format&fit=crop&q=80", imageUrl = "https://images.unsplash.com/photo-1486406146926-c627a92ad1ab?w=600&auto=format&fit=crop&q=80", isViewed = false),
                Story(username = "elena.style", userAvatar = "https://images.unsplash.com/photo-1438761681033-6461ffad8d80?w=150&auto=format&fit=crop&q=80", imageUrl = "https://images.unsplash.com/photo-1515886657613-9f3515b0c78f?w=600&auto=format&fit=crop&q=80", isViewed = false),
                Story(username = "marcus_clicks", userAvatar = "https://images.unsplash.com/photo-1628157582853-a796fa650a6a?w=150&auto=format&fit=crop&q=80", imageUrl = "https://images.unsplash.com/photo-1516035069371-29a1b244cc32?w=600&auto=format&fit=crop&q=80", isViewed = false),
                Story(username = "ana_wellness", userAvatar = "https://images.unsplash.com/photo-1544005313-94ddf0286df2?w=150&auto=format&fit=crop&q=80", imageUrl = "https://images.unsplash.com/photo-1513151233558-d860c5398176?w=600&auto=format&fit=crop&q=80", isViewed = false)
            )
            stories.forEach { storyDao.insertStory(it) }

            // 2. Prepopulation Posts
            val p1 = Post(
                username = "sophia_travels",
                userAvatar = "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=150&auto=format&fit=crop&q=80",
                imageUrl = "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?w=600&auto=format&fit=crop&q=80",
                caption = "Sunset bliss in the Maldives 🌊🏝️ Can we stay here forever? #travel #paradise #wanderlust",
                location = "Maldives, Indian Ocean",
                category = "Travel",
                likesCount = 1420,
                commentsCount = 3,
                isLiked = false,
                isSaved = false
            )
            val p2 = Post(
                username = "liam_design",
                userAvatar = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=150&auto=format&fit=crop&q=80",
                imageUrl = "https://images.unsplash.com/photo-1486406146926-c627a92ad1ab?w=600&auto=format&fit=crop&q=80",
                caption = "Obsessed with lines and light in the financial district. Concrete patterns of modern times. #architecture #cityscape #minimalism",
                location = "New York, USA",
                category = "Architecture",
                likesCount = 890,
                commentsCount = 2,
                isLiked = true, // pre-liked to test liked state UI
                isSaved = true  // pre-saved to test saved state collections default list
            )
            val p3 = Post(
                username = "elena.style",
                userAvatar = "https://images.unsplash.com/photo-1438761681033-6461ffad8d80?w=150&auto=format&fit=crop&q=80",
                imageUrl = "https://images.unsplash.com/photo-1515886657613-9f3515b0c78f?w=600&auto=format&fit=crop&q=80",
                caption = "Sunshine yellow and flowy vibes. Summer collections are finally here. ✨💛 #fashion #ootd #summer",
                location = "Paris, France",
                category = "Fashion",
                likesCount = 2309,
                commentsCount = 2,
                isLiked = false,
                isSaved = false
            )
            val p4 = Post(
                username = "marcus_clicks",
                userAvatar = "https://images.unsplash.com/photo-1628157582853-a796fa650a6a?w=150&auto=format&fit=crop&q=80",
                imageUrl = "https://images.unsplash.com/photo-1516035069371-29a1b244cc32?w=600&auto=format&fit=crop&q=80",
                caption = "The perfect symmetry. Captured on an old vintage film camera. Nostalgia in frames. #photography #film #retro",
                location = "Tokyo, Japan",
                category = "Photography",
                likesCount = 562,
                commentsCount = 2,
                isLiked = false,
                isSaved = false
            )
            val p5 = Post(
                username = "ana_wellness",
                userAvatar = "https://images.unsplash.com/photo-1544005313-94ddf0286df2?w=150&auto=format&fit=crop&q=80",
                imageUrl = "https://images.unsplash.com/photo-1513151233558-d860c5398176?w=600&auto=format&fit=crop&q=80",
                caption = "Golden hour journaling sessions. Mindful mornings set the rhythm for the day. 🌱📖 #lifestyle #mindfulness #journaling",
                location = "Stockholm, Sweden",
                category = "Lifestyle",
                likesCount = 1120,
                commentsCount = 2,
                isLiked = false,
                isSaved = false
            )

            val id1 = postDao.insertPost(p1).toInt()
            val id2 = postDao.insertPost(p2).toInt()
            val id3 = postDao.insertPost(p3).toInt()
            val id4 = postDao.insertPost(p4).toInt()
            val id5 = postDao.insertPost(p5).toInt()

            // 3. Prepopulation Comments
            val initialComments = listOf(
                Comment(postId = id1, username = "elena.style", userAvatar = "https://images.unsplash.com/photo-1438761681033-6461ffad8d80?w=150&auto=format&fit=crop&q=80", text = "This looks absolutely magical, Sophia! Take me with you next time! 😍"),
                Comment(postId = id1, username = "marcus_clicks", userAvatar = "https://images.unsplash.com/photo-1628157582853-a796fa650a6a?w=150&auto=format&fit=crop&q=80", text = "Stunning contrast on the water lines. Beautiful shoot."),
                Comment(postId = id1, username = "liam_design", userAvatar = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=150&auto=format&fit=crop&q=80", text = "Incredible color composition!"),
                Comment(postId = id2, username = "sophia_travels", userAvatar = "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=150&auto=format&fit=crop&q=80", text = "The lines in this photo are mesmerizing Liam! Great work!"),
                Comment(postId = id2, username = "ana_wellness", userAvatar = "https://images.unsplash.com/photo-1544005313-94ddf0286df2?w=150&auto=format&fit=crop&q=80", text = "So structured, feels very soothing and powerful!"),
                Comment(postId = id3, username = "sophia_travels", userAvatar = "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=150&auto=format&fit=crop&q=80", text = "That yellow is gorgeous on you! 🔥"),
                Comment(postId = id3, username = "liam_design", userAvatar = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=150&auto=format&fit=crop&q=80", text = "Paris fashion is unbeatable. Great composition!"),
                Comment(postId = id4, username = "ana_wellness", userAvatar = "https://images.unsplash.com/photo-1544005313-94ddf0286df2?w=150&auto=format&fit=crop&q=80", text = "Wow, Tokyo always has that retro energy. Beautifully caught!"),
                Comment(postId = id4, username = "elena.style", userAvatar = "https://images.unsplash.com/photo-1438761681033-6461ffad8d80?w=150&auto=format&fit=crop&q=80", text = "The look of film is just unmatched."),
                Comment(postId = id5, username = "marcus_clicks", userAvatar = "https://images.unsplash.com/photo-1628157582853-a796fa650a6a?w=150&auto=format&fit=crop&q=80", text = "Lighting is perfection here. Pure warmth."),
                Comment(postId = id5, username = "elena.style", userAvatar = "https://images.unsplash.com/photo-1438761681033-6461ffad8d80?w=150&auto=format&fit=crop&q=80", text = "Mindful mornings are crucial, love this scene!")
            )
            initialComments.forEach { commentDao.insertComment(it) }

            // 4. Prepopulation Collections
            val col1 = PhotoCollection(
                name = "Aesthetic Cities",
                coverImageUrl = "https://images.unsplash.com/photo-1486406146926-c627a92ad1ab?w=600&auto=format&fit=crop&q=80",
                postIdsString = "$id2,$id4"
            )
            val col2 = PhotoCollection(
                name = "Wanderlust Destinations",
                coverImageUrl = "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?w=600&auto=format&fit=crop&q=80",
                postIdsString = "$id1"
            )
            val col3 = PhotoCollection(
                name = "Creative Fashion",
                coverImageUrl = "https://images.unsplash.com/photo-1515886657613-9f3515b0c78f?w=600&auto=format&fit=crop&q=80",
                postIdsString = "$id3"
            )
            collectionDao.insertCollection(col1)
            collectionDao.insertCollection(col2)
            collectionDao.insertCollection(col3)

            // 5. Prepopulation Messages
            val messages = listOf(
                DirectMessage(senderUsername = "sophia_travels", senderAvatar = "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=150&auto=format&fit=crop&q=80", text = "Hey there! Loved your latest photo collections, they are super inspireful."),
                DirectMessage(senderUsername = "user_me", senderAvatar = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=150&auto=format&fit=crop&q=80", text = "Thank you so much Sophia! That means a lot. I loved your Maldives stories!"),
                DirectMessage(senderUsername = "sophia_travels", senderAvatar = "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=150&auto=format&fit=crop&q=80", text = "Here is a quick capture from the sunset flight yesterday! Can't wait to share it on the feed.", imageUrl = "https://images.unsplash.com/photo-1476514525535-07fb3b4ae5f1?w=600&auto=format&fit=crop&q=80")
            )
            messages.forEach { messageDao.insertMessage(it) }

            // 6. Prepopulation Notifications
            val notifications = listOf(
                Notification(type = "like", actorName = "elena.style", actorAvatar = "https://images.unsplash.com/photo-1438761681033-6461ffad8d80?w=150&auto=format&fit=crop&q=80", contentSnippet = "liked your photo in Paris, France", associatedPostId = id3),
                Notification(type = "comment", actorName = "marcus_clicks", actorAvatar = "https://images.unsplash.com/photo-1628157582853-a796fa650a6a?w=150&auto=format&fit=crop&q=80", contentSnippet = "commented: 'Lighting is perfection here. Pure warmth.'", associatedPostId = id5),
                Notification(type = "follow", actorName = "ana_wellness", actorAvatar = "https://images.unsplash.com/photo-1544005313-94ddf0286df2?w=150&auto=format&fit=crop&q=80", contentSnippet = "started following you. Check out their profile!")
            )
            notifications.forEach { notificationDao.insertNotification(it) }
        }
    }
}

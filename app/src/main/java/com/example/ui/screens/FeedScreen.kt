package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.model.Post
import com.example.data.model.Story
import com.example.ui.PixelViewModel
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun FeedScreen(
    viewModel: PixelViewModel,
    modifier: Modifier = Modifier
) {
    val posts by viewModel.posts.collectAsState()
    val stories by viewModel.stories.collectAsState()
    val rankingMode by viewModel.feedRankingMode.collectAsState()
    
    val selectedPostForComments by viewModel.selectedPostForComments.collectAsState()
    var showSharePost by remember { mutableStateOf<Post?>(null) }

    Box(modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Elegant Top Header Bar
            FeedHeader(
                rankingMode = rankingMode,
                onToggleRanking = { viewModel.toggleRankingMode() },
                onNavigateToMessages = { viewModel.navigateToTab("messages") }
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize().weight(1f),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                // Stories Horizontal Bar
                item {
                    StoriesBar(
                        stories = stories,
                        onStoryClick = { username -> viewModel.viewStoryForUser(username) }
                    )
                    Divider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f), 
                        thickness = 1.dp
                    )
                }

                // Sorting explanation snippet
                item {
                    RankingIndicatorBanner(rankingMode = rankingMode)
                }

                if (posts.isEmpty()) {
                    item {
                        EmptyFeedPlaceholder()
                    }
                } else {
                    items(posts, key = { it.id }) { post ->
                        PostCard(
                            post = post,
                            onLike = { viewModel.toggleLike(post) },
                            onCommentClick = { viewModel.selectPostForComments(post) },
                            onSave = { viewModel.toggleSave(post) },
                            onShareClick = { showSharePost = post },
                            onUserClick = { viewModel.navigateToTab("profile") }
                        )
                    }
                }
            }
        }

        // Comments Bottom Sheet Dialogue
        selectedPostForComments?.let { post ->
            CommentsDialog(
                post = post,
                viewModel = viewModel,
                onDismiss = { viewModel.selectPostForComments(null) }
            )
        }

        // Share Dialog Sheet
        showSharePost?.let { post ->
            ShareDialog(
                post = post,
                onDismiss = { showSharePost = null }
            )
        }
    }
}

@Composable
fun FeedHeader(
    rankingMode: String,
    onToggleRanking: () -> Unit,
    onNavigateToMessages: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth().statusBarsPadding(),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Brand Headline with artistic Flair gradient
            val headlineGradient = Brush.linearGradient(
                colors = listOf(
                    MaterialTheme.colorScheme.primary,
                    MaterialTheme.colorScheme.secondary
                )
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "PIXELFLOW",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        brush = headlineGradient,
                        letterSpacing = (-0.5).sp
                    )
                )
            }

            // Top action bar
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Algorithmic vs Chronological toggle icon inside circular background
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    IconButton(
                        onClick = onToggleRanking,
                        modifier = Modifier.testTag("feed_ranking_toggle")
                    ) {
                        Icon(
                            imageVector = if (rankingMode == "algorithmic") Icons.Default.AutoAwesome else Icons.Default.History,
                            contentDescription = "Change Feed Algorithm Mode",
                            tint = if (rankingMode == "algorithmic") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                // Messaging shortcut icon inside circular background
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    IconButton(
                        onClick = onNavigateToMessages,
                        modifier = Modifier.testTag("feed_messages_shortcut")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Send,
                            contentDescription = "Enter Messaging",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StoriesBar(
    stories: List<Story>,
    onStoryClick: (String) -> Unit
) {
    // Group stories by user to avoid duplicate bubbles in row
    val uniqueUserStories = remember(stories) {
        stories.distinctBy { it.username }
    }

    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            // My Story item
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.clickable { onStoryClick("user_me") }
            ) {
                Box(contentAlignment = Alignment.BottomEnd) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data("https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=150&auto=format&fit=crop&q=80")
                            .crossfade(true)
                            .build(),
                        contentDescription = "My Avatar",
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .border(1.5.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape),
                        contentScale = ContentScale.Crop
                    )
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                            .border(2.dp, MaterialTheme.colorScheme.surface, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Story",
                            tint = Color.White,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "My Story",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        items(uniqueUserStories) { story ->
            val artisticGradient = Brush.linearGradient(
                colors = listOf(Color(0xFFFFD600), Color(0xFFFF0069), Color(0xFF7630FF))
            )
            val borderModifier = if (!story.isViewed) {
                Modifier.border(2.5.dp, artisticGradient, CircleShape)
            } else {
                Modifier.border(1.5.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.clickable { onStoryClick(story.username) }
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(story.userAvatar)
                        .crossfade(true)
                        .build(),
                    contentDescription = "${story.username} avatar",
                    modifier = Modifier
                        .size(64.dp)
                        .padding(3.dp)
                        .then(borderModifier)
                        .padding(3.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = story.username,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.width(68.dp)
                )
            }
        }
    }
}

@Composable
fun RankingIndicatorBanner(rankingMode: String) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (rankingMode == "algorithmic") Icons.Default.AutoAwesome else Icons.Default.History,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (rankingMode == "algorithmic") {
                    "Simulated Algorithmic feed prioritising interaction weight (Likes + Comments)"
                } else {
                    "Chronological feed ordered by original publication timestamp"
                },
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PostCard(
    post: Post,
    onLike: () -> Unit,
    onCommentClick: () -> Unit,
    onSave: () -> Unit,
    onShareClick: () -> Unit,
    onUserClick: () -> Unit
) {
    var heartAnimateScale by remember { mutableStateOf(1f) }
    var showOverlayHeart by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .shadow(2.dp, shape = RoundedCornerShape(32.dp))
            .testTag("post_card_${post.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(32.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp, 
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
        )
    ) {
        Column {
            // Header: User Info
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable(onClick = onUserClick)
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(post.userAvatar)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Creator Avatar",
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = post.username,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (post.location.isNotEmpty()) {
                            Text(
                                text = post.location,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Category tag pill
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = post.category,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }

            // Body: Content Image (supports double tap to like) - styled with rounded-24 and lateral padding/margins
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 6.dp)
                    .height(340.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .combinedClickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null, // Disable default card click selection ripple for double taps
                        onDoubleClick = {
                            if (!post.isLiked) {
                                onLike()
                            }
                            showOverlayHeart = true
                            coroutineScope.launch {
                                delay(600)
                                showOverlayHeart = false
                            }
                        },
                        onClick = {}
                    ),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(post.imageUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = post.caption,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                // High-fidelity instant Liked Heart Overlay overlay pop
                androidx.compose.animation.AnimatedVisibility(
                    visible = showOverlayHeart,
                    enter = scaleIn(animationSpec = spring(dampingRatio = 0.5f)) + fadeIn(),
                    exit = scaleOut() + fadeOut()
                ) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.9f),
                        modifier = Modifier.size(90.dp).shadow(4.dp, CircleShape)
                    )
                }
            }

            // Bottom action interactions bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Like button (animated)
                    val scale by animateFloatAsState(
                        targetValue = if (post.isLiked) 1.2f else 1f,
                        animationSpec = spring(dampingRatio = 0.4f, stiffness = Spring.StiffnessMedium)
                    )
                    IconButton(
                        onClick = onLike,
                        modifier = Modifier.testTag("post_like_button_${post.id}")
                    ) {
                        Icon(
                            imageVector = if (post.isLiked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = "Like",
                            tint = if (post.isLiked) PixelPrimary else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(24.dp).align(Alignment.CenterVertically)
                        )
                    }

                    // Comment button
                    IconButton(
                        onClick = onCommentClick,
                        modifier = Modifier.testTag("post_comment_button_${post.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.ChatBubbleOutline,
                            contentDescription = "Comment",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // Share button
                    IconButton(onClick = onShareClick) {
                        Icon(
                            imageVector = Icons.Outlined.Share,
                            contentDescription = "Share",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                // Bookmark Save button
                IconButton(onClick = onSave) {
                    Icon(
                        imageVector = if (post.isSaved) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                        contentDescription = "Save",
                        tint = if (post.isSaved) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // Description values (Likes & caption text box)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "${post.likesCount} likes",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = post.username,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(end = 6.dp)
                    )
                    Text(
                        text = post.caption,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                if (post.commentsCount > 0) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "View all ${post.commentsCount} comments",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .clickable(onClick = onCommentClick)
                            .padding(vertical = 2.dp)
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
            }
        }
    }
}

@Composable
fun CommentsDialog(
    post: Post,
    viewModel: PixelViewModel,
    onDismiss: () -> Unit
) {
    val comments by viewModel.commentsForSelectedPost.collectAsState()
    var commentText by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.8f),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Comments",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close Comments"
                        )
                    }
                }
                Divider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), 
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                // Comment list
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (comments.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier.fillParentMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No comments yet.\nBe the first to share your thoughts!",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 12.sp,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    } else {
                        items(comments) { comment ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.Top
                            ) {
                                AsyncImage(
                                    model = comment.userAvatar,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = comment.username,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "1m ago", // simulated
                                            fontSize = 9.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = comment.text,
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }

                // Input box
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextField(
                        value = commentText,
                        onValueChange = { commentText = it },
                        placeholder = { Text("Add comment info...", fontSize = 12.sp) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("comment_input_field"),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        shape = RoundedCornerShape(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            viewModel.addComment(commentText)
                            commentText = ""
                        },
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier.testTag("comment_submit_button")
                    ) {
                        Text("Post", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun ShareDialog(
    post: Post,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Share Content",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(14.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    IconButton(onClick = onDismiss) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Filled.Send, contentDescription = "Send", tint = MaterialTheme.colorScheme.primary)
                            Text("Direct", fontSize = 10.sp)
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Filled.FileCopy, contentDescription = "Copy Link")
                            Text("Copy URL", fontSize = 10.sp)
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Filled.QrCode, contentDescription = "QR Code")
                            Text("Qr Scan", fontSize = 10.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyFeedPlaceholder() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(400.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.Camera,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.outlineVariant,
                modifier = Modifier.size(60.dp)
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "No creators listed yet.",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

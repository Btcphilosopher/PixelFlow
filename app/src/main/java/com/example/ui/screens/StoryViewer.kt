package com.example.ui.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.model.Story
import com.example.ui.PixelViewModel
import kotlinx.coroutines.delay

@Composable
fun StoryViewer(
    username: String,
    viewModel: PixelViewModel,
    onDismiss: () -> Unit
) {
    val stories by viewModel.stories.collectAsState()
    
    // Group and filter stories matching selected username
    val userStories = remember(stories, username) {
        if (username == "user_me") {
            // My Story fallback or actual stories authored by me
            stories.filter { it.username == "user_me" }.ifEmpty {
                listOf(
                    Story(
                        username = "user_me",
                        userAvatar = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=150&auto=format&fit=crop&q=80",
                        imageUrl = "https://images.unsplash.com/photo-1513151233558-d860c5398176?w=600&auto=format&fit=crop&q=80"
                    )
                )
            }
        } else {
            stories.filter { it.username == username }
        }
    }

    var currentIndex by remember { mutableStateOf(0) }
    var progressPlay by remember { mutableStateOf(0f) }

    // Auto-advance stories effect
    LaunchedEffect(currentIndex, userStories) {
        if (userStories.isEmpty()) {
            onDismiss()
            return@LaunchedEffect
        }
        
        progressPlay = 0f
        val activeStory = userStories[currentIndex]
        viewModel.markStoryAsViewed(activeStory.storyId)

        // Simple step animations simulating timed horizontal slider
        val totalSteps = 100
        for (step in 1..totalSteps) {
            delay(50) // 5000ms divided by 100 steps
            progressPlay = step / totalSteps.toFloat()
        }

        // Timer finished: go next or close
        if (currentIndex < userStories.size - 1) {
            currentIndex++
        } else {
            onDismiss()
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .testTag("story_viewer_modal")
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        // Left third taps go back, right taps advance
                        val screenWidth = size.width
                        if (offset.x < screenWidth / 3f) {
                            if (currentIndex > 0) {
                                currentIndex--
                            } else {
                                onDismiss()
                            }
                        } else {
                            if (currentIndex < userStories.size - 1) {
                                currentIndex++
                            } else {
                                onDismiss()
                            }
                        }
                    }
                }
        ) {
            if (userStories.isNotEmpty()) {
                val currentStory = userStories[currentIndex]

                // Full Bleed Image background
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(currentStory.imageUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Active story view",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                // Top Story header bar
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(top = 16.dp)
                ) {
                    // Timed Progress linear slider bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        userStories.forEachIndexed { i, _ ->
                            val drawProgress = when {
                                i < currentIndex -> 1f
                                i > currentIndex -> 0f
                                else -> progressPlay
                            }
                            LinearProgressIndicator(
                                progress = { drawProgress },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(3.dp),
                                color = Color.White,
                                trackColor = Color.White.copy(alpha = 0.35f)
                            )
                        }
                    }

                    // Author info Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            AsyncImage(
                                model = currentStory.userAvatar,
                                contentDescription = null,
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = currentStory.username,
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Creator Story",
                                    color = Color.White.copy(alpha = 0.7f),
                                    fontSize = 9.sp
                                )
                            }
                        }

                        // Close button 'X' clickable overlay
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.testTag("story_viewer_close_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Exit Viewer",
                                tint = Color.White
                            )
                        }
                    }
                }
            } else {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No stories listed for this creator.", color = Color.White)
                }
            }
        }
    }
}

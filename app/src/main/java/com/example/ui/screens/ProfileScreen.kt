package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.FolderSpecial
import androidx.compose.material.icons.outlined.GridOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.model.PhotoCollection
import com.example.data.model.Post
import com.example.ui.PixelViewModel

@Composable
fun ProfileScreen(
    viewModel: PixelViewModel,
    modifier: Modifier = Modifier
) {
    val posts by viewModel.posts.collectAsState()
    val collections by viewModel.collections.collectAsState()

    // Filter posts for "My Posts" and "Saved Posts"
    val myPosts = remember(posts) { posts.filter { it.username == "user_me" } }
    val savedPosts = remember(posts) { posts.filter { it.isSaved } }

    var selectedTabIndex by remember { mutableStateOf(0) } // 0: Grid, 1: Saved, 2: Collections
    var showCreateCollectionDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Simple Profile Header Bar
        Surface(
            modifier = Modifier.fillMaxWidth().statusBarsPadding(),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp,
            shadowElevation = 2.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "user_me",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                IconButton(onClick = { showCreateCollectionDialog = true }) {
                    Icon(
                        imageVector = Icons.Default.CreateNewFolder,
                        contentDescription = "Create Album Collection",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        // Bio & Statistics Section
        ProfileStatsBioView(
            postCount = posts.size, // total sandbox posts count to look alive
            followerCount = 4210,
            followingCount = 380,
            username = "Creative Coder",
            bioHeadline = "Visualizer & Frontend artisan. Sweden 🇸🇪\nCapturing minimalistic architectural lines and traveling sunbeams."
        )

        Divider(
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
            thickness = 1.dp
        )

        // Triple Navigation Tab Panel
        TabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary
        ) {
            Tab(
                selected = selectedTabIndex == 0,
                onClick = { selectedTabIndex = 0 },
                icon = { Icon(Icons.Outlined.GridOn, contentDescription = "My Posts Grid") },
                text = { Text("Posts", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                modifier = Modifier.testTag("profile_tab_posts")
            )
            Tab(
                selected = selectedTabIndex == 1,
                onClick = { selectedTabIndex = 1 },
                icon = { Icon(Icons.Outlined.BookmarkBorder, contentDescription = "Saved Library") },
                text = { Text("Saved", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                modifier = Modifier.testTag("profile_tab_saved")
            )
            Tab(
                selected = selectedTabIndex == 2,
                onClick = { selectedTabIndex = 2 },
                icon = { Icon(Icons.Outlined.FolderSpecial, contentDescription = "Collections") },
                text = { Text("Albums", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                modifier = Modifier.testTag("profile_tab_albums")
            )
        }

        // Multi-view Content layout
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            when (selectedTabIndex) {
                0 -> ProfilePostsGrid(posts = myPosts) { post ->
                    viewModel.selectPostForComments(post)
                }
                1 -> ProfilePostsGrid(posts = savedPosts) { post ->
                    viewModel.selectPostForComments(post)
                }
                2 -> ProfileCollectionsView(
                    collections = collections,
                    posts = posts,
                    onCollectionClick = { /* Can show filtering */ }
                )
            }
        }

        // Create Collection dialog
        if (showCreateCollectionDialog) {
            CreateCollectionDialog(
                onDismiss = { showCreateCollectionDialog = false },
                onAddCollection = { nameStr, coverUrlStr ->
                    viewModel.createNewCollection(nameStr, coverUrlStr)
                    showCreateCollectionDialog = false
                }
            )
        }
    }
}

@Composable
fun ProfileStatsBioView(
    postCount: Int,
    followerCount: Int,
    followingCount: Int,
    username: String,
    bioHeadline: String
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Circular profile avatar with luxury artistic gradient border
                val bioAvatarGradient = Brush.linearGradient(
                    colors = listOf(Color(0xFFFFD600), Color(0xFFFF0069), Color(0xFF7630FF))
                )
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data("https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=150&auto=format&fit=crop&q=80")
                        .crossfade(true)
                        .build(),
                    contentDescription = "My Avatar",
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .border(2.5.dp, bioAvatarGradient, CircleShape),
                    contentScale = ContentScale.Crop
                )

                // Statistical meters
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 12.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    StatMetricColumn(metricValue = "$postCount", metricLabel = "Posts")
                    StatMetricColumn(metricValue = "4.2K", metricLabel = "Followers")
                    StatMetricColumn(metricValue = "380", metricLabel = "Following")
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Biography Details
            Text(
                text = username,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = bioHeadline,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 16.sp
            )
        }
    }
}

@Composable
fun StatMetricColumn(
    metricValue: String,
    metricLabel: String
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = metricValue,
            fontSize = 16.sp,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = metricLabel,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun ProfilePostsGrid(
    posts: List<Post>,
    onPostGridClick: (Post) -> Unit
) {
    if (posts.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = null,
                    modifier = Modifier.size(50.dp),
                    tint = MaterialTheme.colorScheme.outlineVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "No visual portfolio entries yet.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    } else {
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier
                .fillMaxSize()
                .testTag("profile_gallery_grid"),
            contentPadding = PaddingValues(1.dp),
            horizontalArrangement = Arrangement.spacedBy(1.dp),
            verticalArrangement = Arrangement.spacedBy(1.dp)
        ) {
            items(posts) { post ->
                Box(
                    modifier = Modifier
                        .aspectRatio(1f)
                        .clickable { onPostGridClick(post) }
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
                }
            }
        }
    }
}

@Composable
fun ProfileCollectionsView(
    collections: List<PhotoCollection>,
    posts: List<Post>,
    onCollectionClick: (PhotoCollection) -> Unit
) {
    if (collections.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "No collection albums formed yet.\nTap folder icon on top to compile!",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    } else {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(collections) { col ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .clickable { onCollectionClick(col) },
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        AsyncImage(
                            model = col.coverImageUrl,
                            contentDescription = col.name,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.BottomCenter)
                                .background(Color.Black.copy(alpha = 0.5f))
                                .padding(8.dp)
                        ) {
                            Column {
                                Text(
                                    text = col.name,
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                val associatedCount = col.getPostIds().size
                                Text(
                                    text = "$associatedCount items inside",
                                    color = Color.White.copy(alpha = 0.8f),
                                    fontSize = 9.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CreateCollectionDialog(
    onDismiss: () -> Unit,
    onAddCollection: (String, String) -> Unit
) {
    var collectionName by remember { mutableStateOf("") }
    var coverUrl by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "New Folder Collection",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = collectionName,
                    onValueChange = { collectionName = it },
                    label = { Text("Collection Folder Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("collection_name_field")
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = coverUrl,
                    onValueChange = { coverUrl = it },
                    label = { Text("Cover photo URL (Optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("collection_cover_field")
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { onAddCollection(collectionName, coverUrl) },
                        modifier = Modifier.testTag("collection_create_button")
                    ) {
                        Text("Create")
                    }
                }
            }
        }
    }
}

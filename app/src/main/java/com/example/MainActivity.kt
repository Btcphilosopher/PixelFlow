package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import com.example.data.local.AppDatabase
import com.example.data.repository.PixelRepository
import com.example.ui.PixelViewModel
import com.example.ui.PixelViewModelFactory
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize local Room persistence database & repository
        val db = AppDatabase.getDatabase(applicationContext, lifecycleScope)
        val repository = PixelRepository(
            db.postDao(),
            db.commentDao(),
            db.storyDao(),
            db.messageDao(),
            db.collectionDao(),
            db.notificationDao()
        )
        val factory = PixelViewModelFactory(application, repository)

        setContent {
            MyApplicationTheme {
                // Fetch our central ViewModel instance with our custom factory
                val viewModel: PixelViewModel = androidx.lifecycle.viewmodel.compose.viewModel(factory = factory)
                PixelFlowApp(viewModel)
            }
        }
    }
}

@Composable
fun PixelFlowApp(viewModel: PixelViewModel) {
    val currentTab by viewModel.currentTab.collectAsState()
    val activeStoryUser by viewModel.activeStoryUser.collectAsState()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            PixelBottomNavigationBar(
                currentTab = currentTab,
                onTabSelected = { tab -> viewModel.navigateToTab(tab) }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Main Router Layout
            when (currentTab) {
                "home" -> FeedScreen(viewModel = viewModel)
                "explore" -> ExploreScreen(viewModel = viewModel)
                "create" -> CreateScreen(viewModel = viewModel)
                "notifications" -> NotificationsScreen(viewModel = viewModel)
                "profile" -> ProfileScreen(viewModel = viewModel)
                "messages" -> MessagesScreen(viewModel = viewModel)
                else -> FeedScreen(viewModel = viewModel)
            }
        }
    }

    // Timed overlay Story Viewer panel
    activeStoryUser?.let { username ->
        StoryViewer(
            username = username,
            viewModel = viewModel,
            onDismiss = { viewModel.viewStoryForUser(null) }
        )
    }
}

@Composable
fun PixelBottomNavigationBar(
    currentTab: String,
    onTabSelected: (String) -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant, // #F3EDF7
        tonalElevation = 8.dp,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("pixel_bottom_navigation_bar")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(bottom = 6.dp, top = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            val tabs = listOf(
                NavigationTabItem("home", "Home", Icons.Filled.Home, Icons.Outlined.Home),
                NavigationTabItem("explore", "Explore", Icons.Filled.Search, Icons.Outlined.Search),
                NavigationTabItem("create", "Create", Icons.Filled.Add, Icons.Outlined.Add),
                NavigationTabItem("notifications", "Activity", Icons.Filled.Favorite, Icons.Outlined.FavoriteBorder),
                NavigationTabItem("profile", "Profile", Icons.Filled.Person, Icons.Outlined.Person)
            )

            tabs.forEach { tab ->
                val isSelected = currentTab == tab.id || (tab.id == "home" && currentTab == "messages")
                
                if (tab.id == "create") {
                    // Floating center "Create" button reflecting artistic design HTML!
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .offset(y = (-14).dp)
                            .clickable(onClick = { onTabSelected("create") })
                            .testTag("nav_item_create")
                    ) {
                        Surface(
                            modifier = Modifier
                                .size(56.dp)
                                .shadow(8.dp, shape = RoundedCornerShape(16.dp))
                                .border(4.dp, MaterialTheme.colorScheme.background, RoundedCornerShape(16.dp)),
                            color = MaterialTheme.colorScheme.primary, // #6750A4
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Create",
                                    tint = Color.White,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }
                    }
                } else {
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { onTabSelected(tab.id) },
                        icon = {
                            Icon(
                                imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                                contentDescription = tab.label
                            )
                        },
                        label = { Text(tab.label, fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                        modifier = Modifier.testTag("nav_item_${tab.id}"),
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                            indicatorColor = MaterialTheme.colorScheme.secondaryContainer
                        )
                    )
                }
            }
        }
    }
}

data class NavigationTabItem(
    val id: String,
    val label: String,
    val selectedIcon: androidx.compose.ui.graphics.vector.ImageVector,
    val unselectedIcon: androidx.compose.ui.graphics.vector.ImageVector
)

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(text = "Hello $name!", modifier = modifier)
}

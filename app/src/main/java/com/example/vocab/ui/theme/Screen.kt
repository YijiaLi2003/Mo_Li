package com.example.vocab.ui.theme

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material.icons.outlined.Search
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Home : Screen("home", "Home", Icons.Outlined.Home)
    object Community : Screen("community", "Community", Icons.Outlined.ChatBubbleOutline)
    object Search : Screen("search", "Search", Icons.Outlined.Search)
    object Profile : Screen("profile", "Profile", Icons.Outlined.PersonOutline)
    object LearningSection : Screen("learning_section", "Learning", Icons.Filled.MenuBook)
}

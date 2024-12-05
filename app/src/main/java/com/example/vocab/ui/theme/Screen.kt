package com.example.vocab.ui.theme

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material.icons.outlined.Search
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(
    val route: String,
    val title: String = "",
    val icon: ImageVector? = null
) {
    data object Home : Screen("home", "Home", Icons.Outlined.Home)
    data object Community : Screen("community", "Community", Icons.Outlined.ChatBubbleOutline)
    data object Search : Screen("search", "Search", Icons.Outlined.Search)
    data object Profile : Screen("profile", "Profile", Icons.Outlined.PersonOutline)
    data object LearningSection : Screen(
        "learning_section",
        "Learning",
        Icons.AutoMirrored.Filled.MenuBook
    )

    data object SignUp : Screen("Sign_up")
    data object SignIn : Screen("sign_in")

}

sealed class ProfileSubScreen(val route: String) {
    data object WordBooks : ProfileSubScreen("word_books")
    data object FavouriteWords : ProfileSubScreen("favourite_words")
    data object ReStudyWords : ProfileSubScreen("re_study_words")
    data object WordNotes : ProfileSubScreen("word_notes")
    data object LearningData : ProfileSubScreen("learning_data")
    data object Settings : ProfileSubScreen("settings")
}

sealed class LearnInLandscape(val route: String){
    data object LandScapeLearn : LearnInLandscape("landscape_learn")
}

sealed class LearnInPortrait(val route: String){
    data object PortraitLearn : LearnInPortrait("portrait_learn")
}
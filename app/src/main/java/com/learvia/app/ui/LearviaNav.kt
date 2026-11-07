package com.learvia.app.ui

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.learvia.app.ui.screens.ChapterScreen
import com.learvia.app.ui.screens.CollectionsScreen
import com.learvia.app.ui.screens.CourseScreen
import com.learvia.app.ui.screens.FocusScreen
import com.learvia.app.ui.screens.LibraryScreen
import com.learvia.app.ui.screens.ManagementScreen
import com.learvia.app.ui.screens.MediaLibraryScreen
import com.learvia.app.ui.screens.PlannerScreen
import com.learvia.app.ui.screens.PlayerScreen
import com.learvia.app.ui.screens.SearchScreen
import com.learvia.app.ui.screens.SettingsScreen
import com.learvia.app.ui.screens.StatsScreen

private val idArg = listOf(navArgument("id") { type = NavType.LongType })
private const val PLAYER = "player/{id}?pos={pos}"

@Composable
fun LearviaNav() {
    val nav = rememberNavController()
    val vm: LibraryViewModel = viewModel()
    val back: () -> Unit = { nav.popBackStack() }

    NavHost(nav, startDestination = "library") {
        composable("library") {
            LibraryScreen(vm, { nav.navigate("course/$it") }, { nav.navigate("player/$it?pos=-1") }) { nav.navigate(it) }
        }
        composable("course/{id}", idArg) {
            CourseScreen(vm, it.arguments!!.getLong("id"), back) { ch -> nav.navigate("chapter/$ch") }
        }
        composable("chapter/{id}", idArg) {
            ChapterScreen(vm, it.arguments!!.getLong("id"), back) { cls -> nav.navigate("player/$cls?pos=-1") }
        }
        composable(
            PLAYER,
            arguments = listOf(
                navArgument("id") { type = NavType.LongType },
                navArgument("pos") { type = NavType.LongType; defaultValue = -1L },
            ),
        ) {
            val id = it.arguments!!.getLong("id")
            val pos = it.arguments!!.getLong("pos")
            PlayerScreen(vm, id, pos, back) { next ->
                // Replace the current player so Back returns to the lesson list, not the previous video.
                nav.navigate("player/$next?pos=-1") { popUpTo(PLAYER) { inclusive = true } }
            }
        }
        composable("settings") { SettingsScreen(vm, back) { nav.navigate(it) } }
        composable("manage") { ManagementScreen(vm, back) }
        composable("search") { SearchScreen(vm, back) { nav.navigate(it) } }
        composable("planner") { PlannerScreen(vm, back) }
        composable("focus") { FocusScreen(vm, back) }
        composable("stats") { StatsScreen(vm, back) }
        composable("collections") { CollectionsScreen(vm, back) }
        composable("media") { MediaLibraryScreen(vm, back) }
    }
}

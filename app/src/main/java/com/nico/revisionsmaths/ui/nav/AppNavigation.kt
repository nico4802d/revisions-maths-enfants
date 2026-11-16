package com.nico.revisionsmaths.ui.nav

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.nico.revisionsmaths.data.model.FixedTable
import com.nico.revisionsmaths.data.model.OperationType
import com.nico.revisionsmaths.data.model.QuizConfig
import com.nico.revisionsmaths.ui.screens.CreateProfileScreen
import com.nico.revisionsmaths.ui.screens.HistoryScreen
import com.nico.revisionsmaths.ui.screens.HomeScreen
import com.nico.revisionsmaths.ui.screens.QuizConfigScreen
import com.nico.revisionsmaths.ui.screens.QuizScreen
import com.nico.revisionsmaths.ui.screens.ResultsScreen
import com.nico.revisionsmaths.ui.screens.SettingsScreen

object Routes {
    const val HOME = "home"
    const val CREATE_PROFILE = "createProfile"
    const val CONFIG = "config/{profileId}"
    const val QUIZ = "quiz/{profileId}/{operationType}/{questionCount}/{fixedTable}"
    const val RESULTS = "results/{profileId}"
    const val HISTORY = "history/{profileId}"
    const val SETTINGS = "settings"

    fun config(profileId: Long) = "config/$profileId"
    fun quiz(
        profileId: Long,
        type: OperationType,
        count: Int,
        fixedTable: FixedTable? = null
    ): String = "quiz/$profileId/${type.name}/$count/${fixedTable?.encode() ?: "-"}"
    fun results(profileId: Long) = "results/$profileId"
    fun history(profileId: Long) = "history/$profileId"
}

@Composable
fun AppNavigation() {
    val navController: NavHostController = rememberNavController()
    NavHost(navController = navController, startDestination = Routes.HOME) {
        composable(Routes.HOME) { HomeScreen(navController) }
        composable(Routes.CREATE_PROFILE) { CreateProfileScreen(navController) }
        composable(
            Routes.CONFIG,
            arguments = listOf(navArgument("profileId") { type = NavType.LongType })
        ) { entry ->
            QuizConfigScreen(navController, entry.arguments?.getLong("profileId") ?: 0L)
        }
        composable(
            Routes.QUIZ,
            arguments = listOf(
                navArgument("profileId") { type = NavType.LongType },
                navArgument("operationType") { type = NavType.StringType },
                navArgument("questionCount") { type = NavType.IntType },
                navArgument("fixedTable") { type = NavType.StringType }
            )
        ) { entry ->
            QuizScreen(
                navController,
                entry.arguments?.getLong("profileId") ?: 0L,
                QuizConfig(
                    OperationType.fromName(entry.arguments?.getString("operationType") ?: "MIXED"),
                    entry.arguments?.getInt("questionCount") ?: 10,
                    FixedTable.decode(entry.arguments?.getString("fixedTable"))
                )
            )
        }
        composable(
            Routes.RESULTS,
            arguments = listOf(navArgument("profileId") { type = NavType.LongType })
        ) { entry ->
            ResultsScreen(navController, entry.arguments?.getLong("profileId") ?: 0L)
        }
        composable(
            Routes.HISTORY,
            arguments = listOf(navArgument("profileId") { type = NavType.LongType })
        ) { entry ->
            HistoryScreen(navController, entry.arguments?.getLong("profileId") ?: 0L)
        }
        composable(Routes.SETTINGS) { SettingsScreen(navController) }
    }
}

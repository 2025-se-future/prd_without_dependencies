package com.movieswipe.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.movieswipe.ui.screens.groups.CreateGroupScreen
import com.movieswipe.ui.screens.groups.GroupDetailsScreen
import com.movieswipe.ui.screens.groups.GroupListScreen
import com.movieswipe.ui.screens.groups.JoinGroupScreen
import com.movieswipe.ui.screens.movies.MovieDetailsScreen
import com.movieswipe.ui.screens.users.LoginScreen
import com.movieswipe.ui.screens.users.ProfileScreen
import com.movieswipe.ui.screens.voting.VotingResultsScreen
import com.movieswipe.ui.screens.voting.VotingSessionScreen
import com.movieswipe.ui.viewmodels.users.AuthViewModel

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController(),
    authViewModel: AuthViewModel
) {
    val isLoggedIn by authViewModel.isLoggedIn.collectAsState(initial = false)

    NavHost(
        navController = navController,
        startDestination = if (isLoggedIn) NavigationRoutes.GROUPS else NavigationRoutes.LOGIN
    ) {
        // Authentication Flow
        composable(NavigationRoutes.LOGIN) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(NavigationRoutes.GROUPS) {
                        popUpTo(NavigationRoutes.LOGIN) { inclusive = true }
                    }
                },
                authViewModel = authViewModel
            )
        }

        composable(NavigationRoutes.PROFILE) {
            ProfileScreen(
                onSignedOut = {
                    navController.navigate(NavigationRoutes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                authViewModel = authViewModel
            )
        }

        // Groups Flow
        composable(NavigationRoutes.GROUPS) {
            GroupListScreen(
                onNavigateToProfile = {
                    navController.navigate(NavigationRoutes.PROFILE)
                },
                onNavigateToCreateGroup = {
                    navController.navigate(NavigationRoutes.CREATE_GROUP)
                },
                onNavigateToJoinGroup = {
                    navController.navigate(NavigationRoutes.JOIN_GROUP)
                },
                onNavigateToGroupDetails = { groupId ->
                    navController.navigate(NavigationRoutes.groupDetails(groupId))
                }
            )
        }

        composable(NavigationRoutes.CREATE_GROUP) {
            CreateGroupScreen(
                onGroupCreated = { groupId ->
                    navController.navigate(NavigationRoutes.groupDetails(groupId)) {
                        popUpTo(NavigationRoutes.GROUPS)
                    }
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(NavigationRoutes.JOIN_GROUP) {
            JoinGroupScreen(
                onGroupJoined = { groupId ->
                    navController.navigate(NavigationRoutes.groupDetails(groupId)) {
                        popUpTo(NavigationRoutes.GROUPS)
                    }
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(NavigationRoutes.GROUP_DETAILS) { backStackEntry ->
            val groupId = backStackEntry.arguments?.getString("groupId") ?: ""
            GroupDetailsScreen(
                groupId = groupId,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onStartVoting = { sessionId ->
                    navController.navigate(NavigationRoutes.votingSession(sessionId))
                }
            )
        }

        // Voting Flow
        composable(NavigationRoutes.VOTING_SESSION) { backStackEntry ->
            val sessionId = backStackEntry.arguments?.getString("sessionId") ?: ""
            VotingSessionScreen(
                sessionId = sessionId,
                onVotingComplete = {
                    navController.navigate(NavigationRoutes.votingResults(sessionId)) {
                        popUpTo(NavigationRoutes.GROUPS)
                    }
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(NavigationRoutes.VOTING_RESULTS) { backStackEntry ->
            val sessionId = backStackEntry.arguments?.getString("sessionId") ?: ""
            VotingResultsScreen(
                sessionId = sessionId,
                onNavigateToGroups = {
                    navController.navigate(NavigationRoutes.GROUPS) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onNavigateToMovieDetails = { movieId ->
                    navController.navigate(NavigationRoutes.movieDetails(movieId))
                }
            )
        }

        // Movie Details
        composable(NavigationRoutes.MOVIE_DETAILS) { backStackEntry ->
            val movieId = backStackEntry.arguments?.getString("movieId") ?: ""
            MovieDetailsScreen(
                movieId = movieId,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}

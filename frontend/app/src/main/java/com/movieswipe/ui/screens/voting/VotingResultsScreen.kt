package com.movieswipe.ui.screens.voting

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun VotingResultsScreen(
    sessionId: String,
    onNavigateToGroups: () -> Unit,
    onNavigateToMovieDetails: (String) -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text("Voting Results Screen - To be implemented")
    }
}
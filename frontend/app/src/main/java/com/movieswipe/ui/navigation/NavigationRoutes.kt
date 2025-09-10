package com.movieswipe.ui.navigation

object NavigationRoutes {
    const val LOGIN = "login"
    const val PROFILE = "profile"
    const val GROUPS = "groups"
    const val GROUP_DETAILS = "group_details/{groupId}"
    const val CREATE_GROUP = "create_group"
    const val JOIN_GROUP = "join_group"
    const val VOTING_SESSION = "voting_session/{sessionId}"
    const val VOTING_RESULTS = "voting_results/{sessionId}"
    const val MOVIE_DETAILS = "movie_details/{movieId}"
    
    // Function to create route with parameters
    fun groupDetails(groupId: String) = "group_details/$groupId"
    fun votingSession(sessionId: String) = "voting_session/$sessionId"
    fun votingResults(sessionId: String) = "voting_results/$sessionId"
    fun movieDetails(movieId: String) = "movie_details/$movieId"
}

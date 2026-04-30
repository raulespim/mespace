package com.espimsystems.mespace.app.navigation

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList

class AppNavigator(
    initialRoute: AppRoute = AppRoute.Welcome,
) {

    val backStack: SnapshotStateList<AppRoute> = mutableStateListOf(initialRoute)

    val canNavigateBack: Boolean
        get() = backStack.size > 1

    fun navigateToSpaces() {
        navigateTo(AppRoute.Spaces)
    }

    fun navigateToTasks(
        spaceId: String,
        spaceName: String,
    ) {
        navigateTo(
            AppRoute.Tasks(
                spaceId = spaceId,
                spaceName = spaceName,
            ),
        )
    }

    fun navigateBack(): Boolean {
        if (!canNavigateBack) return false

        backStack.removeLastOrNull()
        return true
    }

    private fun navigateTo(route: AppRoute) {
        val currentRoute = backStack.lastOrNull()

        if (currentRoute == route) return

        backStack.add(route)
    }
}
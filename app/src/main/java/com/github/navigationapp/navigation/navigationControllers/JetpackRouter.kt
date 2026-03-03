package com.github.navigationapp.navigation.navigationControllers

import androidx.navigation.NavController
import com.github.navigationapp.R

class JetpackRouter(
    private val navController: NavController,
    private val removeNavHost: () -> Unit,
) : NavigationRouter {

    override fun navigateTo(key: ScreenKey) {
        navController.navigate(
            resId = key.toDestinationId(),
            args = key.toBundle(),
        )
    }

    override fun back(): Boolean = navController.popBackStack()

    override fun clearContainer() = removeNavHost()
}

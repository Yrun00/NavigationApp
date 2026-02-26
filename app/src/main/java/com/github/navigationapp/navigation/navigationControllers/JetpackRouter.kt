package com.github.navigationapp.navigation.navigationControllers

import android.annotation.SuppressLint
import androidx.navigation.NavController
import com.github.navigationapp.R


class JetpackRouter(
    private val navController: NavController,
) : NavigationRouter {

    override fun navigateTo(key: ScreenKey) {
        navController.navigate(
            resId = key.toDestinationId(),
            args = key.toBundle(),
        )
    }

    override fun back(): Boolean {
        return navController.popBackStack()
    }

    @SuppressLint("RestrictedApi")
    override fun getScreenBBackstackDepth(): Int {
        val count = navController.currentBackStack.value
            .count { it.destination.id == R.id.screenBFragment }
        return (count - 1).coerceAtLeast(0)
    }

    override fun replay(stack: List<ScreenKey>) {
        stack.drop(1).forEach { key -> navigateTo(key) }
    }

    override fun clear() {
        navController.popBackStack(
            destinationId = R.id.screenAFragment,
            inclusive = false,
        )
    }
}
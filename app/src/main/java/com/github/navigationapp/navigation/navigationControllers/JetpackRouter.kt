package com.github.navigationapp.navigation.navigationControllers

import android.annotation.SuppressLint
import androidx.navigation.NavController
import com.github.navigationapp.R


class JetpackRouter(
    private val navController: NavController
) : NavigationRouter {

    override fun navigateTo(key: ScreenKey) {
        navController.navigate(
            resId = key.toDestinationId(),
            args = key.toBundle()
        )
    }

    override fun back(): Boolean {
        return navController.popBackStack()
    }

    override fun clear() {
        navController.popBackStack(
            destinationId = R.id.screenAFragment,
            inclusive = false
        )
    }
}

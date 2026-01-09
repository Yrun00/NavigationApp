package com.github.navigationapp

import android.annotation.SuppressLint
import androidx.navigation.NavController
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

interface NavigationManager {
    fun navigateTo(screen: Screen)
    fun goBack()
    fun getBackStackDepth(): Flow<Int>

    class JetpackNavigationManager(private val navController: NavController) : NavigationManager {
        override fun navigateTo(screen: Screen) {
            when (screen) {
                Screen.ScreenA -> navController.navigate(R.id.screenA)
                Screen.ScreenB -> navController.navigate(R.id.screenB)
                Screen.ScreenC -> navController.navigate(R.id.screenC)
            }
        }

        override fun goBack() {
            navController.popBackStack()
        }

        override fun getBackStackDepth(): Flow<Int> {
            return navController.currentBackStack.map { it.size }
        }

    }
}
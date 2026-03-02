package com.github.navigationapp.navigation

import com.github.navigationapp.navigation.navigationControllers.ScreenKey
import kotlinx.coroutines.flow.StateFlow

interface NavigationHost {
    fun navigateTo(key: ScreenKey)
    fun observeBackStackDepth(): StateFlow<Int>
}
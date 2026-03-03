package com.github.navigationapp.navigation

import kotlinx.coroutines.flow.StateFlow

interface NavigationHost {
    fun navigateTo(key: ScreenKey)
    fun observeBackStackDepth(): StateFlow<Int>
}
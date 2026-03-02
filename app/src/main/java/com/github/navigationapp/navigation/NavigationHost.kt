package com.github.navigationapp.navigation

import com.github.navigationapp.navigation.navigationControllers.ScreenKey

interface NavigationHost {
    fun navigateTo(key: ScreenKey)
}
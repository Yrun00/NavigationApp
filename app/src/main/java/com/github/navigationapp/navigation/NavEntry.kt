package com.github.navigationapp.navigation

import com.github.navigationapp.navigation.navigationControllers.ScreenKey

data class NavEntry(
    val key: ScreenKey,
    val method: NavigationMethod
)

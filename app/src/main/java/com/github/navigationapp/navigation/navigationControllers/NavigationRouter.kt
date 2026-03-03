package com.github.navigationapp.navigation.navigationControllers

import com.github.navigationapp.navigation.ScreenKey

interface NavigationRouter {
    fun navigateTo(key: ScreenKey)
    fun back(): Boolean
    fun attach() {}
    fun detach() {}
    fun clearContainer()
}

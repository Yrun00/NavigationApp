package com.github.navigationapp.navigation.navigationControllers


interface NavigationRouter {
    fun navigateTo(key: ScreenKey)
    fun back(): Boolean
    fun clear()
}

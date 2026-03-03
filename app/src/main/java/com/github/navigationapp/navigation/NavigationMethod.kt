package com.github.navigationapp.navigation

enum class NavigationMethod {
    FRAGMENT_MANAGER,
    JETPACK,
    CICERONE,
    SIMPLE_STACK;

    val needToPlaceInitialScreen: Boolean
        get() = this == FRAGMENT_MANAGER || this == CICERONE
}
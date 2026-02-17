package com.github.navigationapp.navigation

enum class NavigationType {
    FRAGMENT_MANAGER,
    JETPACK,
    CICERONE,
//    CONDUCTOR
}

sealed interface Screen {
    val tag: String

    data class ScreenA(
        val isNested: Boolean = false, override val tag: String = "ScreenA",
    ) : Screen

    data class ScreenB(
        val recursionDepth: Int = 0, override val tag: String = "ScreenB",
    ) : Screen

    object ScreenC : Screen {
        override val tag: String
            get() = "ScreenC"
    }
}

interface NestedNavigationCallback {
    fun onNestedScreenClosed()
}

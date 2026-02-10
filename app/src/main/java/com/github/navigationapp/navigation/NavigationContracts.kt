package com.github.navigationapp.navigation

enum class NavigationType {
    FRAGMENT_MANAGER,
    JETPACK,
    CICERONE,
    CONDUCTOR
}

/**
 * Базовый интерфейс для всех экранов приложения
 */
sealed interface Screen {

    /**
     * Экран A - главный экран с селектором навигации
     * @param isNested - флаг, указывающий что это вложенный экземпляр внутри FragmentC
     */
    data class ScreenA(
        val isNested: Boolean = false,
    ) : Screen

    /**
     * Экран B - рекурсивный экран
     * @param recursionDepth - глубина рекурсии, передаваемая через Bundle
     */
    data class ScreenB(
        val recursionDepth: Int = 0,
    ) : Screen

    /**
     * Экран C - экран с вложенным контейнером для ScreenA
     */
    object ScreenC : Screen
}

/**
 * Callback для уведомления о закрытии вложенного экрана
 */
interface NestedNavigationCallback {
    fun onNestedScreenClosed()
}

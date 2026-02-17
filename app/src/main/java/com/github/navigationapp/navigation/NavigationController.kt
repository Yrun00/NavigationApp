package com.github.navigationapp.navigation

/**
 * Интерфейс для управления навигацией
 * Реализуется для каждого типа навигации
 */
interface NavigationController {

    fun navigateTo(screen: Screen)

    fun goBack(): Boolean

    fun getBackStackSize(): Int
}
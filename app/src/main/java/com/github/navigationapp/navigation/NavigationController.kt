package com.github.navigationapp.navigation

/**
 * Интерфейс для управления навигацией
 * Реализуется для каждого типа навигации
 */
interface NavigationController {
    /**
     * Переход на указанный экран
     */
    fun navigateTo(screen: Screen)

    /**
     * Возврат на предыдущий экран
     * @return true если возврат выполнен, false если backstack пуст
     */
    fun goBack(): Boolean

    /**
     * Получение размера backstack
     * Используется для вычисления глубины рекурсии
     */
    fun getBackStackSize(): Int

    /**
     * Очистка backstack
     * Используется при смене типа навигации
     */
    fun clearBackStack()

    /**
     * Освобождение ресурсов
     */
    fun dispose()
}
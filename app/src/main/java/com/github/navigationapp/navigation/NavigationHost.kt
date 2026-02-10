package com.github.navigationapp.navigation

import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner

/**
 * NavigationHost - управляет навигацией внутри фрагмента
 * 
 * Ключевые возможности:
 * - Создание и управление NavigationController
 * - Смена типа навигации динамически
 * - Обработка системной кнопки "Назад"
 * - Изоляция от других NavigationHost'ов
 */
class NavigationHost(
    private val fragment: Fragment,
    @IdRes private val containerId: Int,
    private val factory: NavigationControllerFactory,
    private val onEmptyBackStack: () -> Unit = {}
) {
    private var currentController: NavigationController? = null
    private var currentType: NavigationType = NavigationType.JETPACK
    
    init {
        // Инициализируем с типом по умолчанию
        switchNavigationType(NavigationType.JETPACK)
        
        // Следим за жизненным циклом фрагмента
        fragment.lifecycle.addObserver(object : LifecycleEventObserver {
            override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
                if (event == Lifecycle.Event.ON_DESTROY) {
                    dispose()
                }
            }
        })
    }
    
    /**
     * Переключение типа навигации
     * Очищает текущий backstack и создает новый контроллер
     */
    fun switchNavigationType(type: NavigationType) {
        if (currentType == type && currentController != null) {
            return // Уже используется этот тип
        }
        
        // Очищаем старый контроллер
        currentController?.let {
            it.clearBackStack()
            it.dispose()
        }
        
        currentType = type
        currentController = factory.createController(
            type = type,
            fragment = fragment,
            containerId = containerId
        )
    }
    
    /**
     * Навигация на экран
     */
    fun navigateTo(screen: Screen) {
        currentController?.navigateTo(screen)
    }
    
    /**
     * Возврат на предыдущий экран
     * Если backstack пуст, вызывает onEmptyBackStack callback
     */
    fun goBack(): Boolean {
        val result = currentController?.goBack() ?: false
        if (!result) {
            onEmptyBackStack()
        }
        return result
    }
    
    /**
     * Получение размера backstack
     */
    fun getBackStackSize(): Int {
        return currentController?.getBackStackSize() ?: 0
    }
    
    /**
     * Получение текущего типа навигации
     */
    fun getCurrentType(): NavigationType = currentType
    
    /**
     * Освобождение ресурсов
     */
    private fun dispose() {
        currentController?.dispose()
        currentController = null
    }
}

/**
 * Фабрика для создания NavigationController разных типов
 */
class NavigationControllerFactory(
    private val fragmentFactory: FragmentFactory
) {
    
    fun createController(
        type: NavigationType,
        fragment: Fragment,
        @IdRes containerId: Int
    ): NavigationController {
        return when (type) {
            NavigationType.FRAGMENT_MANAGER -> {
                FragmentManagerNavigationController(
                    fragmentManager = fragment.childFragmentManager,
                    containerId = containerId,
                    fragmentFactory = fragmentFactory
                )
            }
            NavigationType.JETPACK -> {
                JetpackNavigationController(
                    fragment = fragment,
                    containerId = containerId,
                    fragmentFactory = fragmentFactory
                )
            }
            NavigationType.CICERONE -> {
                CiceroneNavigationController(
                    fragment = fragment,
                    containerId = containerId,
                    fragmentFactory = fragmentFactory
                )
            }
            NavigationType.CONDUCTOR -> {
                ConductorNavigationController(
                    fragment = fragment,
                    containerId = containerId,
                    fragmentFactory = fragmentFactory
                )
            }
        }
    }
}

/**
 * Интерфейс для создания фрагментов по Screen
 */
interface FragmentFactory {
    fun createFragment(screen: Screen): Fragment
}

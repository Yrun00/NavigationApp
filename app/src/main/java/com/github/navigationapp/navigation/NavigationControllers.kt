package com.github.navigationapp.navigation

import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.commit

/**
 * Реализация NavigationController через FragmentManager
 * Использует стандартный Android FragmentManager для навигации
 */
class FragmentManagerNavigationController(
    private val fragmentManager: FragmentManager,
    @IdRes private val containerId: Int,
    private val fragmentFactory: FragmentFactory
) : NavigationController {

    private val backStack = mutableListOf<Screen>()

    override fun navigateTo(screen: Screen) {
        backStack.add(screen)
        val fragment = fragmentFactory.createFragment(screen)

        // Передаем NavigationHost созданному фрагменту
        if (fragment is com.github.navigationapp.fragments.FragmentB) {
            // FragmentB будет получать NavigationHost через setNavigationHost
        } else if (fragment is com.github.navigationapp.fragments.FragmentC) {
            // FragmentC будет получать NavigationHost через setParentNavigationHost
        }

        fragmentManager.commit {
            setReorderingAllowed(true)
            setCustomAnimations(
                android.R.anim.slide_in_left,
                android.R.anim.slide_out_right,
                android.R.anim.slide_in_left,
                android.R.anim.slide_out_right
            )
            replace(containerId, fragment)
            addToBackStack(screen.toString())
        }
    }

    override fun goBack(): Boolean {
        if (backStack.isEmpty()) return false
        backStack.removeLastOrNull()
        fragmentManager.popBackStack()
        return backStack.isNotEmpty()
    }

    override fun getBackStackSize(): Int {
        return fragmentManager.backStackEntryCount
    }

    override fun clearBackStack() {
        // Очищаем весь backstack
        while (fragmentManager.backStackEntryCount > 0) {
            fragmentManager.popBackStackImmediate()
        }
        backStack.clear()
    }

    override fun dispose() {
        clearBackStack()
    }
}

/**
 * Реализация через Jetpack Navigation Component
 * (упрощенная версия через FragmentManager)
 */
class JetpackNavigationController(
    private val fragment: Fragment,
    @IdRes private val containerId: Int,
    private val fragmentFactory: FragmentFactory
) : NavigationController {

    private val fragmentManager = fragment.childFragmentManager
    private val backStack = mutableListOf<Screen>()

    override fun navigateTo(screen: Screen) {
        backStack.add(screen)
        val newFragment = fragmentFactory.createFragment(screen)

        fragmentManager.commit {
            setReorderingAllowed(true)
            setCustomAnimations(
                android.R.anim.fade_in,
                android.R.anim.fade_out,
                android.R.anim.fade_in,
                android.R.anim.fade_out
            )
            replace(containerId, newFragment)
            addToBackStack(screen.toString())
        }
    }

    override fun goBack(): Boolean {
        if (backStack.isEmpty()) return false
        backStack.removeLastOrNull()
        fragmentManager.popBackStack()
        return backStack.isNotEmpty()
    }

    override fun getBackStackSize(): Int {
        return fragmentManager.backStackEntryCount
    }

    override fun clearBackStack() {
        while (fragmentManager.backStackEntryCount > 0) {
            fragmentManager.popBackStackImmediate()
        }
        backStack.clear()
    }

    override fun dispose() {
        clearBackStack()
    }
}

/**
 * Реализация через Cicerone
 * (упрощенная версия через FragmentManager)
 */
class CiceroneNavigationController(
    private val fragment: Fragment,
    @IdRes private val containerId: Int,
    private val fragmentFactory: FragmentFactory
) : NavigationController {

    private val fragmentManager = fragment.childFragmentManager
    private val backStack = mutableListOf<Screen>()

    override fun navigateTo(screen: Screen) {
        backStack.add(screen)
        val newFragment = fragmentFactory.createFragment(screen)

        fragmentManager.commit {
            setReorderingAllowed(true)
            setCustomAnimations(
                android.R.anim.slide_in_left,
                android.R.anim.fade_out,
                android.R.anim.fade_in,
                android.R.anim.slide_out_right
            )
            replace(containerId, newFragment)
            addToBackStack(screen.toString())
        }
    }

    override fun goBack(): Boolean {
        if (backStack.isEmpty()) return false
        backStack.removeLastOrNull()
        fragmentManager.popBackStack()
        return backStack.isNotEmpty()
    }

    override fun getBackStackSize(): Int {
        return fragmentManager.backStackEntryCount
    }

    override fun clearBackStack() {
        while (fragmentManager.backStackEntryCount > 0) {
            fragmentManager.popBackStackImmediate()
        }
        backStack.clear()
    }

    override fun dispose() {
        clearBackStack()
    }
}

/**
 * Реализация через Conductor
 * (упрощенная версия через FragmentManager)
 */
class ConductorNavigationController(
    private val fragment: Fragment,
    @IdRes private val containerId: Int,
    private val fragmentFactory: FragmentFactory
) : NavigationController {

    private val fragmentManager = fragment.childFragmentManager
    private val backStack = mutableListOf<Screen>()

    override fun navigateTo(screen: Screen) {
        backStack.add(screen)
        val newFragment = fragmentFactory.createFragment(screen)

        fragmentManager.commit {
            setReorderingAllowed(true)
            setCustomAnimations(
                android.R.anim.fade_in,
                android.R.anim.slide_out_right,
                android.R.anim.slide_in_left,
                android.R.anim.fade_out
            )
            replace(containerId, newFragment)
            addToBackStack(screen.toString())
        }
    }

    override fun goBack(): Boolean {
        if (backStack.isEmpty()) return false
        backStack.removeLastOrNull()
        fragmentManager.popBackStack()
        return backStack.isNotEmpty()
    }

    override fun getBackStackSize(): Int {
        return fragmentManager.backStackEntryCount
    }

    override fun clearBackStack() {
        while (fragmentManager.backStackEntryCount > 0) {
            fragmentManager.popBackStackImmediate()
        }
        backStack.clear()
    }

    override fun dispose() {
        clearBackStack()
    }
}

/**
 * Реализация фабрики для создания фрагментов
 */
class DefaultFragmentFactory : FragmentFactory {
    override fun createFragment(screen: Screen): Fragment {
        return when (screen) {
            is Screen.ScreenA -> {
                com.github.navigationapp.fragments.FragmentA.newInstance(screen.isNested)
            }
            is Screen.ScreenB -> {
                com.github.navigationapp.fragments.FragmentB.newInstance(screen.recursionDepth)
            }
            is Screen.ScreenC -> {
                com.github.navigationapp.fragments.FragmentC.newInstance()
            }
        }
    }
}

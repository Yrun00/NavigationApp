package com.github.navigationapp.navigation.NavigationControllers

import androidx.annotation.IdRes
import androidx.fragment.app.FragmentManager
import com.github.navigationapp.navigation.FragmentFactory
import com.github.navigationapp.navigation.NavigationController
import com.github.navigationapp.navigation.Screen

/**
 * Реализация NavigationController через FragmentManager
 * Использует стандартный Android FragmentManager для навигации
 */
class FragmentManagerNavigationController(
    private val fragmentManager: FragmentManager,
    @IdRes private val containerId: Int,
    private val fragmentFactory: FragmentFactory,
) : NavigationController {

    override fun navigateTo(screen: Screen) {
        val fragment = fragmentFactory.createFragment(screen)

        fragmentManager.beginTransaction()
            .replace(containerId, fragment, screen.tag)
            .addToBackStack(screen.tag)
            .commit()
    }

    override fun goBack(): Boolean {
        return if (fragmentManager.backStackEntryCount > 0) {
            fragmentManager.popBackStack()
            true
        } else {
            false
        }
    }

    override fun getBackStackSize(): Int {
        var depth = 0
        val total = fragmentManager.backStackEntryCount

        for (i in total - 1 downTo 0) {
            val entry = fragmentManager.getBackStackEntryAt(i)
            val isB = entry.name == Screen.ScreenB().tag
            if (isB) {
                depth++
            } else {
                break
            }
        }
        return depth-1
    }
}
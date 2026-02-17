package com.github.navigationapp.navigation.NavigationControllers

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.github.navigationapp.navigation.DefaultFragmentFactory
import com.github.navigationapp.navigation.FragmentFactory
import com.github.navigationapp.navigation.NavigationController
import com.github.navigationapp.navigation.Screen
import com.github.terrakok.cicerone.Router
import com.github.terrakok.cicerone.androidx.FragmentScreen

class CiceroneNavigationController(
    private val router: Router,
    private val fragmentManager: FragmentManager,
    private val fragmentFactory: FragmentFactory,
) : NavigationController {

    private var bbackStackSize: Int = 0

    override fun navigateTo(screen: Screen) {
        val ciceroneScreen = AppFragmentScreen(screen, fragmentFactory)
        router.navigateTo(ciceroneScreen)
        if (screen.tag == Screen.ScreenB().tag) {
            bbackStackSize++
        }// Cicerone сам дернёт createFragment() у экрана [web:9][web:88]
    }

    override fun goBack(): Boolean {
        return if (fragmentManager.backStackEntryCount > 0) {
            router.exit()
            if (bbackStackSize != 0) {
                bbackStackSize--
            }// превратится в команду Back и уйдёт в Navigator [web:88][web:90]
            true
        } else {
            false
        }
    }

    override fun getBackStackSize(): Int {
        return bbackStackSize - 1
    }
}


class AppFragmentScreen(
    private val appScreen: Screen,
    private val fragmentFactory: FragmentFactory,
) : FragmentScreen {

    override fun createFragment(factory: androidx.fragment.app.FragmentFactory): Fragment {
        return fragmentFactory.createFragment(appScreen)
    }
}
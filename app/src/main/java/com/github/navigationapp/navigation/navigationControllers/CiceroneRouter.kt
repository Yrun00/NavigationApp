package com.github.navigationapp.navigation.navigationControllers

import androidx.fragment.app.FragmentManager
import com.github.navigationapp.navigation.ScreenKey
import com.github.terrakok.cicerone.NavigatorHolder
import com.github.terrakok.cicerone.Router
import com.github.terrakok.cicerone.androidx.AppNavigator

class CiceroneRouter(
    private val router: Router,
    private val fragmentManager: FragmentManager,
    private val navigatorHolder: NavigatorHolder,
    private val getNavigator: () -> AppNavigator,
) : NavigationRouter {

    override fun navigateTo(key: ScreenKey) {
        router.navigateTo(key.toCiceroneScreen())
    }

    override fun back(): Boolean {
        if (fragmentManager.backStackEntryCount == 0) return false
        router.exit()
        return true
    }

    override fun attach() = navigatorHolder.setNavigator(getNavigator())

    override fun detach() = navigatorHolder.removeNavigator()

    override fun clearContainer() {
        fragmentManager.popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
    }
}

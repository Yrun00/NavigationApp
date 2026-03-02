package com.github.navigationapp.navigation.navigationControllers

import android.util.Log
import androidx.fragment.app.FragmentManager
import com.github.terrakok.cicerone.Router

class CiceroneRouter(
    private val router: Router,
    private val fragmentManager: FragmentManager
) : NavigationRouter {

    override fun navigateTo(key: ScreenKey) {
        Log.d("NAV", "CiceroneRouter.navigateTo: $key, navigatorHolder: ${this.router}")
        router.navigateTo(key.toCiceroneScreen())
    }

    override fun back(): Boolean {
        if (fragmentManager.backStackEntryCount == 0) return false
        router.exit()
        return true
    }

    override fun clear() {
        fragmentManager.popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
    }
}

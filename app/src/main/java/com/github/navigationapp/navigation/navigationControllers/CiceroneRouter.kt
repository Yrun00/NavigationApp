package com.github.navigationapp.navigation.navigationControllers

import androidx.fragment.app.FragmentManager
import com.github.navigationapp.navigation.navigationControllers.ScreenKey.C.toCiceroneScreen
import com.github.terrakok.cicerone.Router

class CiceroneRouter(
    private val router: Router,
    private val fragmentManager: FragmentManager
) : NavigationRouter {

    override fun navigateTo(key: ScreenKey) {
        router.navigateTo(key.toCiceroneScreen())
    }

    override fun back(): Boolean {
        if (fragmentManager.backStackEntryCount == 0) return false
        router.exit()
        return true
    }

    override fun getScreenBBackstackDepth(): Int {
        var count = 0
        for (i in 0 until fragmentManager.backStackEntryCount) {
            val tag = fragmentManager.getBackStackEntryAt(i).name
            if (tag?.startsWith("screen_b_") == true) count++
        }
        return (count - 1).coerceAtLeast(0)
    }

    override fun replay(stack: List<ScreenKey>) {
        clear()
        stack.drop(1).forEach { key -> navigateTo(key) }
    }

    override fun clear() {
        fragmentManager.popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
    }
}
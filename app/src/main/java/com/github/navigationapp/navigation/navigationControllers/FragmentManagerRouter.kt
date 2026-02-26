package com.github.navigationapp.navigation.navigationControllers

import androidx.annotation.IdRes
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.commit


// navigation/FragmentManagerRouter.kt
class FragmentManagerRouter(
    private val fragmentManager: FragmentManager,
    @IdRes private val containerId: Int
) : NavigationRouter {

    override fun navigateTo(key: ScreenKey) {
        val fragment = key.instantiateFragment()
        fragmentManager.commit {
            setReorderingAllowed(true)
            replace(containerId, fragment, key.tag())
            addToBackStack(key.tag())
        }
    }

    override fun back(): Boolean {
        if (fragmentManager.backStackEntryCount == 0) return false
        fragmentManager.popBackStack()
        return true
    }

    override fun getScreenBBackstackDepth(): Int {
        var count = 0
        for (i in 0 until fragmentManager.backStackEntryCount) {
            val tag = fragmentManager.getBackStackEntryAt(i).name
            if (tag?.startsWith("screen_b_") == true) count++
        }
        return (count - 1)
    }

    override fun replay(stack: List<ScreenKey>) {
        clear()
        stack.drop(1).forEach { key -> navigateTo(key) }
    }

    override fun clear() {
        fragmentManager.popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
    }
}

package com.github.navigationapp.navigation.navigationControllers

import androidx.annotation.IdRes
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.commit


class FragmentManagerRouter(
    private val fragmentManager: FragmentManager,
    @IdRes private val containerId: Int
) : NavigationRouter {

    override fun navigateTo(key: ScreenKey) {
        fragmentManager.commit {
            setReorderingAllowed(true)
            replace(containerId, key.instantiateFragment(), key.tag())
            addToBackStack(key.tag())
        }
    }

    override fun back(): Boolean {
        if (fragmentManager.backStackEntryCount == 0) return false
        fragmentManager.popBackStack()
        return true
    }

    override fun clear() {
        fragmentManager.popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
    }
}

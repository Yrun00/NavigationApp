package com.github.navigationapp.navigation.navigationControllers

import androidx.annotation.IdRes
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.commit
import androidx.fragment.app.commitNow
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.github.navigationapp.NavigationState
import com.github.navigationapp.R
import com.github.navigationapp.navigation.NavigationMethod

class RouterFactory(
    private val fragmentManager: FragmentManager,
    @IdRes private val containerId: Int,
    private val level: Int,
    private val state: NavigationState,
) {
    private val navHostTag = "nav_host_$level"

    fun create(method: NavigationMethod): NavigationRouter = when (method) {
        NavigationMethod.FRAGMENT_MANAGER -> FragmentManagerRouter(
            fragmentManager = fragmentManager,
            containerId = containerId,
        )

        NavigationMethod.CICERONE -> CiceroneRouter(
            router = state.ciceroneRouter,
            fragmentManager = fragmentManager,
        )

        NavigationMethod.JETPACK -> {
            ensureNavHostFragment()
            JetpackRouter(
                navController = requireNavHostFragment().navController,
            )
        }

        NavigationMethod.SIMPLE_STACK -> SimpleStackRouter(state.simpleBackstack, level = level)
    }

    fun getNavController(): NavController? =
        (fragmentManager.findFragmentByTag(navHostTag) as? NavHostFragment)?.navController
    fun removeNavHostIfPresent() {
        fragmentManager.findFragmentByTag(navHostTag)?.let { navHost ->
            fragmentManager.commit {
                remove(navHost)
            }
            fragmentManager.executePendingTransactions()
        }
    }

    private fun ensureNavHostFragment() {
        if (fragmentManager.findFragmentByTag(navHostTag) != null) return
        val startArgs = ScreenKey.A(nestingLevel = level).toBundle()
        val navHostFragment = NavHostFragment.create(R.navigation.nav_graph, startArgs)
        fragmentManager.commitNow {
            setReorderingAllowed(true)
            replace(containerId, navHostFragment, navHostTag)
        }
    }

    private fun requireNavHostFragment(): NavHostFragment =
        fragmentManager.findFragmentByTag(navHostTag) as NavHostFragment
}


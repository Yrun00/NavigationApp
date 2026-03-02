package com.github.navigationapp.navigation.navigationControllers

import androidx.annotation.IdRes
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.commit
import androidx.navigation.fragment.NavHostFragment
import com.github.navigationapp.NavigationState
import com.github.navigationapp.R
import com.github.navigationapp.navigation.NavigationMethod

class RouterFactory(
    private val fragmentManager: FragmentManager,
    @IdRes private val containerId: Int,
    private val level: Int,           // нужен для уникального тега NavHost
    private val state: NavigationState,
) {
    // Тег NavHostFragment уникален на каждый уровень
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
        val navHostFragment = NavHostFragment.create(R.navigation.nav_graph)
        fragmentManager.commit {
            replace(containerId, navHostFragment, navHostTag)
        }
        fragmentManager.executePendingTransactions()
    }

    private fun requireNavHostFragment(): NavHostFragment =
        fragmentManager.findFragmentByTag(navHostTag) as NavHostFragment
}


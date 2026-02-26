package com.github.navigationapp.navigation.navigationControllers

import androidx.annotation.IdRes
import androidx.fragment.app.FragmentManager
import androidx.navigation.fragment.NavHostFragment
import com.github.navigationapp.NavigationViewModel
import com.github.navigationapp.navigation.NavigationMethod
import com.zhuinden.simplestack.Backstack

class RouterFactory(
    private val fragmentManager: FragmentManager,
    @IdRes private val containerId: Int,
    private val viewModel: NavigationViewModel,
    private val backstack: Backstack
) {

    fun create(method: NavigationMethod): NavigationRouter = when (method) {
        NavigationMethod.FRAGMENT_MANAGER -> FragmentManagerRouter(
            fragmentManager = fragmentManager,
            containerId = containerId
        )
        NavigationMethod.JETPACK -> createJetpack(fragmentManager)
        NavigationMethod.CICERONE -> CiceroneRouter(
            router = viewModel.ciceroneRouter,
            fragmentManager = fragmentManager
        )
        NavigationMethod.SIMPLE_STACK -> SimpleStackRouter(
            backstack = backstack
        )
    }

    fun createNested(method: NavigationMethod): NavigationRouter = when (method) {
        NavigationMethod.FRAGMENT_MANAGER -> FragmentManagerRouter(
            fragmentManager = fragmentManager,
            containerId = containerId
        )
        NavigationMethod.JETPACK -> createJetpack(fragmentManager)
        NavigationMethod.CICERONE -> CiceroneRouter(
            router = viewModel.nestedCiceroneRouter,
            fragmentManager = fragmentManager
        )
        NavigationMethod.SIMPLE_STACK -> SimpleStackRouter(
            backstack = backstack
        )
    }

    private fun createJetpack(fragmentManager: FragmentManager): JetpackRouter {
        fragmentManager.executePendingTransactions()
        val navHostFragment = fragmentManager
            .findFragmentByTag("nav_host") as NavHostFragment
        return JetpackRouter(navHostFragment.navController)
    }
}

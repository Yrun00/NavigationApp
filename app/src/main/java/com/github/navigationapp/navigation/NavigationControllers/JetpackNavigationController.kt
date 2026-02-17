package com.github.navigationapp.navigation.NavigationControllers

import android.content.Context
import androidx.navigation.NavController
import androidx.navigation.NavGraph
import androidx.navigation.NavGraphNavigator
import androidx.navigation.fragment.FragmentNavigator
import com.github.navigationapp.R
import com.github.navigationapp.fragments.FragmentA
import com.github.navigationapp.fragments.FragmentB
import com.github.navigationapp.fragments.FragmentC
import com.github.navigationapp.navigation.NavigationController
import com.github.navigationapp.navigation.Screen


class JetpackNavigationController(
    private val navController: NavController,
) : NavigationController {

    private var bbackStackSize: Int = 0

    override fun navigateTo(screen: Screen) {
        val route = screen.tag // используем tag как route
        if (route == Screen.ScreenB().tag) {
            bbackStackSize++
        }
        navController.navigate(route) // [web:61][web:63]
    }

    override fun goBack(): Boolean {
        if (bbackStackSize != 0) {
            bbackStackSize--
        }
        return navController.popBackStack()
    }

    override fun getBackStackSize(): Int {
        return bbackStackSize - 1
    }



}


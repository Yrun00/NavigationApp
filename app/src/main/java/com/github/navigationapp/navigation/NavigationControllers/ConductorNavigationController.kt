package com.github.navigationapp.navigation.NavigationControllers

import android.R
import android.os.Bundle
import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import com.bluelinelabs.conductor.Controller
import com.bluelinelabs.conductor.Router
import com.bluelinelabs.conductor.RouterTransaction
import com.github.navigationapp.navigation.FragmentFactory
import com.github.navigationapp.navigation.NavigationController
import com.github.navigationapp.navigation.Screen

//class ConductorNavigationController(
//    private val router: Router
//) : NavigationController {
//
//    override fun navigateTo(screen: Screen) {
//        val controller = screen.toController()
//        val transaction = RouterTransaction.with(controller)
//        // по желанию можно задать анимации и т.п.
//        router.pushController(transaction) // push в backstack Conductor [web:13][web:116]
//    }
//
//    override fun goBack(): Boolean {
//        // handleBack() сам смотрит, есть ли что попать и возвращает true/false [web:114][web:117]
//        return router.handleBack()
//    }
//
//    override fun getBackStackSize(): Int {
//        // у Router есть backstack: List<RouterTransaction> [web:111][web:117]
//        return router.backstackSize
//    }
//    fun Screen.toController(): Controller {
//        val bundle = Bundle()
//        bundle.putBoolean("isNested",true)
//
//        return when (this) {
//            is Screen.ScreenA -> AController(bundle)
//            is Screen.ScreenB -> BController(recursionDepth = recursionDepth)
//            Screen.ScreenC -> CController()
//        }
//    }
//}

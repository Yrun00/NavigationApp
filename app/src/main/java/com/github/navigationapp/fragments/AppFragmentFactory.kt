package com.github.navigationapp.fragments

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentFactory

// navigation/AppFragmentFactory.kt
class AppFragmentFactory : FragmentFactory() {

    override fun instantiate(classLoader: ClassLoader, className: String): Fragment {
        return when (className) {
            ScreenAFragment::class.java.name -> ScreenAFragment()
            ScreenBFragment::class.java.name -> ScreenBFragment()
            ScreenCFragment::class.java.name -> ScreenCFragment()
            else -> super.instantiate(classLoader, className)
        }
    }
}

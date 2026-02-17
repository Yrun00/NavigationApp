package com.github.navigationapp.navigation

import androidx.fragment.app.Fragment
import com.github.navigationapp.fragments.FragmentA
import com.github.navigationapp.fragments.FragmentB
import com.github.navigationapp.fragments.FragmentC

class DefaultFragmentFactory : FragmentFactory {
    override fun createFragment(screen: Screen): Fragment {
        return when (screen) {
            is Screen.ScreenA -> {
                FragmentA.newInstance(screen.isNested)
            }
            is Screen.ScreenB -> {
                FragmentB.newInstance(screen.recursionDepth)
            }
            is Screen.ScreenC -> {
                FragmentC.newInstance()
            }
        }
    }
}

package com.github.navigationapp.navigation.navigationControllers

import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.zhuinden.simplestack.StateChange
import com.zhuinden.simplestackextensions.fragments.DefaultFragmentStateChanger

class NoAnimFragmentStateChanger(
    fragmentManager: FragmentManager,
    containerId: Int,
) : DefaultFragmentStateChanger(fragmentManager, containerId) {

    override fun onForwardNavigation(ft: FragmentTransaction, stateChange: StateChange) {
        super.onForwardNavigation(
            ft,
            stateChange,
        )
        ft.setCustomAnimations(
            0,
            0,
            0,
            0,
        )
    }

    override fun onBackwardNavigation(ft: FragmentTransaction, stateChange: StateChange) {
        super.onBackwardNavigation(ft, stateChange)
        ft.setCustomAnimations(0, 0, 0, 0)
    }

    override fun onReplaceNavigation(ft: FragmentTransaction, stateChange: StateChange) {
        super.onReplaceNavigation(ft, stateChange)
        ft.setCustomAnimations(0, 0, 0, 0)
        ft.setTransition(FragmentTransaction.TRANSIT_NONE)
    }
}




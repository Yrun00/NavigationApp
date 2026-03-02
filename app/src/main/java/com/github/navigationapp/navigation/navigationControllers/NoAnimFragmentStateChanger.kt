package com.github.navigationapp.navigation.navigationControllers

import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.zhuinden.simplestack.StateChange
import com.zhuinden.simplestackextensions.fragments.DefaultFragmentStateChanger

class NoAnimFragmentStateChanger(
    fragmentManager: FragmentManager,
    containerId: Int
) : DefaultFragmentStateChanger(fragmentManager, containerId) {

    override fun onForwardNavigation(ft: FragmentTransaction, stateChange: StateChange) {
        super.onForwardNavigation(ft, stateChange)   // super: setCustomAnimations(slide_in, slide_out, ...)
        ft.setCustomAnimations(0, 0, 0, 0)           // мы: обнуляем ПОСЛЕ super, ПЕРЕД add/detach/attach
    }

    override fun onBackwardNavigation(ft: FragmentTransaction, stateChange: StateChange) {
        super.onBackwardNavigation(ft, stateChange)
        ft.setCustomAnimations(0, 0, 0, 0)
    }

    override fun onReplaceNavigation(ft: FragmentTransaction, stateChange: StateChange) {
        // super делает setTransition(TRANSIT_FRAGMENT_OPEN=4097)
        // setTransition и setCustomAnimations — разные механизмы, нужно обнулить оба
        super.onReplaceNavigation(ft, stateChange)
        ft.setCustomAnimations(0, 0, 0, 0)
        ft.setTransition(FragmentTransaction.TRANSIT_NONE)
    }
}




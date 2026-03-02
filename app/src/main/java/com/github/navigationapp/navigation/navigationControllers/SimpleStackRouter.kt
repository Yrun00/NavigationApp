package com.github.navigationapp.navigation.navigationControllers

import com.zhuinden.simplestack.Backstack
import com.zhuinden.simplestack.StateChange


class SimpleStackRouter(
    private val backstack: Backstack,
    private val level: Int
) : NavigationRouter {

    override fun navigateTo(key: ScreenKey) {
        backstack.goTo(key)
    }

    override fun back(): Boolean {
        return backstack.goBack()
    }

    override fun clear() {
        backstack.setHistory(
            listOf(ScreenKey.A(nestingLevel = level)),
            StateChange.REPLACE
        )
    }
}

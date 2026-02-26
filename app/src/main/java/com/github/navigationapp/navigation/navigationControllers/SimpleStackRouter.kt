package com.github.navigationapp.navigation.navigationControllers

import com.zhuinden.simplestack.Backstack
import com.zhuinden.simplestack.StateChange


class SimpleStackRouter(
    private val backstack: Backstack,
) : NavigationRouter {

    override fun navigateTo(key: ScreenKey) {
        backstack.goTo(key)
    }

    override fun back(): Boolean {
        return backstack.goBack()
    }

    override fun getScreenBBackstackDepth(): Int {
        val count = backstack.getHistory<ScreenKey>()
            .count { it is ScreenKey.B }
        return (count - 1).coerceAtLeast(0)
    }

    override fun replay(stack: List<ScreenKey>) {
        backstack.setHistory(
            stack,
            StateChange.REPLACE,
        )
    }

    override fun clear() {
        backstack.setHistory(
            listOf(ScreenKey.A),
            StateChange.REPLACE,
        )
    }
}

package com.github.navigationapp

import com.github.navigationapp.navigation.NavigationMethod
import com.github.navigationapp.navigation.ScreenKey
import com.github.terrakok.cicerone.Cicerone
import com.github.terrakok.cicerone.Router
import com.zhuinden.simplestack.Backstack
import com.zhuinden.simplestack.History
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class NavigationState(val level: Int, parentState: NavigationState? = null) {

    private val _stack = MutableStateFlow<List<ScreenKey>>(listOf(ScreenKey.A(level = level)))
    val stack: StateFlow<List<ScreenKey>> = _stack.asStateFlow()

    private val _method = MutableStateFlow(
        parentState?.method?.value ?: NavigationMethod.FRAGMENT_MANAGER,
    )
    val method: StateFlow<NavigationMethod> = _method.asStateFlow()

    val ciceroneRouter: Cicerone<Router> by lazy { Cicerone.create() }

    val simpleBackstack: Backstack by lazy {
        Backstack().apply {
            this.setup(History.single(ScreenKey.A(level = level)))
        }
    }

    fun push(key: ScreenKey) = _stack.update { it + key }

    fun pop(): Boolean {
        if (_stack.value.size <= 1) return false
        _stack.update { it.dropLast(1) }
        return true
    }

    fun switchMethod(newMethod: NavigationMethod) {
        _method.value = newMethod
    }

    fun dispose() {
        if (simpleBackstack.isInitialized) {
            simpleBackstack.detachStateChanger()
        }
    }
}

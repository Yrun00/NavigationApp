package com.github.navigationapp

import com.github.navigationapp.navigation.NavigationMethod
import com.github.navigationapp.navigation.navigationControllers.ScreenKey
import com.github.terrakok.cicerone.Cicerone
import com.github.terrakok.cicerone.Router
import com.zhuinden.simplestack.Backstack
import com.zhuinden.simplestack.History
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class NavigationState(parentState: NavigationState? = null) {

    private val _stack = MutableStateFlow<List<ScreenKey>>(listOf(ScreenKey.A(0)))
    val stack: StateFlow<List<ScreenKey>> = _stack.asStateFlow()

    // Наследуем метод от родительского уровня — пользователю не нужно выбирать заново
    private val _method = MutableStateFlow(
        parentState?.method?.value ?: NavigationMethod.FRAGMENT_MANAGER
    )
    val method: StateFlow<NavigationMethod> = _method.asStateFlow()

    val commands = MutableSharedFlow<ScreenKey>(extraBufferCapacity = 1)

    // Lazy — создаётся только если этот уровень переключится на CICERONE
    val cicerone: Cicerone<Router> by lazy { Cicerone.create() }
    val ciceroneRouter: Router get() = cicerone.router

    // Lazy — создаётся только если этот уровень переключится на SIMPLE_STACK
    // Живёт в ViewModel, а не в Fragment — переживает ротацию без BackstackDelegate
    val simpleBackstack: Backstack by lazy {
        Backstack().also { it.setup(History.single(ScreenKey.A(nestingLevel = 0))) }
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

    fun reset() {
        _stack.value = listOf(ScreenKey.A(nestingLevel = 0))
    }
}

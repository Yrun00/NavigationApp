package com.github.navigationapp

import androidx.lifecycle.ViewModel
import com.github.navigationapp.navigation.NavigationMethod
import com.github.navigationapp.navigation.navigationControllers.ScreenKey
import com.github.terrakok.cicerone.Cicerone
import com.github.terrakok.cicerone.Router
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class NavigationViewModel : ViewModel() {


    private val _stack = MutableStateFlow<List<ScreenKey>>(listOf(ScreenKey.A))
    val stack: StateFlow<List<ScreenKey>> = _stack.asStateFlow()

    private val _method = MutableStateFlow(NavigationMethod.FRAGMENT_MANAGER)
    val method: StateFlow<NavigationMethod> = _method.asStateFlow()

    var needsReplay: Boolean = false
        private set

    fun push(key: ScreenKey) {
        _stack.update { it + key }
    }

    fun pop(): Boolean {
        if (_stack.value.size <= 1) return false
        _stack.update { it.dropLast(1) }
        return true
    }

    fun switchMethod(newMethod: NavigationMethod) {
        if (newMethod == _method.value) return
        if (_stack.value.size > 1) needsReplay = true
        _method.value = newMethod
    }

    fun onReplayDone() {
        needsReplay = false
    }

    // --- Nested navigation (вложенный A внутри Screen C) ---
    val navigationCommands = MutableSharedFlow<ScreenKey>(extraBufferCapacity = 1)
    val nestedNavigationCommands = MutableSharedFlow<ScreenKey>(extraBufferCapacity = 1)

    fun navigateTo(key: ScreenKey) {
        push(key)
        navigationCommands.tryEmit(key)
    }

    fun navigateToNested(key: ScreenKey) {
        pushNested(key)
        nestedNavigationCommands.tryEmit(key)
    }

    private val _nestedStack = MutableStateFlow<List<ScreenKey>>(listOf(ScreenKey.A))
    val nestedStack: StateFlow<List<ScreenKey>> = _nestedStack.asStateFlow()

    private val _nestedMethod = MutableStateFlow(NavigationMethod.FRAGMENT_MANAGER)
    val nestedMethod: StateFlow<NavigationMethod> = _nestedMethod.asStateFlow()

    var nestedNeedsReplay: Boolean = false
        private set

    fun pushNested(key: ScreenKey) {
        _nestedStack.update { it + key }
    }

    fun popNested(): Boolean {
        if (_nestedStack.value.size <= 1) return false
        _nestedStack.update { it.dropLast(1) }
        return true
    }

    fun switchNestedMethod(newMethod: NavigationMethod) {
        if (newMethod == _nestedMethod.value) return
        if (_nestedStack.value.size > 1) nestedNeedsReplay = true
        _nestedMethod.value = newMethod
    }

    fun onNestedReplayDone() {
        nestedNeedsReplay = false
    }

    fun resetNestedStack() {
        _nestedStack.value = listOf(ScreenKey.A)
        nestedNeedsReplay = false
    }

    // --- Cicerone (живёт здесь, переживает ротацию) ---

    val cicerone: Cicerone<Router> = Cicerone.create()
    val ciceroneRouter: Router get() = cicerone.router

    // Отдельный Cicerone для вложенной навигации в Screen C
    val nestedCicerone: Cicerone<Router> = Cicerone.create()
    val nestedCiceroneRouter: Router get() = nestedCicerone.router
}

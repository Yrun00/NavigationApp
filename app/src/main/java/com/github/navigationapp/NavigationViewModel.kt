package com.github.navigationapp

import androidx.lifecycle.ViewModel
import com.github.navigationapp.navigation.NavEntry
import com.github.navigationapp.navigation.NavigationMethod
import com.github.navigationapp.navigation.navigationControllers.ScreenKey

class NavigationViewModel : ViewModel() {

    private val _states = mutableMapOf<Int, NavigationState>()

    init {
        _states[0] = NavigationState(level = 0)
    }

    fun state(level: Int): NavigationState =
        _states.getOrPut(level) {
            val parent = if (level > 0) state(level - 1) else null
            NavigationState(level = level, parentState = parent)
        }

    fun stateOrNull(level: Int): NavigationState? = _states[level]

    private val _replayStack = mutableListOf<NavEntry>()

    var needsReplay: Boolean = false
        private set

    fun entriesForLevel(targetLevel: Int): List<NavEntry> {
        var level = 0
        val result = mutableListOf<NavEntry>()
        for (entry in _replayStack) {
            if (level == targetLevel) result.add(entry)
            if (entry.key is ScreenKey.C) level++
        }
        return result
    }

    fun push(level: Int, key: ScreenKey) {
        val method = state(level).method.value
        state(level).push(key)
        _replayStack.add(NavEntry(key, method))
        updateNeedsReplay()
    }

    fun pop(level: Int): Boolean {
        val result = state(level).pop()
        if (result) {
            _replayStack.removeLastOrNull()
            updateNeedsReplay()
        }
        return result
    }

    fun switchMethod(level: Int, newMethod: NavigationMethod) {
        if (newMethod == state(level).method.value) return
        state(level).switchMethod(newMethod)
    }

    fun closeLevel(level: Int) {
        val parentState = _states[level - 1]
        _states[level]?.dispose()
        _states.remove(level)
        val cutIndex = _replayStack.indexOfLast {
            it.key is ScreenKey.C && (it.key as ScreenKey.C).hostingLevel == level
        }
        if (cutIndex >= 0) {
            _replayStack.subList(cutIndex, _replayStack.size).clear()
        }
        parentState?.pop()
        updateNeedsReplay()
    }


    private fun updateNeedsReplay() {
        needsReplay = _replayStack
            .zipWithNext()
            .any { (a, b) -> a.method != b.method }
    }

    override fun onCleared() {
        _states.values.forEach { it.dispose() }
        super.onCleared()
    }
}



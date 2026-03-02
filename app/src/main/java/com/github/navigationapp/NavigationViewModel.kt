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

    private val _replayStack = mutableListOf<NavEntry>()
    val replayStack: List<NavEntry> get() = _replayStack.toList()

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
        // Сначала закрываем все уровни выше
        val maxLevel = _states.keys.maxOrNull() ?: 0
        for (l in (maxLevel downTo level + 1)) {
            _states[l]?.dispose()
            _states.remove(l)
        }

        // Затем закрываем сам level
        _states[level]?.dispose()
        _states.remove(level)

        // Пересобираем replayStack (как у тебя уже сделано)
        val newStack = mutableListOf<NavEntry>()
        var currentLevel = 0
        for (entry in _replayStack) {
            if (entry.key is ScreenKey.C && currentLevel == level - 1) {
                currentLevel++
                continue
            }
            if (currentLevel >= level) continue
            newStack.add(entry)
            if (entry.key is ScreenKey.C) currentLevel++
        }
        _replayStack.clear()
        _replayStack.addAll(newStack)

        state(level - 1).pop()
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



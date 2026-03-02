package com.github.navigationapp

import androidx.lifecycle.ViewModel
import com.github.navigationapp.navigation.NavEntry
import com.github.navigationapp.navigation.NavigationMethod
import com.github.navigationapp.navigation.navigationControllers.ScreenKey
import com.github.terrakok.cicerone.Cicerone
import com.github.terrakok.cicerone.Router

class NavigationViewModel : ViewModel() {

    // --- Уровни навигации ---

    private val _states = mutableMapOf<Int, NavigationState>()

    init {
        _states[0] = NavigationState()
    }

    fun state(level: Int): NavigationState =
        _states.getOrPut(level) { NavigationState(parentState = _states[level - 1]) }

    // Вызывается из ScreenCFragment.onDestroyView
    fun disposeLevel(level: Int) {
        _states.remove(level)
        updateNeedsReplay()
    }

    // --- Глобальный replay-стек ---

    // Все экраны, открытые сверх начального A, в порядке навигации.
    // A никогда не добавляется — он всегда подразумевается как начало каждого уровня.
    // Уровень каждой записи вычисляется по количеству ScreenKey.C до неё.
    private val _replayStack = mutableListOf<NavEntry>()
    val replayStack: List<NavEntry> get() = _replayStack.toList()

    var needsReplay: Boolean = false
        private set

    // Все записи, принадлежащие конкретному уровню:
    // уровень записи = количество ScreenKey.C строго до неё
    fun entriesForLevel(targetLevel: Int): List<NavEntry> {
        var level = 0
        val result = mutableListOf<NavEntry>()
        for (entry in _replayStack) {
            if (level == targetLevel) result.add(entry)
            if (entry.key is ScreenKey.C) level++
        }
        return result
    }

    // --- Navigation API ---

    fun navigateTo(level: Int, key: ScreenKey) {
        val method = state(level).method.value
        state(level).push(key)
        state(level).commands.tryEmit(key)
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
        // Метод изменился — если в стеке уже есть записи с другим методом, нужен replay
        updateNeedsReplay()
    }

    // needsReplay = true если в replayStack есть хотя бы одна смена метода между соседними записями
    private fun updateNeedsReplay() {
        needsReplay = _replayStack
            .zipWithNext()
            .any { (a, b) -> a.method != b.method }
    }

    // --- Cicerone (для MainActivity — level 0) ---
    val cicerone: Cicerone<Router> get() = state(0).cicerone

    fun closeLevel(level: Int) {
        _states.remove(level)

        val newStack = mutableListOf<NavEntry>()
        var currentLevel = 0
        for (entry in _replayStack) {
            // C-запись, открывающая закрываемый уровень — не добавляем
            if (entry.key is ScreenKey.C && currentLevel == level - 1) {
                currentLevel++
                continue
            }
            // Все записи глубже — не добавляем
            if (currentLevel >= level) continue

            newStack.add(entry)
            if (entry.key is ScreenKey.C) currentLevel++
        }

        _replayStack.clear()
        _replayStack.addAll(newStack)

        // Убираем C из стека родительского уровня
        state(level - 1).pop()

        updateNeedsReplay()
    }
}


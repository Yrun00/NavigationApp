package com.github.navigationapp

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow

class BaseViewModel(private val navigationManager: NavigationManager) : ViewModel() {

    private val screenCCloseFlows: MutableList<MutableSharedFlow<Unit>> = mutableListOf()

    fun getBackStackDepth(): Flow<Int> = navigationManager.getBackStackDepth()

    // Открытие ScreenB из ScreenA
    fun openBFromA() {
        navigationManager.navigateTo(Screen.ScreenB)
    }

    // Открытие ScreenB из ScreenB (рекурсивно)
    fun openBFromB() {
        navigationManager.navigateTo(Screen.ScreenB)
    }

    // Открытие ScreenC из ScreenA
    fun openCFromA() {
        navigationManager.navigateTo(Screen.ScreenC)
    }

    // Открытие ScreenA из ScreenC (вложенный)
    fun openAFromC() {
        navigationManager.navigateTo(Screen.ScreenA)
    }

    // Закрытие экрана
    fun goBack() {
        navigationManager.goBack()
    }

    // ScreenC регистрирует свой Flow для отслеживания закрытия вложенного ScreenA
    fun registerScreenCCloseFlow(flow: MutableSharedFlow<Unit>) {
        screenCCloseFlows.add(flow)
    }

    // ScreenC удаляет свой Flow при закрытии
    fun unregisterScreenCCloseFlow(flow: MutableSharedFlow<Unit>) {
        screenCCloseFlows.remove(flow)
    }

    // Вложенный ScreenA сообщает о своём закрытии
    suspend fun notifyScreenAClosed() {
        // Если это первый ScreenA (главный), ничего не делаем
        if (screenCCloseFlows.isEmpty()) {
            return
        }

        // Эммитим сигнал ТОЛЬКО последнему (самому свежему) ScreenC
        screenCCloseFlows.last().emit(Unit)

        // Удаляем его из списка, чтобы не эммитить ему в следующий раз
        screenCCloseFlows.removeLastOrNull()
    }
}
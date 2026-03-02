package com.github.navigationapp

import android.os.Bundle
import androidx.annotation.IdRes
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.commitNow
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.github.navigationapp.fragments.ScreenAFragment
import com.github.navigationapp.navigation.NavigationHost
import com.github.navigationapp.navigation.NavigationMethod
import com.github.navigationapp.navigation.navigationControllers.NavigationRouter
import com.github.navigationapp.navigation.navigationControllers.RouterFactory
import com.github.navigationapp.navigation.navigationControllers.ScreenKey
import com.github.terrakok.cicerone.androidx.AppNavigator
import com.zhuinden.simplestack.History
import com.zhuinden.simplestack.StateChange
import com.zhuinden.simplestack.StateChanger
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch

class NavigationHostDelegate(
    private val fragmentManager: FragmentManager,
    @IdRes private val containerId: Int,
    private val level: Int,
    private val viewModel: NavigationViewModel,
    private val getCiceroneNavigator: () -> AppNavigator,
    private val getSimpleStateChanger: () -> StateChanger,
    private val onEmptyStack: () -> Unit,  // finish() или closeThisLevel()
) : NavigationHost {

    private lateinit var router: NavigationRouter
    private lateinit var routerFactory: RouterFactory

    fun initialize(savedInstanceState: Bundle?, lifecycleOwner: LifecycleOwner) {
        routerFactory = RouterFactory(
            fragmentManager = fragmentManager,
            containerId = containerId,
            level = level,
            state = viewModel.state(level),
        )
        val currentMethod = viewModel.state(level).method.value

        when {
            // needsReplay ПЕРВЫМ: покрывает и ротацию (savedState != null),
            // и новый C созданный в цепочке replay (savedState = null)
            viewModel.needsReplay -> {
                replayLevel()
            }
            savedInstanceState == null -> {
                activateRouter(currentMethod)
                setupInitialScreen(currentMethod)
            }
            else -> {
                // Обычная ротация без смены метода — FM сам восстановил фрагменты
                activateRouter(currentMethod)
            }
        }

        subscribeToMethodChanges(lifecycleOwner)
    }

    override fun navigateTo(key: ScreenKey) {
        router.navigateTo(key)
    }

    fun onResume() {
        when (viewModel.state(level).method.value) {
            NavigationMethod.CICERONE ->
                viewModel.state(level).cicerone
                    .getNavigatorHolder().setNavigator(getCiceroneNavigator())

            NavigationMethod.SIMPLE_STACK ->  // ← раньше было Unit
                viewModel.state(level).simpleBackstack
                    .setStateChanger(getSimpleStateChanger())

            else -> Unit
        }
    }

    fun onPause() {
        when (viewModel.state(level).method.value) {
            NavigationMethod.CICERONE ->
                viewModel.state(level).cicerone
                    .getNavigatorHolder().removeNavigator()

            NavigationMethod.SIMPLE_STACK ->
                viewModel.state(level).simpleBackstack.detachStateChanger()  // ← только пауза

            else -> Unit
        }
    }

    fun onDestroyView() {
        if (viewModel.state(level).method.value == NavigationMethod.SIMPLE_STACK) {
            viewModel.state(level).simpleBackstack.detachStateChanger()
        }
    }

    private fun activateRouter(method: NavigationMethod) {
        router = routerFactory.create(method)
        when (method) {
            NavigationMethod.CICERONE ->
                viewModel.state(level).cicerone
                    .getNavigatorHolder().setNavigator(getCiceroneNavigator())

            NavigationMethod.SIMPLE_STACK ->
                viewModel.state(level).simpleBackstack
                    .setStateChanger(getSimpleStateChanger())

            else -> Unit
        }
    }

    private fun setupInitialScreen(method: NavigationMethod) {
        when (method) {
            NavigationMethod.SIMPLE_STACK,
            NavigationMethod.JETPACK -> Unit
            else -> fragmentManager.commitNow {  // ← commitNow
                replace(
                    containerId,
                    ScreenAFragment.newInstance(nestingLevel = level),
                    ScreenKey.A(nestingLevel = level).tag()
                )
            }
        }
    }

    private fun replayLevel() {
        val currentMethod = viewModel.state(level).method.value
        val entries = viewModel.entriesForLevel(level)

        // Шаг 1: Полностью чистим контейнер
        clearContainerForReplay()

        // Шаг 2: SimpleStack — сбросить историю ДО attach state changer,
        // иначе setStateChanger() сразу выстрелит со старой историей
        if (currentMethod == NavigationMethod.SIMPLE_STACK) {
            viewModel.state(level).simpleBackstack.setHistory(
                History.single(ScreenKey.A(nestingLevel = level)),
                StateChange.REPLACE
            )
        }

        // Шаг 3: Активируем роутер
        // Для SS: setStateChanger() → начальный state change [A] → показывает ScreenAFragment
        // Для Cicerone: setNavigator()
        activateRouter(currentMethod)

        // Шаг 4: Для FM/Cicerone/Jetpack: вручную размещаем начальный A
        // (SS уже показал A через state changer в шаге 3)
        setupInitialScreen(currentMethod)
        fragmentManager.executePendingTransactions()

        // Шаг 5: Воспроизводим записанный путь
        // executePendingTransactions() после каждого шага — гарантирует, что
        // ScreenCFragment.onViewCreated → initialize(needsReplay=true) → replayLevel(level+1)
        // отработает синхронно до того, как мы вернёмся наверх
        for (entry in entries) {
            router.navigateTo(entry.key)
            fragmentManager.executePendingTransactions()
            if (entry.key is ScreenKey.C) break  // вложенный C сам завершит свой уровень
        }
    }

    private fun clearContainerForReplay() {
        // Удаляем NavHostFragment (Jetpack)
        routerFactory.removeNavHostIfPresent()

        // Удаляем фрагменты из FM back stack (FM + Cicerone)
        fragmentManager.popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)

        // Удаляем фрагменты SimpleStack — они не в back stack, но всё ещё в контейнере
        val stranded = fragmentManager.fragments
            .filter { it.id == containerId && !it.isRemoving }
        if (stranded.isNotEmpty()) {
            fragmentManager.commitNow {
                stranded.forEach { remove(it) }
            }
        }
    }

    private fun subscribeToMethodChanges(lifecycleOwner: LifecycleOwner) {
        lifecycleOwner.lifecycleScope.launch {
            var previousMethod = viewModel.state(level).method.value
            viewModel.state(level).method
                .drop(1)
                .collect { newMethod ->
                    switchMethod(from = previousMethod, to = newMethod)
                    previousMethod = newMethod
                }
        }

    }

    private fun switchMethod(from: NavigationMethod, to: NavigationMethod) {
        when (from) {
            NavigationMethod.CICERONE ->
                viewModel.state(level).cicerone
                    .getNavigatorHolder().removeNavigator()

            NavigationMethod.SIMPLE_STACK ->
                viewModel.state(level).simpleBackstack.detachStateChanger()

            else -> Unit
        }
        if (from == NavigationMethod.JETPACK) {
            routerFactory.removeNavHostIfPresent()
        } else {
            router.clear()
            fragmentManager.executePendingTransactions()
        }

        activateRouter(to)
        setupInitialScreen(to)
        fragmentManager.executePendingTransactions()

        val entries = viewModel.entriesForLevel(level)
        for (entry in entries) {
            router.navigateTo(entry.key)
            if (entry.key is ScreenKey.C) break
        }
    }

    private fun setupBackPress() {
        // Передаётся через хост — Activity и Fragment по-разному регистрируют back
    }

    fun handleBack(): Boolean {
        val handled = router.back()
        if (handled) {
            viewModel.pop(level)
            return true
        }
        onEmptyStack()
        return false
    }
}

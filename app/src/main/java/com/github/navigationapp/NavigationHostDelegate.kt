package com.github.navigationapp

import android.annotation.SuppressLint
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
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

    private lateinit var scope: CoroutineScope

    fun initialize(savedInstanceState: Bundle?, lifecycleOwner: LifecycleOwner) {
        scope = lifecycleOwner.lifecycleScope
        routerFactory = RouterFactory(
            fragmentManager = fragmentManager,
            containerId = containerId,
            level = level,
            state = viewModel.state(level),
        )
        val currentMethod = viewModel.state(level).method.value

        when {
            viewModel.needsReplay -> {
                replayLevel()
            }

            savedInstanceState == null -> {
                activateRouter(currentMethod)
                setupInitialScreen(currentMethod)
            }

            else -> {
                activateRouter(currentMethod)
            }
        }

        subscribeToMethodChanges(lifecycleOwner)
    }

    override fun navigateTo(key: ScreenKey) {
        router.navigateTo(key)
    }


    @SuppressLint("RestrictedApi")
    override fun observeBackStackDepth(): StateFlow<Int> {
        val state = viewModel.stateOrNull(level) ?: return MutableStateFlow(0)
        return when (state.method.value) {
            NavigationMethod.FRAGMENT_MANAGER,
            NavigationMethod.CICERONE,
                -> {
                val flow = MutableStateFlow(fmConsecutiveBDepth())
                fragmentManager.addOnBackStackChangedListener {
                    flow.value = fmConsecutiveBDepth()
                }
                flow
            }

            NavigationMethod.SIMPLE_STACK ->
                state.stack
                    .map { stack -> stack.takeLastWhile { it is ScreenKey.B }.size - 1 }
                    .stateIn(scope, SharingStarted.Eagerly, 0)

            NavigationMethod.JETPACK ->
                routerFactory.getNavController()!!
                    .currentBackStack
                    .map { entries ->
                        entries.takeLastWhile {
                            it.destination.id == R.id.screenBFragment
                        }.size - 1
                    }
                    .stateIn(scope, SharingStarted.Eagerly, 0)
        }
    }

    private fun fmConsecutiveBDepth(): Int =
        (0 until fragmentManager.backStackEntryCount)
            .map { fragmentManager.getBackStackEntryAt(it).name }
            .takeLastWhile { it?.startsWith("screen_b_") == true }
            .size - 1

    fun onResume() {
        val state = viewModel.stateOrNull(level) ?: return
        when (state.method.value) {
            NavigationMethod.CICERONE ->
                state.cicerone.getNavigatorHolder().setNavigator(getCiceroneNavigator())

            NavigationMethod.SIMPLE_STACK ->
                state.simpleBackstack.setStateChanger(getSimpleStateChanger())

            else -> Unit
        }
    }

    fun onPause() {
        val state = viewModel.stateOrNull(level) ?: return
        when (state.method.value) {
            NavigationMethod.CICERONE ->
                state.cicerone.getNavigatorHolder().removeNavigator()

            NavigationMethod.SIMPLE_STACK ->
                state.simpleBackstack.detachStateChanger()

            else -> Unit
        }
    }

    fun onDestroyView() {
        val state = viewModel.stateOrNull(level) ?: return
        if (state.method.value == NavigationMethod.SIMPLE_STACK) {
            state.simpleBackstack.detachStateChanger()
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
            NavigationMethod.JETPACK,
                -> Unit

            else -> fragmentManager.commitNow {  // ← commitNow
                replace(
                    containerId,
                    ScreenAFragment.newInstance(nestingLevel = level),
                    ScreenKey.A(nestingLevel = level).tag(),
                )
            }
        }
    }

    private fun replayLevel() {
        val currentMethod = viewModel.state(level).method.value
        val entries = viewModel.entriesForLevel(level)
        clearContainerForReplay()
        if (currentMethod == NavigationMethod.SIMPLE_STACK) {
            viewModel.state(level).simpleBackstack.setHistory(
                History.single(ScreenKey.A(nestingLevel = level)),
                StateChange.REPLACE,
            )
        }
        activateRouter(currentMethod)
        setupInitialScreen(currentMethod)
        fragmentManager.executePendingTransactions()
        for (entry in entries) {
            router.navigateTo(entry.key)
            fragmentManager.executePendingTransactions()
            if (entry.key is ScreenKey.C) break  // вложенный C сам завершит свой уровень
        }
    }

    private fun clearContainerForReplay() {
        routerFactory.removeNavHostIfPresent()
        fragmentManager.popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
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
            NavigationMethod.CICERONE -> {
                viewModel.state(level).cicerone.getNavigatorHolder().removeNavigator()
                router.clear()
            }

            NavigationMethod.SIMPLE_STACK -> {
                viewModel.state(level).simpleBackstack.detachStateChanger()
                router.clear()
                val stranded = fragmentManager.fragments
                    .filter { it.id == containerId && !it.isRemoving }
                if (stranded.isNotEmpty()) {
                    fragmentManager.commitNow { stranded.forEach { remove(it) } }
                }
            }

            NavigationMethod.JETPACK -> {
                routerFactory.removeNavHostIfPresent()
            }

            NavigationMethod.FRAGMENT_MANAGER -> {
                router.clear()
            }
        }

        if (to == NavigationMethod.SIMPLE_STACK) {
            viewModel.state(level).simpleBackstack.setHistory(
                History.single(ScreenKey.A(nestingLevel = level)),
                StateChange.REPLACE,
            )
        }

        activateRouter(to)
        setupInitialScreen(to)
        fragmentManager.executePendingTransactions()
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
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
import com.github.navigationapp.navigation.ScreenKey
import com.github.navigationapp.navigation.navigationControllers.NavigationRouter
import com.github.navigationapp.navigation.navigationControllers.RouterFactory
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
    private val onEmptyStack: () -> Unit,
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
            getCiceroneNavigator = getCiceroneNavigator,
            getSimpleStateChanger = getSimpleStateChanger,
        )
        when {
            viewModel.needsReplay -> replayLevel()
            savedInstanceState == null -> {
                val method = viewModel.state(level).method.value
                activateRouter(method)
                setupInitialScreen(method)
            }
            else -> activateRouter(viewModel.state(level).method.value)
        }
        observeMethodChanges(lifecycleOwner)
    }

    override fun navigateTo(key: ScreenKey) = router.navigateTo(key)

    @SuppressLint("RestrictedApi")
    override fun observeBackStackDepth(): StateFlow<Int> {
        val state = viewModel.stateOrNull(level) ?: return MutableStateFlow(0)
        return when (state.method.value) {
            NavigationMethod.FRAGMENT_MANAGER,
            NavigationMethod.CICERONE -> {
                val flow = MutableStateFlow(fmConsecutiveBDepth())
                fragmentManager.addOnBackStackChangedListener { flow.value = fmConsecutiveBDepth() }
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
                        entries.takeLastWhile { it.destination.id == R.id.screenBFragment }.size - 1
                    }
                    .stateIn(scope, SharingStarted.Eagerly, 0)
        }
    }

    fun onResume() {
        if (viewModel.stateOrNull(level) == null) return
        router.attach()
    }

    fun onPause() {
        if (viewModel.stateOrNull(level) == null) return
        router.detach()
    }

    fun onDestroyView() {
        if (viewModel.stateOrNull(level) == null) return
        router.detach()
    }

    fun handleBack(): Boolean {
        if (router.back()) {
            viewModel.pop(level)
            return true
        }
        onEmptyStack()
        return false
    }

    private fun activateRouter(method: NavigationMethod) {
        router = routerFactory.create(method)
        router.attach()
    }

    private fun setupInitialScreen(method: NavigationMethod) {
        when (method) {
            NavigationMethod.SIMPLE_STACK,
            NavigationMethod.JETPACK -> Unit
            else -> fragmentManager.commitNow {
                replace(
                    containerId,
                    ScreenAFragment.newInstance(nestingLevel = level),
                    ScreenKey.A(level = level).getFragmentTag(),
                )
            }
        }
    }

    private fun replayLevel() {
        val method = viewModel.state(level).method.value
        val entries = viewModel.entriesForLevel(level)
        clearContainerForReplay()
        if (method == NavigationMethod.SIMPLE_STACK) {
            viewModel.state(level).simpleBackstack.setHistory(
                History.single(ScreenKey.A(level = level)),
                StateChange.REPLACE,
            )
        }
        activateRouter(method)
        setupInitialScreen(method)
        fragmentManager.executePendingTransactions()
        for (entry in entries) {
            router.navigateTo(entry.key)
            fragmentManager.executePendingTransactions()
            if (entry.key is ScreenKey.C) break
        }
    }

    private fun clearContainerForReplay() {
        routerFactory.removeNavHostIfPresent()
        fragmentManager.popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
        val stranded = fragmentManager.fragments
            .filter { it.id == containerId && !it.isRemoving }
        if (stranded.isNotEmpty()) {
            fragmentManager.commitNow { stranded.forEach { remove(it) } }
        }
    }

    private fun observeMethodChanges(lifecycleOwner: LifecycleOwner) {
        lifecycleOwner.lifecycleScope.launch {
            viewModel.state(level).method.drop(1).collect { newMethod ->
                switchMethod(to = newMethod)
            }
        }
    }

    private fun switchMethod(to: NavigationMethod) {
        router.detach()
        router.clearContainer()
        if (to == NavigationMethod.SIMPLE_STACK) {
            viewModel.state(level).simpleBackstack.setHistory(
                History.single(ScreenKey.A(level = level)),
                StateChange.REPLACE,
            )
        }
        activateRouter(to)
        setupInitialScreen(to)
        fragmentManager.executePendingTransactions()
    }

    private fun fmConsecutiveBDepth(): Int =
        (0 until fragmentManager.backStackEntryCount)
            .map { fragmentManager.getBackStackEntryAt(it).name }
            .takeLastWhile { it?.startsWith("screen_b_") == true }
            .size - 1
}

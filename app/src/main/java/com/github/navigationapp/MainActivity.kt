package com.github.navigationapp

import android.os.Bundle
import androidx.activity.addCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.commit
import androidx.lifecycle.lifecycleScope
import com.github.navigationapp.fragments.AppFragmentFactory
import com.github.navigationapp.fragments.ScreenAFragment
import com.github.navigationapp.navigation.NavigationMethod
import com.github.navigationapp.navigation.navigationControllers.NavigationRouter
import com.github.navigationapp.navigation.navigationControllers.RouterFactory
import com.github.navigationapp.navigation.navigationControllers.ScreenKey
import com.github.terrakok.cicerone.androidx.AppNavigator
import com.zhuinden.simplestack.StateChanger
import com.zhuinden.simplestackextensions.fragments.DefaultFragmentStateChanger
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val viewModel: NavigationViewModel by viewModels()

    private lateinit var router: NavigationRouter
    private lateinit var routerFactory: RouterFactory

    private val ciceroneNavigator by lazy {
        object : AppNavigator(this, R.id.root_fragment_container, supportFragmentManager) {}
    }

    private val simpleStateChanger by lazy {
        StateChanger { stateChange, callback ->
            DefaultFragmentStateChanger(
                supportFragmentManager,
                R.id.root_fragment_container,
            ).handleStateChange(stateChange)
            callback.stateChangeComplete()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        supportFragmentManager.fragmentFactory = AppFragmentFactory()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        routerFactory = RouterFactory(
            fragmentManager = supportFragmentManager,
            containerId = R.id.root_fragment_container,
            level = 0,
            state = viewModel.state(0),
        )

        val currentMethod = viewModel.state(0).method.value

        when {
            savedInstanceState == null -> {
                router = routerFactory.create(currentMethod)
                setupInitialScreen(currentMethod)
            }

            viewModel.needsReplay -> {
                router = routerFactory.create(currentMethod)
                replayLevel()
            }

            else -> {
                // Обычная ротация — Android восстановил фрагменты сам
                router = routerFactory.create(currentMethod)
                if (currentMethod == NavigationMethod.SIMPLE_STACK) {
                    viewModel.state(0).simpleBackstack.setStateChanger(simpleStateChanger)
                }
            }
        }

        subscribeToCommands()
        subscribeToMethodChanges()
        setupBackPress()
    }

    private fun setupInitialScreen(method: NavigationMethod) {
        if (method == NavigationMethod.SIMPLE_STACK) {
            viewModel.state(0).simpleBackstack.setStateChanger(simpleStateChanger)
        } else {
            supportFragmentManager.commit {
                setReorderingAllowed(true)
                replace(
                    R.id.root_fragment_container,
                    ScreenAFragment.newInstance(nestingLevel = 0),
                    ScreenKey.A(nestingLevel = 0).tag(),
                )
            }
        }
    }

    private fun replayLevel() {
        val entries = viewModel.entriesForLevel(0)
        val currentMethod = viewModel.state(0).method.value

        // Очищаем всё что было
        routerFactory.removeNavHostIfPresent()
        if (currentMethod != NavigationMethod.SIMPLE_STACK) {
            supportFragmentManager.popBackStackImmediate(
                null,
                FragmentManager.POP_BACK_STACK_INCLUSIVE,
            )
        }

        // Начальный A
        setupInitialScreen(currentMethod)
        supportFragmentManager.executePendingTransactions()

        // Воспроизводим переходы level 0
        // При встрече C — останавливаемся, C сам реплеится в onViewCreated
        for (entry in entries) {
            router.navigateTo(entry.key)
            if (entry.key is ScreenKey.C) break
        }
    }

    private fun subscribeToCommands() {
        lifecycleScope.launch {
            viewModel.state(0).commands.collect { key ->
                router.navigateTo(key)
            }
        }
    }

    private fun subscribeToMethodChanges() {
        lifecycleScope.launch {
            var previousMethod = viewModel.state(0).method.value
            viewModel.state(0).method
                .drop(1)
                .collect { newMethod ->
                    switchMethod(from = previousMethod, to = newMethod)
                    previousMethod = newMethod
                }
        }
    }

    private fun switchMethod(from: NavigationMethod, to: NavigationMethod) {
        // Отключаем старый метод
        if (from == NavigationMethod.SIMPLE_STACK) {
            viewModel.state(0).simpleBackstack.detachStateChanger()
        }
        if (from == NavigationMethod.JETPACK) {
            routerFactory.removeNavHostIfPresent()
        } else {
            router.clear()
            supportFragmentManager.executePendingTransactions()
        }

        // Создаём новый роутер
        router = routerFactory.create(to)

        // Начальный A для нового метода
        setupInitialScreen(to)
        supportFragmentManager.executePendingTransactions()

        // Воспроизводим текущий стек level 0 через новый метод
        val entries = viewModel.entriesForLevel(0)
        for (entry in entries) {
            router.navigateTo(entry.key)
            if (entry.key is ScreenKey.C) break
        }
    }

    private fun setupBackPress() {
        onBackPressedDispatcher.addCallback(this) {
            val handled = router.back()
            if (handled) {
                viewModel.pop(0)
            } else {
                finish()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        when (viewModel.state(0).method.value) {
            NavigationMethod.CICERONE ->
                viewModel.state(0).cicerone
                    .getNavigatorHolder().setNavigator(ciceroneNavigator)

            NavigationMethod.SIMPLE_STACK ->
                viewModel.state(0).simpleBackstack.reattachStateChanger()

            else -> Unit
        }
    }

    override fun onPause() {
        when (viewModel.state(0).method.value) {
            NavigationMethod.CICERONE ->
                viewModel.state(0).cicerone
                    .getNavigatorHolder().removeNavigator()

            NavigationMethod.SIMPLE_STACK ->
                viewModel.state(0).simpleBackstack.detachStateChanger()

            else -> Unit
        }
        super.onPause()
    }

    override fun onDestroy() {
        if (viewModel.state(0).method.value == NavigationMethod.SIMPLE_STACK) {
            viewModel.state(0).simpleBackstack.detachStateChanger()
        }
        super.onDestroy()
    }
}

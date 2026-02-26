package com.github.navigationapp

import android.os.Bundle
import androidx.activity.addCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.commit
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import com.github.navigationapp.fragments.AppFragmentFactory
import com.github.navigationapp.fragments.ScreenAFragment
import com.github.navigationapp.navigation.NavigationMethod
import com.github.navigationapp.navigation.navigationControllers.NavigationRouter
import com.github.navigationapp.navigation.navigationControllers.RouterFactory
import com.github.navigationapp.navigation.navigationControllers.ScreenKey
import com.github.terrakok.cicerone.androidx.AppNavigator
import com.zhuinden.simplestack.BackstackDelegate
import com.zhuinden.simplestack.History
import com.zhuinden.simplestack.StateChanger
import com.zhuinden.simplestackextensions.fragments.DefaultFragmentStateChanger
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val viewModel: NavigationViewModel by viewModels()
    private val fragmentFactory = AppFragmentFactory()

    // Simple Stack
    private lateinit var backstackDelegate: BackstackDelegate

    // Активный роутер
    private lateinit var activeRouter: NavigationRouter

    // Cicerone Navigator
    private val ciceroneNavigator by lazy {
        object : AppNavigator(this, R.id.root_fragment_container, supportFragmentManager) {}
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        supportFragmentManager.fragmentFactory = fragmentFactory
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        lifecycleScope.launch {
            viewModel.navigationCommands.collect { key ->
                activeRouter.navigateTo(key)
            }
        }
        // Simple Stack инициализация
        backstackDelegate = BackstackDelegate()
        backstackDelegate.onCreate(
            savedInstanceState,
            lastCustomNonConfigurationInstance,
            History.single(ScreenKey.A)
        )

        val fragmentStateChanger = DefaultFragmentStateChanger(
            supportFragmentManager,
            R.id.root_fragment_container,
        )

        backstackDelegate.setStateChanger(StateChanger { stateChange, callback ->
            val fragmentTransaction = supportFragmentManager.beginTransaction()
                .disallowAddToBackStack()

            // Скрываем все предыдущие фрагменты
            stateChange.getPreviousKeys<ScreenKey>().forEach { key ->
                supportFragmentManager.findFragmentByTag(key.tag())?.let {
                    fragmentTransaction.detach(it)
                }
            }

            // Показываем верхний новый фрагмент
            val topKey = stateChange.topNewKey<ScreenKey>()
            var topFragment = supportFragmentManager.findFragmentByTag(topKey.tag())
            if (topFragment == null) {
                topFragment = topKey.instantiateFragment()
                fragmentTransaction.add(R.id.root_fragment_container, topFragment, topKey.tag())
            } else {
                fragmentTransaction.attach(topFragment)
            }

            fragmentTransaction.commitNow()
            callback.stateChangeComplete()
        })
        backstackDelegate.registerForLifecycleCallbacks(this)
        if (savedInstanceState == null) {
            activeRouter = createRouter(viewModel.method.value)
            if (viewModel.method.value != NavigationMethod.SIMPLE_STACK) {
                supportFragmentManager.commit {
                    replace(
                        R.id.root_fragment_container,
                        ScreenAFragment.newInstance(isNested = false),
                        ScreenKey.A.tag(),
                    )
                    // Корневой экран не добавляем в бэкстак
                }
            }
        } else {
            // Ротация
            if (viewModel.needsReplay) {
                activeRouter = createRouter(viewModel.method.value)
                replayNavigation()
                viewModel.onReplayDone()
            } else {
                // Нативный restore — просто пересоздаём роутер без replay
                activeRouter = createRouter(viewModel.method.value)
            }
        }

        // Наблюдаем за сменой метода навигации
        var previousMethod = viewModel.method.value

        lifecycleScope.launch {
            viewModel.method
                .drop(1)
                .collect { newMethod ->
                    switchNavigationMethod(from = previousMethod, to = newMethod)
                    previousMethod = newMethod
                }
        }

        // Системная кнопка назад
        onBackPressedDispatcher.addCallback(this) {
            val handled = activeRouter.back()
            if (handled) {
                viewModel.pop()
            } else {
                finish()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        backstackDelegate.onPostResume()
        viewModel.cicerone.getNavigatorHolder().setNavigator(ciceroneNavigator)
    }

    override fun onPause() {
        backstackDelegate.onPause()
        viewModel.cicerone.getNavigatorHolder().removeNavigator()
        super.onPause()
    }
    override fun onDestroy() {
        backstackDelegate.onDestroy()
        super.onDestroy()
    }

    override fun onRetainCustomNonConfigurationInstance(): Any? {
        return backstackDelegate.onRetainCustomNonConfigurationInstance()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        backstackDelegate.onSaveInstanceState(outState)
    }
    private fun createRouter(method: NavigationMethod): NavigationRouter {
        return RouterFactory(
            fragmentManager = supportFragmentManager,
            containerId = R.id.root_fragment_container,
            viewModel = viewModel,
            backstack = backstackDelegate.backstack,
        ).create(method)
    }

    private fun switchNavigationMethod(from: NavigationMethod, to: NavigationMethod) {
        if (from != NavigationMethod.JETPACK) {
            activeRouter.clear()
            supportFragmentManager.executePendingTransactions() // ждём завершения clear
        }
        if (to == NavigationMethod.JETPACK) addNavHostFragment()
        if (from == NavigationMethod.JETPACK) removeNavHostFragment()

        activeRouter = createRouter(to)
        activeRouter.replay(viewModel.stack.value)
    }


    private fun addNavHostFragment() {
        val navHostFragment = NavHostFragment.create(R.navigation.nav_graph)
        supportFragmentManager.commit {
            setReorderingAllowed(true)
            replace(R.id.root_fragment_container, navHostFragment, "nav_host")
        }
        supportFragmentManager.executePendingTransactions()
    }

    private fun removeNavHostFragment() {
        val navHostFragment = supportFragmentManager.findFragmentByTag("nav_host")
        if (navHostFragment != null) {
            supportFragmentManager.commit {
                setReorderingAllowed(true)
                remove(navHostFragment)
            }
            supportFragmentManager.executePendingTransactions()
        }
    }

    private fun replayNavigation() {
        val stack = viewModel.stack.value
        val method = viewModel.method.value

        if (method == NavigationMethod.JETPACK) {
            addNavHostFragment()
        } else {
            supportFragmentManager.popBackStackImmediate(
                null,
                FragmentManager.POP_BACK_STACK_INCLUSIVE,
            )
        }

        activeRouter.replay(stack)
    }
}
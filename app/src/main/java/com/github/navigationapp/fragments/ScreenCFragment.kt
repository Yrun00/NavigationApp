package com.github.navigationapp.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.commit
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import com.github.navigationapp.NavigationViewModel
import com.github.navigationapp.R
import com.github.navigationapp.navigation.NavigationMethod
import com.github.navigationapp.navigation.navigationControllers.NavigationRouter
import com.github.navigationapp.navigation.navigationControllers.RouterFactory
import com.github.navigationapp.navigation.navigationControllers.ScreenKey
import com.zhuinden.simplestack.Backstack
import com.zhuinden.simplestack.BackstackDelegate
import com.zhuinden.simplestack.History
import com.zhuinden.simplestack.StateChanger
import com.zhuinden.simplestackextensions.fragments.DefaultFragmentStateChanger
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch

/**
 * FragmentC - экран с вложенным контейнером для ScreenA
 *
 * Особенности:
 * - Содержит Box с отступами (через XML)
 * - Внутри Box размещен вложенный экземпляр FragmentA
 * - При закрытии вложенного A - закрывается сам FragmentC
 */
@AndroidEntryPoint
class ScreenCFragment : Fragment(R.layout.fragment_c) {

    private val viewModel: NavigationViewModel by activityViewModels()
    private lateinit var nestedRouter: NavigationRouter
    private lateinit var nestedBackstackDelegate: BackstackDelegate

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        nestedBackstackDelegate = BackstackDelegate()
        nestedBackstackDelegate.onCreate(
            savedInstanceState,
            null,
            History.single(ScreenKey.A)
        )

        val nestedStateChanger = DefaultFragmentStateChanger(
            childFragmentManager,
            R.id.nested_fragment_container
        )
        nestedBackstackDelegate.setStateChanger(StateChanger { stateChange, callback ->
            nestedStateChanger.handleStateChange(stateChange)
            callback.stateChangeComplete()
        })
        // Заголовок
        view.findViewById<ComposeView>(R.id.compose_header).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Screen C",
                        fontSize = 28.sp,
                        color = Color.White
                    )
                    Text(
                        text = "Box with nested Screen A inside:",
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }
        }

        // Инициализируем вложенный роутер
        nestedRouter = RouterFactory(
            fragmentManager = childFragmentManager,
            containerId = R.id.nested_fragment_container,
            viewModel = viewModel,
            backstack = createNestedBackstack(savedInstanceState)
        ).createNested(viewModel.nestedMethod.value)

        if (savedInstanceState == null) {
            // Первый запуск — показываем вложенный Screen A
            childFragmentManager.commit {
                setReorderingAllowed(true)
                replace(
                    R.id.nested_fragment_container,
                    ScreenAFragment.newInstance(isNested = true),
                    ScreenKey.A.tag()
                )
            }
        } else if (viewModel.nestedNeedsReplay) {
            // Была смена метода — replay
            childFragmentManager.popBackStackImmediate(
                null,
                FragmentManager.POP_BACK_STACK_INCLUSIVE
            )
            nestedRouter.replay(viewModel.nestedStack.value)
            viewModel.onNestedReplayDone()
        }
        // else — нативный restore childFragmentManager справится сам

        // Слушаем команды навигации для вложенного A
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.nestedNavigationCommands.collect { key ->
                nestedRouter.navigateTo(key)
            }
        }

        // Слушаем смену метода для вложенной навигации
        viewLifecycleOwner.lifecycleScope.launch {
            var previousNestedMethod = viewModel.nestedMethod.value
            viewModel.nestedMethod
                .drop(1)
                .collect { newMethod ->
                    switchNestedMethod(from = previousNestedMethod, to = newMethod)
                    previousNestedMethod = newMethod
                }
        }

        // Слушаем сигнал "вложенный A закрылся"
        childFragmentManager.setFragmentResultListener(
            NESTED_A_FINISHED,
            viewLifecycleOwner
        ) { _, _ ->
            viewModel.resetNestedStack()
            viewModel.pop() // закрываем сам Screen C
            viewModel.navigationCommands.tryEmit(ScreenKey.A) // сигнал для MainActivity
            parentFragmentManager.popBackStack()
        }
    }

    private fun switchNestedMethod(from: NavigationMethod, to: NavigationMethod) {
        if (from == NavigationMethod.JETPACK) removeNestedNavHost()
        if (to == NavigationMethod.JETPACK) addNestedNavHost()

        nestedRouter = RouterFactory(
            fragmentManager = childFragmentManager,
            containerId = R.id.nested_fragment_container,
            viewModel = viewModel,
            backstack = nestedBackstackDelegate.backstack  // <-- теперь работает
        ).createNested(to)

        nestedRouter.replay(viewModel.nestedStack.value)
    }

    private fun addNestedNavHost() {
        val navHostFragment = NavHostFragment.create(R.navigation.nav_graph)
        childFragmentManager.commit {
            setReorderingAllowed(true)
            replace(R.id.nested_fragment_container, navHostFragment, "nested_nav_host")
        }
        childFragmentManager.executePendingTransactions()
    }

    private fun removeNestedNavHost() {
        val navHostFragment = childFragmentManager.findFragmentByTag("nested_nav_host")
        if (navHostFragment != null) {
            childFragmentManager.commit {
                setReorderingAllowed(true)
                remove(navHostFragment)
            }
            childFragmentManager.executePendingTransactions()
        }
    }

    private fun createNestedBackstack(savedInstanceState: Bundle?): Backstack {
        val delegate = BackstackDelegate()
        delegate.onCreate(
            savedInstanceState,
            null, // nested не использует NonConfigurationInstance
            History.single(ScreenKey.A)
        )
        delegate.registerForLifecycleCallbacks(requireActivity())
        delegate.backstack.setStateChanger(
            StateChanger { stateChange, callback ->
                DefaultFragmentStateChanger(
                    childFragmentManager,
                    R.id.nested_fragment_container
                ).handleStateChange(stateChange)
                callback.stateChangeComplete()
            }
        )
        return delegate.backstack
    }
    override fun onResume() {
        super.onResume()
        nestedBackstackDelegate.onPostResume()
    }

    override fun onPause() {
        nestedBackstackDelegate.onPause()
        super.onPause()
    }

    override fun onDestroyView() {
        nestedBackstackDelegate.onDestroy()
        super.onDestroyView()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        nestedBackstackDelegate.onSaveInstanceState(outState)
    }

    companion object {
        const val NESTED_A_FINISHED = "nested_a_finished"

        fun newInstance(): ScreenCFragment = ScreenCFragment()
    }
}

package com.github.navigationapp.fragments

import android.os.Bundle
import android.view.View
import androidx.activity.addCallback
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
import androidx.core.os.bundleOf
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
import com.github.terrakok.cicerone.androidx.AppNavigator
import com.zhuinden.simplestack.Backstack
import com.zhuinden.simplestack.BackstackDelegate
import com.zhuinden.simplestack.History
import com.zhuinden.simplestack.StateChanger
import com.zhuinden.simplestackextensions.fragments.DefaultFragmentStateChanger
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch


@AndroidEntryPoint
class ScreenCFragment : Fragment(R.layout.fragment_c) {

    private val viewModel: NavigationViewModel by activityViewModels()

    private val hostingLevel: Int
        get() = arguments?.getInt(ARG_HOSTING_LEVEL) ?: 1

    private lateinit var router: NavigationRouter
    private lateinit var routerFactory: RouterFactory

    private val ciceroneNavigator by lazy {
        object : AppNavigator(
            requireActivity(),
            R.id.nested_fragment_container,
            childFragmentManager
        ) {}
    }

    // StateChanger для SimpleStack — один объект, переподключается при resume
    private val simpleStateChanger by lazy {
        StateChanger { stateChange, callback ->
            DefaultFragmentStateChanger(
                childFragmentManager,
                R.id.nested_fragment_container
            ).handleStateChange(stateChange)
            callback.stateChangeComplete()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        routerFactory = RouterFactory(
            fragmentManager = childFragmentManager,
            containerId = R.id.nested_fragment_container,
            level = hostingLevel,
            state = viewModel.state(hostingLevel)
        )

        setupHeader(view)

        val currentMethod = viewModel.state(hostingLevel).method.value

        when {
            savedInstanceState == null -> {
                // Первый запуск — создаём роутер и показываем A
                router = routerFactory.create(currentMethod)
                setupInitialScreen(currentMethod)
            }
            viewModel.needsReplay -> {
                // Ротация + был сменён метод навигации — перестраиваем
                router = routerFactory.create(currentMethod)
                replayLevel()
            }
            else -> {
                // Обычная ротация — Android восстановил фрагменты сам
                router = routerFactory.create(currentMethod)
                // Для SS — только переподключаем StateChanger (Backstack жив в ViewModel)
                if (currentMethod == NavigationMethod.SIMPLE_STACK) {
                    viewModel.state(hostingLevel).simpleBackstack
                        .setStateChanger(simpleStateChanger)
                }
            }
        }

        subscribeToCommands()
        subscribeToMethodChanges()
        setupBackPress()
        setupNestedAFinishedListener()
    }

    // Показываем начальный A — для SS он уже отображён через StateChanger
    private fun setupInitialScreen(method: NavigationMethod) {
        if (method == NavigationMethod.SIMPLE_STACK) {
            viewModel.state(hostingLevel).simpleBackstack.setStateChanger(simpleStateChanger)
        } else {
            childFragmentManager.commit {
                setReorderingAllowed(true)
                replace(
                    R.id.nested_fragment_container,
                    ScreenAFragment.newInstance(nestingLevel = hostingLevel),
                    ScreenKey.A(nestingLevel = hostingLevel).tag()
                )
            }
        }
    }

    private fun replayLevel() {
        val entries = viewModel.entriesForLevel(hostingLevel)
        val currentMethod = viewModel.state(hostingLevel).method.value

        // Очищаем всё что было
        routerFactory.removeNavHostIfPresent()
        if (currentMethod != NavigationMethod.SIMPLE_STACK) {
            childFragmentManager.popBackStackImmediate(
                null,
                FragmentManager.POP_BACK_STACK_INCLUSIVE
            )
        }

        // Устанавливаем начальный A
        setupInitialScreen(currentMethod)
        childFragmentManager.executePendingTransactions()

        // Воспроизводим все переходы этого уровня
        // Все entries одного уровня имеют одинаковый метод (метод меняется только на A)
        entries.forEach { entry -> router.navigateTo(entry.key) }
    }

    private fun subscribeToCommands() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.state(hostingLevel).commands.collect { key ->
                router.navigateTo(key)
            }
        }
    }

    private fun subscribeToMethodChanges() {
        viewLifecycleOwner.lifecycleScope.launch {
            var previousMethod = viewModel.state(hostingLevel).method.value
            viewModel.state(hostingLevel).method
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
            viewModel.state(hostingLevel).simpleBackstack.detachStateChanger()
        }
        if (from == NavigationMethod.JETPACK) {
            routerFactory.removeNavHostIfPresent()
        } else {
            router.clear()
            childFragmentManager.executePendingTransactions()
        }

        // Создаём новый роутер
        router = routerFactory.create(to)

        // Устанавливаем начальный A для нового метода
        setupInitialScreen(to)
        childFragmentManager.executePendingTransactions()

        // Воспроизводим текущий стек через новый метод
        val entries = viewModel.entriesForLevel(hostingLevel)
        entries.forEach { entry -> router.navigateTo(entry.key) }
    }

    private fun setupBackPress() {
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            val handled = router.back()
            if (handled) {
                viewModel.pop(hostingLevel)
            } else {
                closeThisLevel()
            }
        }
    }

    private fun setupNestedAFinishedListener() {
        childFragmentManager.setFragmentResultListener(
            NESTED_A_FINISHED,
            viewLifecycleOwner
        ) { _, _ -> closeThisLevel() }
    }

    private fun closeThisLevel() {
        viewModel.closeLevel(hostingLevel)
        parentFragmentManager.popBackStack()
    }

    private fun setupHeader(view: View) {
        view.findViewById<ComposeView>(R.id.compose_header).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Screen C (Level $hostingLevel)",
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
    }

    override fun onResume() {
        super.onResume()
        when (viewModel.state(hostingLevel).method.value) {
            NavigationMethod.CICERONE ->
                viewModel.state(hostingLevel).cicerone
                    .getNavigatorHolder().setNavigator(ciceroneNavigator)
            NavigationMethod.SIMPLE_STACK ->
                viewModel.state(hostingLevel).simpleBackstack
                    .reattachStateChanger()
            else -> Unit
        }
    }

    override fun onPause() {
        when (viewModel.state(hostingLevel).method.value) {
            NavigationMethod.CICERONE ->
                viewModel.state(hostingLevel).cicerone
                    .getNavigatorHolder().removeNavigator()
            NavigationMethod.SIMPLE_STACK ->
                viewModel.state(hostingLevel).simpleBackstack
                    .detachStateChanger()
            else -> Unit
        }
        super.onPause()
    }

    override fun onDestroyView() {
        // Отключаем StateChanger при уничтожении View (ротация или реальное закрытие)
        if (viewModel.state(hostingLevel).method.value == NavigationMethod.SIMPLE_STACK) {
            viewModel.state(hostingLevel).simpleBackstack.detachStateChanger()
        }
        super.onDestroyView()
    }

    companion object {
        const val NESTED_A_FINISHED = "nested_a_finished"
        private const val ARG_HOSTING_LEVEL = "hosting_level"

        fun newInstance(hostingLevel: Int = 1) = ScreenCFragment().apply {
            arguments = bundleOf(ARG_HOSTING_LEVEL to hostingLevel)
        }
    }
}

package com.github.navigationapp.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import com.github.navigationapp.R
import com.github.navigationapp.navigation.*
import dagger.hilt.android.AndroidEntryPoint

/**
 * FragmentA - главный экран с селектором типа навигации
 * 
 * Может быть:
 * 1. Корневым экраном (isNested = false)
 * 2. Вложенным внутри FragmentC (isNested = true)
 * 
 * Каждый экземпляр имеет свой собственный NavigationHost
 */
@AndroidEntryPoint
class FragmentA : Fragment() {

    private lateinit var navigationHost: NavigationHost
    
    private val isNested: Boolean by lazy {
        arguments?.getBoolean(ARG_IS_NESTED) ?: false
    }

    // Состояние для реактивного обновления UI
    private val currentNavigationType = mutableStateOf(NavigationType.FRAGMENT_MANAGER)
    
    private var nestedCallback: NestedNavigationCallback? = null
    
    private val backPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            if (!navigationHost.goBack()) {
                // Backstack пуст
                if (isNested) {
                    // Уведомляем родителя о закрытии
                    nestedCallback?.onNestedScreenClosed()
                } else {
                    // Выходим из приложения
                    isEnabled = false
                    requireActivity().onBackPressedDispatcher.onBackPressed()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireActivity().onBackPressedDispatcher.addCallback(this, backPressedCallback)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Используем XML layout с контейнером для навигации
        val rootView = inflater.inflate(R.layout.fragment_a_container, container, false)
        
        // Добавляем ComposeView для UI
        val composeView = ComposeView(requireContext()).apply {
            setContent {
                ScreenAContent(
                    isNested = isNested,
                    currentNavigationType = currentNavigationType.value,
                    onNavigationTypeSelected = { type ->
                        currentNavigationType.value = type
                        navigationHost.switchNavigationType(type)
                    },
                    onNavigateToB = {
                        navigationHost.navigateTo(Screen.ScreenB(recursionDepth = 0))
                    },
                    onNavigateToC = {
                        navigationHost.navigateTo(Screen.ScreenC)
                    }
                )
            }
        }
        
        // Заменяем содержимое контейнера на ComposeView
        (rootView as? ViewGroup)?.addView(composeView, 0)
        
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Создаем NavigationHost для этого экрана
        val factory = NavigationControllerFactory(DefaultFragmentFactory())
        navigationHost = NavigationHost(
            fragment = this,
            containerId = R.id.fragment_a_container,
            factory = factory,
            onEmptyBackStack = {
                if (isNested) {
                    nestedCallback?.onNestedScreenClosed()
                }
            }
        )
        
        // Устанавливаем начальный тип навигации
        currentNavigationType.value = NavigationType.FRAGMENT_MANAGER
    }

    fun setNestedCallback(callback: NestedNavigationCallback) {
        this.nestedCallback = callback
    }

    companion object {
        private const val ARG_IS_NESTED = "is_nested"
        
        fun newInstance(isNested: Boolean = false): FragmentA {
            return FragmentA().apply {
                arguments = Bundle().apply {
                    putBoolean(ARG_IS_NESTED, isNested)
                }
            }
        }
    }
}

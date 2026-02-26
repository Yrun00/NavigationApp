package com.github.navigationapp.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.github.navigationapp.NavigationViewModel
import com.github.navigationapp.navigation.navigationControllers.ScreenKey
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
// screens/ScreenAFragment.kt
class ScreenAFragment : Fragment() {

    private val viewModel: NavigationViewModel by activityViewModels()

    private val isNested: Boolean
        get() = arguments?.getBoolean(ARG_IS_NESTED) ?: false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                val currentMethod by if (isNested) {
                    viewModel.nestedMethod.collectAsState()
                } else {
                    viewModel.method.collectAsState()
                }

                ScreenAContent(
                    isNested = isNested,
                    currentNavigationType = currentMethod,
                    onNavigationTypeSelected = { type ->
                        if (isNested) {
                            viewModel.switchNestedMethod(type)
                        } else {
                            viewModel.switchMethod(type)
                        }
                    },
                    onNavigateToB = {
                        if (isNested) {
                            viewModel.navigateToNested(ScreenKey.B(depth = 0))
                        } else {
                            viewModel.navigateTo(ScreenKey.B(depth = 0))
                        }
                    },
                    onNavigateToC = {
                        if (isNested) {
                            viewModel.navigateToNested(ScreenKey.C)
                        } else {
                            viewModel.navigateTo(ScreenKey.C)
                        }
                    }
                )
            }
        }
    }

    companion object {
        private const val ARG_IS_NESTED = "is_nested"

        fun newInstance(isNested: Boolean): ScreenAFragment {
            return ScreenAFragment().apply {
                arguments = bundleOf(ARG_IS_NESTED to isNested)
            }
        }
    }
}

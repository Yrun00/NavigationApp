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

class ScreenAFragment : Fragment() {

    private val viewModel: NavigationViewModel by activityViewModels()

    private val nestingLevel: Int
        get() = arguments?.getInt(ARG_NESTING_LEVEL) ?: 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = ComposeView(requireContext()).apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        setContent {
            val nestingLevel = nestingLevel

            val currentMethod by viewModel.state(nestingLevel).method.collectAsState()

            ScreenAContent(
                nestingLevel = nestingLevel,
                currentNavigationType = currentMethod,
                onNavigationTypeSelected = {
                    viewModel.switchMethod(nestingLevel, it)
                },
                onNavigateToB = {
                    val key = ScreenKey.B(depth = 0, level = nestingLevel)
                    viewModel.push(nestingLevel, key)
                    findNavigationHost().navigateTo(key)
                },
                onNavigateToC = {
                    val key = ScreenKey.C(level = nestingLevel + 1)
                    viewModel.push(nestingLevel, key)
                    findNavigationHost().navigateTo(key)
                },
            )
        }
    }

    companion object {
        const val ARG_NESTING_LEVEL = "nesting_level"

        fun newInstance(nestingLevel: Int = 0) = ScreenAFragment().apply {
            arguments = bundleOf(ARG_NESTING_LEVEL to nestingLevel)
        }
    }
}

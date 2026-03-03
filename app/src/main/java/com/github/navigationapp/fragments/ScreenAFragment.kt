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
import com.github.navigationapp.navigation.ScreenKey

class ScreenAFragment : Fragment() {

    private val viewModel: NavigationViewModel by activityViewModels()

    private val level: Int
        get() = arguments?.getInt(ARG_LEVEL) ?: 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = ComposeView(requireContext()).apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        setContent {
            val level = level

            val currentMethod by viewModel.state(level).method.collectAsState()

            ScreenAContent(
                nestingLevel = level,
                currentNavigationType = currentMethod,
                onNavigationTypeSelected = {
                    viewModel.switchMethod(level, it)
                },
                onNavigateToB = {
                    val key = ScreenKey.B(depth = 0)
                    viewModel.push(level, key)
                    findNavigationHost().navigateTo(key)
                },
                onNavigateToC = {
                    val key = ScreenKey.C(level = level + 1)
                    viewModel.push(level, key)
                    findNavigationHost().navigateTo(key)
                },
            )
        }
    }

    companion object {
        const val ARG_LEVEL = "level"

        fun newInstance(level: Int = 0) = ScreenAFragment().apply {
            arguments = bundleOf(ARG_LEVEL to level)
        }
    }
}

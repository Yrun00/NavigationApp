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


class ScreenBFragment : Fragment() {

    private val viewModel: NavigationViewModel by activityViewModels()

    private val depthFromBundle: Int
        get() = arguments?.getInt(ARG_DEPTH) ?: 0

    private val nestingLevel: Int
        get() = arguments?.getInt(ARG_NESTING_LEVEL) ?: 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = ComposeView(requireContext()).apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        setContent {

            val stack by viewModel.state(nestingLevel).stack.collectAsState()

            val backStackDepth = stack.takeLastWhile { it is ScreenKey.B }.size - 1

            ScreenBContent(
                recursionDepthFromBundle = depthFromBundle,
                backStackDepth = backStackDepth,
                onOpenAnotherB = {
                    val key = ScreenKey.B(depth = depthFromBundle + 1, nestingLevel = nestingLevel)
                    viewModel.push(nestingLevel, key)
                    findNavigationHost().navigateTo(key)
                },
            )
        }
    }

    companion object {
        const val ARG_DEPTH = "depth"
        const val ARG_NESTING_LEVEL = "nesting_level"

        fun newInstance(depth: Int, nestingLevel: Int = 0) = ScreenBFragment().apply {
            arguments = bundleOf(
                ARG_DEPTH to depth,
                ARG_NESTING_LEVEL to nestingLevel,
            )
        }
    }
}

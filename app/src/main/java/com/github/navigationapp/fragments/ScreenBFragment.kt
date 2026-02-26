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
 * FragmentB - рекурсивный экран
 *
 * Особенности:
 * - Получает recursionDepth из Bundle (передается в аргументах)
 * - Вычисляет глубину backstack через NavigationHost родителя
 * - Может открывать новый экземпляр FragmentB с depth + 1
 * - Считает только прямую рекурсию (B -> B -> B)
 */
@AndroidEntryPoint
// screens/ScreenBFragment.kt
class ScreenBFragment : Fragment() {

    private val viewModel: NavigationViewModel by activityViewModels()

    private val depthFromBundle: Int
        get() = arguments?.getInt(ARG_DEPTH) ?: 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                val stack by viewModel.stack.collectAsState()
                val backStackDepth = (stack.count { it is ScreenKey.B } - 1)

                ScreenBContent(
                    recursionDepthFromBundle = depthFromBundle,
                    backStackDepth = backStackDepth,
                    onOpenAnotherB = {
                        viewModel.navigateTo(ScreenKey.B(depth = depthFromBundle + 1))
                    },
                )
            }
        }
    }

    companion object {
        private const val ARG_DEPTH = "depth"

        fun newInstance(depth: Int): ScreenBFragment {
            return ScreenBFragment().apply {
                arguments = bundleOf(ARG_DEPTH to depth)
            }
        }
    }
}

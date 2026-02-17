package com.github.navigationapp.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import com.github.navigationapp.navigation.NavigationHost
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
class FragmentB : Fragment() {


    private val recursionDepth: Int by lazy {
        arguments?.getInt(ARG_RECURSION_DEPTH) ?: 0
    }

    // NavigationHost передается извне
    lateinit var navigationHost: NavigationHost

    override fun onAttach(context: Context) {
        super.onAttach(context)
        val parent = parentFragment
        if (parent is FragmentA) {
            parent.navigationHost.let { host ->
                settNavigationHost(host)
            }
        }
    }

    // Реактивное состояние для обновления UI
    private val backStackDepthState = mutableStateOf(0)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                ScreenBContent(
                    recursionDepthFromBundle = recursionDepth,
                    backStackDepth = backStackDepthState.value,
                    onOpenAnotherB = {
                        navigationHost?.navigateTo(
                            com.github.navigationapp.navigation.Screen.ScreenB(
                                recursionDepth = recursionDepth + 1,
                            ),
                        )
                    },
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Обновляем размер backstack при возобновлении
        updateBackStackDepth()
    }

    /**
     * Метод вызывается родителем для передачи NavigationHost
     */
    fun settNavigationHost(host: NavigationHost) {
        this.navigationHost = host
        updateBackStackDepth()
    }

    private fun updateBackStackDepth() {
        navigationHost?.let {
            backStackDepthState.value = it.getBackStackSize()
        }
    }

    companion object {
        private const val ARG_RECURSION_DEPTH = "recursion_depth"

        fun newInstance(recursionDepth: Int = 0): FragmentB {
            return FragmentB().apply {
                arguments = Bundle().apply {
                    putInt(ARG_RECURSION_DEPTH, recursionDepth)
                }
            }
        }
    }
}

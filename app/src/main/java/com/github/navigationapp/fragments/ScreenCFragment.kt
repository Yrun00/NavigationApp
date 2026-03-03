package com.github.navigationapp.fragments

import android.os.Bundle
import android.view.View
import androidx.activity.addCallback
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.github.navigationapp.NavigationHostDelegate
import com.github.navigationapp.NavigationViewModel
import com.github.navigationapp.R
import com.github.navigationapp.navigation.NavigationHost
import com.github.navigationapp.navigation.NavigationMethod
import com.github.navigationapp.navigation.ScreenKey
import com.github.navigationapp.navigation.navigationControllers.NoAnimFragmentStateChanger
import com.github.terrakok.cicerone.androidx.AppNavigator
import com.zhuinden.simplestack.StateChanger
import kotlinx.coroutines.flow.StateFlow

class ScreenCFragment : Fragment(R.layout.fragment_c), NavigationHost {

    private val viewModel: NavigationViewModel by activityViewModels()

    override val level: Int get() = arguments?.getInt(ARG_LEVEL) ?: 1

    private var delegate: NavigationHostDelegate? = null

    private val ciceroneNavigator by lazy {
        object : AppNavigator(requireActivity(), R.id.nested_fragment_container, childFragmentManager) {}
    }

    private val simpleStateChanger by lazy {
        StateChanger { stateChange, callback ->
            NoAnimFragmentStateChanger(childFragmentManager, R.id.nested_fragment_container)
                .handleStateChange(stateChange)
            callback.stateChangeComplete()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        delegate = NavigationHostDelegate(
            fragmentManager = childFragmentManager,
            containerId = R.id.nested_fragment_container,
            level = level,
            viewModel = viewModel,
            getCiceroneNavigator = { ciceroneNavigator },
            getSimpleStateChanger = { simpleStateChanger },
            onEmptyStack = { closeThisLevel() },
        ).also { it.initialize(savedInstanceState, viewLifecycleOwner) }

        requireActivity().onBackPressedDispatcher
            .addCallback(viewLifecycleOwner) { checkNotNull(delegate).handleBack() }
    }

    override fun navigateTo(key: ScreenKey) = checkNotNull(delegate).navigateTo(key)

    override fun observeBackStackDepth(): StateFlow<Int> = checkNotNull(delegate).observeBackStackDepth()

    override fun onResume() {
        super.onResume()
        delegate?.onResume()
    }

    override fun onPause() {
        delegate?.onPause()
        super.onPause()
    }

    override fun onDestroyView() {
        delegate?.onDestroyView()
        delegate = null
        super.onDestroyView()
    }

    private fun closeThisLevel() {
        viewModel.closeLevel(level)
        val parentLevel = level - 1
        when (viewModel.state(parentLevel).method.value) {
            NavigationMethod.SIMPLE_STACK ->
                viewModel.state(parentLevel).simpleBackstack.goBack()
            NavigationMethod.JETPACK ->
                findNavController().popBackStack()
            else ->
                parentFragmentManager.popBackStack()
        }
    }

    companion object {
        const val ARG_LEVEL = "level"

        fun newInstance(level: Int = 1) = ScreenCFragment().apply {
            arguments = bundleOf(ARG_LEVEL to level)
        }
    }
}

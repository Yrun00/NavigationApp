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
import com.github.navigationapp.navigation.navigationControllers.NoAnimFragmentStateChanger
import com.github.navigationapp.navigation.navigationControllers.ScreenKey
import com.github.terrakok.cicerone.androidx.AppNavigator
import com.zhuinden.simplestack.StateChanger
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class ScreenCFragment : Fragment(R.layout.fragment_c), NavigationHost {

    private val viewModel: NavigationViewModel by activityViewModels()
    private val hostingLevel: Int get() = arguments?.getInt(ARG_HOSTING_LEVEL) ?: 1

    private lateinit var delegate: NavigationHostDelegate

    private val ciceroneNavigator by lazy {
        object :
            AppNavigator(requireActivity(), R.id.nested_fragment_container, childFragmentManager) {}
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

        // Каждый раз новый delegate со свежим viewLifecycleOwner
        delegate = NavigationHostDelegate(
            fragmentManager = childFragmentManager,
            containerId = R.id.nested_fragment_container,
            level = hostingLevel,
            viewModel = viewModel,
            getCiceroneNavigator = { ciceroneNavigator },
            getSimpleStateChanger = { simpleStateChanger },
            onEmptyStack = { closeThisLevel() },
        )

        delegate.initialize(savedInstanceState, viewLifecycleOwner)  // ← свежий lifecycleOwner

        requireActivity().onBackPressedDispatcher
            .addCallback(viewLifecycleOwner) { delegate.handleBack() }
    }

    override fun navigateTo(key: ScreenKey) = delegate.navigateTo(key)
    override fun onResume() {
        super.onResume(); delegate.onResume()
    }

    override fun onPause() {
        delegate.onPause(); super.onPause()
    }

    override fun onDestroyView() {
        delegate.onDestroyView(); super.onDestroyView()
    }

    private fun closeThisLevel() {
        val parentLevel = hostingLevel - 1
        viewModel.closeLevel(hostingLevel) // ViewModel: чистит уровень + делает pop у parentLevel

        when (viewModel.state(parentLevel).method.value) {
            NavigationMethod.SIMPLE_STACK ->
                // FM back stack не используется — нужно идти через сам Backstack
                viewModel.state(parentLevel).simpleBackstack.goBack()

            NavigationMethod.JETPACK ->
                // ScreenCFragment живёт внутри NavHostFragment — findNavController()
                // поднимается по view hierarchy и находит нужный NavController уровня parentLevel
                findNavController().popBackStack()

            else -> // FRAGMENT_MANAGER + CICERONE — оба добавляют в FM back stack
                parentFragmentManager.popBackStack()
        }
    }

    companion object {
        const val ARG_HOSTING_LEVEL = "hosting_level"
        fun newInstance(hostingLevel: Int = 1) = ScreenCFragment().apply {
            arguments = bundleOf(ARG_HOSTING_LEVEL to hostingLevel)
        }
    }
}


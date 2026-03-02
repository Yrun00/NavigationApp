package com.github.navigationapp

import android.os.Bundle
import androidx.activity.addCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.github.navigationapp.navigation.NavigationHost
import com.github.navigationapp.navigation.navigationControllers.NoAnimFragmentStateChanger
import com.github.navigationapp.navigation.navigationControllers.ScreenKey
import com.github.terrakok.cicerone.androidx.AppNavigator
import com.zhuinden.simplestack.StateChanger



class MainActivity : AppCompatActivity(), NavigationHost {

    private val viewModel: NavigationViewModel by viewModels()

    private val delegate by lazy {
        NavigationHostDelegate(
            fragmentManager = supportFragmentManager,
            containerId = R.id.root_fragment_container,
            level = 0,
            viewModel = viewModel,
            getCiceroneNavigator = { ciceroneNavigator },
            getSimpleStateChanger = { simpleStateChanger },
            onEmptyStack = { finish() },
        )
    }

    private val ciceroneNavigator by lazy {
        object : AppNavigator(this, R.id.root_fragment_container, supportFragmentManager) {}
    }

    private val simpleStateChanger by lazy {
        StateChanger { stateChange, callback ->
            NoAnimFragmentStateChanger(
                supportFragmentManager,
                R.id.root_fragment_container,
            )
                .handleStateChange(stateChange)
            callback.stateChangeComplete()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        delegate.initialize(savedInstanceState, this)  // Activity — lifecycleOwner сам

        onBackPressedDispatcher.addCallback(this) { delegate.handleBack() }
    }

    override fun navigateTo(key: ScreenKey) = delegate.navigateTo(key)

    override fun onResume() {
        super.onResume()
        delegate.onResume()
    }

    override fun onPause() {
        delegate.onPause()
        super.onPause()
    }

    override fun onDestroy() {
        delegate.onDestroyView()
        super.onDestroy()
    }
}

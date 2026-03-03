package com.github.navigationapp.navigation.navigationControllers

import androidx.annotation.IdRes
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.commitNow
import com.zhuinden.simplestack.Backstack
import com.zhuinden.simplestack.StateChanger

class SimpleStackRouter(
    private val backstack: Backstack,
    private val fragmentManager: FragmentManager,
    @IdRes private val containerId: Int,
    private val getStateChanger: () -> StateChanger,
) : NavigationRouter {

    override fun navigateTo(key: ScreenKey) = backstack.goTo(key)

    override fun back(): Boolean = backstack.goBack()

    override fun attach() = backstack.setStateChanger(getStateChanger())

    override fun detach() = backstack.detachStateChanger()

    override fun clearContainer() {
        val stranded = fragmentManager.fragments
            .filter { it.id == containerId && !it.isRemoving }
        if (stranded.isNotEmpty()) {
            fragmentManager.commitNow { stranded.forEach { remove(it) } }
        }
    }
}

package com.github.navigationapp.navigation

import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import com.github.navigationapp.R
import com.github.navigationapp.fragments.ScreenAFragment
import com.github.navigationapp.fragments.ScreenBFragment
import com.github.navigationapp.fragments.ScreenCFragment
import com.github.terrakok.cicerone.androidx.FragmentScreen
import com.zhuinden.simplestackextensions.fragments.DefaultFragmentKey
import kotlinx.parcelize.Parcelize

@Parcelize
sealed class ScreenKey : DefaultFragmentKey() {

    abstract val level: Int

    data class A(override val level: Int) : ScreenKey()
    data class B(val depth: Int, override val level: Int) : ScreenKey()
    data class C(override val level: Int) : ScreenKey()

    override fun getFragmentTag(): String = when (this) {
        is A -> "screen_a"
        is B -> "screen_b_$depth"
        is C -> "screen_c_$level"
    }

    public override fun instantiateFragment(): Fragment = when (this) {
        is A -> ScreenAFragment.newInstance(nestingLevel = level)
        is B -> ScreenBFragment.newInstance(depth = depth, nestingLevel = level)
        is C -> ScreenCFragment.newInstance(hostingLevel = level)
    }

    fun toDestinationId(): Int = when (this) {
        is A -> R.id.screenAFragment
        is B -> R.id.screenBFragment
        is C -> R.id.screenCFragment
    }

    fun toBundle(): Bundle = when (this) {
        is A -> bundleOf(
            ScreenAFragment.ARG_NESTING_LEVEL to level,
        )
        is B -> bundleOf(
            ScreenBFragment.ARG_DEPTH to depth,
            ScreenBFragment.ARG_NESTING_LEVEL to level,
        )
        is C -> bundleOf(
            ScreenCFragment.ARG_HOSTING_LEVEL to level,
        )
    }

    fun toCiceroneScreen(): FragmentScreen = FragmentScreen(key = getFragmentTag()) {
        instantiateFragment().also { fragment ->
            fragment.arguments = (fragment.arguments ?: Bundle()).apply {
                putParcelable(ARG_SCREEN_KEY, this@ScreenKey)
            }
        }
    }

    companion object {
        const val ARG_SCREEN_KEY = "screen_key"
    }
}
package com.github.navigationapp.navigation.navigationControllers

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

    object A : ScreenKey()
    data class B(val depth: Int) : ScreenKey()
    object C : ScreenKey()

    fun tag(): String = when (this) {
        is A -> "screen_a"
        is B -> "screen_b_$depth"
        is C -> "screen_c"
    }

    public override fun instantiateFragment(): Fragment = when (this) {
        is A -> ScreenAFragment.newInstance(isNested = false)
        is B -> ScreenBFragment.newInstance(depth = this.depth)
        is C -> ScreenCFragment.newInstance()
    }

    override fun getFragmentTag(): String = tag()

    fun toDestinationId(): Int = when (this) {
        is ScreenKey.A -> R.id.screenAFragment
        is ScreenKey.B -> R.id.screenBFragment
        is ScreenKey.C -> R.id.screenCFragment
    }

    fun toBundle(): Bundle = bundleOf("screen_key" to this)

    fun toCiceroneScreen(): FragmentScreen = when (this) {
        is ScreenKey.A -> FragmentScreen(key = this.tag()) {
            ScreenAFragment.newInstance(isNested = false).also {
                it.arguments = (it.arguments ?: Bundle()).apply {
                    putParcelable("screen_key", this@ScreenKey)
                }
            }
        }

        is ScreenKey.B -> FragmentScreen(key = this.tag()) {
            ScreenBFragment.newInstance(depth = this.depth).also {
                it.arguments = (it.arguments ?: Bundle()).apply {
                    putParcelable("screen_key", this@ScreenKey)
                }
            }
        }

        is ScreenKey.C -> FragmentScreen(key = this.tag()) {
            ScreenCFragment.newInstance().also {
                it.arguments = (it.arguments ?: Bundle()).apply {
                    putParcelable("screen_key", this@ScreenKey)
                }
            }
        }
    }

}
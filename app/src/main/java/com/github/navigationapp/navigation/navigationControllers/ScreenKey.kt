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

    data class A(val nestingLevel: Int = 0) : ScreenKey()
    data class B(val depth: Int, val nestingLevel: Int = 0) : ScreenKey()
    data class C(val hostingLevel: Int = 1) : ScreenKey()

    fun tag(): String = when (this) {
        is A -> "screen_a"
        is B -> "screen_b_$depth"       // depth уже уникален в рамках одного FM
        is C -> "screen_c_$hostingLevel" // hostingLevel нужен — C-в-C в одном FM
    }

    override fun getFragmentTag(): String = tag()

    public override fun instantiateFragment(): Fragment = when (this) {
        is A -> ScreenAFragment.newInstance(nestingLevel = nestingLevel)
        is B -> ScreenBFragment.newInstance(depth = depth, nestingLevel = nestingLevel)
        is C -> ScreenCFragment.newInstance(hostingLevel = hostingLevel)
    }

    fun toDestinationId(): Int = when (this) {
        is A -> R.id.screenAFragment
        is B -> R.id.screenBFragment
        is C -> R.id.screenCFragment
    }

    fun toBundle(): Bundle = bundleOf("screen_key" to this)

    fun toCiceroneScreen(): FragmentScreen = FragmentScreen(key = tag()) {
        instantiateFragment().also { fragment ->
            fragment.arguments = (fragment.arguments ?: Bundle()).apply {
                putParcelable("screen_key", this@ScreenKey)
            }
        }
    }
}

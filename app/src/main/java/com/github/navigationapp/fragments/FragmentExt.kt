package com.github.navigationapp.fragments

import androidx.fragment.app.Fragment
import com.github.navigationapp.navigation.NavigationHost

fun Fragment.findNavigationHost(): NavigationHost {
    var parent: Fragment? = parentFragment
    while (parent != null) {
        if (parent is NavigationHost) {
            return parent
        }
        parent = parent.parentFragment
    }
    return requireActivity() as NavigationHost
}
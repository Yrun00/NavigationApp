package com.github.navigationapp.fragments

import android.util.Log
import androidx.fragment.app.Fragment
import com.github.navigationapp.navigation.NavigationHost

fun Fragment.findNavigationHost(): NavigationHost {
    var parent: Fragment? = parentFragment
    while (parent != null) {
        Log.d("NAV_HOST", "checking ${parent::class.simpleName} — NavigationHost? ${parent is NavigationHost}")
        if (parent is NavigationHost) {
            Log.d("NAV_HOST", "FOUND: ${parent::class.simpleName}")
            return parent
        }
        parent = parent.parentFragment
    }
    Log.d("NAV_HOST", "FOUND Activity")
    return requireActivity() as NavigationHost
}
package com.github.navigationapp.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import com.github.navigationapp.R
import com.github.navigationapp.navigation.NavigationHost
import com.github.navigationapp.navigation.NestedNavigationCallback
import dagger.hilt.android.AndroidEntryPoint

/**
 * FragmentC - экран с вложенным контейнером для ScreenA
 * 
 * Особенности:
 * - Содержит Box с отступами (через XML)
 * - Внутри Box размещен вложенный экземпляр FragmentA
 * - При закрытии вложенного A - закрывается сам FragmentC
 */
@AndroidEntryPoint
class FragmentC : Fragment() {

    private var parentNavigationHost: NavigationHost? = null
    
    private val backPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            // FragmentC просто закрывается, навигация внутри вложенного A
            // обрабатывается самим FragmentA
            parentNavigationHost?.goBack()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireActivity().onBackPressedDispatcher.addCallback(this, backPressedCallback)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Используем XML layout с контейнером для вложенного фрагмента
        return inflater.inflate(R.layout.fragment_c, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Создаем вложенный FragmentA только при первом создании
        if (savedInstanceState == null) {
            // Создаем вложенный FragmentA
            val nestedFragmentA = FragmentA.newInstance(isNested = true)
            
            // Устанавливаем callback для уведомления о закрытии
            nestedFragmentA.setNestedCallback(object : NestedNavigationCallback {
                override fun onNestedScreenClosed() {
                    // Закрываем FragmentC через родительскую навигацию
                    parentNavigationHost?.goBack()
                }
            })
            
            // Добавляем вложенный фрагмент
            childFragmentManager.beginTransaction()
                .replace(R.id.nested_fragment_container, nestedFragmentA)
                .commit()
        }
    }

    /**
     * Метод вызывается родителем для передачи NavigationHost
     */
    fun setParentNavigationHost(host: NavigationHost) {
        this.parentNavigationHost = host
    }

    companion object {
        fun newInstance(): FragmentC {
            return FragmentC()
        }
    }
}

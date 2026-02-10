package com.github.navigationapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.github.navigationapp.fragments.FragmentA
import dagger.hilt.android.AndroidEntryPoint

/**
 * MainActivity - главная Activity приложения
 *
 * Использует Single-Activity архитектуру:
 * - Содержит только один контейнер для корневого FragmentA
 * - Вся навигация происходит через фрагменты
 */
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Загружаем корневой FragmentA только при первом запуске
        if (savedInstanceState == null) {
            val fragmentA = FragmentA.newInstance(isNested = false)

            supportFragmentManager.beginTransaction()
                .replace(R.id.root_fragment_container, fragmentA)
                .commit()
        }
    }
}

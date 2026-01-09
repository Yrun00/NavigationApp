package com.github.navigationapp.Fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat.enableEdgeToEdge
import androidx.fragment.app.Fragment
import com.github.navigationapp.BaseViewModel
import com.github.navigationapp.MainActivity
import com.github.navigationapp.ui.theme.NavigationAppTheme

class FragmentB : Fragment() {
    private val viewModel: BaseViewModel by lazy {
        (requireActivity() as MainActivity).viewModel
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = androidx.compose.ui.platform.ComposeView(requireContext()).apply {
        setContent {
            ScreenBContent(viewModel)
        }

    }

    @Composable
    fun ScreenBContent(
        viewModel: BaseViewModel
    ) {
        val backStackDepth by viewModel.getBackStackDepth().collectAsState(initial = 0)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Green)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Screen B",
                fontSize = 32.sp,
                color = Color.White,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            // Счётчик глубины по back stack'у
            Text(
                text = "Back Stack Depth: $backStackDepth",
                fontSize = 20.sp,
                color = Color.White,
                modifier = Modifier.padding(bottom = 16.dp)
            )


            // Кнопка открыть ещё одну ScreenB
            Button(
                onClick = { viewModel.openBFromB() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Open Another Screen B")
            }
        }
    }
}
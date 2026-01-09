package com.github.navigationapp.Fragments


import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.github.navigationapp.BaseViewModel
import com.github.navigationapp.MainActivity
import com.github.navigationapp.NavigationManager
import com.github.navigationapp.Screen


class FragmentA : Fragment() {

    private val viewModel: BaseViewModel by lazy {
        (requireActivity() as MainActivity).viewModel
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = androidx.compose.ui.platform.ComposeView(requireContext()).apply {
        setContent {
            ScreenAContent(
                viewModel = viewModel,
                onNavigate = { screen ->
                    when (screen) {
                        Screen.ScreenA -> {} // Главный ScreenA, ничего не делаем
                        Screen.ScreenB -> viewModel.openBFromA()
                        Screen.ScreenC -> viewModel.openCFromA()
                    }
                }
            )
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }
}

@Composable
fun ScreenAContent(
    viewModel: BaseViewModel,
    onNavigate: (Screen) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Red)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Screen A",
            fontSize = 32.sp,
            color = Color.White,
            modifier = Modifier.padding(top = 32.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Две кнопки для навигации
        Button(
            onClick = { onNavigate(Screen.ScreenB) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Go to Screen B")
        }

        Button(
            onClick = { onNavigate(Screen.ScreenC) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Go to Screen C")
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Селектор метода навигации
        Text(
            text = "Navigation Method",
            fontSize = 20.sp,
            color = Color.White
        )

        Button(
            onClick = { /* TODO: Switch to FragmentManager */ },
            modifier = Modifier.fillMaxWidth(),
            enabled = false
        ) {
            Text("FragmentManager")
        }

        Button(
            onClick = { /* TODO: Switch to Jetpack Navigation */ },
            modifier = Modifier.fillMaxWidth(),
            enabled = false
        ) {
            Text("Jetpack Navigation")
        }

        Button(
            onClick = { /* TODO: Switch to Conductor */ },
            modifier = Modifier.fillMaxWidth(),
            enabled = false
        ) {
            Text("Conductor")
        }

        Button(
            onClick = { /* TODO: Switch to Custom Router */ },
            modifier = Modifier.fillMaxWidth(),
            enabled = false
        ) {
            Text("Custom Router")
        }
    }
}
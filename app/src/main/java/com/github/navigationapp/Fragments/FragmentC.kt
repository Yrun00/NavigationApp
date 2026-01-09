package com.github.navigationapp.Fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import androidx.compose.ui.platform.ComposeView
import com.github.navigationapp.BaseViewModel
import com.github.navigationapp.MainActivity
import com.github.navigationapp.Screen
import kotlinx.coroutines.flow.MutableSharedFlow

class FragmentC : Fragment() {

    private lateinit var viewModel: BaseViewModel
    private lateinit var closeFlow: MutableSharedFlow<Unit>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = (requireActivity() as MainActivity).viewModel
        closeFlow = MutableSharedFlow<Unit>(replay = 0)
        viewModel.registerScreenCCloseFlow(closeFlow)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = ComposeView(requireContext()).apply {
        setContent {
            ScreenCContent(
                viewModel = viewModel,
                closeFlow = closeFlow
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.unregisterScreenCCloseFlow(closeFlow)
    }
}

@Composable
fun ScreenCContent(
    viewModel: BaseViewModel,
    closeFlow: MutableSharedFlow<Unit>
) {
    // Слушаем сигнал о закрытии вложенного ScreenA
    LaunchedEffect(Unit) {
        closeFlow.collect {
            viewModel.goBack()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Blue)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Screen C",
            fontSize = 28.sp,
            color = Color.White,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        // Box с отступами, содержащий вложенный ScreenA
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .background(Color.Yellow)
        ) {
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
}
package com.github.navigationapp.fragments

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ScreenCContent() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF64B5F6)) // Синий фон
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Screen C",
            fontSize = 28.sp,
            color = Color.White,
            modifier = Modifier.padding(vertical = 16.dp)
        )
        
        Text(
            text = "Box with nested Screen A inside:",
            fontSize = 14.sp,
            color = Color.White.copy(alpha = 0.8f),
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        // Box с отступами, содержащий вложенный FragmentA
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .background(Color.White.copy(alpha = 0.1f))
                .border(3.dp, Color.White)
        ) {
            // Здесь будет размещен вложенный FragmentA через FragmentContainerView
            // В XML layout: fragment_c.xml
        }
    }
}

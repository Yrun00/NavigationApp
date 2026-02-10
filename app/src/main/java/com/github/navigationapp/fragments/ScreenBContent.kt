package com.github.navigationapp.fragments

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ScreenBContent(
    recursionDepthFromBundle: Int,
    backStackDepth: Int,
    onOpenAnotherB: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF81C784)) // Зеленый фон
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

        // Счетчик глубины из Bundle (прямая рекурсия)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .background(Color.White.copy(alpha = 0.2f))
                .border(2.dp, Color.White)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Recursion Depth (Bundle):",
                    fontSize = 16.sp,
                    color = Color.White
                )
                Text(
                    text = "$recursionDepthFromBundle",
                    fontSize = 48.sp,
                    color = Color(0xFFFFEB3B)
                )
                Text(
                    text = "Direct recursion only (B -> B -> B)",
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.7f)
                )
            }
        }

        // Счетчик глубины BackStack
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .background(Color.White.copy(alpha = 0.2f))
                .border(2.dp, Color.White)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "BackStack Depth:",
                    fontSize = 16.sp,
                    color = Color.White
                )
                Text(
                    text = "$backStackDepth",
                    fontSize = 48.sp,
                    color = Color(0xFFFF9800)
                )
                Text(
                    text = "Total screens in backstack",
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.7f)
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Кнопка рекурсивного открытия
        Button(
            onClick = onOpenAnotherB,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2))
        ) {
            Text("Open Another Screen B", fontSize = 18.sp)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Press Back to return (depth will decrease)",
            fontSize = 12.sp,
            color = Color.White.copy(alpha = 0.7f)
        )
    }
}
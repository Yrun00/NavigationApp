package com.github.navigationapp.fragments

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.navigationapp.navigation.*

/**
 * FragmentA - главный экран с селектором типа навигации
 * 
 * Может быть:
 * 1. Корневым экраном (isNested = false)
 * 2. Вложенным внутри FragmentC (isNested = true)
 * 
 * Каждый экземпляр имеет свой собственный NavigationHost
 */


@Composable
fun ScreenAContent(
    isNested: Boolean,
    currentNavigationType: NavigationMethod,
    onNavigationTypeSelected: (NavigationMethod) -> Unit,
    onNavigateToB: () -> Unit,
    onNavigateToC: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFE57373)) // Красный фон
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = if (isNested) "Screen A (Nested)" else "Screen A",
            fontSize = 32.sp,
            color = Color.White,
            modifier = Modifier.padding(top = 32.dp)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Кнопки навигации
        Button(
            onClick = onNavigateToB,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2))
        ) {
            Text("Go to Screen B", fontSize = 18.sp)
        }
        
        Button(
            onClick = onNavigateToC,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF388E3C))
        ) {
            Text("Go to Screen C", fontSize = 18.sp)
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Селектор типа навигации
        Text(
            text = "Navigation Method:",
            fontSize = 20.sp,
            color = Color.White
        )
        
        Text(
            text = "Current: ${currentNavigationType.name}",
            fontSize = 14.sp,
            color = Color.White.copy(alpha = 0.8f)
        )
        
        Spacer(modifier = Modifier.height(8.dp))

        NavigationMethod.entries.forEach { type ->
            val isSelected = currentNavigationType == type
            Button(
                onClick = { onNavigationTypeSelected(type) },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isSelected) 
                        Color(0xFFFFA726) 
                    else 
                        Color(0xFF757575)
                )
            ) {
                Text(
                    text = type.name.replace("_", " "),
                    fontSize = 16.sp
                )
            }
        }
        
        if (isNested) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "This is a nested instance with independent navigation",
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.7f)
            )
        }
    }
}

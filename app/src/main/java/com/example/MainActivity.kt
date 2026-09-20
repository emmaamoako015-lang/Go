package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.example.data.ChatRepository
import com.example.ui.components.DynamicChromaticBackground
import com.example.ui.screens.HomeScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        val context = LocalContext.current
        val repository = remember { ChatRepository(context = context.applicationContext) }
        DynamicChromaticBackground(
          modifier = Modifier.fillMaxSize()
        ) {
          HomeScreen(repository = repository)
        }
      }
    }
  }
}


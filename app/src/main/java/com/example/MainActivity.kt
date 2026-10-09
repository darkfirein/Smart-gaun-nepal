package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.security.FirebaseAppCheckManager
import com.example.ui.screens.HomeScreen
import com.example.ui.theme.SmartGaunTheme
import com.example.viewmodel.HomeViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseAppCheckManager.initialize(applicationContext)
        enableEdgeToEdge()
        setContent {
            SmartGaunTheme {
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("smart_gaun_root")
                ) {
                    val homeViewModel: HomeViewModel = viewModel()
                    HomeScreen(viewModel = homeViewModel)
                }
            }
        }
    }
}

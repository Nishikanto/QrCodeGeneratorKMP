package com.caplyptasapps.qrcodegeneratorkmp

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun App() {
    Scaffold { paddingValues ->
        MainScreen(
            modifier = Modifier.fillMaxSize().padding(paddingValues),
            qrGenerator = QrCodeGeneratorImpl()
        )
    }
}
package com.caplyptasapps.qrcodegeneratorkmp.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.caplyptasapps.qrcodegeneratorkmp.MainScreen
import com.caplyptasapps.qrcodegeneratorkmp.QrCodeGeneratorImpl

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Scaffold { paddingValues ->
                    MainScreen(
                        modifier = Modifier.fillMaxSize().padding(paddingValues),
                        qrGenerator = QrCodeGeneratorImpl(this)
                    )
                }
            }
        }
    }
}


@Preview(showBackground = true, showSystemUi = true)
@Composable
fun DefaultPreview() {
    MyApplicationTheme {
        Scaffold { paddingValues ->
            MainScreen(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                qrGenerator = QrCodeGeneratorImpl(LocalContext.current)
            )
        }
    }
}


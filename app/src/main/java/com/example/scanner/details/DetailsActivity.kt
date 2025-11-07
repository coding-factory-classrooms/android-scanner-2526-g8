package com.example.scanner.details

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.scanner.ui.theme.ScannerTheme

class DetailsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val recordId = intent.getStringExtra("record_id")

        enableEdgeToEdge()

        setContent {
            ScannerTheme {
                DetailsScreen(recordId)
            }
        }
    }
}

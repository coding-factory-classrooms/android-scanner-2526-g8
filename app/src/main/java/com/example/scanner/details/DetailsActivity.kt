package com.example.scanner.details

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.scanner.ui.theme.ScannerTheme
import com.example.scanner.list.ListActivity

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

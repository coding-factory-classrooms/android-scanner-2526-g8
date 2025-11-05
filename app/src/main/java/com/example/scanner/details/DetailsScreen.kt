package com.example.scanner.details

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import com.example.scanner.ui.theme.ScannerTheme

@Composable
fun DetailsScreen(context: Context, intent: Intent, onFinish: () -> Unit) {
    val filename = intent.getStringExtra("photo_filename")
    val text = intent.getStringExtra("sendImageToAPI")
    val bmp = filename?.let {
        context.openFileInput(it).use { ins ->
            BitmapFactory.decodeStream(ins)
        }
    }
    ScannerTheme {
        Scaffold(
            topBar = {
                @OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
                TopAppBar(
                    title = { Text("Résultat") },
                    navigationIcon = {
                        IconButton(onClick = { onFinish() }) {
                            Icon(Icons.Filled.ArrowBack, contentDescription = "Retour")
                        }
                    }
                )
            }
        ) { inner ->
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(inner)
                    .padding(16.dp)
            ) {
                if (bmp != null) {
                    Image(bitmap = bmp.asImageBitmap(), contentDescription = null)
                    Text("${text}")
                } else {
                    Text("Aucune image reçue")
                }
            }
        }
    }
}

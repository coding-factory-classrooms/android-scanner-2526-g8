// kotlin
package com.example.scanner.details

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.example.scanner.Paper.PhotoRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailsScreen(
    context: Context,
    intent: Intent,
    onFinish: () -> Unit
) {

    // j'extrait les infos de l'intent, le photo_ID est pour la version on clique sur un détail et celle de legacy est pour la version je prend la photo
    val photoId = remember(intent) { intent.getStringExtra("PHOTO_ID") }
    val legacyFileName = remember(intent) { intent.getStringExtra("photo_filename") }
    val legacyText = remember(intent) { intent.getStringExtra("photo_text_content") }

    val recordFromRepo = remember(photoId) { photoId?.let { PhotoRepository.get(it) } }

    val displayBitmap = remember(recordFromRepo, legacyFileName) {
        recordFromRepo?.let { BitmapFactory.decodeFile(it.imagePath) }
            ?: legacyFileName?.let { fn ->
                context.openFileInput(fn).use { BitmapFactory.decodeStream(it) }
            }
    }
    val displayText = recordFromRepo?.text ?: (legacyText ?: "")
    val displayDate = recordFromRepo?.createdAtDisplay
    val displayTranslation = recordFromRepo?.translatedText

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Détails") },
                navigationIcon = {
                    IconButton(onClick = onFinish) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Retour")
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            if (displayBitmap == null && displayText.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Aucune donnée.")
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    displayBitmap?.let {
                        Image(
                            bitmap = it.asImageBitmap(),
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(240.dp),
                            contentScale = ContentScale.Crop
                        )
                    }
                    displayDate?.let { Text("Date : $it") }
                    Text("Texte OCR :")
                    Text(displayText)
                    Divider()
                    Text("Traduction :")
                    Text(displayTranslation ?: "— Non traduit car j'ai eu la flemme hier —")
                }
            }
        }
    }
}

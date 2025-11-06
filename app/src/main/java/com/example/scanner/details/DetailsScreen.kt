// kotlin
package com.example.scanner.details

import android.app.Activity
import android.graphics.BitmapFactory
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.scanner.photo.PhotoRepository
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailsScreen(
    recordId: String?,
) {
    val context = LocalContext.current

    val record = PhotoRepository.get(recordId)

    if (record == null) {
        Toast.makeText(context, "L'enregistrement n'existe pas", Toast.LENGTH_SHORT).show()
        (context as Activity).finish()

        return
    }

    val fis = context.openFileInput(record.imagePath)
    val bmp = BitmapFactory.decodeStream(fis)
    fis.close()

    val date = Calendar.getInstance().apply {
        timeInMillis = record.createdAtEpochMs
    }

    val formatter = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())

    val displayDate = formatter.format(date.time)

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Détails") }, navigationIcon = {
                IconButton(onClick = { (context as Activity).finish() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
                }
            })
        }) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Image(
                    bitmap = bmp.asImageBitmap(),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp),
                    contentScale = ContentScale.Crop
                )

                Text("Date : $displayDate")
                Text("Texte OCR : ${record.text}")

                HorizontalDivider()

                Text("Traduction : ${record.translatedText}")
            }
        }
    }
}

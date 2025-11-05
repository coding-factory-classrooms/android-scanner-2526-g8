package com.example.scanner.camera

import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

import androidx.compose.ui.platform.LocalContext



@Composable
fun CameraScreen() {
    CameraQuickTest()
}


@Composable
fun CameraQuickTest() {
    // je prépare le context pour passer après à la page de détail
    val context = LocalContext.current
    var launched by remember { mutableStateOf(false) }
    val photo = remember { mutableStateOf<Bitmap?>(null) }

    // Lancement de l'activité caméra
    val takePicturePreview = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bmp ->
        photo.value = bmp
        if (bmp != null) {
            val filename = "photo_${System.currentTimeMillis()}.png"
            context.openFileOutput(filename, android.content.Context.MODE_PRIVATE).use { out ->
                bmp.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, out)
            }

            val file = java.io.File(context.filesDir, filename)


            val intent = android.content.Intent(context, com.example.scanner.details.DetailsActivity::class.java).apply {
                putExtra("photo_filename", filename)
            }
            context.startActivity(intent)
            photo.value = null
        }
    }


    // lancement de la caméra directe
    LaunchedEffect(Unit) {
        if (!launched) {
            launched = true
            takePicturePreview.launch(null)
        }
    }
}
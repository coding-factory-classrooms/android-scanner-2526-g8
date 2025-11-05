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
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun CameraScreen() {
    CameraQuickTest()
}

@Composable
fun CameraQuickTest(vm: CameraViewModel = viewModel()) {
    val context = LocalContext.current
    var launched by remember { mutableStateOf(false) }
    val photo = remember { mutableStateOf<Bitmap?>(null) }

    val takePicturePreview = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        photo.value = bitmap
        if (bitmap != null) {
            val filename = "photo_${System.currentTimeMillis()}.png"
            context.openFileOutput(filename, android.content.Context.MODE_PRIVATE).use { out ->
                bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, out)
            }
        }
    }

    LaunchedEffect(photo.value) {
        photo.value?.let { bitmap ->
            val filename = "photo_${System.currentTimeMillis()}.png"
            val message = vm.sendImageToAPI(bitmap)
            val intent = android.content.Intent(
                context,
                com.example.scanner.details.DetailsActivity::class.java
            ).apply {
                putExtra("photo_filename", filename)
                putExtra("sendImageToAPI", message?.toString() ?: "")
            }
            context.startActivity(intent)
            photo.value = null
        }
    }

    LaunchedEffect(Unit) {
        if (!launched) {
            launched = true
            takePicturePreview.launch(null)
        }
    }
}

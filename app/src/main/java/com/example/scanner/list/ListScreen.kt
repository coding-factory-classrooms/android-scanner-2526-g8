// kotlin
package com.example.scanner.list

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.scanner.Paper.PhotoRecord
import com.example.scanner.Paper.PhotoRepository
import com.example.scanner.R
import com.example.scanner.details.DetailsActivity
import com.example.scanner.test.TestActivity
import com.example.scanner.ui.theme.ScannerTheme
import java.io.File

@Composable
fun ListScreen(vm: ListViewModel = viewModel()) {
    val context = LocalContext.current
    val uiState by vm.uiStateFlow.collectAsState()
    val photoState = remember { mutableStateOf<Bitmap?>(null) }

    Scaffold(
        floatingActionButton = {
            Box(modifier = Modifier.fillMaxSize()) {
                CameraButton(
                    onPhotoTaken = { bitmap ->
                        if (bitmap != null) {
                            vm.sendImageToAPI(bitmap)
                            photoState.value = bitmap
                        }
                    },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp)
                        .navigationBarsPadding()
                        .height(80.dp)
                )

                TestButton(
                    onButtonClick = {
                        val intent = Intent(context, TestActivity::class.java)
                        context.startActivity(intent)
                    },
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(16.dp)
                        .height(60.dp)
                        .navigationBarsPadding()
                )
            }
        },
        floatingActionButtonPosition = FabPosition.End
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
        ) {
            ListScreenBody(uiState, photoState.value, vm)
        }
    }
}

@Composable
fun ListScreenBody(uiState: ListUiState, photo: Bitmap?, vm: ListViewModel) {
    val context = LocalContext.current

    when (uiState) {
        is ListUiState.Error -> {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(text = uiState.error)
            }
        }
        // ici c'est quand on clique sur une row et qu'on affiche les détails d'une image déjà scannée
        ListUiState.Initial -> ItemsList { rec ->
            val intent = Intent(context, DetailsActivity::class.java).apply {
                putExtra("PHOTO_ID", rec.id)
            }
            context.startActivity(intent)
        }

        ListUiState.Loading -> {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator()
                Text(text = "Loading...")
            }
        }

        is ListUiState.Success -> {

            // j'ai prit ma photo mais il n'y as aucun texte de détecté et j'ai une réponse API
            when (val text = uiState.message) {
                null -> {
                    LaunchedEffect(Unit) {
                        Toast.makeText(
                            context,
                            "No text has been detected in the image",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    ItemsList { rec ->
                        val intent = Intent(context, DetailsActivity::class.java).apply {
                            putExtra("PHOTO_ID", rec.id)
                        }
                        context.startActivity(intent)
                    }
                }

                else -> {

                    // Lier au fait que je viens de prendre la photo et après l'attente de l'ocr et une réussite
                    LaunchedEffect(text) {
                        // sauvegarde fichier
                        val fileName = "photo_${System.currentTimeMillis()}.png"
                        val file = java.io.File(context.filesDir, fileName)
                        val bmp = requireNotNull(photo)
                        context.openFileOutput(fileName, Context.MODE_PRIVATE).use { out ->
                            bmp.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, out)
                        }
                        val imagePath = file.absolutePath

                        // la je crée la fiche dans le paper et sa récupere aussi l'id de la fiche
                        val photoId = vm.savePhotoRecord(imagePath = imagePath, ocrText = text)

                        // appeler l'api de traduction ici
                        try {
                            /*val translated =  la tu refait ton truck chelou de réussite ou pas etc */

                                // la t'apelle la maj de la fiche avec la traduction
                               /* com.example.scanner.Paper.PhotoRepository.updateTranslation(
                                    id = photoId,
                                    targetLanguage = "en",          // ou une variable choisie
                                    translatedText = translated
                                )*/
                        } catch (t: Throwable) {
                            // gestion d'erreur tmtc
                        }

                        //  ouvrir les détails via l'id
                        val intent = Intent(context, com.example.scanner.details.DetailsActivity::class.java).apply {
                            putExtra("PHOTO_ID", photoId)
                        }
                        context.startActivity(intent)
                    }
                }
            }
        }
    }
}

@Composable
fun ItemsList(
    onOpen: (PhotoRecord) -> Unit = {}
) {
    val recordsState = remember { mutableStateOf(PhotoRepository.getAll()) }

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(
            recordsState.value,
            key = { it.id }
        ) { record ->
            PhotoRow(
                record = record,
                // le onOpen sert à ouvrir les détails d'une image déjà scannée il est remonté plus haut
                onClick = { onOpen(record) },
                // pour le favori en true/false
                onToggleFavorite = {
                    PhotoRepository.toggleFavorite(record.id)
                    recordsState.value = PhotoRepository.getAll()
                },
                // pour la corbeille de suppression
                onDelete = {
                    PhotoRepository.delete(record.id)
                    recordsState.value = PhotoRepository.getAll()
                }
            )
            Divider()
        }
    }
}

@Composable
private fun PhotoRow(
    record: PhotoRecord,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val bmp = remember(record.imagePath) {
            android.graphics.BitmapFactory.decodeFile(record.imagePath)
        }
        bmp?.let {
            androidx.compose.foundation.Image(
                bitmap = it.asImageBitmap(),
                contentDescription = null,
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(Modifier.width(12.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(record.createdAtDisplay)
            val preview = if (record.text.length > 80) record.text.take(80) + "…" else record.text
            Text(preview)
        }
        IconButton(onClick = onToggleFavorite) {
            Icon(
                imageVector = if (record.isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                contentDescription = "Favori"
            )
        }
        IconButton(onClick = onDelete) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Supprimer"
            )
        }
    }
}

@Composable
fun CameraButton(onPhotoTaken: (Bitmap?) -> Unit, modifier: Modifier = Modifier) {
    val takePicturePreview = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview(),
        onResult = onPhotoTaken
    )

    Button(
        onClick = { takePicturePreview.launch(null) },
        contentPadding = PaddingValues(8.dp),
        modifier = modifier
    ) {
        Icon(
            painterResource(R.drawable.camera),
            contentDescription = "Caméra",
            modifier = Modifier.size(65.dp)
        )
    }
}

@Composable
fun TestButton(onButtonClick: () -> Unit, modifier: Modifier = Modifier) {
    Button(
        onClick = onButtonClick,
        contentPadding = PaddingValues(8.dp),
        modifier = modifier
    ) {
        Text(text = "test")
    }
}

@Preview
@Composable
fun ListScreenPreview() {
    ScannerTheme {
        ListScreen()
    }
}

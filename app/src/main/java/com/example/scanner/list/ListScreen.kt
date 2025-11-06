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
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.TextFieldValue
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

        // afficher la liste et ouvrir un détail quand on clique une row existante
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
            when (val text = uiState.message) {
                // OCR a répondu mais aucun texte détecté
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
                //  on sauvegarde, on crée la fiche, puis on ouvre le détail
                else -> {
                    LaunchedEffect(text) {
                        val fileName = "photo_${System.currentTimeMillis()}.png"
                        val file = File(context.filesDir, fileName)
                        val bmp = requireNotNull(photo)
                        context.openFileOutput(fileName, Context.MODE_PRIVATE).use { out ->
                            bmp.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, out)
                        }
                        val imagePath = file.absolutePath

                        val photoId = vm.savePhotoRecord(imagePath = imagePath, ocrText = text)

                        val intent = Intent(context, DetailsActivity::class.java).apply {
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
    var queryText by remember { mutableStateOf(TextFieldValue("")) }
    var onlyFavorites by remember { mutableStateOf(false) }
    var refreshTick by remember { mutableStateOf(0) } // force un recalcul après favoris/suppression

    val records = remember(queryText, onlyFavorites, refreshTick) {
        PhotoRepository.query(
            text = queryText.text.takeIf { it.isNotBlank() },
            onlyFavorites = onlyFavorites
        )
    }

    Column(Modifier.fillMaxSize()) {
        FiltersBarSimple(
            queryText = queryText,
            onQueryChange = { queryText = it },
            onlyFavorites = onlyFavorites,
            onToggleFavorites = { onlyFavorites = !onlyFavorites }
        )

        LazyColumn(Modifier.fillMaxSize()) {
            items(records, key = { it.id }) { rec ->
                PhotoRow(
                    record = rec,
                    onClick = { onOpen(rec) },
                    onToggleFavorite = {
                        PhotoRepository.toggleFavorite(rec.id)
                        refreshTick++
                    },
                    onDelete = {
                        PhotoRepository.delete(rec.id)
                        refreshTick++
                    }
                )
                Divider()
            }
        }
    }
}

@Composable
private fun FiltersBarSimple(
    queryText: TextFieldValue,
    onQueryChange: (TextFieldValue) -> Unit,
    onlyFavorites: Boolean,
    onToggleFavorites: () -> Unit
) {
    Column(Modifier.fillMaxWidth().padding(12.dp)) {
        OutlinedTextField(
            value = queryText,
            onValueChange = onQueryChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Rechercher dans le texte") },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
            singleLine = true
        )
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            FilterChip(
                selected = onlyFavorites,
                onClick = onToggleFavorites,
                label = { Text("Favoris") }
            )
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
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onClick() }, // clic sur la vignette ouvre le détail
                contentScale = ContentScale.Crop
            )
            Spacer(Modifier.width(12.dp))
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .clickable { onClick() }
        ) {
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

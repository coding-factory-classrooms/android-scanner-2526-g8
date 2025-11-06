// kotlin
package com.example.scanner.list

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.example.scanner.R
import com.example.scanner.TranslateAPI
import com.example.scanner.common.GoogleVisionAPI
import com.example.scanner.details.DetailsActivity
import com.example.scanner.photo.PhotoModel
import com.example.scanner.photo.PhotoRepository
import com.example.scanner.test.TestActivity
import com.example.scanner.ui.theme.ScannerTheme
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun ListScreen(vm: ListViewModel = viewModel()) {
    val context = LocalContext.current
    val uiState by vm.uiStateFlow.collectAsState()

    val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl("https://vision.googleapis.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    vm.api =  retrofit.create(GoogleVisionAPI::class.java)

    Scaffold(
        floatingActionButton = {
            Box(modifier = Modifier.fillMaxSize()) {
                CameraButton(
                    onPhotoTaken = { bitmap ->
                        if (bitmap != null) {
                            vm.sendImageToAPI(bitmap)
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
        }, floatingActionButtonPosition = FabPosition.End
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
        ) {
            ListScreenBody(uiState)
        }
    }
}

@Composable
fun ListScreenBody(uiState: ListUiState) {
    val context = LocalContext.current

    when (uiState) {
        is ListUiState.Error -> {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(text = uiState.error)
            }
        }

        // afficher la liste et ouvrir un détail quand on clique une row existante
        ListUiState.Initial -> ItemsList()

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
            ItemsList()

            LaunchedEffect(Unit) {
                when (val text = uiState.content.message) {
                    null -> Toast.makeText(
                        context, "No text has been detected in the image", Toast.LENGTH_SHORT
                    ).show()

                    else -> {
                        val fileName = "photo_${System.currentTimeMillis()}.png"
                        context.openFileOutput(fileName, Context.MODE_PRIVATE).use { out ->
                            uiState.content.bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                        }

                        // la je crée la fiche dans le paper et sa récupere aussi l'id de la fiche
                        val newRecord =
                            PhotoRepository.createFrom(imagePath = fileName, ocrText = text)

                        val cleanText = text.lowercase()

                        val targetLanguage = "fr"

                        // appeler l'api de traduction ici
                        TranslateAPI.translate(cleanText, targetLanguage) {
                            if (it != null) {
                                PhotoRepository.updateTranslation(
                                    id = newRecord.id,
                                    targetLanguage = targetLanguage,
                                    translatedText = it
                                )
                                //  ouvrir les détails via l'id
                                val intent = Intent(
                                    context, DetailsActivity::class.java
                                ).apply {
                                    putExtra("record_id", newRecord.id)
                                }

                                context.startActivity(intent)
                            }
                        }

                    }
                }
            }
        }
    }
}


@Composable
fun ItemsList() {
    val context = LocalContext.current

    var queryText by remember { mutableStateOf(TextFieldValue("")) }
    var onlyFavorites by remember { mutableStateOf(false) }
    var refreshTick by remember { mutableStateOf(0) } // force à recalculer les infos

    val records = remember(queryText, onlyFavorites, refreshTick) {
        PhotoRepository.query(
            text = queryText.text.takeIf { it.isNotBlank() }, onlyFavorites = onlyFavorites
        )
    }

    // ici je fait mes query pour actualiser la barre de recherche et le filtre favoris
    Column(Modifier.fillMaxSize()) {
        FiltersBarSimple(
            queryText = queryText,
            onQueryChange = { queryText = it },
            onlyFavorites = onlyFavorites,
            onToggleFavorites = { onlyFavorites = !onlyFavorites })
        LazyColumn(Modifier.fillMaxSize()) {
            items(records, key = { it.id }) { rec ->
                PhotoRow(record = rec, onClick = {
                    val intent = Intent(context, DetailsActivity::class.java)

                    intent.putExtra("record_id", rec.id)
                    context.startActivity(intent)
                }, onToggleFavorite = {
                    PhotoRepository.toggleFavorite(rec.id)
                    refreshTick++
                }, onDelete = {
                    PhotoRepository.delete(rec.id)
                    refreshTick++
                })
                HorizontalDivider()
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
    Column(Modifier
        .fillMaxWidth()
        .padding(12.dp)) {
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
                selected = onlyFavorites, onClick = onToggleFavorites, label = { Text("Favoris") })
        }
    }
}


@Composable
private fun PhotoRow(
    record: PhotoModel, onClick: () -> Unit, onToggleFavorite: () -> Unit, onDelete: () -> Unit
) {
    val context = LocalContext.current

    Row(modifier = Modifier
        .fillMaxWidth()
        .clickable { onClick() }
        .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically) {
        val fis = context.openFileInput(record.imagePath)
        val bmp = BitmapFactory.decodeStream(fis)
        fis.close()

        Image(
            bitmap = bmp.asImageBitmap(),
            contentDescription = null,
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(8.dp)),
            contentScale = ContentScale.Crop
        )

        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            val date = Calendar.getInstance().apply {
                timeInMillis = record.createdAtEpochMs
            }

            val formatter = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
            Text(formatter.format(date.time))

            val preview = if (record.text.length > 80) record.text.take(80) + "…" else record.text
            Text("OCR: $preview")

            record.translatedText?.let { translated ->
                val tPreview = if (translated.length > 80) translated.take(80) + "…" else translated
                Text("Translation: $tPreview", style = MaterialTheme.typography.bodySmall)
            }
        }

        IconButton(onClick = onToggleFavorite) {
            Icon(
                imageVector = if (record.isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                contentDescription = "Favori"
            )
        }
        IconButton(onClick = onDelete) {
            Icon(
                imageVector = Icons.Default.Delete, contentDescription = "Supprimer"
            )
        }
    }
}


@Composable
fun CameraButton(onPhotoTaken: (Bitmap?) -> Unit, modifier: Modifier = Modifier) {
    val takePicturePreview = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview(), onResult = onPhotoTaken
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
        onClick = onButtonClick, contentPadding = PaddingValues(8.dp), modifier = modifier
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

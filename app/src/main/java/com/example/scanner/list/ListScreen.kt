// kotlin
package com.example.scanner.list

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.scanner.ui.theme.ScannerTheme
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.ui.res.painterResource
import com.example.scanner.R
import androidx.compose.ui.Alignment
import androidx.compose.material3.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.scanner.details.DetailsActivity
import com.example.scanner.test.TestActivity

@Composable
fun ListScreen(vm: ListViewModel = viewModel()) {
    val context = LocalContext.current

    val uiState by vm.uiStateFlow.collectAsState()

    Scaffold(
        floatingActionButton = {
            Box(modifier = Modifier.fillMaxSize()) {
                CameraButton(
                    onPhotoTaken = { bitmap ->
                        if (bitmap != null) {
                            val filename = "photo_${System.currentTimeMillis()}.png"

                            // save file in phone local storage as png
                            context.openFileOutput(filename, Context.MODE_PRIVATE).use { out ->
                                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                            }

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
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.Center
            ) {
                Text(text = uiState.error)
            }
        }
        ListUiState.Initial -> ItemsList()
        ListUiState.Loading -> {
            Column(
                modifier = Modifier
                    .fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator()
                Text(text = "Loading...")
            }
        }
        is ListUiState.Success -> {
            val intent = Intent(context, DetailsActivity::class.java)
            context.startActivity(intent)
        }
    }
}

@Composable
fun ItemsList() {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
    ) {
        // items
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
        modifier = modifier.defaultMinSize(
            minWidth = 1.dp,
            minHeight = 1.dp
        ) // j'évite qu'il puisse être negatif
    ) {
        Icon(
            painterResource(R.drawable.camera),
            contentDescription = "Caméra",
            modifier = Modifier.size(65.dp) // taille de l'icon
        )
    }
}

@Composable
fun TestButton(onButtonClick: () -> Unit, modifier: Modifier = Modifier) {
    Button(
        onClick = onButtonClick,
        contentPadding = PaddingValues(8.dp),
        modifier = modifier.defaultMinSize(minWidth = 1.dp, minHeight = 1.dp)
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

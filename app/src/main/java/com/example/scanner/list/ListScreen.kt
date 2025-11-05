// kotlin
package com.example.scanner.list

import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
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
import com.example.scanner.camera.CameraActivity
import com.example.scanner.ui.theme.ScannerTheme
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.ui.res.painterResource
import com.example.scanner.R
import androidx.compose.ui.Alignment
import androidx.compose.material3.Text
import com.example.scanner.test.TestActivity

@Composable
fun ListScreen() {
    val context = LocalContext.current

    Scaffold(
        floatingActionButton = {
            // Conteneur qui prend toute la largeur pour positionner un bouton à gauche et un à droite
            androidx.compose.foundation.layout.Box(modifier = Modifier.fillMaxSize()) {
                CameraButton(
                    onButtonClick = {
                        val intent = Intent(context, CameraActivity::class.java)
                        context.startActivity(intent)
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // items
        }
    }
}

@Composable
fun CameraButton(onButtonClick: () -> Unit, modifier: Modifier = Modifier) {
    Button(
        onClick = onButtonClick,
        contentPadding = PaddingValues(8.dp),
        modifier = modifier
            .defaultMinSize(minWidth = 1.dp, minHeight = 1.dp) // j'évite qu'il puisse être negatif
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
        modifier = modifier
            .defaultMinSize(minWidth = 1.dp, minHeight = 1.dp)
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

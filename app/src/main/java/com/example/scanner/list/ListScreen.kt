package com.example.scanner.list

import android.content.Intent
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
import androidx.compose.material3.Text
import androidx.compose.ui.res.painterResource
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.scanner.R
import com.example.scanner.camera.CameraViewModel

@Composable
fun ListScreen(vm: CameraViewModel = viewModel()) {
    // Obtenir le context actuel
    val context = LocalContext.current


    Scaffold(
        floatingActionButton = {
            CameraButton(
                 onButtonClick = {
                    // Charger le context pour démarrer une activité pour camera activity
                    val intent = Intent(context, CameraActivity::class.java)
                    context.startActivity(intent)
                },
                modifier = Modifier
                    .padding(16.dp)
                    .navigationBarsPadding()
                    .height(10.dp)
            )
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
        Button(onClick = { vm.sendImageToAPI(context) }) {
            Text("Send test api call")
        }
    }
}

@Composable
fun CameraButton(onButtonClick: () -> Unit, modifier: Any) {
    Button(
        onClick = onButtonClick,
        contentPadding = PaddingValues(8.dp),
        modifier = Modifier
            .padding(16.dp)
            .defaultMinSize(minWidth = 1.dp, minHeight = 1.dp) // j'évite qu'il puisse être negatif
    ) {
        Icon(
            painterResource(R.drawable.camera),
            contentDescription = "Caméra",
            modifier = Modifier.size(65.dp) // taille de l'icon
        )
    }
}




@Preview
@Composable
fun ListScreenPreview() {
    ScannerTheme {
        ListScreen()
    }
}
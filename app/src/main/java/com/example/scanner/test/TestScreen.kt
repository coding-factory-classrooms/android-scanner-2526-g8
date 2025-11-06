package com.example.scanner.test.TestScreen

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.scanner.R
import com.example.scanner.list.ListActivity
import com.example.scanner.list.ListViewModel
import com.example.scanner.ui.theme.ScannerTheme

@Composable
fun TestScreen(
    onTest2: () -> Unit = {},
    onTest3: () -> Unit = {},
    onTest4: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        Spacer(modifier = Modifier.padding(20.dp))
        // Bouton de retour en haut
        val context = LocalContext.current
        Button(
            onClick = {
                val intent = Intent(context, ListActivity::class.java)
                context.startActivity(intent)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Retour")
        }

        // spacer de 16dp
        Spacer(modifier = Modifier.padding(25.dp))
        Button1()

        Button2()

        Button(
            onClick = onTest3,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Test 3")
        }

        Button(
            onClick = onTest4,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Test 4")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TestScreenPreview() {
    ScannerTheme {
        TestScreen(
            onTest2 = { /* TODO: appeler ton test 2 */ },
            onTest3 = { /* TODO: appeler ton test 3 */ },
            onTest4 = { /* TODO: appeler ton test 4 */ }
        )
    }
}

@Composable
fun Button1(vm: ListViewModel = viewModel()) {
    val context = LocalContext.current

    Button(
        onClick = {
            val bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.test_img)
            vm.sendImageToAPI(bitmap)
        },
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(text = "Envoyer l'image à l'API")
    }
}

@Composable
fun Button2(vm: ListViewModel = viewModel()) {
    val context = LocalContext.current

    Button(
        onClick = {
            // Charge l’image du dossier drawable
            val bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.test_img)

            // Sauvegarde une copie locale pour simuler une photo prise
            val fileName = "test_${System.currentTimeMillis()}.png"
            val file = java.io.File(context.filesDir, fileName)
            // sauvegarde de la bitmap dans le fichier
            context.openFileOutput(fileName, Context.MODE_PRIVATE).use { out ->
                bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, out)
            }

            val imagePath = file.absolutePath

            // Texte simulé
            val fakeOcrText = "Ceci est un texte OCR simulé pour les tests 3333."

            // Création de la fiche dans paper via le vm
            val photoId = vm.savePhotoRecord(imagePath = imagePath, ocrText = fakeOcrText)

            // Retourne automatiquement à la liste pour voir le résultat
            val intent = Intent(context, com.example.scanner.list.ListActivity::class.java)
            intent.putExtra("PHOTO_ID", photoId)
            context.startActivity(intent)
        },
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(text = "Créer une ligne de test ocr etc")
    }
}


package com.example.scanner.test

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.scanner.R
import com.example.scanner.list.ListActivity
import com.example.scanner.list.ListViewModel
import com.example.scanner.photo.PhotoObject
import com.example.scanner.ui.theme.ScannerTheme
import java.io.File

@Composable
fun TestScreen(
    modifier: Modifier = Modifier,
    onTest3: () -> Unit = {},
    onTest4: () -> Unit = {},
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
            onTest3 = { /* TODO: appeler ton test 3 */ },
            onTest4 = { /* TODO: appeler ton test 4 */ }
        )
    }
}

@Composable
fun Button1(vm: ListViewModel = viewModel()) {
    val res = LocalResources.current

    Button(
        onClick = {
            val bitmap = BitmapFactory.decodeResource(res, R.drawable.test_img)
            vm.sendImageToAPI(bitmap)
        },
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(text = "Envoyer l'image à l'API")
    }
}

@Composable
fun Button2() {
    val context = LocalContext.current
    val res = LocalResources.current

    Button(
        onClick = {
            // Charge l’image du dossier drawable
            val bitmap = BitmapFactory.decodeResource(res, R.drawable.test_img)

            // Sauvegarde une copie locale pour simuler une photo prise
            val fileName = "test_${System.currentTimeMillis()}.png"
            val file = File(context.filesDir, fileName)
            // sauvegarde de la bitmap dans le fichier
            context.openFileOutput(fileName, Context.MODE_PRIVATE).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }

            val imagePath = file.absolutePath

            // Texte simulé, change cette ligne si tu veux tester le filtre de recherche
            val fakeOcrText = "Ceci est un texte OCR simulé pour les tests 3333."
            val targetLanguage = "fr"
            val translatedText = "This is a simulated OCR for tests 3333"

            // Création de la fiche dans paper via le vm
            PhotoObject.repo.createFrom(
                imagePath = imagePath,
                ocrText = fakeOcrText,
                targetLanguage = targetLanguage,
                translatedText = translatedText
            )

            // Retourne automatiquement à la liste pour voir le résultat
            val intent = Intent(context, ListActivity::class.java)
            context.startActivity(intent)
        },
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(text = "Créer une ligne de test ocr etc")
    }
}


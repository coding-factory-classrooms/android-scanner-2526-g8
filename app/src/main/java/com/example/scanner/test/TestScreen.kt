package com.example.scanner.test.TestScreen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import com.example.scanner.ui.theme.ScannerTheme

@Composable
fun TestScreen(
    onTest1: () -> Unit = {},
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
        Button(
            onClick = onTest1,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Test 1")
        }

        Button(
            onClick = onTest2,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Test 2")
        }

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
            onTest1 = { /* TODO: appeler ton test 1 */ },
            onTest2 = { /* TODO: appeler ton test 2 */ },
            onTest3 = { /* TODO: appeler ton test 3 */ },
            onTest4 = { /* TODO: appeler ton test 4 */ }
        )
    }
}

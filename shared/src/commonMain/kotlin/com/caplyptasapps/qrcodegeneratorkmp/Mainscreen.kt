package com.caplyptasapps.qrcodegeneratorkmp

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.caplyptasapps.qrcodegeneratorkmp.qrcodeutility.QrCodeGenerator
import com.caplyptasapps.qrcodegeneratorkmp.qrcodeutility.QrImageResult
import com.caplyptasapps.qrcodegeneratorkmp.qrcodeutility.QrStyleConfig
import com.caplyptasapps.qrcodegeneratorkmp.qrcodeutility.QrVisualCategory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(qrGenerator: QrCodeGenerator, modifier: Modifier) {
    var inputText by remember { mutableStateOf("https://github.com/") }
    var selectedStyle by remember { mutableStateOf(QrVisualCategory.STANDARD) }
    val qrBitmap = remember { mutableStateOf<QrImageResult?>(null) }
    val isGenerating = remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()

    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "QR Code Generator",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                label = { Text("Enter Text or URL", color = MaterialTheme.colorScheme.onSurface) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Uri),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Style Dropdown
            var expanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }) {
                OutlinedTextField(
                    readOnly = true,
                    value = selectedStyle.name,
                    onValueChange = {

                    },
                    label = { Text("QR Style", color = MaterialTheme.colorScheme.onSurface) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                    )
                )

                ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    QrVisualCategory.entries.forEach { style ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    style.name,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            },
                            onClick = {
                                selectedStyle = style
                                expanded = false

                                generateQrCode(
                                    isGenerating,
                                    coroutineScope,
                                    qrGenerator,
                                    qrBitmap,
                                    inputText,
                                    selectedStyle
                                )
                            },
                            colors = MenuDefaults.itemColors(
                                textColor = MaterialTheme.colorScheme.onSurface
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    generateQrCode(
                        isGenerating,
                        coroutineScope,
                        qrGenerator,
                        qrBitmap,
                        inputText,
                        selectedStyle
                    )
                },
                enabled = inputText.isNotBlank(),
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            ) {
                Text("Generate QR Code")
            }

            Spacer(modifier = Modifier.height(24.dp))

            qrBitmap.value?.let {
                when (it) {
                    is QrImageResult.Android -> {
                        Image(
                            bitmap = it.bitmap,
                            contentDescription = "QR Code",
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(1f)
                        )
                    }

                    is QrImageResult.IOS -> {
                        it.imageBitmap?.let { imageBitmap ->
                            Image(
                                bitmap = imageBitmap,
                                contentDescription = "QR Code",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(1f)
                            )
                        } ?: kotlin.run {
                            Text(
                                "Failed to load QR code",
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }

                    QrImageResult.Error -> {
                        Text(
                            "Failed to generate QR code",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            if (isGenerating.value) {
                Spacer(modifier = Modifier.height(16.dp))
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

fun generateQrCode(
    isGenerating: MutableState<Boolean>,
    coroutineScope: CoroutineScope,
    qrGenerator: QrCodeGenerator,
    qrBitmap: MutableState<QrImageResult?>,
    inputText: String,
    selectedStyle: QrVisualCategory,
) {
    isGenerating.value = true
    coroutineScope.launch {
        qrBitmap.value = qrGenerator.generateQrBitmap(
            QrStyleConfig(
                preparedText = inputText,
                qrVisualCategory = selectedStyle,
                width = 800,
                height = 800,
                qrColor = 0xFF000000.toInt(),
                logoUri = null,
                backgroundUri = null,
                logoBgColor = 0xFFFFFFFF.toInt()
            )
        )
        isGenerating.value = false
    }
}


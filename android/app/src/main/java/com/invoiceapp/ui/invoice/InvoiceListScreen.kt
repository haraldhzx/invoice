package com.invoiceapp.ui.invoice

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.invoiceapp.model.Invoice
import com.invoiceapp.model.InvoiceStatus
import com.invoiceapp.util.Resource
import java.io.File
import java.io.FileOutputStream
import java.text.NumberFormat
import java.time.format.DateTimeFormatter
import java.util.Currency
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoiceListScreen(
    viewModel: InvoiceViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val invoicesState by viewModel.invoicesState.collectAsState()
    val uploadState by viewModel.uploadState.collectAsState()

    var showUploadMenu by remember { mutableStateOf(false) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            try {
                val inputStream = context.contentResolver.openInputStream(it)
                val file = File(context.cacheDir, "invoice_${System.currentTimeMillis()}.jpg")
                inputStream?.use { input ->
                    FileOutputStream(file).use { output ->
                        input.copyTo(output)
                    }
                }
                viewModel.uploadInvoice(file)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.loadInvoices()
    }

    LaunchedEffect(uploadState) {
        when (uploadState) {
            is Resource.Success -> {
                // Show success message
                viewModel.clearUploadState()
            }
            else -> {}
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showUploadMenu = true }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Invoice")
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val state = invoicesState) {
                is Resource.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                is Resource.Success -> {
                    val invoices = state.data?.content ?: emptyList()
                    if (invoices.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "No invoices yet",
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Tap + to add your first invoice",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(invoices) { invoice ->
                                InvoiceCard(invoice = invoice)
                            }
                        }
                    }
                }
                is Resource.Error -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Failed to load invoices",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = state.message ?: "Unknown error",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(onClick = { viewModel.loadInvoices() }) {
                                Text("Retry")
                            }
                        }
                    }
                }
                null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }

            // Upload menu dialog
            if (showUploadMenu) {
                AlertDialog(
                    onDismissRequest = { showUploadMenu = false },
                    title = { Text("Add Invoice") },
                    text = {
                        Column {
                            ListItem(
                                headlineContent = { Text("Take Photo") },
                                leadingContent = { Icon(Icons.Default.CameraAlt, contentDescription = null) },
                                modifier = Modifier.clickable {
                                    showUploadMenu = false
                                    // TODO: Launch camera
                                }
                            )
                            ListItem(
                                headlineContent = { Text("Choose from Gallery") },
                                leadingContent = { Icon(Icons.Default.Upload, contentDescription = null) },
                                modifier = Modifier.clickable {
                                    showUploadMenu = false
                                    imagePickerLauncher.launch("image/*")
                                }
                            )
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { showUploadMenu = false }) {
                            Text("Cancel")
                        }
                    }
                )
            }

            // Upload progress
            if (uploadState is Resource.Loading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        modifier = Modifier.padding(32.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator()
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Processing invoice...")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun InvoiceCard(invoice: Invoice) {
    val dateFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy")
    val numberFormat = NumberFormat.getCurrencyInstance(Locale.US).apply {
        currency = Currency.getInstance(invoice.currency)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = invoice.vendorName ?: "Unknown Vendor",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = invoice.date?.format(dateFormatter) ?: "No date",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = numberFormat.format(invoice.totalAmount),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    StatusChip(status = invoice.status)
                }
            }

            if (invoice.category != null) {
                Spacer(modifier = Modifier.height(8.dp))
                AssistChip(
                    onClick = { },
                    label = { Text(invoice.category.name) },
                    leadingIcon = {
                        if (invoice.category.icon != null) {
                            Text(invoice.category.icon)
                        }
                    }
                )
            }

            if (invoice.confidence != null && invoice.confidence.toDouble() < 0.7) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Confidence: ${(invoice.confidence.toDouble() * 100).toInt()}%",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
fun StatusChip(status: InvoiceStatus) {
    val (color, text) = when (status) {
        InvoiceStatus.COMPLETED -> MaterialTheme.colorScheme.primary to "Completed"
        InvoiceStatus.PROCESSING -> MaterialTheme.colorScheme.secondary to "Processing"
        InvoiceStatus.REVIEW_REQUIRED -> MaterialTheme.colorScheme.error to "Review"
        InvoiceStatus.PENDING -> MaterialTheme.colorScheme.tertiary to "Pending"
        InvoiceStatus.REJECTED -> MaterialTheme.colorScheme.error to "Rejected"
    }

    SuggestionChip(
        onClick = { },
        label = {
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall
            )
        },
        colors = SuggestionChipDefaults.suggestionChipColors(
            containerColor = color.copy(alpha = 0.1f),
            labelColor = color
        )
    )
}

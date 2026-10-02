package com.example.androidapp.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.androidapp.model.Product

@Composable
fun ProductScreen(viewModel: ProductViewModel = viewModel()) {

    val products    by viewModel.products.collectAsState()
    val isLoading   by viewModel.isLoading.collectAsState()
    val errorMsg    by viewModel.errorMessage.collectAsState()
    val successMsg  by viewModel.successMessage.collectAsState()

    var nameInput   by remember { mutableStateOf("") }
    var descInput   by remember { mutableStateOf("") }
    var priceInput  by remember { mutableStateOf("") }
    var stockInput  by remember { mutableStateOf("") }
    var searchQuery by remember { mutableStateOf("") }

    LaunchedEffect(Unit) { viewModel.loadProducts() }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {

        Text("🛍️ Products", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(16.dp))

        // Error message
        errorMsg?.let {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("❌ $it", modifier = Modifier.padding(8.dp),
                    color = MaterialTheme.colorScheme.onErrorContainer)
            }
            Spacer(Modifier.height(8.dp))
        }

        // Success message
        successMsg?.let {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("✅ $it", modifier = Modifier.padding(8.dp),
                    color = MaterialTheme.colorScheme.onPrimaryContainer)
            }
            Spacer(Modifier.height(8.dp))
        }

        // Add Product form
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text("Add New Product", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))

                OutlinedTextField(value = nameInput, onValueChange = { nameInput = it },
                    label = { Text("Name") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = descInput, onValueChange = { descInput = it },
                    label = { Text("Description") }, modifier = Modifier.fillMaxWidth())
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = priceInput, onValueChange = { priceInput = it },
                        label = { Text("Price $") }, modifier = Modifier.weight(1f))
                    OutlinedTextField(value = stockInput, onValueChange = { stockInput = it },
                        label = { Text("Stock") }, modifier = Modifier.weight(1f))
                }
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = {
                        viewModel.createProduct(
                            name        = nameInput,
                            description = descInput,
                            price       = priceInput.toDoubleOrNull() ?: 0.0,
                            stock       = stockInput.toIntOrNull() ?: 0
                        )
                        nameInput = ""; descInput = ""; priceInput = ""; stockInput = ""
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading
                ) { Text("Add Product") }
            }
        }

        Spacer(Modifier.height(16.dp))

        // Search bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it; viewModel.searchProducts(it) },
            label = { Text("Search products...") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(8.dp))

        if (isLoading) {
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }

        // Products list
        LazyColumn {
            items(products) { product ->
                ProductCard(product = product, onDelete = { viewModel.deleteProduct(product.id) })
            }
        }
    }
}

@Composable
fun ProductCard(product: Product, onDelete: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(product.name, style = MaterialTheme.typography.titleSmall)
                Text(product.description, style = MaterialTheme.typography.bodySmall,
                    maxLines = 1)
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("💲${"%.2f".format(product.price)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary)
                    Text("Stock: ${product.stock}",
                        style = MaterialTheme.typography.bodySmall)
                }
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

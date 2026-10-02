package com.example.androidapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.androidapp.model.User
import com.example.androidapp.ui.ProductScreen
import com.example.androidapp.ui.UserViewModel

// ─────────────────────────────────────────────────────────────────────────────
// TABS — defines the two bottom nav items
// ─────────────────────────────────────────────────────────────────────────────
enum class AppTab(val label: String) {
    USERS("Users"),
    PRODUCTS("Products")
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                MainScreen()
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// MAIN SCREEN — scaffold with bottom navigation
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun MainScreen() {
    var selectedTab by remember { mutableStateOf(AppTab.USERS) }

    Scaffold(
        bottomBar = {
            NavigationBar {
                // Users tab
                NavigationBarItem(
                    selected = selectedTab == AppTab.USERS,
                    onClick  = { selectedTab = AppTab.USERS },
                    icon     = { Icon(Icons.Default.Person, contentDescription = "Users") },
                    label    = { Text("Users") }
                )
                // Products tab
                NavigationBarItem(
                    selected = selectedTab == AppTab.PRODUCTS,
                    onClick  = { selectedTab = AppTab.PRODUCTS },
                    icon     = { Icon(Icons.Default.ShoppingCart, contentDescription = "Products") },
                    label    = { Text("Products") }
                )
            }
        }
    ) { paddingValues ->
        // Show the correct screen based on selected tab
        Box(modifier = Modifier.padding(paddingValues)) {
            when (selectedTab) {
                AppTab.USERS    -> UserScreen()
                AppTab.PRODUCTS -> ProductScreen()
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// USER SCREEN
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun UserScreen(viewModel: UserViewModel = viewModel()) {

    val users      by viewModel.users.collectAsState()
    val isLoading  by viewModel.isLoading.collectAsState()
    val errorMsg   by viewModel.errorMessage.collectAsState()
    val successMsg by viewModel.successMessage.collectAsState()

    var nameInput   by remember { mutableStateOf("") }
    var emailInput  by remember { mutableStateOf("") }
    var ageInput    by remember { mutableStateOf("") }
    var searchQuery by remember { mutableStateOf("") }

    LaunchedEffect(Unit) { viewModel.loadUsers() }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {

        Text("👤 Users", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(16.dp))

        errorMsg?.let {
            Card(colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer),
                modifier = Modifier.fillMaxWidth()) {
                Text("❌ $it", modifier = Modifier.padding(8.dp),
                    color = MaterialTheme.colorScheme.onErrorContainer)
            }
            Spacer(Modifier.height(8.dp))
        }

        successMsg?.let {
            Card(colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer),
                modifier = Modifier.fillMaxWidth()) {
                Text("✅ $it", modifier = Modifier.padding(8.dp),
                    color = MaterialTheme.colorScheme.onPrimaryContainer)
            }
            Spacer(Modifier.height(8.dp))
        }

        // Add User form
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text("Add New User", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = nameInput, onValueChange = { nameInput = it },
                    label = { Text("Name") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = emailInput, onValueChange = { emailInput = it },
                    label = { Text("Email") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = ageInput, onValueChange = { ageInput = it },
                    label = { Text("Age") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = {
                        viewModel.createUser(nameInput, emailInput, ageInput.toIntOrNull() ?: 0)
                        nameInput = ""; emailInput = ""; ageInput = ""
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading
                ) { Text("Add User") }
            }
        }

        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it; viewModel.searchUsers(it) },
            label = { Text("Search users...") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(8.dp))

        if (isLoading) {
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }

        LazyColumn {
            items(users) { user ->
                UserCard(user = user, onDelete = { viewModel.deleteUser(user.id) })
            }
        }
    }
}

@Composable
fun UserCard(user: User, onDelete: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(user.name,  style = MaterialTheme.typography.titleSmall)
                Text(user.email, style = MaterialTheme.typography.bodySmall)
                Text("Age: ${user.age}", style = MaterialTheme.typography.bodySmall)
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

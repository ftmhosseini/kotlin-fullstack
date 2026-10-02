package com.example.androidapp.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.androidapp.model.Product
import com.example.androidapp.network.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ProductViewModel : ViewModel() {

    private val api = RetrofitClient.api

    private val _products = MutableStateFlow<List<Product>>(emptyList())
    val products: StateFlow<List<Product>> = _products

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage

    fun loadProducts() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val response = api.getAllProducts()
                if (response.isSuccessful) {
                    _products.value = response.body()?.data ?: emptyList()
                } else {
                    _errorMessage.value = "Failed to load: ${response.code()}"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Network error: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun createProduct(name: String, description: String, price: Double, stock: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = api.createProduct(
                    Product(name = name, description = description, price = price, stock = stock)
                )
                if (response.isSuccessful && response.body()?.success == true) {
                    _successMessage.value = "Product '$name' created!"
                    loadProducts()
                } else {
                    _errorMessage.value = response.body()?.message ?: "Failed to create"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Network error: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteProduct(id: Long) {
        viewModelScope.launch {
            try {
                val response = api.deleteProduct(id)
                if (response.isSuccessful) {
                    _successMessage.value = "Product deleted"
                    loadProducts()
                } else {
                    _errorMessage.value = "Failed to delete"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Network error: ${e.message}"
            }
        }
    }

    fun searchProducts(query: String) {
        if (query.isBlank()) { loadProducts(); return }
        viewModelScope.launch {
            try {
                val response = api.searchProducts(query)
                if (response.isSuccessful) {
                    _products.value = response.body()?.data ?: emptyList()
                }
            } catch (e: Exception) {
                _errorMessage.value = "Search failed: ${e.message}"
            }
        }
    }
}

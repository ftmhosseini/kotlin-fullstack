package com.example.androidapp.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.androidapp.model.User
import com.example.androidapp.network.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class UserViewModel : ViewModel() {

    private val api = RetrofitClient.api

    private val _users = MutableStateFlow<List<User>>(emptyList())
    val users: StateFlow<List<User>> = _users

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage

    fun loadUsers() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val response = api.getAllUsers()
                if (response.isSuccessful) {
                    _users.value = response.body()?.data ?: emptyList()
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

    fun createUser(name: String, email: String, age: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = api.createUser(User(name = name, email = email, age = age))
                if (response.isSuccessful && response.body()?.success == true) {
                    _successMessage.value = "User '$name' created!"
                    loadUsers()
                } else {
                    _errorMessage.value = response.body()?.message ?: "Failed to create user"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Network error: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteUser(id: Long) {
        viewModelScope.launch {
            try {
                val response = api.deleteUser(id)
                if (response.isSuccessful) {
                    _successMessage.value = "User deleted"
                    loadUsers()
                } else {
                    _errorMessage.value = "Failed to delete"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Network error: ${e.message}"
            }
        }
    }

    fun searchUsers(query: String) {
        if (query.isBlank()) { loadUsers(); return }
        viewModelScope.launch {
            try {
                val response = api.searchUsers(query)
                if (response.isSuccessful) {
                    _users.value = response.body()?.data ?: emptyList()
                }
            } catch (e: Exception) {
                _errorMessage.value = "Search failed: ${e.message}"
            }
        }
    }

    fun clearMessages() {
        _errorMessage.value = null
        _successMessage.value = null
    }
}

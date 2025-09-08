package com.example.num8rix.ui.screens

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class MyDatabaseViewModelFactory(
    private val application: Application
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MyDatabaseViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MyDatabaseViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
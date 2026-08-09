package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.data.WorkloadRepository

class AppViewModelFactory(private val repository: WorkloadRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SubjectViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SubjectViewModel(repository) as T
        }
        if (modelClass.isAssignableFrom(TeacherViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TeacherViewModel(repository) as T
        }
        if (modelClass.isAssignableFrom(DistributionViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DistributionViewModel(repository) as T
        }
        if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SettingsViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

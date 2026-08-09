package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.WorkloadRepository
import com.example.data.entities.DistributionSettings
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(private val repository: WorkloadRepository) : ViewModel() {
    val settings: StateFlow<DistributionSettings> = repository.settings
        .map { it ?: DistributionSettings() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = DistributionSettings()
        )

    fun updateSettings(newSettings: DistributionSettings) {
        viewModelScope.launch {
            repository.insertSettings(newSettings)
        }
    }
}

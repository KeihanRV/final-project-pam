package com.example.final_project_pam.viewmodel

import android.app.Application
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.final_project_pam.data.model.InstalledApp
import com.example.final_project_pam.data.model.SelectedApp
import com.example.final_project_pam.repository.AppSelectRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.launch

class AppSelectViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AppSelectRepository(application)

    private val _uiState = MutableStateFlow<AppSelectUiState>(AppSelectUiState.Loading)
    val uiState: StateFlow<AppSelectUiState> = _uiState

    private val _pendingSelections = MutableStateFlow<Set<String>>(emptySet())
    val pendingSelections: StateFlow<Set<String>> = _pendingSelections

    private val _installedApps = MutableStateFlow<List<InstalledApp>>(emptyList())
    val installedApps: StateFlow<List<InstalledApp>> = _installedApps

    private val _currentSelectedPackages = MutableStateFlow<Set<String>>(emptySet())

    val hasChanges: StateFlow<Boolean> = combine(_pendingSelections, _currentSelectedPackages) { pending, current ->
        pending != current
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.value = when (val current = _uiState.value) {
                is AppSelectUiState.Success -> current.copy(isRefreshing = true)
                is AppSelectUiState.Error -> current.copy(isRetrying = true)
                else -> AppSelectUiState.Loading
            }

            try {
                val userId = repository.getUserId()
                    ?: throw IllegalStateException("User not authenticated")

                val selectedApps = repository.getUserSelectedApps(userId)
                val installedApps = repository.getInstalledApps()

                _installedApps.value = installedApps
                _currentSelectedPackages.value = selectedApps.map { it.packageName }.toSet()
                _pendingSelections.value = _currentSelectedPackages.value

                _uiState.value = AppSelectUiState.Success(
                    selectedApps = selectedApps,
                    installedApps = installedApps
                )
            } catch (e: Exception) {
                _uiState.value = AppSelectUiState.Error(
                    message = e.message ?: "Gagal memuat data",
                    selectedApps = emptyList()
                )
            }
        }
    }

    fun updateMinutes(packageName: String, minutes: Int) {
        val validMinutes = minutes.coerceIn(1, 1440)
        viewModelScope.launch {
            val current = _uiState.value as? AppSelectUiState.Success ?: return@launch
            val target = current.selectedApps.find { it.packageName == packageName } ?: return@launch

            _uiState.value = current.copy(
                pendingActionPackage = packageName,
                isSaving = true
            )

            val userId = repository.getUserId() ?: run {
                _uiState.value = current.copy(
                    isSaving = false,
                    pendingActionPackage = null,
                    snackbarMessage = "User tidak terautentikasi"
                )
                return@launch
            }

            val updated = repository.upsertSelectedApp(
                uid = userId,
                packageName = target.packageName,
                appLabel = target.appLabel,
                unscrollMinutes = validMinutes
            )

            if (updated != null) {
                val newList = current.selectedApps.map {
                    if (it.packageName == packageName) it.copy(unscrollMinutes = validMinutes) else it
                }
                repository.updateCachedApps(newList)
                _uiState.value = current.copy(
                    selectedApps = newList,
                    isSaving = false,
                    pendingActionPackage = null,
                    snackbarMessage = "Durasi diperbarui"
                )
            } else {
                _uiState.value = current.copy(
                    isSaving = false,
                    pendingActionPackage = null,
                    snackbarMessage = "Gagal memperbarui durasi"
                )
            }
        }
    }

    fun deleteApp(packageName: String) {
        viewModelScope.launch {
            val current = _uiState.value as? AppSelectUiState.Success ?: return@launch

            _uiState.value = current.copy(
                pendingActionPackage = packageName,
                isSaving = true
            )

            val userId = repository.getUserId() ?: run {
                _uiState.value = current.copy(
                    isSaving = false,
                    pendingActionPackage = null,
                    snackbarMessage = "User tidak terautentikasi"
                )
                return@launch
            }

            val success = repository.deleteSelectedApp(userId, packageName)
            if (success) {
                val newList = current.selectedApps.filter { it.packageName != packageName }
                _currentSelectedPackages.value = _currentSelectedPackages.value - packageName
                _pendingSelections.value = _currentSelectedPackages.value
                repository.updateCachedApps(newList)
                _uiState.value = current.copy(
                    selectedApps = newList,
                    isSaving = false,
                    pendingActionPackage = null,
                    snackbarMessage = "Aplikasi dihapus"
                )
            } else {
                _uiState.value = current.copy(
                    isSaving = false,
                    pendingActionPackage = null,
                    snackbarMessage = "Gagal menghapus aplikasi"
                )
            }
        }
    }

    fun launchApp(packageName: String) {
        val intent = repository.getLaunchIntent(packageName)
        if (intent != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            try {
                getApplication<Application>().startActivity(intent)
            } catch (e: Exception) {
                _uiState.value = when (val current = _uiState.value) {
                    is AppSelectUiState.Success -> current.copy(
                        snackbarMessage = "Gagal membuka aplikasi"
                    )
                    else -> current
                }
            }
        } else {
            _uiState.value = when (val current = _uiState.value) {
                is AppSelectUiState.Success -> current.copy(
                    snackbarMessage = "Aplikasi tidak ditemukan"
                )
                else -> current
            }
        }
    }

    fun toggleSelection(packageName: String) {
        _pendingSelections.value = _pendingSelections.value.toMutableSet().apply {
            if (contains(packageName)) remove(packageName) else add(packageName)
        }
    }

    fun saveSelectedApps(onComplete: () -> Unit) {
        viewModelScope.launch {
            val current = _uiState.value as? AppSelectUiState.Success ?: return@launch

            _uiState.value = current.copy(isSaving = true)

            val userId = repository.getUserId() ?: run {
                _uiState.value = current.copy(
                    isSaving = false,
                    snackbarMessage = "User tidak terautentikasi"
                )
                return@launch
            }

            val selectedPackages = _pendingSelections.value
            val installedApps = _installedApps.value
            val newSelected = mutableListOf<SelectedApp>()

            for (packageName in selectedPackages) {
                val installed = installedApps.find { it.packageName == packageName } ?: continue
                val existing = current.selectedApps.find { it.packageName == packageName }

                val result = repository.upsertSelectedApp(
                    uid = userId,
                    packageName = packageName,
                    appLabel = installed.label,
                    unscrollMinutes = existing?.unscrollMinutes ?: 15
                )
                if (result != null) newSelected.add(result)
            }

            val toDelete = _currentSelectedPackages.value - selectedPackages
            for (packageName in toDelete) {
                repository.deleteSelectedApp(userId, packageName)
            }

            _currentSelectedPackages.value = selectedPackages
            _pendingSelections.value = selectedPackages
            repository.updateCachedApps(newSelected)

            _uiState.value = current.copy(
                selectedApps = newSelected,
                isSaving = false,
                snackbarMessage = "Aplikasi tersimpan"
            )

            onComplete()
        }
    }

    fun initPickerSelections() {
        _pendingSelections.value = _currentSelectedPackages.value
        if (_installedApps.value.isEmpty()) {
            val apps = repository.getInstalledApps()
            _installedApps.value = apps
        }
    }

    fun clearSnackbar() {
        _uiState.value = when (val current = _uiState.value) {
            is AppSelectUiState.Success -> current.copy(snackbarMessage = null)
            is AppSelectUiState.Error -> current
            else -> current
        }
    }
}

package com.example.final_project_pam.features.profile.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.final_project_pam.features.profile.data.model.UserProfile
import com.example.final_project_pam.features.profile.data.repository.ProfileRepository
import com.example.final_project_pam.features.profile.data.repository.ProfileRepositoryImpl
import com.example.final_project_pam.features.profile.ui.state.ProfileUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.contentOrNull

class ProfileViewModel(
    private val repository: ProfileRepository = ProfileRepositoryImpl()
) : ViewModel() {

    private val _uiState = MutableStateFlow<ProfileUiState>(ProfileUiState.Idle)
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    private val _name = MutableStateFlow("")
    val name: StateFlow<String> = _name.asStateFlow()

    private val _phoneNumber = MutableStateFlow("")
    val phoneNumber: StateFlow<String> = _phoneNumber.asStateFlow()

    private val _birthDate = MutableStateFlow("")
    val birthDate: StateFlow<String> = _birthDate.asStateFlow()

    private val _avatarUrl = MutableStateFlow("")
    val avatarUrl: StateFlow<String> = _avatarUrl.asStateFlow()

    private val _isEditMode = MutableStateFlow(false)
    val isEditMode: StateFlow<Boolean> = _isEditMode.asStateFlow()

    init {
        fetchProfile()
    }

    fun fetchProfile() {
        viewModelScope.launch {
            _uiState.value = ProfileUiState.Loading
            val userId = repository.getCurrentUserId()
            val userInfo = repository.getCurrentUserInfo()
            
            if (userId != null) {
                var profile = repository.getProfile(userId)
                
                // Ambil data dari metadata Supabase Auth (auth.users)
                val authName = userInfo?.userMetadata?.get("full_name")?.jsonPrimitive?.contentOrNull 
                    ?: userInfo?.userMetadata?.get("name")?.jsonPrimitive?.contentOrNull 
                    ?: ""
                val authAvatar = userInfo?.userMetadata?.get("avatar_url")?.jsonPrimitive?.contentOrNull ?: ""
                val authEmail = userInfo?.email ?: ""

                if (profile == null) {
                    profile = UserProfile(
                        userId = userId,
                        userEmail = authEmail,
                        name = authName,
                        avatar = authAvatar
                    )
                } else {
                    profile = profile.copy(
                        userEmail = profile.userEmail.takeIf { !it.isNullOrBlank() } ?: authEmail,
                        name = profile.name.takeIf { !it.isNullOrBlank() } ?: authName,
                        avatar = profile.avatar.takeIf { !it.isNullOrBlank() } ?: authAvatar
                    )
                }

                _uiState.value = ProfileUiState.Success(profile)
                
                // Sinkronkan ke State form
                _name.value = profile.name ?: ""
                _phoneNumber.value = profile.phoneNumber ?: ""
                _birthDate.value = profile.birthDate ?: ""
                
                // Tambahkan cache buster agar Coil mendeteksi perubahan gambar avatar baru
                _avatarUrl.value = if (!profile.avatar.isNullOrBlank()) {
                    "${profile.avatar.substringBefore("?t=")}?t=${System.currentTimeMillis()}"
                } else {
                    ""
                }
            } else {
                _uiState.value = ProfileUiState.Error("Sesi berakhir, silakan login kembali")
            }
        }
    }

    fun onNameChange(newName: String) { _name.value = newName }
    fun onPhoneNumberChange(newPhone: String) { _phoneNumber.value = newPhone }
    fun onBirthDateChange(newDate: String) { _birthDate.value = newDate }
    fun toggleEditMode() { _isEditMode.value = !_isEditMode.value }

    fun uploadAvatar(byteArray: ByteArray) {
        viewModelScope.launch {
            val userId = repository.getCurrentUserId()
            if (userId != null) {
                val previousState = _uiState.value
                _uiState.value = ProfileUiState.Loading 
                try {
                    val publicUrl = repository.uploadAvatar(userId, byteArray)
                    val publicUrlWithBuster = "${publicUrl.substringBefore("?t=")}?t=${System.currentTimeMillis()}"
                    _avatarUrl.value = publicUrlWithBuster
                    
                    // Simpan URL avatar baru ke database user_profile secara langsung
                    if (previousState is ProfileUiState.Success) {
                        val updatedProfile = previousState.profile.copy(avatar = publicUrl)
                        repository.updateProfile(updatedProfile)
                        _uiState.value = ProfileUiState.Success(updatedProfile)
                    } else {
                        val userInfo = repository.getCurrentUserInfo()
                        val authName = userInfo?.userMetadata?.get("full_name")?.jsonPrimitive?.contentOrNull 
                            ?: userInfo?.userMetadata?.get("name")?.jsonPrimitive?.contentOrNull 
                            ?: ""
                        val authEmail = userInfo?.email ?: ""
                        val newProfile = UserProfile(
                            userId = userId,
                            userEmail = authEmail,
                            name = authName,
                            avatar = publicUrl
                        )
                        repository.updateProfile(newProfile)
                        _uiState.value = ProfileUiState.Success(newProfile)
                    }
                } catch (e: Exception) {
                    _uiState.value = ProfileUiState.Error("Gagal mengunggah gambar: ${e.message}")
                    if (previousState is ProfileUiState.Success) {
                        _uiState.value = previousState
                    }
                }
            }
        }
    }

    fun updateProfile() {
        if (_name.value.isBlank()) {
            _uiState.value = ProfileUiState.Error("Nama tidak boleh kosong")
            return
        }

        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState is ProfileUiState.Success) {
                _uiState.value = ProfileUiState.Loading
                try {
                    val cleanAvatarUrl = _avatarUrl.value.substringBefore("?t=")
                    val updatedProfile = currentState.profile.copy(
                        name = _name.value.trim(),
                        phoneNumber = _phoneNumber.value.trim().takeIf { it.isNotEmpty() },
                        birthDate = _birthDate.value.trim().takeIf { it.isNotEmpty() },
                        avatar = cleanAvatarUrl.trim().takeIf { it.isNotEmpty() }
                    )
                    repository.updateProfile(updatedProfile)
                    _uiState.value = ProfileUiState.Success(updatedProfile)
                    _isEditMode.value = false
                } catch (e: Exception) {
                    _uiState.value = ProfileUiState.Error("Gagal menyimpan: ${e.message}")
                }
            }
        }
    }

    fun deleteProfile() {
        viewModelScope.launch {
            val userId = repository.getCurrentUserId()
            if (userId != null) {
                _uiState.value = ProfileUiState.Loading
                try {
                    repository.deleteProfile(userId)
                    _uiState.value = ProfileUiState.Deleted
                } catch (e: Exception) {
                    _uiState.value = ProfileUiState.Error("Gagal menghapus akun")
                }
            }
        }
    }
}

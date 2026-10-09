package com.example.model

enum class UserRole(val value: String) {
    CITIZEN("citizen"),
    WARD_ADMIN("ward_admin"),
    SYSTEM_ADMIN("system_admin");

    companion object {
        fun fromString(value: String): UserRole {
            return entries.firstOrNull { it.value.equals(value, ignoreCase = true) || it.name.equals(value, ignoreCase = true) } ?: CITIZEN
        }
    }
}

data class GaunUser(
    val uid: String,
    val email: String? = null,
    val displayName: String? = null,
    val role: UserRole = UserRole.CITIZEN,
    val assignedMunicipalityId: String? = null,
    val assignedWard: Int? = null,
    val isAnonymous: Boolean = false
) {
    val isAdmin: Boolean
        get() = role == UserRole.WARD_ADMIN || role == UserRole.SYSTEM_ADMIN
}

sealed interface AuthUiState {
    data object Idle : AuthUiState
    data object Loading : AuthUiState
    data class Authenticated(val user: GaunUser) : AuthUiState
    data class Error(val message: String) : AuthUiState
}

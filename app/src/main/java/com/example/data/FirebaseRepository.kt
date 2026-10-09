package com.example.data

import android.content.Context
import androidx.annotation.VisibleForTesting
import com.example.model.EmergencyContact
import com.example.model.GaunUser
import com.example.model.HealthFacility
import com.example.model.LocalNotice
import com.example.model.UserRole
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class FirebaseRepository(private val context: Context) {

    private val currentUserFlow = MutableStateFlow(
        GaunUser(uid = "guest_citizen", displayName = "नागरिक", role = UserRole.CITIZEN, isAnonymous = true)
    )

    private val noticesFlow = MutableStateFlow<List<LocalNotice>>(emptyList())
    private val contactsFlow = MutableStateFlow<List<EmergencyContact>>(emptyList())
    private val facilitiesFlow = MutableStateFlow<List<HealthFacility>>(emptyList())

    fun getCurrentUserFlow(): Flow<GaunUser> = currentUserFlow.asStateFlow()

    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    fun setDemoAdminRole(isAdmin: Boolean) {
        currentUserFlow.value = if (isAdmin) {
            GaunUser(
                uid = "admin_ward_officer",
                email = "ward.officer@smartgaun.gov.np",
                displayName = "वडा अधिकृत (Admin)",
                role = UserRole.WARD_ADMIN,
                isAnonymous = false
            )
        } else {
            GaunUser(
                uid = "guest_citizen",
                displayName = "नागरिक",
                role = UserRole.CITIZEN,
                isAnonymous = true
            )
        }
    }

    fun signInWithEmail(email: String, pass: String, onResult: (Boolean, String?) -> Unit) {
        // Enforce input validation; password is NEVER persisted in memory, storage, or logs
        if (email.isNotBlank() && email.contains("@") && pass.length >= 6) {
            currentUserFlow.value = GaunUser(
                uid = "admin_ward_officer",
                email = email.trim(),
                displayName = "वडा अधिकृत (Admin)",
                role = UserRole.WARD_ADMIN,
                isAnonymous = false
            )
            onResult(true, null)
        } else {
            onResult(false, "कृपया मान्य आधिकारिक इमेल र कम्तिमा ६ वर्णको पासवर्ड प्रविष्ट गर्नुहोस्")
        }
    }

    fun signOut() {
        // Clear local administrative session back to guest citizen
        currentUserFlow.value = GaunUser(
            uid = "guest_citizen",
            displayName = "नागरिक",
            role = UserRole.CITIZEN,
            isAnonymous = true
        )
    }

    fun isCurrentAdmin(): Boolean = currentUserFlow.value.isAdmin

    fun getNoticesFlow(): Flow<List<LocalNotice>> = noticesFlow.asStateFlow()

    fun addNotice(notice: LocalNotice): Boolean {
        if (!isCurrentAdmin()) return false
        val current = noticesFlow.value.toMutableList()
        val index = current.indexOfFirst { it.id == notice.id }
        if (index >= 0) {
            current[index] = notice
        } else {
            current.add(0, notice)
        }
        noticesFlow.value = current
        return true
    }

    fun deleteNotice(noticeId: String): Boolean {
        if (!isCurrentAdmin()) return false
        noticesFlow.value = noticesFlow.value.filterNot { it.id == noticeId }
        return true
    }

    fun getEmergencyContactsFlow(): Flow<List<EmergencyContact>> = contactsFlow.asStateFlow()

    fun addEmergencyContact(contact: EmergencyContact): Boolean {
        if (!isCurrentAdmin()) return false
        val current = contactsFlow.value.toMutableList()
        val index = current.indexOfFirst { it.id == contact.id }
        if (index >= 0) {
            current[index] = contact
        } else {
            current.add(contact)
        }
        contactsFlow.value = current
        return true
    }

    fun deleteEmergencyContact(contactId: String): Boolean {
        if (!isCurrentAdmin()) return false
        contactsFlow.value = contactsFlow.value.filterNot { it.id == contactId }
        return true
    }

    fun getHealthFacilitiesFlow(): Flow<List<HealthFacility>> = facilitiesFlow.asStateFlow()

    fun addHealthFacility(facility: HealthFacility): Boolean {
        if (!isCurrentAdmin()) return false
        val current = facilitiesFlow.value.toMutableList()
        val index = current.indexOfFirst { it.id == facility.id }
        if (index >= 0) {
            current[index] = facility
        } else {
            current.add(facility)
        }
        facilitiesFlow.value = current
        return true
    }

    fun deleteHealthFacility(facilityId: String): Boolean {
        if (!isCurrentAdmin()) return false
        facilitiesFlow.value = facilitiesFlow.value.filterNot { it.id == facilityId }
        return true
    }
}

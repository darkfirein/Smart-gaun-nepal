package com.example.data.security

import android.content.Context
import com.example.BuildConfig
import com.google.firebase.FirebaseApp
import com.google.firebase.appcheck.AppCheckProviderFactory
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * Firebase App Check & Google Play Integrity Manager (Phase 18).
 *
 * Security Boundaries:
 * - Production builds: Strictly installs [PlayIntegrityAppCheckProviderFactory].
 * - Debug builds: Uses [DebugAppCheckProviderFactory] via reflection to avoid packaging debug tokens in release.
 * - No hardcoded API keys, debug tokens, or service-account JSON exist in source code.
 */
object FirebaseAppCheckManager {

    private var isInitialized = false

    fun initialize(context: Context, isDebug: Boolean = BuildConfig.DEBUG) {
        if (isInitialized) return

        // Verify FirebaseApp is initialized before installing App Check
        if (FirebaseApp.getApps(context).isEmpty()) {
            return
        }

        try {
            val appCheck = FirebaseAppCheck.getInstance()
            val factory = getProviderFactory(isDebug)
            appCheck.installAppCheckProviderFactory(factory)
            isInitialized = true
        } catch (_: Exception) {
            // Silently handle if platform environment or test runner lacks Play Services
        }
    }

    fun getProviderFactory(isDebug: Boolean): AppCheckProviderFactory {
        return if (isDebug) {
            // In debug mode, safely load DebugAppCheckProviderFactory via reflection
            // This prevents DebugAppCheckProviderFactory from leaking into release APKs.
            try {
                val debugClass = Class.forName("com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory")
                val getInstanceMethod = debugClass.getMethod("getInstance")
                getInstanceMethod.invoke(null) as AppCheckProviderFactory
            } catch (_: Exception) {
                PlayIntegrityAppCheckProviderFactory.getInstance()
            }
        } else {
            // Production strictly uses Google Play Integrity
            PlayIntegrityAppCheckProviderFactory.getInstance()
        }
    }

    suspend fun getAppCheckToken(): String? = suspendCancellableCoroutine { cont ->
        try {
            val appCheck = FirebaseAppCheck.getInstance()
            appCheck.getAppCheckToken(false)
                .addOnSuccessListener { tokenResult ->
                    cont.resume(tokenResult.token)
                }
                .addOnFailureListener {
                    cont.resume(null)
                }
        } catch (_: Exception) {
            cont.resume(null)
        }
    }

    fun isAppCheckInitialized(): Boolean = isInitialized
}

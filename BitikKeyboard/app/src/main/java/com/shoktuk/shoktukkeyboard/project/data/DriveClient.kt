package com.shoktuk.shoktukkeyboard.project.data

import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.gms.auth.api.identity.AuthorizationRequest
import com.google.android.gms.auth.api.identity.AuthorizationResult
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.common.api.Scope
import com.google.android.gms.tasks.Task
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.http.ByteArrayContent
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

object DriveClient {
    private const val APP_NAME = "Shoktuk Keyboard"
    private val driveScope = Scope(DriveScopes.DRIVE_APPDATA)

    private var pendingResolution: kotlinx.coroutines.CancellableContinuation<AuthorizationResult?>? = null
    private var launcher: ActivityResultLauncher<IntentSenderRequest>? = null

    @Volatile
    var isAuthorized: Boolean = false
        private set

    var lastError: String? = null
        private set

    private var cachedAccessToken: String? = null

    fun registerLauncher(activity: ComponentActivity) {
        launcher = activity.registerForActivityResult(
            ActivityResultContracts.StartIntentSenderForResult()
        ) { result ->
            val continuation = pendingResolution
            pendingResolution = null
            if (result.resultCode == android.app.Activity.RESULT_OK && result.data != null) {
                val resolved = runCatching {
                    Identity.getAuthorizationClient(activity).getAuthorizationResultFromIntent(result.data!!)
                }.onFailure { lastError = it.message }.getOrNull()
                continuation?.resume(resolved)
            } else {
                lastError = "Sign-in was cancelled"
                continuation?.resume(null)
            }
        }
    }

    suspend fun ensureAuthorized(activity: ComponentActivity, allowUi: Boolean = false): Boolean {
        val request = AuthorizationRequest.builder().setRequestedScopes(listOf(driveScope)).build()
        val result = runCatching {
            Identity.getAuthorizationClient(activity).authorize(request).awaitTask()
        }.onFailure { lastError = it.message }.getOrNull()

        if (result == null) {
            isAuthorized = false
            return false
        }

        if (result.hasResolution() && !allowUi) {
            isAuthorized = false
            return false
        }

        val resolved = if (result.hasResolution()) resolve(activity, result) else result
        cachedAccessToken = resolved?.accessToken
        isAuthorized = cachedAccessToken != null
        if (!isAuthorized && lastError == null) lastError = "Authorization did not return an access token"
        return isAuthorized
    }

    suspend fun signIn(activity: ComponentActivity): Boolean = ensureAuthorized(activity, allowUi = true)

    fun signOut() {
        cachedAccessToken = null
        isAuthorized = false
    }

    private suspend fun resolve(activity: ComponentActivity, result: AuthorizationResult): AuthorizationResult? {
        val pendingIntent = result.pendingIntent ?: return null
        val launcher = this.launcher ?: run {
            lastError = "Drive sign-in isn't wired up yet (registerLauncher was never called)"
            return null
        }
        return suspendCancellableCoroutine { continuation ->
            pendingResolution = continuation
            runCatching {
                launcher.launch(IntentSenderRequest.Builder(pendingIntent.intentSender).build())
            }.onFailure {
                lastError = it.message
                pendingResolution = null
                continuation.resume(null)
            }
        }
    }

    private suspend fun requireDrive(activity: ComponentActivity): Drive? {
        if (cachedAccessToken == null && !ensureAuthorized(activity)) return null
        val token = cachedAccessToken ?: return null
        return runCatching {
            val credential = GoogleCredential().setAccessToken(token)
            Drive.Builder(GoogleNetHttpTransport.newTrustedTransport(), GsonFactory.getDefaultInstance(), credential)
                .setApplicationName(APP_NAME)
                .build()
        }.onFailure { lastError = it.message }.getOrNull()
    }

    suspend fun readFile(activity: ComponentActivity, name: String): String? {
        val drive = requireDrive(activity) ?: return null
        return withContext(Dispatchers.IO) {
            runCatching {
                val fileId = findFileId(drive, name) ?: return@runCatching null
                drive.files().get(fileId).executeMediaAsInputStream().bufferedReader().use { it.readText() }
            }.onFailure { lastError = it.message }.getOrNull()
        }
    }

    suspend fun writeFile(activity: ComponentActivity, name: String, content: String): Boolean {
        val drive = requireDrive(activity) ?: return false
        return withContext(Dispatchers.IO) {
            runCatching {
                val mediaContent = ByteArrayContent("application/json", content.toByteArray())
                val existingId = findFileId(drive, name)
                if (existingId != null) {
                    drive.files().update(existingId, null, mediaContent).execute()
                } else {
                    val metadata = com.google.api.services.drive.model.File().apply {
                        this.name = name
                        parents = listOf("appDataFolder")
                    }
                    drive.files().create(metadata, mediaContent).execute()
                }
                lastError = null
                true
            }.onFailure { lastError = it.message }.getOrDefault(false)
        }
    }

    suspend fun listFiles(activity: ComponentActivity, namePrefix: String): List<String>? {
        val drive = requireDrive(activity) ?: return null
        return withContext(Dispatchers.IO) {
            runCatching {
                val result = drive.files().list()
                    .setSpaces("appDataFolder")
                    .setFields("files(id, name)")
                    .execute()
                lastError = null
                result.files.filter { it.name.startsWith(namePrefix) }.map { it.name }
            }.onFailure { lastError = it.message }.getOrNull()
        }
    }

    suspend fun deleteFile(activity: ComponentActivity, name: String): Boolean {
        val drive = requireDrive(activity) ?: return false
        return withContext(Dispatchers.IO) {
            runCatching {
                val fileId = findFileId(drive, name) ?: return@runCatching true
                drive.files().delete(fileId).execute()
                lastError = null
                true
            }.onFailure { lastError = it.message }.getOrDefault(false)
        }
    }

    private fun findFileId(drive: Drive, name: String): String? {
        val result = drive.files().list()
            .setSpaces("appDataFolder")
            .setQ("name = '${name.replace("'", "\\'")}'")
            .setFields("files(id, name)")
            .execute()
        return result.files.firstOrNull()?.id
    }
}

private suspend fun <T> Task<T>.awaitTask(): T = suspendCancellableCoroutine { continuation ->
    addOnSuccessListener { continuation.resume(it) }
    addOnFailureListener { continuation.resumeWithException(it) }
}

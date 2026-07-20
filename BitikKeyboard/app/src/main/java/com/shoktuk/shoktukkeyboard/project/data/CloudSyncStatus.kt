package com.shoktuk.shoktukkeyboard.project.data

import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.writingSystem
import com.shoktuk.shoktukkeyboard.project.screens.settings.loadSavedStringItems

object CloudSyncStatus {
    enum class AccountState { CHECKING, SIGNED_OUT, SIGNED_IN, ERROR }

    var accountState by mutableStateOf(AccountState.SIGNED_OUT)
        private set
    var settingsSyncedKeyCount by mutableStateOf<Int?>(null)
        private set
    var savedStringsCloudCount by mutableStateOf<Int?>(null)
        private set
    var savedStringsErrorDescription by mutableStateOf<String?>(null)
        private set
    var lastCheckedAt by mutableStateOf<Long?>(null)
        private set
    var isRefreshing by mutableStateOf(false)
        private set

    suspend fun signIn(activity: ComponentActivity) {
        isRefreshing = true
        try {
            val ok = DriveClient.signIn(activity)
            if (!ok) {
                accountState = AccountState.ERROR
                savedStringsErrorDescription = DriveClient.lastError
                return
            }
            refresh(activity, alreadyRefreshing = true)
        } finally {
            isRefreshing = false
        }
    }

    fun signOut() {
        DriveClient.signOut()
        accountState = AccountState.SIGNED_OUT
        settingsSyncedKeyCount = null
        savedStringsCloudCount = null
        savedStringsErrorDescription = null
    }

    suspend fun refresh(activity: ComponentActivity, alreadyRefreshing: Boolean = false) {
        if (!alreadyRefreshing) isRefreshing = true
        try {
            val authorized = DriveClient.isAuthorized || DriveClient.ensureAuthorized(activity)
            if (!authorized) {
                accountState = AccountState.SIGNED_OUT
                settingsSyncedKeyCount = null
                savedStringsCloudCount = null
                lastCheckedAt = System.currentTimeMillis()
                return
            }

            accountState = AccountState.SIGNED_IN
            settingsSyncedKeyCount = SettingsCloudSync.cloudSettingCount(activity)
            savedStringsCloudCount = refreshSavedStringsCloudCount(activity)
            lastCheckedAt = System.currentTimeMillis()
        } finally {
            if (!alreadyRefreshing) isRefreshing = false
        }
    }

    private suspend fun refreshSavedStringsCloudCount(activity: ComponentActivity): Int? {
        val script = activity.writingSystem

        SavedStringsCloudStore.fetchAll(activity, script)?.let {
            savedStringsErrorDescription = null
            return it.size
        }

        val local = loadSavedStringItems(activity, script)
        if (local.isEmpty()) {
            savedStringsErrorDescription = null
            return 0
        }
        savedStringsErrorDescription = DriveClient.lastError

        SavedStringsCloudStore.save(activity, script, local)
        if (DriveClient.lastError != null) {
            savedStringsErrorDescription = DriveClient.lastError
            return null
        }

        val retried = SavedStringsCloudStore.fetchAll(activity, script)
        if (retried != null) {
            savedStringsErrorDescription = null
            return retried.size
        }
        savedStringsErrorDescription = DriveClient.lastError
        return null
    }
}

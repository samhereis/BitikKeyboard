package com.shoktuk.shoktukkeyboard.project.screens.settings

import androidx.activity.ComponentActivity
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.shoktuk.shoktukkeyboard.project.data.CloudSyncStatus
import com.shoktuk.shoktukkeyboard.project.systems.localization.Loc_Settings
import kotlinx.coroutines.launch
import java.text.DateFormat
import java.util.Date

@Composable
fun CloudSettingsScreen() {
    val ctx = LocalContext.current
    val activity = ctx as? ComponentActivity
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        if (activity != null) CloudSyncStatus.refresh(activity)
    }

    val accountStateLabel = when (CloudSyncStatus.accountState) {
        CloudSyncStatus.AccountState.CHECKING -> Loc_Settings.cloudChecking.localizedTitle(ctx)
        CloudSyncStatus.AccountState.SIGNED_IN -> Loc_Settings.cloudSignedIn.localizedTitle(ctx)
        CloudSyncStatus.AccountState.SIGNED_OUT -> Loc_Settings.cloudNoAccount.localizedTitle(ctx)
        CloudSyncStatus.AccountState.ERROR -> Loc_Settings.cloudUnavailable.localizedTitle(ctx)
    }

    fun countLabel(count: Int?) = count?.toString() ?: "—"

    val lastCheckedLabel = CloudSyncStatus.lastCheckedAt?.let {
        DateFormat.getTimeInstance(DateFormat.SHORT).format(Date(it))
    } ?: "—"

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            tonalElevation = 0.dp,
            shadowElevation = 0.dp,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                StatusRow(Loc_Settings.cloudAccount.localizedTitle(ctx), accountStateLabel)
                StatusRow(Loc_Settings.cloudSettingsSynced.localizedTitle(ctx), countLabel(CloudSyncStatus.settingsSyncedKeyCount))
                StatusRow(Loc_Settings.cloudSavedStrings.localizedTitle(ctx), countLabel(CloudSyncStatus.savedStringsCloudCount))
                StatusRow(Loc_Settings.cloudLastChecked.localizedTitle(ctx), lastCheckedLabel)

                if (CloudSyncStatus.isRefreshing) {
                    CircularProgressIndicator(modifier = Modifier.fillMaxWidth())
                } else if (CloudSyncStatus.accountState == CloudSyncStatus.AccountState.SIGNED_IN) {
                    Button(
                        onClick = { activity?.let { a -> scope.launch { CloudSyncStatus.refresh(a) } } },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text(Loc_Settings.cloudRefresh.localizedTitle(ctx)) }
                } else {
                    Button(
                        onClick = { activity?.let { a -> scope.launch { CloudSyncStatus.signIn(a) } } },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text(Loc_Settings.cloudSignInHint.localizedTitle(ctx)) }
                }
            }
        }

        CloudSyncStatus.savedStringsErrorDescription?.let { error ->
            Text(
                "${Loc_Settings.cloudError.localizedTitle(ctx)}: $error",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun StatusRow(title: String, value: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(title)
        Text(value, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

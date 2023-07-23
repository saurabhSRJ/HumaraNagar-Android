package com.humara.nagar.ui

import android.app.Application
import android.content.Context
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import com.humara.nagar.Logger
import com.humara.nagar.base.BaseViewModel

class InAppUpdateViewModel(application: Application) : BaseViewModel(application) {
    private lateinit var appUpdateManager: AppUpdateManager
    private val _installStateLiveData: MutableLiveData<Int> = MutableLiveData()
    val installStateLiveData: LiveData<Int> = _installStateLiveData
    var downloadingShown: Boolean = false
    // Create a listener to track request state updates.
    val listener = InstallStateUpdatedListener { state ->
        _installStateLiveData.postValue(state.installStatus())
    }

    companion object {
        private const val MIN_DAYS_FOR_UPDATE_DEFAULT = 1
        private const val HIGH_PRIORITY_UPDATE = 5
    }

    fun checkUpdate(context: Context, activityResultLauncher: ActivityResultLauncher<IntentSenderRequest>) {
        downloadingShown = false
        appUpdateManager = AppUpdateManagerFactory.create(context)
        // Returns an intent object that you use to check for an update.
        val appUpdateInfoTask = appUpdateManager.appUpdateInfo
        appUpdateInfoTask.addOnSuccessListener { appUpdateInfo ->
            if (isStaleEnough(appUpdateInfo).not()) {
                // Update is not old enough, exit
                Logger.debugLog("App in not old enough")
                return@addOnSuccessListener
            }
            Logger.debugLog("update availability: ${appUpdateInfo.updateAvailability()}")
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
                Logger.debugLog("App update download in progress")
                appUpdateManager.startUpdateFlowForResult(
                    appUpdateInfo,
                    activityResultLauncher,
                    AppUpdateOptions.newBuilder(AppUpdateType.IMMEDIATE)
                        .build())
            } else if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE) {
                // Before starting an update, register a listener for updates.
                appUpdateManager.registerListener(listener)
                //Check update type
                val appUpdateType = if (appUpdateInfo.updatePriority() >= HIGH_PRIORITY_UPDATE && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)) {
                    AppUpdateType.IMMEDIATE
                } else {
                    AppUpdateType.FLEXIBLE
                }
                Logger.debugLog("app update available type: $appUpdateType")
                // Request the update.
                appUpdateManager.startUpdateFlowForResult(
                    appUpdateInfo,
                    activityResultLauncher,
                    AppUpdateOptions.newBuilder(appUpdateType).build()
                )
            } else if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_NOT_AVAILABLE) {
                _installStateLiveData.postValue(InstallStatus.UNKNOWN)
            }
        }
        appUpdateInfoTask.addOnFailureListener {
            _installStateLiveData.postValue(InstallStatus.FAILED)
        }
    }

    fun completeUpdate() {
        appUpdateManager.completeUpdate()
    }

    override fun onCleared() {
        super.onCleared()
        // When status updates are no longer needed, unregister the listener.
        if (this::appUpdateManager.isInitialized) {
            appUpdateManager.unregisterListener(listener)
        }
    }

    private fun isStaleEnough(appUpdateInfo: AppUpdateInfo): Boolean {
        Logger.debugLog("staleness: ${appUpdateInfo.clientVersionStalenessDays()}")
        return appUpdateInfo.clientVersionStalenessDays() == null || appUpdateInfo.clientVersionStalenessDays()!! >= MIN_DAYS_FOR_UPDATE_DEFAULT
    }
}
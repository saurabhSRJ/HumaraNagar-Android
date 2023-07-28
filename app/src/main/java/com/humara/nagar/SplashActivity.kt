package com.humara.nagar

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.google.android.material.snackbar.Snackbar
import com.google.android.play.core.install.model.InstallStatus
import com.humara.nagar.analytics.AnalyticsData
import com.humara.nagar.analytics.AnalyticsTracker
import com.humara.nagar.base.BaseActivity
import com.humara.nagar.base.ViewModelFactory
import com.humara.nagar.databinding.ActivitySplashScreenBinding
import com.humara.nagar.ui.AppConfigViewModel
import com.humara.nagar.ui.InAppUpdateViewModel
import com.humara.nagar.ui.MainActivity
import com.humara.nagar.ui.common.GenericAlertDialog
import com.humara.nagar.ui.signup.OnBoardingActivity
import com.humara.nagar.ui.signup.model.AppUpdateConfig
import com.humara.nagar.utils.*
import org.json.JSONObject

class SplashActivity : BaseActivity() {
    private lateinit var binding: ActivitySplashScreenBinding
    private val appConfigViewModel: AppConfigViewModel by viewModels {
        ViewModelFactory()
    }
    private val inAppUpdateViewModel: InAppUpdateViewModel by viewModels {
        ViewModelFactory()
    }
    private val activityResultLauncher = registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result: ActivityResult ->
        AnalyticsTracker.sendEvent(AnalyticsData.EventName.IN_APP_UPDATE_RESULT, JSONObject().apply {
            put(AnalyticsData.Parameters.IN_APP_UPDATE_RESULT_CODE, result.resultCode)
        })
    }

    companion object {
        const val TAG = "SplashActivity"

        fun start(context: Context) {
            val intent = Intent(context, SplashActivity::class.java)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        binding = ActivitySplashScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)
        if (DeviceHelper.isMinSdk26) {
            NotificationUtils.setupDefaultNotificationChannel(this)
        }
        initViewModelObservers()
        initConfig()
    }

    private fun initViewModelObservers() {
        appConfigViewModel.run {
            observeErrorAndException(this)
            appConfigAndUserRefDataSuccessLiveData.observe(this@SplashActivity) {
                Logger.debugLog("app config success")
                launchNextScreen()
            }
            appOptionalUpdateLiveData.observe(this@SplashActivity) {
                showOptionalAppUpdateDialog(it) {
                    launchNextScreen()
                }
            }
            appForceUpdateLiveData.observe(this@SplashActivity) {
                showForceUpdateDialog(it)
            }
            userRoleChangedLiveData.observe(this@SplashActivity) {
                // User role is different from the locally saved role. Please login again
                this@SplashActivity.showToast(getString(R.string.session_expired_message))
                logout()
            }
        }
        inAppUpdateViewModel.run {
            installStateLiveData.observe(this@SplashActivity) { installStatus ->
                Logger.debugLog("install state: $installStatus")
                if (installStatus == InstallStatus.DOWNLOADING && downloadingShown.not()) {
                    AnalyticsTracker.sendEvent(AnalyticsData.EventName.IN_APP_UPDATE_ACCEPTED, JSONObject().apply {
                        put(AnalyticsData.Parameters.EVENT_TYPE, AnalyticsData.EventType.BUTTON_CLICK)
                    })
                    downloadingShown = true
                    binding.ivIcon.showSnackBar(getString(R.string.app_update_downloading))
                } else if (installStatus == InstallStatus.DOWNLOADED) {
                    Snackbar.make(binding.root, R.string.app_update_downloaded, Snackbar.LENGTH_INDEFINITE)
                        .setAction(R.string.install) { completeUpdate() }
                        .setActionTextColor(ContextCompat.getColor(this@SplashActivity, R.color.green_138808))
                        .show()
                } else if (installStatus == InstallStatus.FAILED) {
                    AnalyticsTracker.sendEvent(AnalyticsData.EventName.IN_APP_UPDATE_FAILED, JSONObject().apply {
                        put(AnalyticsData.Parameters.EVENT_TYPE, AnalyticsData.EventType.BUTTON_CLICK)
                    })
                    binding.ivIcon.showSnackBar(getString(R.string.app_update_failed), R.drawable.ic_error_vector)
                    this@SplashActivity.startActivity(IntentUtils.redirectToPlayStoreAppIntent(this@SplashActivity, BuildConfig.APPLICATION_ID))
                } else if (installStatus == InstallStatus.UNKNOWN) {
                    AnalyticsTracker.sendEvent(AnalyticsData.EventName.APP_UPDATE_NOT_AVAILABLE, null)
                    launchNextScreen()
                }
            }
        }
    }

    private fun showForceUpdateDialog(appUpdateConfig: AppUpdateConfig) {
        GenericAlertDialog.show(
            supportFragmentManager,
            title = getString(R.string.update_app),
            message = appUpdateConfig.description ?: getString(R.string.update_app),
            isCancelable = false,
            positiveButtonText = getString(R.string.update),
            positiveButtonListener = {
                inAppUpdateViewModel.checkUpdate(this, activityResultLauncher)
            }
        )
    }

    private fun showOptionalAppUpdateDialog(appUpdateConfig: AppUpdateConfig, onCancelUpdate: () -> Unit) {
        GenericAlertDialog.show(
            supportFragmentManager,
            title = getString(R.string.update_app),
            message = appUpdateConfig.description ?: getString(R.string.update_app),
            isCancelable = false,
            positiveButtonText = getString(R.string.update),
            positiveButtonListener = {
                inAppUpdateViewModel.checkUpdate(this, activityResultLauncher)
            },
            negativeButtonText = getString(R.string.skip),
            negativeButtonClickListener = { onCancelUpdate() }
        )
    }

    /* Note: When implicit deeplink is clicked system back button exits the app and goes back to previous app. However navigation up button works as expected
        https://stackoverflow.com/questions/69482684/navigation-component-implicit-deep-link-back-press-exits-the-app
     */
    private fun launchNextScreen() {
        val launchIntent = Intent(this, MainActivity::class.java).apply {
            data = intent.data
            Logger.debugLog("intent data: $data")
        }
        startActivity(launchIntent)
        finish()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        Logger.debugLog("onNewIntent called")
    }

    private fun initConfig() {
        Handler(Looper.getMainLooper()).postDelayed({
            if (getUserPreference().isUserLoggedIn) {
                appConfigViewModel.getAppConfigAndUserReferenceData()
            } else {
                OnBoardingActivity.startActivity(this, getScreenName())
                finish()
            }
        }, 2000)
    }

    override fun getScreenName() = AnalyticsData.ScreenName.SPLASH_ACTIVITY
}
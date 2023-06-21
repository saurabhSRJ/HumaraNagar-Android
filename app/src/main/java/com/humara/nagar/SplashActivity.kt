package com.humara.nagar

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.viewModels
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.humara.nagar.analytics.AnalyticsData
import com.humara.nagar.base.BaseActivity
import com.humara.nagar.base.ViewModelFactory
import com.humara.nagar.databinding.ActivitySplashScreenBinding
import com.humara.nagar.ui.AppConfigViewModel
import com.humara.nagar.ui.MainActivity
import com.humara.nagar.ui.signup.OnBoardingActivity
import com.humara.nagar.utils.NotificationUtils

class SplashActivity : BaseActivity() {
    private lateinit var binding: ActivitySplashScreenBinding
    private val appConfigViewModel: AppConfigViewModel by viewModels {
        ViewModelFactory()
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationUtils.setupDefaultNotificationChannel(this)
        }
        initViewModelObservers()
        initConfig()
    }

    private fun initViewModelObservers() {
        appConfigViewModel.run {
            observeErrorAndException(this)
            appConfigSuccessLiveData.observe(this@SplashActivity) {
                launchNextScreen()
            }
        }
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
        }, 1000)
    }

    override fun getScreenName() = AnalyticsData.ScreenName.SPLASH_ACTIVITY
}
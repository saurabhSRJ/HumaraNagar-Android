package com.humara.nagar

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.viewModels
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.bumptech.glide.Glide
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
        Glide.with(this).asGif().load(R.drawable.splash_screen_gif).into(binding.animatedView)
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

    private fun launchNextScreen() {
        val launchIntent = Intent(this, MainActivity::class.java).apply {
            data = intent.data
        }
        startActivity(launchIntent)
        finish()
    }

    private fun initConfig() {
        if (getUserPreference().isUserLoggedIn) {
            appConfigViewModel.getAppConfigAndUserReferenceData()
        } else {
            Handler(Looper.getMainLooper()).postDelayed({
                OnBoardingActivity.startActivity(this, getScreenName())
                finish()
            }, 1000)
        }
    }

    override fun getScreenName() = AnalyticsData.ScreenName.SPLASH_ACTIVITY
}
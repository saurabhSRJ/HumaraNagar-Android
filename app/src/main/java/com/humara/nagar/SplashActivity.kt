package com.humara.nagar

import android.content.Context
import android.content.Intent
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
        super.onCreate(savedInstanceState)
        installSplashScreen()
        binding = ActivitySplashScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initViewModelObservers()
        initConfig()
    }

    private fun initViewModelObservers() {
        appConfigViewModel.run {
            observeErrorAndException(this)
            appConfigLiveData.observe(this@SplashActivity) {
                MainActivity.startActivity(this@SplashActivity, getScreenName())
                finish()
            }
        }
    }

    private fun initConfig() {
        MainActivity.startActivity(this@SplashActivity, getScreenName())
        finish()
//        if (getUserPreference().isUserLoggedIn) {
//            appConfigViewModel.getAppConfig()
//        } else {
//            Handler(Looper.getMainLooper()).postDelayed({
//                OnBoardingActivity.startActivity(this, getScreenName())
//                finish()
//            }, 1000)
//        }
    }

    override fun getScreenName() = AnalyticsData.ScreenName.SPLASH_ACTIVITY
}
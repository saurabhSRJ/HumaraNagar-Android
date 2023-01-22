package com.example.humaranagar

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.activity.viewModels
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.humaranagar.base.BaseActivity
import com.example.humaranagar.base.ViewModelFactory
import com.example.humaranagar.databinding.ActivitySplashScreenBinding
import com.example.humaranagar.ui.AppConfigViewModel
import com.example.humaranagar.ui.MainActivity
import com.example.humaranagar.ui.signup.OnBoardingActivity

class SplashActivity : BaseActivity() {
    private lateinit var binding: ActivitySplashScreenBinding
    private val appConfigViewModel: AppConfigViewModel by viewModels {
        ViewModelFactory()
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
                MainActivity.startActivity(this@SplashActivity)
                finish()
            }
        }
    }

    private fun initConfig() {
        if (getUserPreference().isUserLoggedIn) {
            //TODO: fetch config api
            appConfigViewModel.getAppConfig()
        } else {
            Handler(Looper.getMainLooper()).postDelayed({
                startActivity(Intent(this, OnBoardingActivity::class.java))
                finish()
            }, 1000)
        }
    }
}
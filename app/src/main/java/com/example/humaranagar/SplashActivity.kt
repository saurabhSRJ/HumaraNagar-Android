package com.example.humaranagar

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.humaranagar.base.BaseActivity
import com.example.humaranagar.databinding.ActivitySplashScreenBinding
import com.example.humaranagar.ui.signup.OnBoardingActivity

class SplashActivity: BaseActivity() {
    private lateinit var binding: ActivitySplashScreenBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        binding = ActivitySplashScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)
        Handler(Looper.getMainLooper()).postDelayed({
            initConfig()
        }, 1000)
    }

    private fun initConfig() {
        if (getUserPreference().isUserLoggedIn) {
            //TODO: fetch config api
            MainActivity.startActivity(this)
            finish()
        } else {
            startActivity(Intent(this, OnBoardingActivity::class.java))
            finish()
        }
    }
}
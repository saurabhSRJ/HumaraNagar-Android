package com.humara.nagar.ui

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.core.app.NotificationManagerCompat
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.humara.nagar.R
import com.humara.nagar.analytics.AnalyticsData
import com.humara.nagar.base.BaseActivity
import com.humara.nagar.constants.IntentKeyConstants
import com.humara.nagar.databinding.ActivityMainBinding
import com.humara.nagar.permissions.PermissionHandler
import com.humara.nagar.utils.PermissionUtils

class MainActivity : BaseActivity() {
    private lateinit var binding: ActivityMainBinding

    companion object {
        fun startActivity(context: Context, source: String) {
            val intent = Intent(context, MainActivity::class.java).apply {
                putExtra(IntentKeyConstants.SOURCE, source)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK and Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        checkAndRequestNotificationPermission()
        val navView: BottomNavigationView = binding.navView
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment_activity_main) as NavHostFragment
        val navController = navHostFragment.navController
        navView.setupWithNavController(navController)
    }

    private fun checkAndRequestNotificationPermission() {
        if (NotificationManagerCompat.from(this).areNotificationsEnabled().not() && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            PermissionUtils.askPermissions(supportFragmentManager, PermissionUtils.notificationPermissions, object : PermissionHandler {
                override fun onPermissionGranted() {
                    //NA
                }

                override fun onPermissionDenied(permissions: List<String>) {
                    //NA
                }
            }, isPermissionNecessary = false)
        }
    }

    override fun getScreenName() = AnalyticsData.ScreenName.MAIN_ACTIVITY
}
package com.humara.nagar.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.core.app.NotificationManagerCompat
import androidx.core.view.WindowInsetsCompat.Type.ime
import androidx.core.view.WindowInsetsCompat.toWindowInsetsCompat
import androidx.core.view.isGone
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.humara.nagar.Logger
import com.humara.nagar.R
import com.humara.nagar.SplashActivity
import com.humara.nagar.analytics.AnalyticsData
import com.humara.nagar.analytics.AnalyticsTracker
import com.humara.nagar.base.BaseActivity
import com.humara.nagar.base.ViewModelFactory
import com.humara.nagar.constants.IntentKeyConstants
import com.humara.nagar.databinding.ActivityMainBinding
import com.humara.nagar.fluid_resize.FluidContentResizer
import com.humara.nagar.permissions.PermissionHandler
import com.humara.nagar.ui.common.GenericAlertDialog
import com.humara.nagar.utils.NotificationUtils
import com.humara.nagar.utils.PermissionUtils
import com.humara.nagar.utils.getAppSharedPreferences
import com.humara.nagar.utils.getUserSharedPreferences
import org.json.JSONObject

class MainActivity : BaseActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private val appConfigViewModel: AppConfigViewModel by viewModels {
        ViewModelFactory()
    }

    companion object {
        fun startActivity(context: Context, source: String) {
            val intent = Intent(context, MainActivity::class.java).apply {
                putExtra(IntentKeyConstants.SOURCE, source)
//                flags = Intent.FLAG_ACTIVITY_NEW_TASK and Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        if (getUserPreference().isUserLoggedIn.not()) {
            blockUnauthorizedAccess()
        }
        initViewModelObservers()
        setUpBottomNavigation()
        checkAndRequestNotificationPermission()
        FluidContentResizer.listen(this)
    }

    private fun initViewModelObservers() {
        appConfigViewModel.logoutLiveData.observe(this) {
            observeProgress(appConfigViewModel, false)
            SplashActivity.start(this)
        }
    }

    private fun setUpBottomNavigation() {
        val navView: BottomNavigationView = binding.navView
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment_activity_main) as NavHostFragment
        navController = navHostFragment.navController
        //the state of each menu item (i.e. bottom nav destinations) is saved and restored when you use setupWithNavController
        navView.setupWithNavController(navController)
        handleBottomNavVisibilityOnSoftInput()
    }

    private fun handleBottomNavVisibilityOnSoftInput() {
        window.decorView.setOnApplyWindowInsetsListener { view, insets ->
            val insetsCompat = toWindowInsetsCompat(insets, view)
            binding.navView.isGone = insetsCompat.isVisible(ime())
            view.onApplyWindowInsets(insets)
        }
    }

    private fun checkAndRequestNotificationPermission() {
        if (NotificationManagerCompat.from(this).areNotificationsEnabled().not()) {
            PermissionUtils.askPermissions(supportFragmentManager, PermissionUtils.notificationPermissions, object : PermissionHandler {
                override fun onPermissionGranted() {
                    setUpPushNotificationToken()
                }

                override fun onPermissionDenied(permissions: List<String>) {
                    //NA
                }
            }, isPermissionNecessary = false)
        } else {
            setUpPushNotificationToken()
        }
    }

    /**
     * Replace new token with existing token and update to the server, if both are not same
     */
    private fun setUpPushNotificationToken() {
        appConfigViewModel.fetchFcmTokenAndUpdateToServerIfRequired()
    }

    private fun blockUnauthorizedAccess() {
        Logger.debugLog("Unauthorized access")
        GenericAlertDialog.show(this, getString(R.string.unauthorized_access), getString(R.string.session_expired_message), false, getString(R.string.logout)) {
            logout(getScreenName())
        }
    }

    fun logout(source: String) {
        AnalyticsTracker.sendEvent(AnalyticsData.EventName.LOGOUT, JSONObject().put(AnalyticsData.Parameters.SOURCE, source))
        NotificationUtils.clearAllNotification(this)
        getUserSharedPreferences().clearAll()
        getAppSharedPreferences().logOut(false)
        appConfigViewModel.logout()
    }

    override fun getScreenName() = AnalyticsData.ScreenName.MAIN_ACTIVITY
}
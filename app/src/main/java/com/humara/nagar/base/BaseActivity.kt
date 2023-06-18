package com.humara.nagar.base

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.activity.viewModels
import androidx.annotation.DrawableRes
import com.humara.nagar.Logger
import com.humara.nagar.R
import com.humara.nagar.SplashActivity
import com.humara.nagar.analytics.AnalyticsData
import com.humara.nagar.analytics.AnalyticsTracker
import com.humara.nagar.constants.IntentKeyConstants
import com.humara.nagar.network.retrofit.UnauthorizedException
import com.humara.nagar.permissions.PermissionActivity
import com.humara.nagar.shared_pref.AppPreference
import com.humara.nagar.shared_pref.UserPreference
import com.humara.nagar.ui.AppConfigViewModel
import com.humara.nagar.ui.common.GenericAlertDialog
import com.humara.nagar.ui.common.GenericStatusDialog
import com.humara.nagar.ui.common.RelativeLayoutProgressDialog
import com.humara.nagar.ui.common.StatusData
import com.humara.nagar.utils.LocaleManager
import com.humara.nagar.utils.NotificationUtils
import com.humara.nagar.utils.getAppSharedPreferences
import com.humara.nagar.utils.getUserSharedPreferences
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException

/**
 * Base Activity for all the Activities present in the Project. Provides some common functionality for all the Activities.
 * By default, Kotlin classes are final – they can't be inherited. To make a class inheritable, mark it with the open keyword
 */
abstract class BaseActivity : PermissionActivity() {
    private lateinit var progressDialogue: Dialog
    private val appConfigViewModel: AppConfigViewModel by viewModels {
        ViewModelFactory()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_to_left)
        if (shouldLogScreenView()) {
            AnalyticsTracker.sendEvent(
                getScreenName(),
                appendCommonParams(null).apply {
                    put(AnalyticsData.Parameters.EVENT_TYPE, AnalyticsData.EventType.SCREEN_VIEW)
                })
        }
        initViewModelObservers()
    }

    private fun initViewModelObservers() {
        appConfigViewModel.logoutLiveData.observe(this) {
            getUserSharedPreferences().clearAll()
            getAppSharedPreferences().logOut(false)
            SplashActivity.start(this)
            finish()
        }
    }

    open fun appendCommonParams(properties: JSONObject? = null): JSONObject {
        val params = properties ?: JSONObject()

        return params.apply {
            try {
                intent?.getStringExtra(IntentKeyConstants.SOURCE)?.let {
                    put(AnalyticsData.Parameters.SOURCE, it)
                }
                put(AnalyticsData.Parameters.PAGE_TYPE, getScreenName())
                put(AnalyticsData.Parameters.LANGUAGE_CODE, getAppPreference().appLanguage)
                put(AnalyticsData.Parameters.IS_ADMIN, getUserPreference().isAdminUser)
            } catch (e: JSONException) {
                Logger.logException(getScreenName(), e, Logger.LogLevel.ERROR)
            }
        }
    }

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleManager.getWrapper(newBase))
    }

    override fun onBackPressed() {
        onBackPressedDispatcher.onBackPressed()
        overridePendingTransition(R.anim.slide_in_from_left, R.anim.slide_out_to_right)
    }

    override fun onDestroy() {
        super.onDestroy()
        hideKeyboard()
        hideProgress()
    }

    protected open fun observeProgress(viewModel: BaseViewModel, isDismissible: Boolean = true) {
        viewModel.progressLiveData.observe(this) { progress ->
            if (progress) {
                showProgress(isDismissible)
            } else {
                hideProgress()
            }
        }
    }

    protected open fun observeErrorAndException(
        viewModel: BaseViewModel,
        errorAction: () -> Unit = { },
        dismissAction: () -> Unit = { }
    ) {
        viewModel.errorLiveData.observe(this) {
            showErrorDialog(null, it.message, errorAction = errorAction, dismissAction = dismissAction)
        }
        observerException(viewModel)
    }

    protected open fun observerException(
        viewModel: BaseViewModel,
        errorAction: () -> Unit = { },
        dismissAction: () -> Unit = { }
    ) {
        viewModel.exceptionLiveData.observe(this) { exception ->
            Logger.debugLog("Exception caught: $exception")
            when (exception) {
                is IOException -> {
                    showNoInternetDialog(errorAction = errorAction, dismissAction = dismissAction)
                }
                is UnauthorizedException -> {
                    blockUnauthorizedAccess()
                }
                else -> {
                    showErrorDialog(errorAction = errorAction, dismissAction = dismissAction)
                }
            }
        }
    }

    private fun showNoInternetDialog(errorAction: () -> Unit = { }, dismissAction: () -> Unit = { }) {
        showErrorDialog(getString(R.string.no_internet), getString(R.string.no_internet_message), errorAction = errorAction, dismissAction = dismissAction)
    }

    open fun showErrorDialog(
        title: String? = null,
        subtitle: String? = null,
        ctaText: String? = null,
        @DrawableRes icon: Int? = null,
        errorAction: () -> Unit = {},
        dismissAction: () -> Unit = {},
    ) {
        GenericStatusDialog.show(
            supportFragmentManager,
            StatusData(
                GenericStatusDialog.State.ERROR,
                if (title.isNullOrEmpty()) getString(R.string.error) else title,
                if (subtitle.isNullOrEmpty()) getString(R.string.some_error_occoured) else subtitle,
                ctaText,
                icon
            ),
            object : GenericStatusDialog.StatusDialogClickListener {
                override fun ctaClickListener() {
                    errorAction.invoke()
                }

                override fun dismissClickListener() {
                    super.dismissClickListener()
                    dismissAction.invoke()
                }
            }
        )
    }

    open fun blockUnauthorizedAccess() {
        Logger.debugLog("Unauthorized access")
        GenericAlertDialog.show(supportFragmentManager, getString(R.string.unauthorized_access), getString(R.string.session_expired_message), false, getString(R.string.logout)) {
            logout(getScreenName())
        }
    }

    fun logout(source: String) {
        AnalyticsTracker.sendEvent(AnalyticsData.EventName.LOGOUT, JSONObject().put(AnalyticsData.Parameters.SOURCE, source))
        NotificationUtils.clearAllNotification(this)
        appConfigViewModel.logout()
    }

    protected fun showProgress(isDismissible: Boolean) {
        if (this::progressDialogue.isInitialized.not()) {
            progressDialogue = RelativeLayoutProgressDialog.onCreateDialogModel(this).apply {
                setCancelable(isDismissible)
            }
        }
        if (progressDialogue.isShowing.not()) {
            progressDialogue.show()
        }
    }

    protected fun hideProgress() {
        if (this::progressDialogue.isInitialized && progressDialogue.isShowing) {
            progressDialogue.dismiss()
        }
    }

    fun hideKeyboard() {
        val view: View? = this.currentFocus
        view?.let {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(it.windowToken, 0)
        }
    }

    abstract fun getScreenName(): String

    fun shouldLogScreenView(): Boolean = true

    /**
     * Return App preference being set and used throughout the app
     * @return [AppPreference]
     */
    fun getAppPreference(): AppPreference = application.getAppSharedPreferences()

    /**
     * Return User preference data(i.e user profile) being set and used throughout the app.
     * @return [UserPreference]
     */
    fun getUserPreference(): UserPreference = application.getUserSharedPreferences()

    fun getSource(): String {
        return intent?.getStringExtra(IntentKeyConstants.SOURCE) ?: getScreenName()
    }
}
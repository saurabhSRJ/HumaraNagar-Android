package com.humara.nagar.base

import android.app.Dialog
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import com.humara.nagar.Logger
import com.humara.nagar.NagarApp
import com.humara.nagar.R
import com.humara.nagar.analytics.AnalyticsData
import com.humara.nagar.analytics.AnalyticsTracker
import com.humara.nagar.constants.IntentKeyConstants
import com.humara.nagar.network.ApiError
import com.humara.nagar.shared_pref.AppPreference
import com.humara.nagar.shared_pref.UserPreference
import com.humara.nagar.ui.common.GenericStatusDialog
import com.humara.nagar.ui.common.RelativeLayoutProgressDialog
import com.humara.nagar.ui.common.StatusData
import com.humara.nagar.utils.LocaleManager
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException

/**
 * Base Activity for all the Activities present in the Project. Provides some common functionality for all the Activities.
 * By default, Kotlin classes are final – they can't be inherited. To make a class inheritable, mark it with the open keyword
 */
abstract class BaseActivity : AppCompatActivity() {
    private lateinit var progressDialogue: Dialog

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

    protected open fun observeErrorAndException(apiError: ApiError, viewModel: BaseViewModel) {
        showErrorDialog(null, apiError.message)
        observerException(viewModel)
    }

    protected open fun observeErrorAndException(viewModel: BaseViewModel) {
        viewModel.errorLiveData.observe(this) {
            showErrorDialog(null, it.message)
        }
        observerException(viewModel)
    }

    protected open fun observerException(viewModel: BaseViewModel) {
        viewModel.exceptionLiveData.observe(this) { exception ->
            if (exception is IOException) {
                showNoInternetDialog()
            } else {
                showErrorDialog()
            }
        }
    }

    private fun showNoInternetDialog() {
        showErrorDialog(getString(R.string.no_internet), getString(R.string.no_internet_message))
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

    /**
     * Sets the Status Bar Color
     * @param color, is the id value of the color resource
     */
    protected fun changeStatusBarColor(color: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            WindowCompat.getInsetsController(window, window.decorView).apply {
                isAppearanceLightStatusBars = true
            }
            window.statusBarColor = ContextCompat.getColor(this, color)
        }
    }

    abstract fun getScreenName(): String

    fun shouldLogScreenView(): Boolean = true

    /**
     * Return App preference being set and used throughout the app
     * @return [AppPreference]
     */
    fun getAppPreference(): AppPreference {
        return (application as NagarApp).appSharedPreference
    }

    /**
     * Return User preference data(i.e user profile) being set and used throughout the app.
     * @return [UserPreference]
     */
    fun getUserPreference(): UserPreference {
        return (application as NagarApp).userSharedPreference
    }

    fun getSource(): String {
        return intent?.getStringExtra(IntentKeyConstants.SOURCE) ?: getScreenName()
    }
}
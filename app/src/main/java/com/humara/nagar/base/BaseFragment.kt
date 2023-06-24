package com.humara.nagar.base

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.humara.nagar.Logger
import com.humara.nagar.R
import com.humara.nagar.analytics.AnalyticsData
import com.humara.nagar.analytics.AnalyticsTracker
import com.humara.nagar.constants.IntentKeyConstants
import com.humara.nagar.network.retrofit.UnauthorizedException
import com.humara.nagar.shared_pref.AppPreference
import com.humara.nagar.shared_pref.UserPreference
import com.humara.nagar.ui.common.RelativeLayoutProgressDialog
import com.humara.nagar.utils.getAppSharedPreferences
import com.humara.nagar.utils.getUserSharedPreferences
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException

/**
 * Base Fragment for all the Fragments present in the Project. Provides some common functionality for all the Fragments.
 */
abstract class BaseFragment : Fragment() {
    private lateinit var progressDialogue: Dialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (shouldLogScreenView()) {
            AnalyticsTracker.sendEvent(
                getScreenName(),
                appendCommonParams(null).apply {
                    put(AnalyticsData.Parameters.EVENT_TYPE, AnalyticsData.EventType.SCREEN_VIEW)
                }
            )
        }
    }

    open fun appendCommonParams(properties: JSONObject? = null): JSONObject {
        val params = properties ?: JSONObject()
        return params.apply {
            try {
                arguments?.getString(IntentKeyConstants.SOURCE)?.let {
                    put(AnalyticsData.Parameters.SOURCE, it)
                }
                put(AnalyticsData.Parameters.PAGE_TYPE, getScreenName())
                put(AnalyticsData.Parameters.LANGUAGE_CODE, getAppPreference().appLanguage)
                put(AnalyticsData.Parameters.ROLE, getUserPreference().role)
            } catch (e: JSONException) {
                Logger.logException(getScreenName(), e, Logger.LogLevel.ERROR)
            }
        }
    }

    override fun onPause() {
        hideProgress()
        super.onPause()
    }

    /**
     * Generic function to get parent activity of the fragment. Since the function is inlined, no reflection is needed and normal operators like !is and as are now available for you to use
     */
    inline fun <reified T : AppCompatActivity> getParentActivity(): T? {
        var parentActivity: T? = null
        activity?.let {
            parentActivity = it as T
        }
        return parentActivity
    }

    protected open fun observeProgress(viewModel: BaseViewModel, isDismissible: Boolean = true) {
        viewModel.progressLiveData.observe(this) { progress ->
            if (progress) {
                showProgress(isDismissible)
                Log.d("saurabh", "show progress $javaClass")
            } else {
                hideProgress()
            }
        }
    }

    protected open fun observeErrorAndException(
        viewModel: BaseViewModel,
        errorAction: () -> Unit = { findNavController().navigateUp() },
        dismissAction: () -> Unit = { findNavController().navigateUp() }
    ) {
        viewModel.errorLiveData.observe(this) {
            showErrorDialog(null, it.message, errorAction = errorAction, dismissAction = dismissAction)
        }
        observeException(viewModel, errorAction, dismissAction)
    }

    protected open fun observeException(
        viewModel: BaseViewModel,
        errorAction: () -> Unit = { findNavController().navigateUp() },
        dismissAction: () -> Unit = { findNavController().navigateUp() }
    ) {
        viewModel.exceptionLiveData.observe(this) { exception ->
            Logger.debugLog("Exception caught: $exception")
            when (exception) {
                is IOException -> {
                    showNoInternetDialog(errorAction = errorAction, dismissAction = dismissAction)
                }
                is UnauthorizedException -> {
                    getParentActivity<BaseActivity>()?.blockUnauthorizedAccess()
                }
                else -> {
                    showErrorDialog(errorAction = errorAction, dismissAction = dismissAction)
                }
            }
        }
    }

    private fun showNoInternetDialog(
        errorAction: () -> Unit = { findNavController().navigateUp() },
        dismissAction: () -> Unit = { findNavController().navigateUp() }
    ) {
        showErrorDialog(getString(R.string.no_internet), getString(R.string.no_internet_message), errorAction = errorAction, dismissAction = dismissAction)
    }

    open fun showErrorDialog(
        title: String? = null,
        subtitle: String? = null,
        ctaText: String? = null,
        @DrawableRes icon: Int? = null,
        errorAction: () -> Unit = { findNavController().navigateUp() },
        dismissAction: () -> Unit = { findNavController().navigateUp() }
    ) {
        val activity = getParentActivity<BaseActivity>()
        activity?.showErrorDialog(title, subtitle, ctaText, icon, errorAction, dismissAction)
    }

    /* Kotlin requires explicit modifiers for overridable members and overrides. Add open if you need function/member to be overridable by default they are final.
        public, protected, internal and private are visibility modifiers, by default public
     */
    open fun showProgress(isDismissible: Boolean) {
        if (this::progressDialogue.isInitialized.not()) {
            progressDialogue = RelativeLayoutProgressDialog.onCreateDialogModel(requireActivity()).apply {
                setCancelable(isDismissible)
            }
        }
        progressDialogue.show()
    }

    open fun hideProgress() {
        if (this::progressDialogue.isInitialized && progressDialogue.isShowing) {
            progressDialogue.dismiss()
            Log.d("saurabh", "hide progress $javaClass")
        }
    }

    fun showKeyboard(editText: EditText) {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT)
    }

    fun hideKeyboard() {
        val view: View? = requireActivity().currentFocus
        view?.let {
            val imm =
                requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(it.windowToken, 0)
        }
    }

    /**
     * Determines whether the current fragment is alive on the window.
     * @return `true` if the current fragment is alive, `false` otherwise
     */
    fun isFragmentAlive(): Boolean {
        val isDeactivated = isRemoving || isDetached || context == null
        return !isDeactivated
    }

    abstract fun getScreenName(): String

    open fun shouldLogScreenView(): Boolean = true

    /**
     * Return App preference being set and used throughout the app.
     *
     * @return [AppPreference]
     */
    fun getAppPreference(): AppPreference = requireContext().getAppSharedPreferences()

    /**
     * Return User preference data(i.e user profile) being set and used throughout the app.
     *
     * @return [UserPreference]
     */
    fun getUserPreference(): UserPreference = requireContext().getUserSharedPreferences()
}
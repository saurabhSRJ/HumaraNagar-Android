package com.humara.nagar.base

import android.app.Dialog
import android.os.Bundle
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.humara.nagar.Logger
import com.humara.nagar.analytics.AnalyticsData
import com.humara.nagar.analytics.AnalyticsTracker
import com.humara.nagar.constants.IntentKeyConstants
import com.humara.nagar.shared_pref.AppPreference
import com.humara.nagar.shared_pref.UserPreference
import com.humara.nagar.ui.common.RelativeLayoutProgressDialog
import com.humara.nagar.utils.getAppSharedPreferences
import com.humara.nagar.utils.getUserSharedPreferences
import org.json.JSONException
import org.json.JSONObject

abstract class BaseBottomSheetDialogFragment : BottomSheetDialogFragment() {
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
                put(AnalyticsData.Parameters.IS_ADMIN, getUserPreference().isAdminUser)
            } catch (e: JSONException) {
                Logger.logException(getScreenName(), e, Logger.LogLevel.ERROR)
            }
        }
    }

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
        }
    }

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

    open fun shouldLogScreenView(): Boolean = true

    abstract fun getScreenName(): String
}
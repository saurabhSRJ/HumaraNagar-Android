package com.example.humaranagar.base

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.PersistableBundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import com.example.humaranagar.NagarApp
import com.example.humaranagar.R
import com.example.humaranagar.shared_pref.AppPreference
import com.example.humaranagar.shared_pref.UserPreference
import com.example.humaranagar.ui.common.RelativeLayoutProgressDialog
import com.example.humaranagar.utils.LocaleManager

/**
 * Base Activity for all the Activities present in the Project. Provides some common functionality for all the Activities.
 */
open class BaseActivity : AppCompatActivity() {
    private lateinit var progressDialogue: Dialog

    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)
        changeStatusBarColor(Color.TRANSPARENT)
        overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_to_left)
    }

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleManager.getWrapper(newBase))
    }

    override fun onBackPressed() {
        onBackPressedDispatcher.onBackPressed()
        overridePendingTransition(R.anim.slide_in_from_left, R.anim.slide_out_to_right)
    }

    override fun onPause() {
        hideKeyboard()
        hideProgress()
        super.onPause()
    }

    fun showProgress(isDismissible: Boolean) {
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
            progressDialogue.hide()
        }
    }

    open fun hideKeyboard() {
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
    fun changeStatusBarColor(color: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            WindowCompat.getInsetsController(window, window.decorView).apply {
                isAppearanceLightStatusBars = true
            }
            window.statusBarColor = ContextCompat.getColor(this, color)
        }
    }

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
    open fun getUserPreference(): UserPreference? {
        return (application as NagarApp).userSharedPreference
    }
}
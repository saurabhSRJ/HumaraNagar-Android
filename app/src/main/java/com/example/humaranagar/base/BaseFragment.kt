package com.example.humaranagar.base

import android.app.Dialog
import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.fragment.app.Fragment
import com.example.humaranagar.NagarApp
import com.example.humaranagar.shared_pref.AppPreference
import com.example.humaranagar.shared_pref.UserPreference
import com.example.humaranagar.ui.common.RelativeLayoutProgressDialog

/**
 * Base Fragment for all the Fragments present in the Project. Provides some common functionality for all the Fragemnts.
 */
open class BaseFragment : Fragment() {
    private lateinit var progressDialogue: Dialog

    override fun onPause() {
        hideProgress()
        super.onPause()
    }

    protected fun showProgress(isDismissible: Boolean) {
        if (this::progressDialogue.isInitialized.not()) {
            progressDialogue =
                RelativeLayoutProgressDialog.onCreateDialogModel(requireActivity()).apply {
                    setCancelable(isDismissible)
                }
        }
        if (progressDialogue.isShowing.not()) {
            progressDialogue.show()
        }
    }

    protected open fun showKeyboard(editText: EditText) {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(editText, InputMethodManager.SHOW_FORCED)
    }

    protected open fun hideProgress() {
        if (this::progressDialogue.isInitialized && progressDialogue.isShowing) {
            progressDialogue.hide()
        }
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
     * Return App preference being set and used throughout the app. An replacement of {com.zinka.fleet.dataBase.StoredObjectValue}
     *
     * @return [AppPreference]
     */
    fun getAppPreference(): AppPreference {
        return (requireContext().applicationContext as NagarApp).appSharedPreference
    }

    /**
     * Return User preference data(i.e user profile) being set and used throughout the app. An replacement of {com.zinka.fleet.dataBase.StoredObjectValue}
     *
     * @return [UserPreference]
     */
    fun getUserPreference(): UserPreference {
        return (requireContext().applicationContext as NagarApp).userSharedPreference
    }
}
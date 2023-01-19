package com.example.humaranagar.base

import android.app.Dialog
import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.humaranagar.NagarApp
import com.example.humaranagar.shared_pref.AppPreference
import com.example.humaranagar.shared_pref.UserPreference
import com.example.humaranagar.ui.common.RelativeLayoutProgressDialog

/**
 * Base Fragment for all the Fragments present in the Project. Provides some common functionality for all the Fragments.
 */
open class BaseFragment : Fragment() {
    private lateinit var progressDialogue: Dialog

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
            } else {
                hideProgress()
            }
        }
    }

    /* Kotlin requires explicit modifiers for overridable members and overrides. Add open if you need function/member to be overridable by default they are final.
        public, protected, internal and private are visibility modifiers, by default public
     */
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

    protected fun hideProgress() {
        if (this::progressDialogue.isInitialized && progressDialogue.isShowing) {
            progressDialogue.hide()
        }
    }

    fun showKeyboard(editText: EditText) {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(editText, InputMethodManager.SHOW_FORCED)
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
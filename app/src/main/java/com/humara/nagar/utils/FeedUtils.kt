package com.humara.nagar.utils

import android.content.Context
import androidx.fragment.app.FragmentManager
import com.humara.nagar.R
import com.humara.nagar.constants.NetworkConstants
import com.humara.nagar.ui.common.GenericAlertDialog

object FeedUtils {
    fun showDeletePostConfirmationDialog(fragmentManager: FragmentManager, context: Context, deleteListener: () -> Unit) {
        GenericAlertDialog.show(fragmentManager, context.getString(R.string.delete_post), context.getString(R.string.delete_post_warning_message), isCancelable = true,
            context.getString(R.string.delete), context.getString(R.string.cancel)) {
            deleteListener()
        }
    }

    fun getDocumentUrl(url: String): String {
        return NetworkConstants.NetworkAPIConstants.BASE_MEDIA_URL.plus(url)
    }
}
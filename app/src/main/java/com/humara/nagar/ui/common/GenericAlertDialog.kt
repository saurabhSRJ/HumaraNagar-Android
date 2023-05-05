package com.humara.nagar.ui.common

import android.app.AlertDialog
import android.content.Context

class GenericAlertDialog {
    companion object {
        fun show(context: Context, title: String, message: String, isCancelable: Boolean, positiveButtonText: String, positiveButtonListener: () -> Unit = {}) {
            val builder = AlertDialog.Builder(context)
            builder.setTitle(title)
                .setMessage(message)
                .setCancelable(isCancelable)
                .setPositiveButton(positiveButtonText) { _, _ ->
                    builder.create().dismiss()
                    positiveButtonListener.invoke()
                }
            builder.create().show()
        }
    }
}
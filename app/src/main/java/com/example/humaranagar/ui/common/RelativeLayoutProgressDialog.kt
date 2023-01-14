package com.example.humaranagar.ui.common

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.Window
import androidx.fragment.app.FragmentActivity
import com.example.humaranagar.databinding.ProgressDialogBinding

class RelativeLayoutProgressDialog {
    companion object {
        fun onCreateDialogModel(activity: FragmentActivity): Dialog {
            val binding: ProgressDialogBinding = ProgressDialogBinding.inflate(LayoutInflater.from(activity), null, false)
            binding.root.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            val dialog = Dialog(activity)
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.setContentView(binding.root)
            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.parseColor("#1A000000")))
            dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            return dialog
        }
    }
}
package com.humara.nagar.ui

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import com.humara.nagar.NagarApp
import com.humara.nagar.R
import com.humara.nagar.analytics.AnalyticsData
import com.humara.nagar.base.BaseActivity
import com.humara.nagar.constants.IntentKeyConstants

class ForceLogoutActivity : BaseActivity() {
    companion object {
        fun startForceLogoutActivity(context: Context, source: String) {
            context.startActivity(Intent(context, ForceLogoutActivity::class.java).apply {
                putExtra(IntentKeyConstants.SOURCE, source)
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            })
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_force_logout)
        createLogoutDialog()
    }

    private fun createLogoutDialog() {
        AlertDialog.Builder(this).apply {
            setMessage(getString(R.string.session_expired_message))
            setCancelable(false)
            setPositiveButton(
                resources.getString(R.string.logout)
            ) { dialog: DialogInterface, _: Int ->
                (application as NagarApp).logout(this@ForceLogoutActivity, getSource())
                dialog.cancel()
                finishAndRemoveTask()
            }
        }.create().show()
    }

    override fun getScreenName() = AnalyticsData.ScreenName.FORCE_LOGOUT_SCREEN

}
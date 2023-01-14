package com.example.humaranagar

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.example.humaranagar.base.BaseActivity
import com.example.humaranagar.ui.signup.otp_verification.OtpVerificationFragment
import com.example.humaranagar.ui.signup.signup_or_login.SignupOrLoginFragment

class OnBoardingActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activitiy_onboarding)
        showSignupOrLoginFragment(false)
    }

    private fun showSignupOrLoginFragment(shouldShowReverseEntryAnimation: Boolean) {
        showFragment(
            SignupOrLoginFragment(),
            shouldShowReverseEntryAnimation,
            shouldShowReverseEntryAnimation,
            SignupOrLoginFragment.TAG
        )
    }

    private fun showFragment(
        fragmentToShow: Fragment,
        shouldShowEntryAndExitAnimations: Boolean,
        shouldShowReverseEntryAnimation: Boolean,
        tag: String?
    ) {
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        if (shouldShowEntryAndExitAnimations) fragmentTransaction.setCustomAnimations(
            if (shouldShowReverseEntryAnimation) R.anim.slide_in_from_left else R.anim.slide_in_from_right,
            R.anim.slide_out_to_left
        )
        fragmentTransaction.replace(R.id.container, fragmentToShow, tag).commitAllowingStateLoss()
    }

    override fun onBackPressed() {
        if (supportFragmentManager.findFragmentByTag(OtpVerificationFragment.TAG) != null) {
            supportFragmentManager.popBackStack()
            showSignupOrLoginFragment(true)
        } else {
            finish()
        }
    }
}
package com.example.humaranagar.ui.signup

import android.os.Bundle
import androidx.activity.viewModels
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import com.example.humaranagar.ui.MainActivity
import com.example.humaranagar.R
import com.example.humaranagar.base.BaseActivity
import com.example.humaranagar.base.ViewModelFactory
import com.example.humaranagar.databinding.ActivitiyOnboardingBinding
import com.example.humaranagar.ui.signup.otp_verification.OtpVerificationFragment
import com.example.humaranagar.ui.signup.profile_creation.ProfileCreationFragment
import com.example.humaranagar.ui.signup.signup_or_login.SignupOrLoginFragment

class OnBoardingActivity : BaseActivity() {
    private val onBoardingViewModel by viewModels<OnBoardingViewModel> {
        ViewModelFactory()
    }
    private lateinit var binding: ActivitiyOnboardingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitiyOnboardingBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initViewModelObservers()
    }

    private fun initViewModelObservers() {
        onBoardingViewModel.run {
            isUserUnderAnExistingRegistrationProcessLiveData.observe(this@OnBoardingActivity) { isAlreadyUnderRegistrationProcess ->
                if (isAlreadyUnderRegistrationProcess) {
                    showProfileCreationFragment()
                } else {
                    showSignupOrLoginFragment(false)
                }
            }
            successfulUserCheckLiveData.observe(this@OnBoardingActivity) {
                showOtpFragment()
            }
            profileCreationRequiredLiveData.observe(this@OnBoardingActivity) {
                showProfileCreationFragment()
            }
            successfulUserLoginLiveData.observe(this@OnBoardingActivity) {
                showHomeScreen()
            }
            checkIfUserIsUnderOngoingRegistrationProcess()
        }
    }

    private fun showHomeScreen() {
        MainActivity.startActivity(this)
        finish()
    }

    private fun showSignupOrLoginFragment(shouldShowReverseEntryAnimation: Boolean) {
        showFragment(
            SignupOrLoginFragment(),
            shouldShowReverseEntryAnimation,
            shouldShowReverseEntryAnimation,
            SignupOrLoginFragment.TAG
        )
    }

    private fun showOtpFragment() {
        showFragment(OtpVerificationFragment(),
            shouldShowEntryAndExitAnimations = true,
            shouldShowReverseEntryAnimation = false,
            OtpVerificationFragment.TAG
        )
    }

    private fun showProfileCreationFragment() {
        showFragment(ProfileCreationFragment(),
            shouldShowEntryAndExitAnimations = true,
            shouldShowReverseEntryAnimation = false,
            tag = ProfileCreationFragment.TAG
        )
    }

    private fun showFragment(
        fragmentToShow: Fragment,
        shouldShowEntryAndExitAnimations: Boolean,
        shouldShowReverseEntryAnimation: Boolean,
        tag: String?
    ) = supportFragmentManager.commit(true) {
        if (shouldShowEntryAndExitAnimations) setCustomAnimations(
            if (shouldShowReverseEntryAnimation) R.anim.slide_in_from_left else R.anim.slide_in_from_right,
            R.anim.slide_out_to_left
        )
        replace(binding.container.id, fragmentToShow, tag)
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
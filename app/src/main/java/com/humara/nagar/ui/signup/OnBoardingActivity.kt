package com.humara.nagar.ui.signup

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import com.humara.nagar.R
import com.humara.nagar.analytics.AnalyticsData
import com.humara.nagar.base.BaseActivity
import com.humara.nagar.base.ViewModelFactory
import com.humara.nagar.constants.IntentKeyConstants
import com.humara.nagar.databinding.ActivitiyOnboardingBinding
import com.humara.nagar.fluid_resize.FluidContentResizer
import com.humara.nagar.ui.AppConfigViewModel
import com.humara.nagar.ui.MainActivity
import com.humara.nagar.ui.signup.otp_verification.OtpVerificationFragment
import com.humara.nagar.ui.signup.pending_approval.PendingApprovalFragment
import com.humara.nagar.ui.signup.profile_creation.ProfileCreationFragment
import com.humara.nagar.ui.signup.signup_or_login.SignupOrLoginFragment

class OnBoardingActivity : BaseActivity() {
    private val onBoardingViewModel by viewModels<OnBoardingViewModel> {
        ViewModelFactory()
    }
    private val appConfigViewModel by viewModels<AppConfigViewModel> {
        ViewModelFactory()
    }
    private lateinit var binding: ActivitiyOnboardingBinding

    companion object {
        fun startActivity(context: Context, source: String) {
            val intent = Intent(context, OnBoardingActivity::class.java).apply {
                putExtra(IntentKeyConstants.SOURCE, source)
            }
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitiyOnboardingBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initViewModelObservers()
        FluidContentResizer.listen(this)
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
            successfulUserCheckLiveData.observe(this@OnBoardingActivity) { isEligibleToLogin ->
                if (isEligibleToLogin) {
                    showOtpFragment()
                } else {
                    showPendingApprovalFragment()
                }
            }
            profileCreationRequiredLiveData.observe(this@OnBoardingActivity) { isNewUser ->
                if (isNewUser) {
                    showProfileCreationFragment()
                } else {
                    fetchAppConfig()
                }
            }
            successfulUserSignupLiveData.observe(this@OnBoardingActivity) {
                onUserOnBoard()
            }
            showHomeScreenLiveData.observe(this@OnBoardingActivity) {
                showHomeScreen()
            }
            checkIfUserIsUnderOngoingRegistrationProcess()
        }
        appConfigViewModel.run {
            observeProgress(this, false)
            observeErrorAndException(this)
            appConfigLiveData.observe(this@OnBoardingActivity) {
                onBoardingViewModel.onUserOnBoard()
            }
        }
    }

    private fun fetchAppConfig() {
        appConfigViewModel.getAppConfig()
    }

    private fun showHomeScreen() {
        MainActivity.startActivity(this, getScreenName())
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
        showFragment(
            OtpVerificationFragment(),
            shouldShowEntryAndExitAnimations = true,
            shouldShowReverseEntryAnimation = false,
            OtpVerificationFragment.TAG
        )
    }

    private fun showPendingApprovalFragment() {
        showFragment(
            PendingApprovalFragment(),
            shouldShowEntryAndExitAnimations = true,
            shouldShowReverseEntryAnimation = false,
            PendingApprovalFragment.TAG
        )
    }

    private fun showProfileCreationFragment() {
        showFragment(
            // We want to restore the ProfileCreationFragment instance so that user's input is not lost. If no instance is available then creating a new instance
            supportFragmentManager.findFragmentByTag(ProfileCreationFragment.TAG) ?: ProfileCreationFragment(),
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
    ) {
        supportFragmentManager.commit {
            if (shouldShowEntryAndExitAnimations) setCustomAnimations(
                if (shouldShowReverseEntryAnimation) R.anim.slide_in_from_left else R.anim.slide_in_from_right,
                if (shouldShowReverseEntryAnimation) R.anim.slide_out_to_right else R.anim.slide_out_to_left
            )
            replace(R.id.container, fragmentToShow, tag)
        }
    }

    override fun onBackPressed() {
        if (supportFragmentManager.findFragmentByTag(OtpVerificationFragment.TAG) != null) {
            supportFragmentManager.popBackStack()
            showSignupOrLoginFragment(true)
        } else {
            finish()
        }
    }

    override fun getScreenName() = AnalyticsData.ScreenName.ONBOARD_ACTIVITY
}
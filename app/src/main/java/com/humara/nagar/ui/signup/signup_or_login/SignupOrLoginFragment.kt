package com.humara.nagar.ui.signup.signup_or_login

import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.activityViewModels
import com.google.android.material.tabs.TabLayoutMediator
import com.humara.nagar.R
import com.humara.nagar.adapter.WelcomeBannerAdapter
import com.humara.nagar.analytics.AnalyticsData
import com.humara.nagar.base.BaseFragment
import com.humara.nagar.base.ViewModelFactory
import com.humara.nagar.constants.NetworkConstants
import com.humara.nagar.databinding.FragmentSignupOrLoginBinding
import com.humara.nagar.ui.common.ViewPagerSwitcher
import com.humara.nagar.ui.common.WebViewActivity
import com.humara.nagar.ui.signup.OnBoardingViewModel
import com.humara.nagar.ui.signup.signup_or_login.model.WelcomeBannerModel

class SignupOrLoginFragment : BaseFragment() {
    private lateinit var binding: FragmentSignupOrLoginBinding
    private var hasRootViewAlreadyScrolled = false
    private val onBoardingViewModel by activityViewModels<OnBoardingViewModel> {
        ViewModelFactory()
    }

    companion object {
        const val TAG = "SignupOrLoginFragment"
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentSignupOrLoginBinding.inflate(inflater, container, false)
        initView()
        initViewModelObservers()
        return binding.root
    }

    private fun initViewModelObservers() {
        onBoardingViewModel.run {
            observeProgress(this, false)
            observeErrorAndException(this, errorAction = {}, dismissAction = {})
            invalidMobileNumberLiveData.observe(viewLifecycleOwner) {
                binding.tvMobileNumberError.isVisible = it
            }
        }
    }

    private fun initView() {
        binding.run {
            val bannerList = listOf(
                WelcomeBannerModel(R.drawable.ic_welcome_banner_1, getString(R.string.we_notify)),
                WelcomeBannerModel(R.drawable.ic_welcome_banner_2, getString(R.string.we_connect)),
                WelcomeBannerModel(R.drawable.ic_welcome_banner_3, getString(R.string.we_solve)),
                WelcomeBannerModel(R.drawable.ic_welcome_banner_4, getString(R.string.we_listen))
            )
            val adapter = WelcomeBannerAdapter()
            adapter.setData(bannerList)
            vpWelcomeBanner.adapter = adapter
            TabLayoutMediator(tabIndicator, vpWelcomeBanner) { _, _ ->
            }.attach()
            val mPageSwitcher = ViewPagerSwitcher(binding.vpWelcomeBanner)
            mPageSwitcher.pageSwitcher(2)
            etMobileNumberInput.setOnFocusChangeListener { _, hasFocus ->
                if (hasFocus && hasRootViewAlreadyScrolled.not()) {
                    movePhoneNumberInputUpwardsWithAnimation()
                }
            }
            etMobileNumberInput.doAfterTextChanged {
                binding.run {
                    onBoardingViewModel.setInvalidMobileNumberLiveData(false)
                    btnContinue.isEnabled = it?.length == 10
                }
            }
            etMobileNumberInput.setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_GO && binding.btnContinue.isEnabled) {
                    hideKeyboardAndHandlePhoneNumberInput()
                }
                false
            }
            btnContinue.setOnClickListener {
                hideKeyboardAndHandlePhoneNumberInput()
            }
            tvTerms.setOnClickListener {
                openWebView(getString(R.string.terms_amp_conditions), NetworkConstants.NetworkAPIConstants.TERMS_CONDITION_URL)
            }
            tvPrivacyPolicy.setOnClickListener {
                openWebView(getString(R.string.privacy_policy), NetworkConstants.NetworkAPIConstants.PRIVACY_POLICY_URL)
            }
        }
    }

    private fun openWebView(title: String, url: String) {
        WebViewActivity.startActivity(requireActivity(), getScreenName(), url, title)
    }

    private fun hideKeyboardAndHandlePhoneNumberInput() {
        hideKeyboard()
        val mobileNumber = binding.etMobileNumberInput.text.toString()
        onBoardingViewModel.handleMobileNumberInput(mobileNumber)
    }

    private fun movePhoneNumberInputUpwardsWithAnimation() {
        ObjectAnimator.ofFloat(
            binding.root,
            "translationY",
            -binding.vpWelcomeBanner.height.toFloat() + 100
        ).apply {
            duration = 500
            start()
        }
        binding.run {
            vpWelcomeBanner.visibility = View.GONE
            tabIndicator.visibility = View.GONE
            ivLogo.visibility = View.VISIBLE
            tvAppName.visibility = View.VISIBLE
        }
        hasRootViewAlreadyScrolled = true
    }

    override fun getScreenName() = AnalyticsData.ScreenName.SIGNUP_OR_LOGIN_FRAGMENT
}
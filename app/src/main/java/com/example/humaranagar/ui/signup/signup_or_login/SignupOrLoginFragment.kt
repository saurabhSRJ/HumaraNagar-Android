package com.example.humaranagar.ui.signup.signup_or_login

import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.activityViewModels
import com.example.humaranagar.R
import com.example.humaranagar.base.BaseFragment
import com.example.humaranagar.base.ViewModelFactory
import com.example.humaranagar.databinding.FragmentSignupOrLoginBinding
import com.example.humaranagar.ui.signup.signup_or_login.model.WelcomeBannerModel
import com.example.humaranagar.ui.common.ViewPagerSwitcher
import com.example.humaranagar.ui.signup.OnBoardingViewModel
import com.google.android.material.tabs.TabLayoutMediator

class SignupOrLoginFragment : BaseFragment() {
    private lateinit var binding: FragmentSignupOrLoginBinding
    private var hasRootViewAlreadyScrolled = false
    private val onBoardingViewModel by activityViewModels<OnBoardingViewModel> {
        ViewModelFactory()
    }

    companion object {
        const val TAG = "SignupOrLoginFragment"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSignupOrLoginBinding.inflate(inflater, container, false)
        initView()
        initViewModelObservers()
        return binding.root
    }

    private fun initViewModelObservers() {
        onBoardingViewModel.run {
            observeProgress(this, false)
            observeErrorAndException(this)
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
        }
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
}
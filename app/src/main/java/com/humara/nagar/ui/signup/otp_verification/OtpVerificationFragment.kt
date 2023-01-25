package com.humara.nagar.ui.signup.otp_verification

import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.activityViewModels
import com.humara.nagar.R
import com.humara.nagar.analytics.AnalyticsData
import com.humara.nagar.base.BaseActivity
import com.humara.nagar.base.BaseFragment
import com.humara.nagar.base.ViewModelFactory
import com.humara.nagar.constants.Constants
import com.humara.nagar.databinding.FragmentOtpVerificationBinding
import com.humara.nagar.ui.signup.OnBoardingViewModel
import com.humara.nagar.utils.*

class OtpVerificationFragment : BaseFragment() {
    private val onBoardingViewModel by activityViewModels<OnBoardingViewModel> {
        ViewModelFactory()
    }
    private lateinit var binding: FragmentOtpVerificationBinding

    private var mobileNumber: String = ""
    private lateinit var countDownTimer: CountDownTimer

    companion object {
        const val TAG = "OtpVerificationFragment"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentOtpVerificationBinding.inflate(inflater, container, false)
        mobileNumber = getUserPreference().mobileNumber
        initViewModelObservers()
        initView()
        initializeTimer()
        return binding.root
    }

    private fun initViewModelObservers() {
        onBoardingViewModel.run {
            observeProgress(this, false)
            observeErrorAndException(this)
            invalidOtpLiveData.observe(viewLifecycleOwner) { message ->
                binding.tvOtpErrorMessage.setStringWithColor(
                    message ?: getString(R.string.incorrect_otp_message),
                    ContextCompat.getColor(requireContext(), R.color.red_error)
                )
            }
            successfulOtpResendLiveData.observe(viewLifecycleOwner) {
                requireContext().showToast(getString(R.string.otp_sent_to_s, mobileNumber))
                setResendOtpTimerView(false)
            }
        }
    }

    private fun initView() {
        binding.run {
            otpView.doAfterTextChanged { pin ->
                btnContinue.isEnabled = pin?.length == requireContext().resources.getInteger(R.integer.otp_length)
                resetOtpErrorMessageView()
            }
            otpView.setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_GO && btnContinue.isEnabled) {
                    hideKeyboardAndVerifyOtp()
                }
                false
            }
            ivBack.setOnClickListener {
                getParentActivity<BaseActivity>()?.onBackPressed()
            }
            btnContinue.setOnClickListener { hideKeyboardAndVerifyOtp() }
            tvOtpSentTo.setStringWithColors(
                getString(R.string.otp_sent_to).plus(" "),
                Utils.getMobileNumberWithCountryCode(mobileNumber),
                ContextCompat.getColor(requireContext(), R.color.grey_828282),
                ContextCompat.getColor(requireContext(), R.color.dark_grey_333333)
            )
            tvResend.setNonDuplicateClickListener {
                onBoardingViewModel.resendOtp()
                resetOtpErrorMessageView()
            }
            gainFocusAndShowKeyboard()
        }
    }

    private fun initializeTimer() {
        countDownTimer = object : CountDownTimer(Constants.OTP_RESEND_TIMER_IN_MS, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                binding.tvTimer.text = getString(
                    R.string.timer_count,
                    String.format("%02d", millisUntilFinished / 1000)
                )
            }

            override fun onFinish() {
                binding.run {
                    setResendOtpTimerView(true)
                }
            }
        }
        countDownTimer.start()
    }

    private fun resetOtpErrorMessageView() {
        binding.tvOtpErrorMessage.setStringWithColor(
            getString(R.string.didn_t_receive_otp),
            ContextCompat.getColor(requireContext(), R.color.grey_828282)
        )
    }

    private fun setResendOtpTimerView(resendEnabled: Boolean) {
        if (resendEnabled.not()) countDownTimer.start()
        binding.run {
            tvTimer.isVisible = resendEnabled.not()
            tvResend.isEnabled = resendEnabled
            tvResend.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    if (resendEnabled) R.color.blue_4285F4 else R.color.grey_AEAEAE
                )
            )
        }
    }

    private fun gainFocusAndShowKeyboard() {
        binding.otpView.requestFocus()
        showKeyboard(binding.otpView)
    }

    private fun hideKeyboardAndVerifyOtp() {
        hideKeyboard()
        onBoardingViewModel.verifyOtp(binding.otpView.text.toString())
    }

    override fun onDestroyView() {
        countDownTimer.cancel()
        super.onDestroyView()
    }

    override fun getScreenName() = AnalyticsData.ScreenName.OTP_VERIFICATION_FRAGMENT
}
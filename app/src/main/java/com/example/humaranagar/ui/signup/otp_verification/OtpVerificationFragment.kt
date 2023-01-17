package com.example.humaranagar.ui.signup.otp_verification

import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.activityViewModels
import com.example.humaranagar.R
import com.example.humaranagar.base.BaseActivity
import com.example.humaranagar.base.BaseFragment
import com.example.humaranagar.base.ViewModelFactory
import com.example.humaranagar.databinding.FragmentOtpVerificationBinding
import com.example.humaranagar.network.BaseRepository
import com.example.humaranagar.ui.signup.OnBoardingViewModel
import com.example.humaranagar.utils.*

class OtpVerificationFragment : BaseFragment() {
    private val onBoardingViewModel by activityViewModels<OnBoardingViewModel> {
        ViewModelFactory(BaseRepository())
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
        initView()
        initializeTimer()
        initViewModelObservers()
        return binding.root
    }

    private fun initViewModelObservers() {
        onBoardingViewModel.run {
            observeProgress(this, false)
            invalidOtpLiveData.observe(viewLifecycleOwner) { isInvalidOtp ->
                if (isInvalidOtp) {
                    binding.tvOtpErrorMessage.setStringWithColor(
                        getString(R.string.incorrect_otp_message),
                        ContextCompat.getColor(requireContext(), R.color.red_error)
                    )
                } else {
                    binding.tvOtpErrorMessage.setStringWithColor(
                        getString(R.string.didn_t_receive_otp),
                        ContextCompat.getColor(requireContext(), R.color.grey_828282)
                    )
                }
            }
            successfulOtpResendLiveData.observe(viewLifecycleOwner) {
                requireContext().showToast(getString(R.string.otp_sent_to_s, mobileNumber))
            }
        }
    }

    private fun initView() {
        binding.run {
            otpView.doAfterTextChanged { pin ->
                btnContinue.isEnabled =
                    pin?.length == requireContext().resources.getInteger(R.integer.otp_length)
                onBoardingViewModel.setInvalidOtpLiveData(false)
            }
            otpView.setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_GO && btnContinue.isEnabled) {
                    hideKeyboardAndVerifyOtp()
                }
                false
            }
            ivBack.setOnClickListener {
                (activity as? BaseActivity)?.onBackPressed()
            }
            btnContinue.setOnClickListener { hideKeyboardAndVerifyOtp() }
            tvOtpSentTo.setStringWithColors(
                getString(R.string.otp_sent_to),
                Utils.getMobileNumberWithCountryCode(mobileNumber),
                ContextCompat.getColor(requireContext(), R.color.grey_828282),
                ContextCompat.getColor(requireContext(), R.color.dark_grey_333333)
            )
            tvResend.setNonDuplicateClickListener {
                resendOtpAndRestartTimer()
            }
            gainFocusAndShowKeyboard()
        }
    }

    private fun initializeTimer() {
        countDownTimer = object : CountDownTimer(30 * 1000, 1000) {
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

    private fun resendOtpAndRestartTimer() {
        onBoardingViewModel.resendOtp()
        onBoardingViewModel.setInvalidOtpLiveData(false)
        binding.run {
            countDownTimer.start()
            setResendOtpTimerView(false)
        }
    }

    private fun setResendOtpTimerView(resendEnabled: Boolean) {
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
        onBoardingViewModel.progressLiveData.value = false
        super.onDestroyView()
    }
}
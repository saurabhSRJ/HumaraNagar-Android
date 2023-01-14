package com.example.humaranagar.ui.signup.otp_verification

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.core.content.ContextCompat
import androidx.core.widget.doAfterTextChanged
import com.example.humaranagar.MainActivity
import com.example.humaranagar.R
import com.example.humaranagar.base.BaseActivity
import com.example.humaranagar.base.BaseFragment
import com.example.humaranagar.databinding.FragmentOtpVerificationBinding
import com.example.humaranagar.utils.Utils
import com.example.humaranagar.utils.setNonDuplicateClickListener
import com.example.humaranagar.utils.showToast

class OtpVerificationFragment : BaseFragment() {
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
        mobileNumber = getUserPreference().getMobileNumber()
        initView()
        initializeTimer()
        return binding.root
    }

    private fun initView() {
        binding.run {
            otpView.doAfterTextChanged { pin ->
                btnContinue.isEnabled =
                    pin?.length == requireContext().resources.getInteger(R.integer.otp_length)
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
            tvOtpSentTo.text = Utils.getStringWithColors(
                getString(R.string.otp_sent_to),
                " +91 ".plus(mobileNumber),
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
                    tvTimer.visibility = View.GONE
                    tvResend.isEnabled = true
                    tvResend.setTextColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.blue_4285F4
                        )
                    )
                }
            }
        }
        countDownTimer.start()
    }

    private fun resendOtpAndRestartTimer() {
        binding.run {
            countDownTimer.start()
            tvTimer.visibility = View.VISIBLE
            tvResend.setTextColor(ContextCompat.getColor(requireContext(), R.color.grey_AEAEAE))
            tvResend.isEnabled = false
        }
        requireContext().showToast("Otp sent to $mobileNumber")
    }

    private fun gainFocusAndShowKeyboard() {
        binding.otpView.requestFocus()
        showKeyboard(binding.otpView)
    }

    private fun hideKeyboardAndVerifyOtp() {
        hideKeyboard()
        requireActivity().finish()
        startActivity(Intent(activity, MainActivity::class.java))
    }

    override fun onDestroy() {
        super.onDestroy()
        countDownTimer.cancel()
    }
}
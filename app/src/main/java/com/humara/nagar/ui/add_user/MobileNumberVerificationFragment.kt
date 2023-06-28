package com.humara.nagar.ui.add_user

import android.animation.AnimatorInflater
import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.core.animation.doOnEnd
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import com.humara.nagar.R
import com.humara.nagar.analytics.AnalyticsData
import com.humara.nagar.base.BaseFragment
import com.humara.nagar.base.ViewModelFactory
import com.humara.nagar.constants.Constants
import com.humara.nagar.constants.NetworkConstants
import com.humara.nagar.databinding.FragmentMobileNumberVerificationBinding
import com.humara.nagar.utils.StringUtils.setStringWithColor
import com.humara.nagar.utils.StringUtils.setStringWithColors
import com.humara.nagar.utils.Utils
import com.humara.nagar.utils.setNonDuplicateClickListener
import com.humara.nagar.utils.showToast

class MobileNumberVerificationFragment : BaseFragment() {
    private lateinit var binding: FragmentMobileNumberVerificationBinding
    private val addUserViewModel: AddUserViewModel by navGraphViewModels(R.id.add_user_navigation) {
        ViewModelFactory()
    }
    private val navController: NavController by lazy {
        findNavController()
    }
    private lateinit var countDownTimer: CountDownTimer

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentMobileNumberVerificationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViewModelObservers()
        initView()
    }

    private fun initViewModelObservers() {
        addUserViewModel.run {
            observeProgress(this, false)
            observeErrorAndException(this, errorAction = {}, dismissAction = {})
            invalidMobileNumberLiveData.observe(viewLifecycleOwner) {
                binding.mobileNumberInputLayout.tvMobileNumberError.run {
                    text = getString(R.string.please_enter_a_valid_10_digit_mobile_number)
                    isVisible = it
                }
            }
            duplicateUserLiveData.observe(viewLifecycleOwner) {
                binding.mobileNumberInputLayout.tvMobileNumberError.run {
                    visibility = View.VISIBLE
                    text = context.getString(R.string.mobile_number_already_belong_to_a_user)
                }
            }
            successfulUserCheckLiveData.observe(viewLifecycleOwner) {
                openVerifyOtpViewWithFlipAnimation()
            }
            profileCreationRequiredLiveData.observe(viewLifecycleOwner) {
                val action = MobileNumberVerificationFragmentDirections.actionMobileNumberVerificationFragmentToAddUserDetailsFragment()
                navController.navigate(action)
            }
            invalidOtpLiveData.observe(viewLifecycleOwner) {
                binding.otpVerificationLayout.tvOtpErrorMessage.setStringWithColor(
                    getString(R.string.incorrect_otp_message),
                    ContextCompat.getColor(requireContext(), R.color.red_error)
                )
            }
            otpExpiredLiveData.observe(viewLifecycleOwner) {
                binding.otpVerificationLayout.tvOtpErrorMessage.setStringWithColor(
                    getString(R.string.otp_expired_message),
                    ContextCompat.getColor(requireContext(), R.color.red_error)
                )
            }
            successfulOtpResendLiveData.observe(viewLifecycleOwner) {
                requireContext().showToast(getString(R.string.otp_sent_to_s, mobileNumberLiveData.value))
                setResendOtpTimerView(false)
            }
        }
    }

    private fun initView() {
        binding.btnBack.setOnClickListener { navController.navigateUp() }
        binding.clRoot.setOnClickListener { hideKeyboard() }
        binding.mobileNumberInputLayout.run {
            etMobileNumberInput.requestFocus()
            showKeyboard(etMobileNumberInput)
            etMobileNumberInput.doAfterTextChanged {
                addUserViewModel.setInvalidMobileNumberLiveData(false)
                btnContinue.isEnabled = it?.length == 10
            }
            etMobileNumberInput.setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_GO && btnContinue.isEnabled) {
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

    private fun openVerifyOtpViewWithFlipAnimation() {
        binding.otpVerificationLayout.root.visibility = View.VISIBLE
        val scale = requireContext().resources.displayMetrics.density
        val cameraDistance = 10000 * scale
        binding.mobileNumberInputLayout.root.cameraDistance = cameraDistance
        binding.otpVerificationLayout.root.cameraDistance = cameraDistance
        val flipOutAnimationSet = AnimatorInflater.loadAnimator(requireContext(), R.animator.card_flip_left_out)
        flipOutAnimationSet.setTarget(binding.mobileNumberInputLayout.root)
        val flipInAnimationSet = AnimatorInflater.loadAnimator(requireContext(), R.animator.card_flip_right_in)
        flipInAnimationSet.setTarget(binding.otpVerificationLayout.root)
        flipOutAnimationSet.start()
        flipInAnimationSet.start()
        flipInAnimationSet.doOnEnd { binding.mobileNumberInputLayout.root.visibility = View.GONE }
        initOtpVerificationLayout()
    }

    private fun initOtpVerificationLayout() {
        initializeTimer()
        binding.otpVerificationLayout.run {
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
            btnContinue.setOnClickListener { hideKeyboardAndVerifyOtp() }
            tvOtpSentTo.setStringWithColors(
                getString(R.string.otp_sent_to).plus(" "),
                Utils.getMobileNumberWithCountryCode(addUserViewModel.mobileNumberLiveData.value!!),
                ContextCompat.getColor(requireContext(), R.color.grey_828282),
                ContextCompat.getColor(requireContext(), R.color.dark_grey_333333)
            )
            tvResend.setNonDuplicateClickListener {
                addUserViewModel.resendOtp()
                resetOtpErrorMessageView()
            }
        }
    }

    private fun openWebView(title: String, url: String) {
        val action = MobileNumberVerificationFragmentDirections.actionMobileNumberVerificationFragmentToWebViewFragment2(url, getScreenName(), title)
        navController.navigate(action)
    }

    private fun hideKeyboardAndHandlePhoneNumberInput() {
        hideKeyboard()
        val mobileNumber = binding.mobileNumberInputLayout.etMobileNumberInput.text.toString()
        addUserViewModel.handleMobileNumberInput(mobileNumber)
    }

    private fun hideKeyboardAndVerifyOtp() {
        hideKeyboard()
        addUserViewModel.verifyOtpAndLogin(binding.otpVerificationLayout.otpView.text.toString())
    }

    private fun resetOtpErrorMessageView() {
        binding.otpVerificationLayout.tvOtpErrorMessage.setStringWithColor(
            getString(R.string.didn_t_receive_otp),
            ContextCompat.getColor(requireContext(), R.color.grey_828282)
        )
    }

    private fun initializeTimer() {
        countDownTimer = object : CountDownTimer(Constants.OTP_RESEND_TIMER_IN_MS, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                binding.otpVerificationLayout.tvTimer.text = getString(
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

    private fun setResendOtpTimerView(resendEnabled: Boolean) {
        if (resendEnabled.not()) countDownTimer.start()
        binding.otpVerificationLayout.run {
            tvTimer.isVisible = resendEnabled.not()
            tvResend.isEnabled = resendEnabled
            tvResend.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    if (resendEnabled) R.color.primary_color else R.color.grey_AEAEAE
                )
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (this::countDownTimer.isInitialized) countDownTimer.cancel()
    }

    override fun getScreenName() = AnalyticsData.ScreenName.ADD_USER_MOBILE_VERIFICATION_FRAGMENT
}
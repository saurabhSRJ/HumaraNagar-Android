package com.humara.nagar.ui.signup.profile_creation

import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.get
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.google.android.material.button.MaterialButton
import com.humara.nagar.R
import com.humara.nagar.analytics.AnalyticsData
import com.humara.nagar.base.BaseActivity
import com.humara.nagar.base.BaseFragment
import com.humara.nagar.base.ViewModelFactory
import com.humara.nagar.databinding.FragmentProfileCreationBinding
import com.humara.nagar.ui.AppConfigViewModel
import com.humara.nagar.ui.common.DatePickerDialogFragment
import com.humara.nagar.ui.signup.OnBoardingViewModel
import com.humara.nagar.ui.signup.model.GenderDetails
import com.humara.nagar.ui.signup.model.WardDetails
import com.humara.nagar.utils.Utils
import com.humara.nagar.utils.setNonDuplicateClickListener
import com.humara.nagar.utils.showToast

class ProfileCreationFragment : BaseFragment() {
    private lateinit var binding: FragmentProfileCreationBinding
    private val onBoardingViewModel by activityViewModels<OnBoardingViewModel> {
        ViewModelFactory()
    }
    private val profileCreationViewModel by activityViewModels<ProfileCreationViewModel> {
        ViewModelFactory()
    }
    private val appConfigViewModel by viewModels<AppConfigViewModel> {
        ViewModelFactory()
    }

    companion object {
        const val TAG = "ProfileCreationFragment"
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentProfileCreationBinding.inflate(inflater, container, false)
        initViewModelObservers()
        initView()
        return binding.root
    }

    private fun initViewModelObservers() {
        appConfigViewModel.run {
            observeProgress(this, false)
            observeErrorAndException(this, errorAction = { handleBack() }, dismissAction = { handleBack() })
            wardDetailsLiveData.observe(viewLifecycleOwner) { wardDetails ->
                binding.inputWard.setOptions(wardDetails.toTypedArray())
            }
            genderDetailsLiveData.observe(viewLifecycleOwner) {
                addGenderButtons(it)
            }
            userRefDataSuccessLiveData.observe(viewLifecycleOwner) {
                getGenders()
                getWards()
            }
            getUserReferenceData()
        }
        profileCreationViewModel.run {
            observeProgress(this, false)
            observeErrorAndException(this, errorAction = { handleBack() }, dismissAction = { handleBack() })
            getDateOfBirth().observe(viewLifecycleOwner) { dob ->
                binding.inputDob.setInput(dob)
                binding.inputWard.requestFocus()
            }
            invalidDateOfBirthLiveData.observe(viewLifecycleOwner) {
                requireContext().showToast(getString(R.string.dob_invalid_message))
            }
            getSubmitButtonState().observe(viewLifecycleOwner) { isEnabled ->
                binding.btnSubmit.isEnabled = isEnabled
            }
        }
        onBoardingViewModel.run {
            observeProgress(this, false)
            observeErrorAndException(this, errorAction = {}, dismissAction = {})
        }
    }

    private fun addGenderButtons(genders: List<GenderDetails>) {
        binding.run {
            genders.forEachIndexed { index, genderDetails ->
                val button = MaterialButton(requireContext(), null, R.attr.GenderButtons).apply {
                    text = genderDetails.name
                    id = index
                    tag = genderDetails
                }
                toggleGender.addView(button)
            }
            toggleGender.check(toggleGender[0].id)
        }
    }

    private fun initView() {
        binding.run {
            val capWordsType = InputType.TYPE_TEXT_FLAG_CAP_WORDS
            inputName.apply {
                setUserInputListener {
                    profileCreationViewModel.setUserName(it)
                }
                setInputType(capWordsType)
            }
            inputGuardianName.apply {
                setUserInputListener {
                    profileCreationViewModel.setParentName(it)
                }
                setInputType(capWordsType)
            }
            inputPhoneNumber.setInput(Utils.getMobileNumberWithCountryCode(getUserPreference().mobileNumber))
            inputDob.setLayoutListener(false) {
                DatePickerDialogFragment().show(childFragmentManager, DatePickerDialogFragment.TAG)
            }
            toggleGender.addOnButtonCheckedListener { _, checkedId, isChecked ->
                if (!isChecked) return@addOnButtonCheckedListener
                val tag = binding.toggleGender.findViewById<MaterialButton>(checkedId).tag as GenderDetails
                profileCreationViewModel.setGender(tag)
            }
            inputWard.setUserInputListener {
                if (it is WardDetails) {
                    profileCreationViewModel.setWard(it)
                }
            }
            btnSubmit.setNonDuplicateClickListener {
                hideKeyboard()
                onBoardingViewModel.updateSavedUserDetailsAndSignup(profileCreationViewModel.getProfileCreationObjectWithCollectedData())
            }
            clHeader.setOnClickListener { hideKeyboard() }
            clForm.setOnClickListener { hideKeyboard() }
        }
    }

    private fun handleBack() {
        getParentActivity<BaseActivity>()?.onBackPressed()
    }

    override fun getScreenName() = AnalyticsData.ScreenName.PROFILE_CREATION_FRAGMENT
}
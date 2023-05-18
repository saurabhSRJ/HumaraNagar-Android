package com.humara.nagar.ui.signup.profile_creation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.humara.nagar.R
import com.humara.nagar.analytics.AnalyticsData
import com.humara.nagar.base.BaseActivity
import com.humara.nagar.base.BaseFragment
import com.humara.nagar.base.ViewModelFactory
import com.humara.nagar.databinding.FragmentProfileCreationBinding
import com.humara.nagar.ui.AppConfigViewModel
import com.humara.nagar.ui.common.DatePickerDialogFragment
import com.humara.nagar.ui.signup.OnBoardingViewModel
import com.humara.nagar.ui.signup.model.Gender
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
            observeErrorAndException(this)
            userLocalitiesLiveData.observe(viewLifecycleOwner) {
                binding.inputLocality.setOptions(it.toTypedArray())
            }
            appConfigSuccessLiveData.observe(viewLifecycleOwner) {
                getUserLocalities()
            }
            getAppConfigAndUserReferenceData()
        }
        profileCreationViewModel.run {
            observeProgress(this, false)
            observeErrorAndException(this, errorAction = { getParentActivity<BaseActivity>()?.onBackPressed() }, dismissAction = { getParentActivity<BaseActivity>()?.onBackPressed() })
            getDateOfBirth().observe(viewLifecycleOwner) { dob ->
                binding.inputDob.setInput(dob)
                binding.inputLocality.requestFocus()
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

    private fun initView() {
        binding.run {
            inputName.setUserInputListener {
                profileCreationViewModel.setUserName(it)
            }
            inputGuardianName.setUserInputListener {
                profileCreationViewModel.setParentName(it)
            }
            inputPhoneNumber.setInput(Utils.getMobileNumberWithCountryCode(getUserPreference().mobileNumber))
            inputDob.setLayoutListener(false) {
                DatePickerDialogFragment().show(childFragmentManager, DatePickerDialogFragment.TAG)
            }
            toggleGender.addOnButtonCheckedListener { _, checkedId, _ ->
                profileCreationViewModel.setGender(getSelectedGender(checkedId))
            }
            inputLocality.setUserInputListener {
                profileCreationViewModel.setLocality(it)
            }
            btnSubmit.setNonDuplicateClickListener {
                hideKeyboard()
                onBoardingViewModel.updateSavedUserDetailsAndSignup(profileCreationViewModel.getProfileCreationObjectWithCollectedData())
            }
        }
    }

    private fun getSelectedGender(checkedId: Int): String {
        return when (checkedId) {
            binding.buttonMale.id -> Gender.MALE.name
            else -> Gender.FEMALE.name
        }
    }

    override fun getScreenName() = AnalyticsData.ScreenName.PROFILE_CREATION_FRAGMENT
}
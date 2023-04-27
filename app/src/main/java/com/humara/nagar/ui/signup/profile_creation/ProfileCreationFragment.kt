package com.humara.nagar.ui.signup.profile_creation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.fragment.app.activityViewModels
import com.humara.nagar.base.BaseFragment
import com.humara.nagar.base.ViewModelFactory
import com.humara.nagar.R
import com.humara.nagar.analytics.AnalyticsData
import com.humara.nagar.databinding.FragmentProfileCreationBinding
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

    companion object {
        const val TAG = "ProfileCreationFragment"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProfileCreationBinding.inflate(inflater, container, false)

        initViewModelObservers()
        initView()

        return binding.root
    }

    private fun initViewModelObservers() {
        profileCreationViewModel.run {
            dateOfBirthLiveData.observe(viewLifecycleOwner) { dob ->
                binding.inputDob.setInput(dob)
            }
            invalidDateOfBirthLiveData.observe(viewLifecycleOwner) {
                requireContext().showToast(getString(R.string.dob_invalid_message))
            }
            enableSubmitButtonLiveData.observe(viewLifecycleOwner) {
                binding.btnSubmit.isEnabled = it
            }
        }
        onBoardingViewModel.run {
            observeProgress(this, false)
            observeErrorAndException(this)
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
            inputLocality.setImeOptionType(EditorInfo.IME_ACTION_DONE)
            btnSubmit.setNonDuplicateClickListener {
                hideKeyboard()
                onBoardingViewModel.updateSavedUserDetailsAndSignup(profileCreationViewModel.getUserObjectWithCollectedData())
            }
        }
    }

    private fun getSelectedGender(checkedId: Int): String {
        return if (checkedId == binding.buttonMale.id) {
            Gender.MALE.name
        } else {
            Gender.FEMALE.name
        }
    }

    override fun getScreenName() = AnalyticsData.ScreenName.PROFILE_CREATION_FRAGMENT
}
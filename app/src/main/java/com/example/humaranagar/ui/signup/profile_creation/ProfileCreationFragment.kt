package com.example.humaranagar.ui.signup.profile_creation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.fragment.app.activityViewModels
import com.example.humaranagar.R
import com.example.humaranagar.base.BaseFragment
import com.example.humaranagar.base.ViewModelFactory
import com.example.humaranagar.databinding.FragmentProfileCreationBinding
import com.example.humaranagar.network.BaseRepository
import com.example.humaranagar.ui.common.DatePickerDialogFragment
import com.example.humaranagar.ui.signup.OnBoardingViewModel
import com.example.humaranagar.ui.signup.model.Gender
import com.example.humaranagar.utils.Utils
import com.example.humaranagar.utils.setNonDuplicateClickListener
import com.example.humaranagar.utils.showToast

class ProfileCreationFragment : BaseFragment() {
    private lateinit var binding: FragmentProfileCreationBinding
    private val onBoardingViewModel by activityViewModels<OnBoardingViewModel> {
        ViewModelFactory(BaseRepository())
    }
    private val profileCreationViewModel by activityViewModels<ProfileCreationViewModel> {
        ViewModelFactory(BaseRepository())
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
        initView()
        initViewModelObservers()
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
        observeProgress(onBoardingViewModel, false)
    }

    private fun initView() {
        binding.run {
            inputPhoneNumber.setInput(Utils.getMobileNumberWithCountryCode(getUserPreference().mobileNumber))
            inputDob.setCalendarListener {
                DatePickerDialogFragment().show(childFragmentManager, DatePickerDialogFragment.TAG)
            }
            inputName.setUserInputListener {
                profileCreationViewModel.setUserName(it)
            }
            inputGuardianName.setUserInputListener {
                profileCreationViewModel.setParentName(it)
            }
            inputWardNumber.setUserInputListener {
                profileCreationViewModel.setWardNumber(it)
            }
            inputWardNumber.setImeOptionType(EditorInfo.IME_ACTION_DONE)
            toggleGender.addOnButtonCheckedListener { _, checkedId, _ ->
                profileCreationViewModel.setGender(getSelectedGender(checkedId))
            }
            btnSubmit.setNonDuplicateClickListener {
                hideKeyboard()
                onBoardingViewModel.updateUserDetails(profileCreationViewModel.getUserObjectWithCollectedData())
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
}
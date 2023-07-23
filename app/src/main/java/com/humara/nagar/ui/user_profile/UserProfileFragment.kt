package com.humara.nagar.ui.user_profile

import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.humara.nagar.R
import com.humara.nagar.analytics.AnalyticsData
import com.humara.nagar.base.BaseFragment
import com.humara.nagar.base.ViewModelFactory
import com.humara.nagar.databinding.FragementUserProfileBinding
import com.humara.nagar.ui.common.DatePickerDialogFragment
import com.humara.nagar.ui.common.DateSelectionListener
import com.humara.nagar.ui.user_profile.model.UserProfile
import com.humara.nagar.utils.*
import com.humara.nagar.utils.StringUtils.setStringWithTypeface

class UserProfileFragment : BaseFragment() {
    private lateinit var binding: FragementUserProfileBinding
    private val userProfileViewModel by viewModels<UserProfileViewModel> {
        ViewModelFactory()
    }
    private val navController: NavController by lazy {
        findNavController()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragementUserProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViewModelObservers()
        initView()
    }

    private fun initViewModelObservers() {
        userProfileViewModel.run {
            observeProgress(this)
            observeErrorAndException(this, errorAction = {}, dismissAction = {})
            dobLiveData.observe(viewLifecycleOwner) {
                binding.inputDob.setInput(it)
            }
            invalidEmailLiveData.observe(viewLifecycleOwner) {
                binding.inputEmail.setError(getString(R.string.invalid_email_id))
            }
            invalidDateOfBirthLiveData.observe(viewLifecycleOwner) {
                context?.showToast(getString(R.string.dob_invalid_message))
            }
            userProfileLiveData.observe(viewLifecycleOwner) {
                inflateUserDetails(it)
            }
            updateProfileSuccessLiveData.observe(viewLifecycleOwner) {
                context?.showToast(getString(R.string.user_profile_updated))
                updateUserDetails(it)
            }
        }
    }

    private fun updateUserDetails(profile: UserProfile) {
        binding.run {
            inputGuardianName.setInput(profile.fatherSpouseName)
            val dob = DateTimeUtils.convertIsoDateTimeFormat(profile.dateOfBirth, "dd-MM-yyyy")
            userProfileViewModel.setDob(dob)
            profile.bio?.let {
                inputAboutMe.setInput(it)
            }
            profile.email?.let {
                inputEmail.setInput(it)
            }
        }
    }

    private fun inflateUserDetails(profile: UserProfile) {
        binding.run {
            svDetails.visibility = View.VISIBLE
            btnSave.visibility = View.VISIBLE
            ivUserPhoto.loadUrl(getUserPreference().profileImage, R.drawable.ic_user_image_placeholder)
            tvUserName.text = profile.name
            tvRoleAndWard.text = FeedUtils.getRoleAndWardText(requireContext(), profile.role, profile.ward)
            tvMobileNumber.text = profile.phoneNumber
            tvAddedBy.text = getString(R.string.added_by_s, profile.createdBy ?: getString(R.string.self))
            inputGuardianName.setInput(profile.fatherSpouseName)
            val dob = DateTimeUtils.convertIsoDateTimeFormat(profile.dateOfBirth, "dd-MM-yyyy")
            userProfileViewModel.setDob(dob)
            profile.bio?.let {
                inputAboutMe.setInput(it)
            }
            profile.email?.let {
                inputEmail.setInput(it)
            }
            profile.createdOn?.let {
                val addedByText = getString(R.string.joined_in_s, DateTimeUtils.convertIsoDateTimeFormat(it, "MMMM, yyyy"))
                tvJoinedDate.setStringWithTypeface(0, addedByText.indexOf(":"), addedByText, ResourcesCompat.getFont(requireContext(), R.font.open_sans_semibold))
            }
        }
    }

    private fun initView() {
        binding.run {
            toolbar.apply {
                toolbarTitle.text = getString(R.string.profile)
                leftIcon.setOnClickListener { navController.navigateUp() }
            }
            btnChangeImage.setNonDuplicateClickListener {
                navController.navigate(UserProfileFragmentDirections.actionUserProfileFragmentToUpdateProfileImageFragment(isEdit = true, getScreenName()))
            }
            inputAboutMe.apply {
                switchToMultiLined(4)
                setUserInputListener {
                    userProfileViewModel.setBio(it)
                }
            }
            inputDob.setLayoutListener(false) {
                openDatePickerDialog()
            }
            inputGuardianName.setUserInputListener {
                userProfileViewModel.setParentName(it)
            }
            inputEmail.apply {
                setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS)
                setImeOptionType(EditorInfo.IME_ACTION_DONE)
                setUserInputListener {
                    userProfileViewModel.setEmail(it)
                }
            }
            btnSave.setNonDuplicateClickListener {
                userProfileViewModel.updateProfile()
            }
            clContainer.setOnClickListener { hideKeyboard() }
        }
    }

    private fun openDatePickerDialog() {
        DatePickerDialogFragment.show(parentFragmentManager, object : DateSelectionListener {
            override fun onDateSelection(dob: String) {
                userProfileViewModel.setDob(dob)
            }
        })
    }

    override fun getScreenName() = AnalyticsData.ScreenName.USER_PROFILE_FRAGMENT
}
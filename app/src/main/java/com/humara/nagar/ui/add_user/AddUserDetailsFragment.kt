package com.humara.nagar.ui.add_user

import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.core.view.get
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import com.google.android.material.button.MaterialButton
import com.humara.nagar.R
import com.humara.nagar.Role
import com.humara.nagar.analytics.AnalyticsData
import com.humara.nagar.base.BaseFragment
import com.humara.nagar.base.ViewModelFactory
import com.humara.nagar.databinding.FragmentAddUserDetailsBinding
import com.humara.nagar.ui.AppConfigViewModel
import com.humara.nagar.ui.common.DatePickerDialogFragment
import com.humara.nagar.ui.common.DateSelectionListener
import com.humara.nagar.ui.common.GenericAlertDialog
import com.humara.nagar.ui.residents.ResidentsManagementViewModel
import com.humara.nagar.ui.signup.model.GenderDetails
import com.humara.nagar.ui.signup.model.RoleDetails
import com.humara.nagar.ui.signup.model.WardDetails
import com.humara.nagar.ui.signup.profile_creation.ProfileCreationViewModel
import com.humara.nagar.utils.Utils
import com.humara.nagar.utils.setNonDuplicateClickListener
import com.humara.nagar.utils.showToast

class AddUserDetailsFragment : BaseFragment() {
    private lateinit var binding: FragmentAddUserDetailsBinding
    private val addUserViewModel: AddUserViewModel by navGraphViewModels(R.id.add_user_navigation) {
        ViewModelFactory()
    }
    private val profileCreationViewModel: ProfileCreationViewModel by viewModels {
        ViewModelFactory()
    }
    private val appConfigViewModel by viewModels<AppConfigViewModel> {
        ViewModelFactory()
    }
    private val residentsManagementViewModel by navGraphViewModels<ResidentsManagementViewModel>(R.id.residents_navigation) {
        ViewModelFactory()
    }
    private val navController: NavController by lazy {
        findNavController()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireActivity().onBackPressedDispatcher.addCallback(this) {
            showExitConfirmationDialog()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentAddUserDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViewModelObservers()
        initView()
    }

    private fun initViewModelObservers() {
        appConfigViewModel.run {
            observeProgress(this, false)
            observeErrorAndException(this, errorAction = { handleBack() }, dismissAction = { handleBack() })
            roleDetailsLiveData.observe(viewLifecycleOwner) { roles ->
                var filteredRoles = roles
                if (getUserPreference().role?.id != Role.HumaraNagarTeam.roleId) {
                    filteredRoles = roles.filterNot { it.id == Role.HumaraNagarTeam.roleId }
                }
                binding.inputRole.setOptions(filteredRoles.toTypedArray())
            }
            wardDetailsLiveData.observe(viewLifecycleOwner) { wardDetails ->
                binding.inputWard.setOptions(wardDetails.toTypedArray())
            }
            genderDetailsLiveData.observe(viewLifecycleOwner) {
                addGenderButtons(it)
            }
            getRoles()
            getGenders()
            getWards()
        }
        profileCreationViewModel.run {
            getDateOfBirth().observe(viewLifecycleOwner) { dob ->
                binding.inputDob.setInput(dob)
                binding.inputWard.requestFocus()
            }
            invalidDateOfBirthLiveData.observe(viewLifecycleOwner) {
                requireContext().showToast(getString(R.string.dob_invalid_message))
            }
            getAddUserButtonState().observe(viewLifecycleOwner) { isEnabled ->
                binding.btnSubmit.isEnabled = isEnabled
            }
        }
        addUserViewModel.run {
            observeProgress(this, false)
            observeErrorAndException(this, errorAction = {}, dismissAction = {})
            successfulUserAdditionLiveData.observe(viewLifecycleOwner) {
                context?.showToast(getString(R.string.user_successfully_added))
                residentsManagementViewModel.setUserAdditionSuccess()
                handleBack()
            }
        }
    }

    private fun initView() {
        binding.run {
            btnBack.setOnClickListener { showExitConfirmationDialog() }
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
            inputPhoneNumber.setInput(Utils.getMobileNumberWithCountryCode(addUserViewModel.mobileNumberLiveData.value!!))
            inputDob.setLayoutListener(false) {
                openDatePickerDialog()
            }
            toggleGender.addOnButtonCheckedListener { _, checkedId, isChecked ->
                if (!isChecked) return@addOnButtonCheckedListener
                val tag = binding.toggleGender.findViewById<MaterialButton>(checkedId).tag as GenderDetails
                profileCreationViewModel.setGender(tag)
            }
            inputWard.setUserInputListener {
                if (it is WardDetails) {
                    profileCreationViewModel.setWard(it)
                    hideKeyboard()
                }
            }
            inputRole.setUserInputListener {
                if (it is RoleDetails) {
                    profileCreationViewModel.setRole(it)
                    hideKeyboard()
                }
            }
            btnSubmit.setNonDuplicateClickListener {
                hideKeyboard()
                addUserViewModel.createUser(profileCreationViewModel.getAddUserDetailsObjectWithCollectedData(addUserViewModel.mobileNumberLiveData.value!!))
            }
            clHeader.setOnClickListener { hideKeyboard() }
            clForm.setOnClickListener { hideKeyboard() }
        }
    }

    private fun addGenderButtons(genders: List<GenderDetails>) {
        binding.run {
            genders.forEachIndexed { _, genderDetails ->
                val button = MaterialButton(requireContext(), null, R.attr.GenderButtons).apply {
                    text = genderDetails.name
                    tag = genderDetails
                }
                toggleGender.addView(button)
            }
            toggleGender.check(toggleGender[0].id)
        }
    }

    private fun handleBack() {
        navController.navigateUp()
    }

    private fun openDatePickerDialog() {
        DatePickerDialogFragment.show(parentFragmentManager, object : DateSelectionListener {
            override fun onDateSelection(dob: String) {
                profileCreationViewModel.setDateOfBirth(dob)
            }
        })
    }

    private fun showExitConfirmationDialog() {
        GenericAlertDialog.show(parentFragmentManager, getString(R.string.sure_you_want_to_exit), getString(R.string.user_exit_confirmation_message), isCancelable = true,
            getString(R.string.exit), getString(R.string.stay)) {
            handleBack()
        }
    }

    override fun getScreenName() = AnalyticsData.ScreenName.ADD_USER_DETAILS_FRAGMENT
}
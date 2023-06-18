package com.humara.nagar.ui.common

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.humara.nagar.R
import com.humara.nagar.analytics.AnalyticsData
import com.humara.nagar.base.BaseFragment
import com.humara.nagar.base.ViewModelFactory
import com.humara.nagar.constants.IntentKeyConstants
import com.humara.nagar.databinding.FragmentAddProfilePhotoBinding
import com.humara.nagar.ui.MainActivity
import com.humara.nagar.ui.signup.OnBoardingViewModel
import com.humara.nagar.utils.loadUrl
import com.humara.nagar.utils.setNonDuplicateClickListener
import com.humara.nagar.utils.showToast

class UpdateProfileImageFragment : BaseFragment(), MediaSelectionListener {
    companion object {
        const val TAG = "AddProfilePhotoFragment"
        private const val IS_EDIT = "is_edit"
        fun getInstance(source: String, isEdit: Boolean = true): UpdateProfileImageFragment {
            return UpdateProfileImageFragment().apply {
                arguments = Bundle().apply {
                    putString(IntentKeyConstants.SOURCE, source)
                    putBoolean(IS_EDIT, isEdit)
                }
            }
        }
    }

    private lateinit var binding: FragmentAddProfilePhotoBinding
    private var isEditFlow: Boolean = false
    private val onBoardingViewModel: OnBoardingViewModel by viewModels {
        ViewModelFactory()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentAddProfilePhotoBinding.inflate(inflater, container, false)
        arguments?.run {
            isEditFlow = getBoolean(IS_EDIT, false)
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViewModelObservers()
        initView()
    }

    private fun initViewModelObservers() {
        onBoardingViewModel.run {
            observeProgress(this)
            observeErrorAndException(this, errorAction = {}, dismissAction = {})
            addProfileImageStatusLiveData.observe(viewLifecycleOwner) { success ->
                if (success) {
                    loadUserProfileImage()
                } else {
                    context?.showToast(getString(R.string.some_error_occoured))
                }
            }
        }
    }

    private fun initView() {
        binding.run {
            loadUserProfileImage()
            if (isEditFlow) {
                btnBack.visibility = View.VISIBLE
                btnContinue.visibility = View.GONE
                btnContinue.text = getString(R.string.done)
            } else {
                btnBack.visibility = View.GONE
                btnContinue.visibility = View.VISIBLE
            }
            btnBack.setOnClickListener { requireActivity().onBackPressed() }
            btnEdit.setNonDuplicateClickListener {
                MediaSelectionBottomSheet.show(parentFragmentManager, this@UpdateProfileImageFragment)
            }
            btnContinue.setOnClickListener {
                if (isEditFlow) {
                    findNavController().navigateUp()
                } else {
                    MainActivity.startActivity(requireContext(), getScreenName())
                    activity?.finish()
                }
            }
        }
    }

    private fun loadUserProfileImage() {
        getUserPreference().profileImage?.let { url ->
            binding.run {
                ivProfilePhoto.loadUrl(url, R.drawable.ic_user_image_placeholder)
                btnContinue.visibility = View.VISIBLE
            }
        }
    }

    override fun onMediaSelection(uris: List<Uri>) {
        if (uris.isNotEmpty()) {
            onBoardingViewModel.updateProfileImage(uris[0])
        }
    }

    override fun getScreenName() = AnalyticsData.ScreenName.ADD_PROFILE_PHOTO_FRAGMENT
}
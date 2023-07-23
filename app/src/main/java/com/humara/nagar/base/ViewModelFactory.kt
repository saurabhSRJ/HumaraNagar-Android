package com.humara.nagar.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import com.humara.nagar.ui.AppConfigViewModel
import com.humara.nagar.ui.add_user.AddUserViewModel
import com.humara.nagar.ui.common.MediaSelectionViewModel
import com.humara.nagar.ui.home.HomeViewModel
import com.humara.nagar.ui.home.create_post.CreatePollViewModel
import com.humara.nagar.ui.home.create_post.CreatePostViewModel
import com.humara.nagar.ui.home.post_details.PostDetailsViewModel
import com.humara.nagar.ui.report.ReportViewModel
import com.humara.nagar.ui.report.complaint_status.ComplaintStatusViewModel
import com.humara.nagar.ui.report.complaints.ComplaintManagementViewModel
import com.humara.nagar.ui.report.complaints.ComplaintsViewModel
import com.humara.nagar.ui.residents.ResidentsManagementViewModel
import com.humara.nagar.ui.residents.ResidentsViewModel
import com.humara.nagar.ui.signup.OnBoardingViewModel
import com.humara.nagar.ui.signup.profile_creation.ProfileCreationViewModel
import com.humara.nagar.ui.user_profile.UserProfileViewModel

class ViewModelFactory : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T =
        with(modelClass) {
            // Get the Application object from extras
            val application = checkNotNull(extras[APPLICATION_KEY])
            when {
                isAssignableFrom(OnBoardingViewModel::class.java) -> {
                    OnBoardingViewModel(application)
                }
                isAssignableFrom(ProfileCreationViewModel::class.java) -> {
                    ProfileCreationViewModel(application, extras.createSavedStateHandle())
                }
                isAssignableFrom(AppConfigViewModel::class.java) -> {
                    AppConfigViewModel(application)
                }
                isAssignableFrom(ReportViewModel::class.java) -> {
                    ReportViewModel(application, extras.createSavedStateHandle())
                }
                isAssignableFrom(ComplaintStatusViewModel::class.java) -> {
                    ComplaintStatusViewModel(application, extras.createSavedStateHandle())
                }
                isAssignableFrom(ComplaintsViewModel::class.java) -> {
                    ComplaintsViewModel(application)
                }
                isAssignableFrom(ComplaintManagementViewModel::class.java) -> {
                    ComplaintManagementViewModel(application)
                }
                isAssignableFrom(ResidentsViewModel::class.java) -> {
                    ResidentsViewModel(application)
                }
                isAssignableFrom(PostDetailsViewModel::class.java) -> {
                    PostDetailsViewModel(application, extras.createSavedStateHandle())
                }
                isAssignableFrom(HomeViewModel::class.java) -> {
                    HomeViewModel(application)
                }
                isAssignableFrom(CreatePostViewModel::class.java) -> {
                    CreatePostViewModel(application, extras.createSavedStateHandle())
                }
                isAssignableFrom(CreatePollViewModel::class.java) -> {
                    CreatePollViewModel(application, extras.createSavedStateHandle())
                }
                isAssignableFrom(MediaSelectionViewModel::class.java) -> {
                    MediaSelectionViewModel(application)
                }
                isAssignableFrom(AddUserViewModel::class.java) -> {
                    AddUserViewModel(application, extras.createSavedStateHandle())
                }
                isAssignableFrom(ResidentsManagementViewModel::class.java) -> {
                    ResidentsManagementViewModel(application)
                }
                isAssignableFrom(UserProfileViewModel::class.java) -> {
                    UserProfileViewModel(application, extras.createSavedStateHandle())
                }
                else -> throw IllegalArgumentException("Unknown ViewModel class")
            }
        } as T
}
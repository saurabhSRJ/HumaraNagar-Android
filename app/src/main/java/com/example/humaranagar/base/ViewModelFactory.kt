package com.example.humaranagar.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.humaranagar.network.BaseRepository
import com.example.humaranagar.ui.signup.OnBoardingViewModel
import com.example.humaranagar.ui.signup.profile_creation.ProfileCreationViewModel

class ViewModelFactory(
    private val repository: BaseRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T =
        with(modelClass) {
            // Get the Application object from extras
            val application = checkNotNull(extras[APPLICATION_KEY])
            when {
                isAssignableFrom(OnBoardingViewModel::class.java) -> {
                    OnBoardingViewModel(application, repository)
                }
                isAssignableFrom(ProfileCreationViewModel::class.java) -> {
                    ProfileCreationViewModel(application, repository)
                }
                else -> throw IllegalArgumentException("Unknown ViewModel class")
            }
        } as T
}
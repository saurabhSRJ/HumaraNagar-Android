package com.example.humaranagar.base

import android.app.Application
import androidx.lifecycle.*
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.humaranagar.NagarApp
import com.example.humaranagar.network.BaseRepository
import com.example.humaranagar.shared_pref.AppPreference
import com.example.humaranagar.shared_pref.UserPreference

open class BaseViewModel(application: Application, repository: BaseRepository) :
    AndroidViewModel(application) {
    /**
     * LiveData to show progress in activity/fragment
     * Remember to set this LiveData to 'false' in OnDestroyView() of fragment when using shared ViewModel
     */
    val progressLiveData: MutableLiveData<Boolean> by lazy { MutableLiveData() }

    /**
     * Return User preference data(i.e user profile) being set and used throughout the app.
     * @return [UserPreference]
     */
    protected fun getUserPreference(): UserPreference =
        (getApplication() as NagarApp).userSharedPreference

    /**
     * Return App preference being set and used throughout the app.
     * @return [AppPreference]
     */
    protected fun getAppPreference(): AppPreference =
        (getApplication() as NagarApp).appSharedPreference
}
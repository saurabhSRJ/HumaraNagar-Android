package com.humara.nagar.ui

import android.app.Application
import com.humara.nagar.database.AppDatabase
import com.humara.nagar.network.BaseRepository
import com.humara.nagar.ui.signup.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AppConfigRepository(context: Application) : BaseRepository(context) {
    private val apiService = getRetrofit().create(AppConfigService::class.java)
    private val database: AppDatabase by lazy { AppDatabase.getDataBase(context) }

    suspend fun getAppConfig(request: AppConfigRequest) = withContext(Dispatchers.IO) {
        apiService.getAppConfig(request)
    }

    suspend fun getUserReferenceDetails(request: UserReferenceDataRequest) = withContext(Dispatchers.IO) {
        apiService.getReferenceDetails(request)
    }

    suspend fun logout(request: LogoutRequest) = withContext(Dispatchers.IO) {
        apiService.logout(request)
    }

    suspend fun insertLocalities(localities: List<LocalityDetails>) = withContext(Dispatchers.IO) {
        database.referenceDataDao().insertLocalities(localities)
    }

    suspend fun insertCategories(categories: List<CategoryDetails>) = withContext(Dispatchers.IO) {
        database.referenceDataDao().insertCategories(categories)
    }

    suspend fun getUserLocalities(wardId: Int): List<LocalityDetails> = withContext(Dispatchers.IO) {
        database.referenceDataDao().getLocalitiesByWardId(wardId)
    }

    suspend fun getComplaintCategories(): List<CategoryDetails> = withContext(Dispatchers.IO) {
        database.referenceDataDao().getAllCategories()
    }
}
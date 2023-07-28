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

    suspend fun getAppConfig() = withContext(Dispatchers.IO) {
        apiService.getAppConfig()
    }

    suspend fun getUserReferenceDetails(request: UserReferenceDataRequest) = withContext(Dispatchers.IO) {
        apiService.getReferenceDetails(request)
    }

    suspend fun logout(request: LogoutRequest) = withContext(Dispatchers.IO) {
        apiService.logout(request)
    }

    suspend fun insertRoles(roles: List<RoleDetails>) = withContext(Dispatchers.IO) {
        database.referenceDataDao().insertRoles(roles)
    }

    suspend fun insertWards(wards: List<WardDetails>) = withContext(Dispatchers.IO) {
        database.referenceDataDao().insertWards(wards)
    }

    suspend fun insertGenders(genders: List<GenderDetails>) = withContext(Dispatchers.IO) {
        database.referenceDataDao().insertGenders(genders)
    }

    suspend fun insertCategories(categories: List<CategoryDetails>) = withContext(Dispatchers.IO) {
        database.referenceDataDao().insertCategories(categories)
    }

    suspend fun insertFeedFilters(filters: List<FeedFilter>) = withContext(Dispatchers.IO) {
        database.referenceDataDao().insertFeedFilters(filters)
    }

    suspend fun getAllRoles() = withContext(Dispatchers.IO) {
        database.referenceDataDao().getAllRoles()
    }

    suspend fun getAllWards() = withContext(Dispatchers.IO) {
        database.referenceDataDao().getAllWards()
    }

    suspend fun getWardId(ward: String) = withContext(Dispatchers.IO) {
        database.referenceDataDao().getWardId(ward)
    }

    suspend fun getAllGenders() = withContext(Dispatchers.IO) {
        database.referenceDataDao().getAllGenders()
    }

    suspend fun getComplaintCategories(): List<CategoryDetails> = withContext(Dispatchers.IO) {
        database.referenceDataDao().getAllCategories()
    }

    suspend fun clearDatabase() = withContext(Dispatchers.IO) {
        database.clearAllTables()
    }
}
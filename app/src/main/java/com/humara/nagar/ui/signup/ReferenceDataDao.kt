package com.humara.nagar.ui.signup

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.humara.nagar.ui.signup.model.*

@Dao
interface ReferenceDataDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoles(roles: List<RoleDetails>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWards(wards: List<WardDetails>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGenders(genders: List<GenderDetails>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategories(categories: List<CategoryDetails>)

    @Query("SELECT * FROM categories")
    suspend fun getAllCategories(): List<CategoryDetails>

    @Query("SELECT * FROM wards")
    suspend fun getAllWards(): List<WardDetails>

    @Query("SELECT * FROM genders")
    suspend fun getAllGenders(): List<GenderDetails>
}
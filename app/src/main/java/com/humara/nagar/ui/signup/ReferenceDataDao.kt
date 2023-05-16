package com.humara.nagar.ui.signup

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.humara.nagar.ui.signup.model.CategoryDetails
import com.humara.nagar.ui.signup.model.LocalityDetails

@Dao
interface ReferenceDataDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertLocalities(localities: List<LocalityDetails>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCategories(categories: List<CategoryDetails>)

    @Query("SELECT * FROM localities WHERE ward_id = :wardId")
    suspend fun getLocalitiesByWardId(wardId: Int): List<LocalityDetails>

    @Query("SELECT * FROM categories")
    suspend fun getAllCategories(): List<CategoryDetails>
}
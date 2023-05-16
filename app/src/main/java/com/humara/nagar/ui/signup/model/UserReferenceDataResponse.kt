package com.humara.nagar.ui.signup.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

data class UserReferenceDataResponse(
    @SerializedName("localities") val localities: ArrayList<LocalityDetails>,
    @SerializedName("categories") val categories: ArrayList<CategoryDetails>
)

@Entity(tableName = "localities")
data class LocalityDetails(
    @PrimaryKey
    @SerializedName("id")
    val id: Int,
    @ColumnInfo(name = "name")
    @SerializedName("name")
    val name: String,
    @ColumnInfo(name = "ward_id")
    @SerializedName("ward_id")
    val wardId: Int
)

@Entity(tableName = "categories")
data class CategoryDetails(
    @PrimaryKey
    @SerializedName("id")
    val id: Int,
    @ColumnInfo(name = "name")
    @SerializedName("name")
    val name: String
)

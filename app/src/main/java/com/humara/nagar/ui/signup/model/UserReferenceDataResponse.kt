package com.humara.nagar.ui.signup.model

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

data class UserReferenceDataResponse(
    @SerializedName("roles") val roles: List<RoleDetails>,
    @SerializedName("wards") val wards: List<WardDetails>,
    @SerializedName("genders") val genders: List<GenderDetails>,
    @SerializedName("categories") val categories: List<CategoryDetails>,
    @SerializedName("feed_filters") val feedFilters: List<FeedFilter>
)

@Entity(tableName = "feed_filters")
data class FeedFilter(
    @PrimaryKey
    @SerializedName("id")
    val id: Int,
    @ColumnInfo(name = "name")
    @SerializedName("name")
    val name: String
)

@Entity(tableName = "roles")
data class RoleDetails(
    @PrimaryKey
    @SerializedName("id")
    val id: Int,
    @ColumnInfo(name = "name")
    @SerializedName("name")
    val name: String
)

@Entity(tableName = "wards")
@Parcelize
data class WardDetails(
    @PrimaryKey
    @SerializedName("id")
    val id: Int,
    @ColumnInfo(name = "name")
    @SerializedName("name")
    val name: String
): Parcelable {
    override fun toString() = name
}

@Entity(tableName = "genders")
@Parcelize
data class GenderDetails(
    @PrimaryKey
    @SerializedName("id")
    val id: Int,
    @ColumnInfo(name = "name")
    @SerializedName("name")
    val name: String
): Parcelable {
    override fun toString() = name
}

@Entity(tableName = "categories")
@Parcelize
data class CategoryDetails(
    @PrimaryKey
    @SerializedName("id")
    val id: Int,
    @ColumnInfo(name = "name")
    @SerializedName("name")
    val name: String
): Parcelable

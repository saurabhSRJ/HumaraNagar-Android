package com.humara.nagar.ui.home.model

import android.content.Context
import com.google.gson.annotations.SerializedName
import com.humara.nagar.Role
import com.humara.nagar.utils.DateTimeUtils
import com.humara.nagar.utils.getUserSharedPreferences

data class Post(
    @SerializedName("post_id") val postId: Long,
    @SerializedName("type") val type: String,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("last_updated_at") val lastUpdatedAt: String? = null,
    @SerializedName("total_likes") var totalLikes: Int = 0,
    @SerializedName("is_liked_by_user") var isLikedByUser: Int = 0,
    @SerializedName("total_comments") val totalComments: Int = 0,
    @SerializedName("caption") val caption: String?,
    @SerializedName("user_id") val userId: Long,
    @SerializedName("name") val name: String,
    @SerializedName("image") val profileImage: String? = null,
    @SerializedName("role") val role: String,
    @SerializedName("ward") val ward: String,
    @SerializedName("info") val info: PostInfo?
) {
    fun hasUserLike(): Boolean = isLikedByUser != 0

    fun isEditableByUser(context: Context): Boolean {
        return (context.getUserSharedPreferences().userId == userId) || Role.isFromHumaraNagarTeam(context.getUserSharedPreferences().role?.id ?: 0)
    }
}

data class PostInfo(
    @SerializedName("medias") val mediaDetails: List<MediaDetail>? = null, // image, video or document url and thumbnails
    @SerializedName("question") val question: String?,
    @SerializedName("total_votes") val totalVotes: Int,
    @SerializedName("expiry_time") val expiryTime: String,
    @SerializedName("user_vote") val userVote: Int?,
    @SerializedName("options") val options: List<PollOption> = listOf()
) {
    fun isAllowedToVote(): Boolean = userVote == null && DateTimeUtils.isIsoTimeLessThanNow(expiryTime).not()

    fun isExpired(): Boolean = DateTimeUtils.isIsoTimeLessThanNow(expiryTime)

    fun getOptionsText(): List<String> = options.map { it.option }
}

data class MediaDetail(
    @SerializedName("media") val media: String,
    @SerializedName("thumbnail_path") val thumbnailUrl: String?
)

data class PollOption(
    @SerializedName("option_id") val optionId: Int,
    @SerializedName("option") val option: String,
    @SerializedName("votes") val votes: Int = 0
)

enum class PostType(val type: String) {
    POLL("poll"),
    IMAGE("image"),
    VIDEO("video"),
    TEXT("text"),
    DOCUMENT("document")
}

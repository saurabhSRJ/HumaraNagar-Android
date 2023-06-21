package com.humara.nagar.ui.home.model

import android.content.Context
import com.google.gson.annotations.SerializedName
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
    @SerializedName("locality") val locality: String? = null,
    @SerializedName("image") val profileImage: String? = null,
    @SerializedName("info") val info: PostInfo?
) {
    fun hasUserLike(): Boolean = isLikedByUser != 0

    fun isCreatedByUser(context: Context): Boolean {
        return context.getUserSharedPreferences().userId == userId
    }
}

data class PostInfo(
    @SerializedName("medias") val medias: List<String>? = null, // image, video or document urls
    @SerializedName("thumbnails") val thumbnails: List<String>? = null,
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

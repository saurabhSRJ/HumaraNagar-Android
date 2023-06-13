package com.humara.nagar.constants

interface NetworkConstants {
    interface NetworkHeaderConstants {
        companion object {
            const val ACCEPT_LANGUAGE = "Accept-Language"
            const val APP_VERSION = "App-Version"
            const val ANDROID_VERSION = "Android-Version"
            const val AUTHORIZATION = "Authorization"
        }
    }

    interface NetworkAPIConstants {
        companion object {
            const val OTP = "/otp"
            const val LOGIN = "/login"
            const val SIGNUP = "/signup"
            const val CONFIG = "/config"
            const val LOGOUT = "/logout"
            const val COMPLAINT = "/complaint"
            const val REF_DATA = "/ref-data"
            const val TOKEN = "/token"
            const val COMPLAINT_DETAILS = "/complaint/{id}"
            const val ACKNOWLEDGE_COMPLAINT = "/complaint/acknowledge/{id}"
            const val FINISH_COMPLAINT = "/complaint/finish/{id}"
            const val WITHDRAW_COMPLAINT = " /complaint/withdraw/{id}"
            const val RATE_COMPLAINT = "/complaint/rate/{id}"
            const val POSTS = "/posts"
            const val CRUD_POST = "/posts/{id}"
            const val CHANGE_POST_LIKE = "/posts/like/{id}"
            const val POLL_VOTE = "/posts/poll/vote/{id}"
            const val POST_COMMENTS = "/posts/comments/{id}"
            const val ADD_COMMENT = "/posts/comment/{id}"
            const val PRIVACY_POLICY_URL = "https://humaranagar.in"
            const val TERMS_CONDITION_URL = "https://humaranagar.in"
            const val ABOUT_US_URL = "https://humaranagar.in"
        }
    }

    interface NetworkQueryConstants {
        companion object {
            const val ID = "id"
            const val PAGE = "page"
            const val LIMIT = "limit"
        }
    }

    interface NetworkFormDataConstants {
        companion object {
            const val USER_ID = "user_id"
            const val LOCATION = "location"
            const val LOCALITY = "locality"
            const val CATEGORY = "category"
            const val COMMENTS = "comments"
            const val IMAGE = "image"
            const val LATITUDE = "location_latitude"
            const val LONGITUDE = "location_longitude"
        }
    }
}
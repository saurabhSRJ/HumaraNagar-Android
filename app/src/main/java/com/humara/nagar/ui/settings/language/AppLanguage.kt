package com.humara.nagar.ui.settings.language

import com.humara.nagar.constants.Constants

enum class AppLanguage(val lang: String) {
    ENGLISH("English"),
    HINDI("हिंदी");

    companion object {
        fun getLanguageCode(appLanguage: AppLanguage): String {
            return when(appLanguage) {
                ENGLISH -> Constants.ENGLISH
                HINDI -> Constants.HINDI
            }
        }
    }
}
package com.humara.nagar.utils

import android.os.Build

object DeviceHelper {
    inline val isMinSdk33: Boolean
        get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU

    inline val isMinSdk29: Boolean
        get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q

    inline val isMinSdk26: Boolean
        get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
}
package com.humara.nagar.ui.custom_views

import android.os.Parcelable
import android.util.SparseArray
import android.view.View
import kotlinx.parcelize.Parcelize

@Parcelize
data class FormState(
    val superSavedState: Parcelable?,
    val childrenStates: SparseArray<Parcelable>? = null
) : View.BaseSavedState(superSavedState), Parcelable
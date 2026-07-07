package com.hamric.core.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Category(
    val id: String,
    val name: String,
    val headlineCount: Int = 0
) : Parcelable
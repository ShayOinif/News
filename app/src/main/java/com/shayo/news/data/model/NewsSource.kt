package com.shayo.news.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class NewsSource(
    val name: String
) : Parcelable
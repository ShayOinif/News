package com.shayo.news.utils.imageloader

import android.widget.ImageView

interface ImageLoader {
    fun load(imageResource: String, target: ImageView)
}

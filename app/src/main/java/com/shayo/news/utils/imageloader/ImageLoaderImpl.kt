package com.shayo.news.utils.imageloader

import android.widget.ImageView
import com.squareup.picasso.Picasso
import javax.inject.Inject

class ImageLoaderImpl @Inject constructor(private val picasso: Picasso) : ImageLoader {

    override fun load(imageResource: String, target: ImageView) {
        picasso.load(imageResource).into(target)
    }
}

package com.shayo.news.db

import androidx.room.TypeConverter
import com.shayo.news.data.model.NewsSource

class Converters {
    @TypeConverter
    fun sourceFromString(value: String?): NewsSource? {
        return value?.let { NewsSource(it) }
    }

    @TypeConverter
    fun sourceToString(source: NewsSource?): String {
        return source?.name.toString()
    }
}

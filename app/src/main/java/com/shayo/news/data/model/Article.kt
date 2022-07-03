package com.shayo.news.data.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.shayo.news.data.model.domain.DomainArticle
import kotlinx.parcelize.Parcelize

@Entity(tableName = "news_article")
@Parcelize
data class Article(
    val author: String?,
    val content: String?,
    val description: String?,
    val publishedAt: String?,
    val source: NewsSource?,
    val title: String?,
    @PrimaryKey
    val url: String,
    val urlToImage: String?,
    var createdAt: Long
) : Parcelable {
    companion object {
        fun convertFromDomain(domainArticle: DomainArticle) =
            with(domainArticle) {
                Article(
                    author,
                    content,
                    description,
                    publishedAt,
                    source,
                    title,
                    url,
                    urlToImage,
                    createdAt
                )
            }
    }
}

package com.shayo.news.data.model.domain

import com.shayo.news.data.model.Article
import com.shayo.news.data.model.NewsSource

data class DomainArticle(
    val author: String?,
    val content: String?,
    val description: String?,
    val publishedAt: String?,
    val source: NewsSource?,
    val title: String?,
    val url: String,
    val urlToImage: String?,
    var createdAt: Long,
    val isFavorite: Boolean = false
) {
    companion object {
        fun createFromResponse(article: Article, isFavorite: Boolean = false) =
            DomainArticle(
                article.author,
                article.content,
                article.description,
                article.publishedAt,
                article.source,
                article.title,
                article.url,
                article.urlToImage,
                article.createdAt,
                isFavorite
            )
    }
}
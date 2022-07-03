package com.shayo.news.data.model.response

import com.shayo.news.data.model.Article

data class ArticleResponse(
    val articles: List<Article>,
    val status: String,
    val totalResults: Int
)
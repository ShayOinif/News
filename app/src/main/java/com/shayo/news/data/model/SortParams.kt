package com.shayo.news.data.model

data class SortParams(
    val relevancy: String = "relevancy",
    val popularity: String = "popularity",
    val publishedAt: String = "publishedAt",

    val byName: String = "byName",
    val bySource: String = "bySource",
)
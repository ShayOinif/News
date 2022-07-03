package com.shayo.news.network

import com.shayo.news.data.model.response.ArticleResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface NewsApiInterface {
    companion object {
        const val BASE_URL = "https://newsapi.org/"
        const val HEADER_AUTHORIZATION = "X-Api-Key"
    }

    @GET("v2/everything")
    suspend fun searchNewsArticles(
        @Query("q") query: String?,
        @Query("sortBy") sortParams: String?,
        @Query("pageSize") pageSize: Int,
        @Query("page") page: Int,
    ): ArticleResponse

    @GET("v2/top-headlines")
    suspend fun getTopHeadlines(
        @Query("country") country: String?,
        @Query("category") category: String?,
        @Query("pageSize") pageSize: Int,
        @Query("page") page: Int,
    ): ArticleResponse
}
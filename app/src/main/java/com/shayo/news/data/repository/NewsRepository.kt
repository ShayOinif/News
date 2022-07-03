package com.shayo.news.data.repository

import android.util.Log
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.shayo.news.data.model.Article
import com.shayo.news.db.ArticlesDao
import com.shayo.news.network.NewsApiInterface
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NewsRepository @Inject constructor(
    private val service: NewsApiInterface,
    private val articlesDao: ArticlesDao
) {
    fun getSearchResultStream(
        query: String?,
        sortBy: String?
    ): Flow<PagingData<Article>> {
        return Pager(
            config = PagingConfig(
                pageSize = 10,
                maxSize = 30,
                initialLoadSize = 10,
                enablePlaceholders = true
            ),
        ) {
            NewsPagingSource(
                service::searchNewsArticles,
                query ?: "latest",
                sortBy,
            )
        }.flow
    }

    fun getTopHeadlinedStream(
        country: String?,
        category: String?
    ): Flow<PagingData<Article>> {
        return Pager(
            config = PagingConfig(
                pageSize = 10,
                maxSize = 30,
                initialLoadSize = 10,
                enablePlaceholders = true
            ),
        ) {
            NewsPagingSource(
                service::getTopHeadlines,
                country,
                category,
            )
        }.flow
    }

    val getAllFavorites: Flow<List<Article>> = articlesDao.getAllArticles()

    suspend fun insertArticle(article: Article) {
        articlesDao.insertNews(article)
    }

    suspend fun deleteArticle(url: String) {
        articlesDao.deleteNewsArticle(url)
    }

    fun savedArticlesByName(queryString: String): Flow<PagingData<Article>> {

        Log.d("Shay", queryString)
        return Pager(
            config = PagingConfig(
                pageSize = 10,
                maxSize = 30,
                initialLoadSize = 10,
                enablePlaceholders = true
            )
        )
        {
            articlesDao.getSavedArticlesByName(queryString)
        }.flow
    }

    fun savedArticlesBySort(sortParams: String?): Flow<PagingData<Article>> {

        Log.d("Shay", "sort")
        val pagingSource = when (sortParams) {
            "relevancy" -> articlesDao::getSavedArticlesByDateLatest
            "bySource" -> articlesDao::getSavedArticlesSourceAsc
            "byName" -> articlesDao::getSavedArticlesNameAsc
            else -> articlesDao::getSavedArticlesNameAsc
        }

        return Pager(
            config = PagingConfig(
                pageSize = 10,
                maxSize = 30,
                initialLoadSize = 10,
                enablePlaceholders = true
            )
        ) {
            pagingSource()
        }.flow
    }
}
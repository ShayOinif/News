package com.shayo.news.db

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.shayo.news.data.model.Article
import kotlinx.coroutines.flow.Flow

@Dao
interface ArticlesDao {
    @Query("SELECT * FROM news_article ORDER BY createdAt DESC")
    fun getSavedArticlesByDateLatest(): PagingSource<Int, Article>

    @Query("SELECT * FROM news_article WHERE title LIKE '%' || :queryString || '%' OR description LIKE '%' || :queryString || '%'")
    fun getSavedArticlesByName(queryString: String): PagingSource<Int, Article>

    @Query("SELECT * FROM news_article ORDER BY title ASC")
    fun getSavedArticlesNameAsc(): PagingSource<Int, Article>


    @Query("SELECT * FROM news_article")
    fun getAllArticles(): Flow<List<Article>>

    @Query("SELECT * FROM news_article ORDER BY source ASC")
    fun getSavedArticlesSourceAsc(): PagingSource<Int, Article>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNews(article: Article)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addAllNews(articles: List<Article>)

    @Query("DELETE FROM news_article WHERE url LIKE :newsUrl")
    suspend fun deleteNewsArticle(newsUrl: String)

    @Query("DELETE FROM news_article")
    suspend fun deleteAllArticles()
}


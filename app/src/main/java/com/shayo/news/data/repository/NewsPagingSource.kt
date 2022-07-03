package com.shayo.news.data.repository

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.shayo.news.data.model.Article
import com.shayo.news.data.model.response.ArticleResponse
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

const val STARTING_PAGE_INDEX = 1
const val NETWORK_PAGE_SIZE = 10

class NewsPagingSource @Inject constructor(
    private val newsApiMethod: suspend (String?, String?, Int, Int) -> ArticleResponse,
    private val queryOrCountry: String?,
    private val sortOrCategory: String?,
) : PagingSource<Int, Article>() {

    override fun getRefreshKey(state: PagingState<Int, Article>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Article> {
        val position = params.key ?: STARTING_PAGE_INDEX

        return try {
            val articles = newsApiMethod(
                queryOrCountry,
                sortOrCategory,
                position,
                params.loadSize
            ).articles

            val nextKey = if (articles.isEmpty()) {
                null
            } else {
                position + (params.loadSize / NETWORK_PAGE_SIZE)
            }
            LoadResult.Page(
                data = articles,
                prevKey = if (position == STARTING_PAGE_INDEX) null else position - 1,
                nextKey = nextKey
            )
        } catch (exception: IOException) {
            return LoadResult.Error(exception)
        } catch (exception: HttpException) {
            return LoadResult.Error(exception)
        }
    }
}
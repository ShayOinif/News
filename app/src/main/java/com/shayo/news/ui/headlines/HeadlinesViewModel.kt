package com.shayo.news.ui.headlines

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import com.shayo.news.data.model.Article
import com.shayo.news.data.model.domain.DomainArticle
import com.shayo.news.data.repository.NewsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HeadlinesViewModel @Inject constructor(
    private val repository: NewsRepository
) : ViewModel() {
    private val categoryFlow = MutableStateFlow("Business")

    private lateinit var languageState: LanguageState

    private lateinit var _topHeadlinesFlow: Flow<PagingData<DomainArticle>>
    val topHeadlinesFlow
        get() = _topHeadlinesFlow

    fun setCategory(category: String) {
        categoryFlow.value = category
    }

    fun setFavorite(article: DomainArticle) {
        viewModelScope.launch {
            if (article.isFavorite)
                repository.deleteArticle(article.url)
            else {
                article.createdAt = System.currentTimeMillis()

                repository.insertArticle(
                    Article.convertFromDomain(article)
                )
            }
        }
    }

    fun attachLanguageState(languageState: LanguageState) {
        this.languageState = languageState
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun start() {
        _topHeadlinesFlow =
            combine(
                languageState.state,
                categoryFlow
            ) { country, category ->
                PreferenceFlows(
                    country,
                    category
                )
            }.flatMapLatest {
                repository.getTopHeadlinedStream(it.country, it.category).cachedIn(viewModelScope)
                    .combine(repository.getAllFavorites) { pagedData, favorites ->
                        pagedData.map { article ->
                            DomainArticle.createFromResponse(
                                article,
                                favorites.contains(article)
                            )
                        }
                    }
            }
    }

    data class PreferenceFlows(
        val country: String,
        val category: String
    )
}
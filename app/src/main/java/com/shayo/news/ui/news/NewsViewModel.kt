package com.shayo.news.ui.news

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import androidx.paging.map
import com.shayo.news.data.model.Article
import com.shayo.news.data.model.FragmentType
import com.shayo.news.data.model.domain.DomainArticle
import com.shayo.news.data.repository.NewsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class NewsViewModel @Inject constructor(
    private val repository: NewsRepository,
    private val state: SavedStateHandle
) : ViewModel() {

    private lateinit var whichFragmentType: FragmentType

    private var currentQuery =
        MutableStateFlow(
            state.getLiveData<String?>(
                "query",
                null
            ).value
        ) //todo: change to const

    private val sortParamsFlow =
        MutableStateFlow(state.getLiveData<String?>("sort", null).value)

    @OptIn(ExperimentalCoroutinesApi::class)
    val searchNewsFlow = combine(
        currentQuery,
        sortParamsFlow,
    ) { (query, sort) ->
        QueryWithSort(query, sort)
    }.flatMapLatest {
        if (whichFragmentType == FragmentType.NEWS) {
            repository.getSearchResultStream(it.query, it.sort).cachedIn(viewModelScope)
                .combine(repository.getAllFavorites) { pagedData, favorites ->
                    pagedData.map { article ->
                        DomainArticle.createFromResponse(
                            article,
                            favorites.contains(article)
                        )
                    }
                }
        } else {
            val flow = it.query?.let { query ->
                repository.savedArticlesByName(query)
            } ?: repository.savedArticlesBySort(it.sort)

            flow.cachedIn(viewModelScope).map { pagedData ->
                pagedData.map { article ->
                    DomainArticle.createFromResponse(
                        article,
                        true
                    )
                }
            }
        }
    }

    fun setFragmentState(fragmentType: FragmentType) {
        whichFragmentType = fragmentType
    }

    fun searchNews(query: String?) {
        val checkQuery = if (query.isNullOrBlank())
            null
        else
            query


        state["query"] = checkQuery
        currentQuery.value = checkQuery
    }

    fun newsSortedByUser(sort: String) {
        state["sort"] = sort
        sortParamsFlow.value = sort
    }

    fun setFavorite(article: DomainArticle) {
        viewModelScope.launch {
            if (article.isFavorite)
                repository.deleteArticle(article.url)
            else
                repository.insertArticle(
                    Article.convertFromDomain(article)
                )
        }
    }

    override fun onCleared() {
        state["query"] = null
        state["sort"] = null
        super.onCleared()
    }
}

data class QueryWithSort(
    val query: String?,
    val sort: String?
)
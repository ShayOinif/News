package com.shayo.news.ui.news

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.SearchView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.navArgs
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.OrientationHelper
import com.shayo.news.R
import com.shayo.news.data.model.FragmentType
import com.shayo.news.data.model.SortParams
import com.shayo.news.data.model.domain.DomainArticle
import com.shayo.news.databinding.FragmentNewsSearchListBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class NewsSearchFragment : Fragment(R.layout.fragment_news_search_list) {

    private var _binding: FragmentNewsSearchListBinding? = null
    private val binding get() = _binding!!

    private val viewModel: NewsViewModel by viewModels()

    private val emptySearchStateFlow = MutableStateFlow(false)

    @Inject
    lateinit var newsAdapter: NewsAdapter

    private lateinit var fragmentStateFlow: Flow<FragmentState>

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentNewsSearchListBinding.bind(view)

        viewModel.setFragmentState(navArgs<NewsSearchFragmentArgs>().value.whichFragment)

        setHasOptionsMenu(true)

        binding.retryButton.setOnClickListener {
            newsAdapter.refresh()
        }

        newsAdapter.setCallback(viewModel::setFavorite)

        fragmentStateFlow = combine(
            emptySearchStateFlow,
            newsAdapter.loadStateFlow
        ) { empty, loadState ->
            when (loadState.source.refresh) {
                is LoadState.Error -> FragmentState.ERROR
                is LoadState.Loading -> FragmentState.LOADING
                is LoadState.NotLoading -> {
                    if (empty)
                        FragmentState.EMPTY_SEARCH
                    else
                        FragmentState.OK
                }
            }
        }

        binding.apply {
            newsSearchList.apply {
                setHasFixedSize(true)
                adapter = newsAdapter.withLoadStateHeaderAndFooter(
                    header = NewsLoadStateAdapter { newsAdapter.retry() },
                    footer = NewsLoadStateAdapter { newsAdapter.retry() }
                )

                val divider = DividerItemDecoration(context, OrientationHelper.VERTICAL)

                divider.setDrawable(
                    context.getDrawable(R.drawable.divider)!!
                )

                addItemDecoration(divider)
            }

            swipeRefresh.setOnRefreshListener {
                newsAdapter.refresh()
                swipeRefresh.isRefreshing = false
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.searchNewsFlow.collectLatest { value: PagingData<DomainArticle> ->
                    newsAdapter.submitData(value)
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                fragmentStateFlow.collectLatest { fragmentState ->

                    binding.apply {
                        progressBar.isVisible = fragmentState == FragmentState.LOADING
                        newsSearchList.isVisible = fragmentState == FragmentState.OK
                        errorMessageTextView.isVisible = fragmentState == FragmentState.ERROR
                        retryButton.isVisible = fragmentState == FragmentState.ERROR
                        freshSearchTextView.isVisible = fragmentState == FragmentState.EMPTY_SEARCH
                    }
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)

        inflater.inflate(R.menu.menu_item, menu)

        if (navArgs<NewsSearchFragmentArgs>().value.whichFragment == FragmentType.NEWS) {
            menu.findItem(R.id.byName).isVisible = false
            menu.findItem(R.id.bySource).isVisible = false
        } else {
            menu.findItem(R.id.relevance).isVisible = false
            menu.findItem(R.id.popularity).isVisible = false
        }


        val searchItem = menu.findItem(R.id.action_search)

        val searchView = searchItem.actionView as SearchView

        searchView.setOnSearchClickListener {
            emptySearchStateFlow.value = true
        }

        searchView.setOnCloseListener {
            emptySearchStateFlow.value = false

            false
        }

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {

            override fun onQueryTextSubmit(query: String?) = false

            override fun onQueryTextChange(newText: String?): Boolean {

                binding.newsSearchList.scrollToPosition(0)

                viewModel.searchNews(newText)

                emptySearchStateFlow.value = newText.isNullOrBlank()

                return true
            }
        })
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val param = when (item.itemId) {
            R.id.relevance -> {
                SortParams().relevancy
            }

            R.id.popularity -> {
                SortParams().popularity
            }
            R.id.recent -> {
                SortParams().publishedAt
            }
            R.id.bySource -> {
                SortParams().bySource
            }
            R.id.byName -> {
                SortParams().byName
            }
            else -> return super.onOptionsItemSelected(item)
        }

        viewModel.newsSortedByUser(param)
        binding.newsSearchList.scrollToPosition(0)
        return true
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

private enum class FragmentState {
    ERROR,
    LOADING,
    OK,
    EMPTY_SEARCH
}
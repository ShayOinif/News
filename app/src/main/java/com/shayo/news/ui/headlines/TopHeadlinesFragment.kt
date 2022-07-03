package com.shayo.news.ui.headlines

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.OrientationHelper
import com.shayo.news.R
import com.shayo.news.data.model.domain.DomainArticle
import com.shayo.news.databinding.FragmentNewsSearchListBinding
import com.shayo.news.ui.news.NewsAdapter
import com.shayo.news.ui.news.NewsLoadStateAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class TopHeadlinesFragment : Fragment() {
    private var _binding: FragmentNewsSearchListBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HeadlinesViewModel by viewModels()

    @Inject
    lateinit var newsAdapter: NewsAdapter

    @Inject
    lateinit var languageState: LanguageState

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentNewsSearchListBinding.inflate(inflater, container, false)

        arguments?.takeIf { it.containsKey("tab") }?.apply {
            viewModel.setCategory(getString("tab") ?: headlinesCategory[0])
        }

        viewModel.attachLanguageState(languageState)

        viewModel.start()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setHasOptionsMenu(true)

        newsAdapter.setCallback(viewModel::setFavorite)

        binding.freshSearchTextView.isVisible = false

        binding.apply {
            retryButton.setOnClickListener { newsAdapter.refresh() }

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
                viewModel.topHeadlinesFlow.collectLatest { value: PagingData<DomainArticle> ->
                    newsAdapter.submitData(value)
                }
            }
        }

        newsAdapter.addLoadStateListener { combinedLoadStates ->
            binding.apply {
                combinedLoadStates.source.refresh.let { loadState ->
                    progressBar.isVisible = loadState is LoadState.Loading
                    newsSearchList.isVisible = loadState is LoadState.NotLoading
                    errorMessageTextView.isVisible = loadState is LoadState.Error
                    retryButton.isVisible = loadState is LoadState.Error
                }
            }
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
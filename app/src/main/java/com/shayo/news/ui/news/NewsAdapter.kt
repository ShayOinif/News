package com.shayo.news.ui.news

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.view.isVisible
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.shayo.news.R
import com.shayo.news.data.model.domain.DomainArticle
import com.shayo.news.databinding.SearchListItemBinding
import com.shayo.news.utils.getDateTimeDifference
import com.shayo.news.utils.imageloader.ImageLoader
import javax.inject.Inject

class NewsAdapter @Inject constructor(
    private val imageLoader: ImageLoader,
) :
    PagingDataAdapter<DomainArticle, NewsAdapter.NewsViewHolder>(DiffUtilCallback()) {

    private lateinit var favoriteCallback: (DomainArticle) -> Unit

    fun setCallback(callback: (DomainArticle) -> Unit) {
        favoriteCallback = callback
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewsViewHolder {
        val binding =
            SearchListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return NewsViewHolder(binding, imageLoader, favoriteCallback)
    }

    override fun onBindViewHolder(holder: NewsViewHolder, position: Int) {
        val currentItem = getItem(position)
        if (currentItem != null) {
            holder.bind(currentItem)
        }
    }

    class NewsViewHolder(
        private val binding: SearchListItemBinding,
        private val imageLoader: ImageLoader,
        private val favoriteCallback: (DomainArticle) -> Unit,
        private var item: DomainArticle? = null,

        ) :
        RecyclerView.ViewHolder(binding.root) {
        init {
            binding.apply {
                title.setOnClickListener {
                    item?.let {
                        openNewsArticleInChrome(it.url, itemView.context)
                    }
                }
                newsImage.setOnClickListener {
                    item?.let {
                        openNewsArticleInChrome(it.url, itemView.context)
                    }
                }
            }
        }

        fun bind(article: DomainArticle) {
            this.item = article
            binding.apply {

                val date = getDateTimeDifference(article.publishedAt.toString())

                title.text = article.title
                sourceName.text = article.source?.name

                timePublished.isVisible = if (date.days.toInt() == 0) {
                    timePublished.text = "${date.hours} hours ago"

                    true
                } else if (date.hours.toInt() == 0) {
                    timePublished.text = "${date.minutes} minutes ago"

                    true
                } else if (date.minutes.toInt() == 0) {
                    timePublished.text = "${date.seconds} seconds ago"

                    true
                } else {
                    false
                }

                val imgUrl = article.urlToImage
                imgUrl?.let {
                    imageLoader.load(imgUrl, newsImage)
                }


                val image = if (article.isFavorite) {
                    R.drawable.ic_baseline_bookmark_24
                } else {
                    R.drawable.ic_baseline_bookmark_border_24
                }

                binding.saveArticleImg.setImageResource(image)

                saveArticleImg.setOnClickListener {
                    favoriteCallback(article)
                }
            }
        }

        private fun openNewsArticleInChrome(url: String, context: Context) {
            val builder: CustomTabsIntent.Builder = CustomTabsIntent.Builder()
            val customTabsIntent = builder.build()
            customTabsIntent.launchUrl(context, Uri.parse(url))
        }
    }
}


private class DiffUtilCallback : DiffUtil.ItemCallback<DomainArticle>() {
    override fun areItemsTheSame(oldItem: DomainArticle, newItem: DomainArticle): Boolean {
        return oldItem.url == newItem.url
    }

    override fun areContentsTheSame(oldItem: DomainArticle, newItem: DomainArticle): Boolean {
        return oldItem == newItem
    }
}
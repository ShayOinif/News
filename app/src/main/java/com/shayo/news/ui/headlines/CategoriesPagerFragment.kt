package com.shayo.news.ui.headlines


import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.fragment.app.Fragment
import com.google.android.material.tabs.TabLayoutMediator
import com.shayo.news.R
import com.shayo.news.databinding.FragmentCategoriesPagerBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class CategoriesPagerFragment : Fragment(R.layout.fragment_categories_pager) {

    @Inject
    lateinit var collectionAdapter: HeadlineCategoryAdapter

    @Inject
    lateinit var languageState: LanguageState

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentCategoriesPagerBinding.bind(view)

        setHasOptionsMenu(true)

        collectionAdapter = HeadlineCategoryAdapter(this)

        binding.pager.adapter = collectionAdapter

        TabLayoutMediator(binding.tabLayout, binding.pager) { tab, position ->
            tab.text = headlinesCategory[position]
        }.attach()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)

        inflater.inflate(R.menu.menu_country, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {


        val language = when (item.itemId) {
            R.id.us -> "us"
            R.id.fr -> "fr"
            R.id.il -> "il"
            R.id.gb -> "gb"
            R.id.it -> "it"
            else -> return super.onOptionsItemSelected(item)
        }

        languageState.changeState(language)
        return true
    }
}

val headlinesCategory = listOf(
    "Business",
    "Entertainment",
    "General",
    "Health",
    "Science",
    "Sports",
    "Technology",
)
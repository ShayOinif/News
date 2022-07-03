package com.shayo.news.ui.headlines

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import javax.inject.Inject

class HeadlineCategoryAdapter @Inject constructor(
    fragment: Fragment
) : FragmentStateAdapter(fragment) {
    override fun getItemCount() = headlinesCategory.size

    override fun createFragment(position: Int): Fragment {
        val fragment: Fragment = TopHeadlinesFragment()

        fragment.arguments = Bundle().apply {
            putString("tab", headlinesCategory[position])
        }

        return fragment
    }
}
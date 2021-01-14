package app.openair.ui.feature.main

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import app.openair.R
import app.openair.ui.feature.showExercisesList.ShowExercisesFragment
import app.openair.ui.feature.showStats.ShowStatsFragment
import kotlinx.coroutines.ExperimentalCoroutinesApi

private val TAB_TITLES = arrayOf(
    R.string.main_tab_exercises,
    R.string.main_tab_stats
)

/**
 * A [FragmentStateAdapter] that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
class SectionsStateAdapter(fragment: MainActivity) : FragmentStateAdapter(fragment) {

    @ExperimentalCoroutinesApi
    override fun createFragment(position: Int): Fragment {
        return when(position){
            0 -> ShowExercisesFragment.newInstance()
            else -> ShowStatsFragment.newInstance()
        }
    }

    fun getPageTitleResource(position: Int): Int {
        return TAB_TITLES[position]
    }

    override fun getItemCount(): Int {
        return TAB_TITLES.size
    }
}
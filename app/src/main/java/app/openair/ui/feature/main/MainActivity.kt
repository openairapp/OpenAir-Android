package app.openair.ui.feature.main

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import app.openair.R
import app.openair.themeColor
import app.openair.ui.feature.recordExercise.RecordService
import app.openair.ui.feature.recordExercise.SetupExerciseActivity
import app.openair.ui.feature.recordExercise.TrackExerciseActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setSupportActionBar(findViewById(R.id.my_toolbar))

        val sectionsPagerAdapter = SectionsStateAdapter(this)
        val viewPager: ViewPager2 = findViewById(R.id.view_pager)
        viewPager.adapter = sectionsPagerAdapter

        val tabs: TabLayout = findViewById(R.id.tabs)
        TabLayoutMediator(tabs, viewPager) { tab, position ->
            tab.text = this.resources.getString(sectionsPagerAdapter.getPageTitleResource(position))
        }.attach()
        tabs.setTabTextColors(themeColor(R.attr.colorOnPrimary), themeColor(R.attr.colorOnPrimary))
        tabs.setSelectedTabIndicatorColor(themeColor(R.attr.colorPrimaryVariantAlternate))


        val fab: FloatingActionButton = findViewById(R.id.fab)
        fab.setOnClickListener {
            // change our FAB based on if there's an exercise in progress or not
            val intent = if (RecordService.running) {
                Intent(this, TrackExerciseActivity::class.java)
            } else {
                Intent(this, SetupExerciseActivity::class.java)
            }
            startActivity(intent)
        }
    }

    fun onViewCurrentExercise(v: View) {
        val intent = Intent(this, TrackExerciseActivity::class.java)
        startActivity(intent)
    }
}
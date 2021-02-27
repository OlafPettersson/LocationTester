package org.pettersson.locationtester.ui.main

import android.content.Context
import android.content.res.Resources
import android.graphics.drawable.Drawable
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import org.pettersson.locationtester.R

private val TAB_TITLES = arrayOf(
    R.string.tab_text_1,
    R.string.tab_text_2,
    R.string.tab_text_3,
    R.string.tab_text_4

)

private val TAB_ICONS = arrayOf(
    R.drawable.ic_add_location,
    R.drawable.ic_map_black_18dp,
    R.drawable.ic_baseline_settings_24,
    R.drawable.ic_compass_calibration_black
)

/**
 * A [FragmentPagerAdapter] that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
class LocationTesterPagerAdapter(private val context: Context,
                                 fm: FragmentManager)
    : FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    override fun getItem(position: Int): Fragment {

        if(position == 0) {
            return MeasurementsFragment()
        }

        if(position == 1) {
            return MapFragment()
        }

        if(position == 2) {
            return SettingsFragment()
        }

        if(position == 3) {
            return DetailsFragment()
        }

        throw NotImplementedError("am I missing something?")
    }


    fun allowSwipeForRefresh(position: Int): Boolean {
        return position != 1 // && position != 3
    }
    override fun getPageTitle(position: Int): CharSequence? {
        return context.resources.getString(TAB_TITLES[position])
    }

    fun getIcon(position: Int, theme: Resources.Theme): Drawable? {
        return context.resources.getDrawable(TAB_ICONS[position], theme)
    }

    override fun getCount(): Int {
        return 4
    }

}
package org.pettersson.locationtester.ui.main

import android.os.Bundle
import android.view.View
import android.view.View.OnLongClickListener
import android.widget.AdapterView
import android.widget.AdapterView.OnItemLongClickListener
import android.widget.ListAdapter
import android.widget.ListView
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.preference.*
import org.pettersson.locationtester.viewModels.LocationProviderViewModel
import org.pettersson.locationtester.viewModels.LocationProvidersViewModel
import java.lang.reflect.Type


/**
 * A placeholder fragment containing a simple view.
 */
class SettingsFragment : PreferenceFragmentCompat() {

    private val viewModel : LocationProvidersViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.providers.observe(viewLifecycleOwner,
            Observer { updatePreferenceScreen(preferenceScreen, it) })

    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        this.preferenceScreen = preferenceManager.createPreferenceScreen(requireContext())
    }

    private fun updatePreferenceScreen(preference: PreferenceScreen, providers: List<LocationProviderViewModel>){
        preference.removeAll()

        var preferenceCategory : PreferenceCategory? = null
        var prevType : Type? = null

        providers.forEach { lp ->
            if(prevType != lp.javaClass){
                prevType = lp.javaClass
                val name = (lp.javaClass.typeName.split('.')
                                                 .last()
                                                 .replace("LocationProvider", "")
                                                 .replace("ViewModel", ""))
                preferenceCategory = PreferenceCategory(requireContext())
                preferenceCategory!!.title = name
                preference.addPreference(preferenceCategory)
            }

            val checkBoxPreference = CheckBoxPreference(requireContext())
            checkBoxPreference.title = lp.name
            checkBoxPreference.key = lp.id
            checkBoxPreference.isChecked = lp.isEnabled
            preferenceCategory!!.addPreference(checkBoxPreference)

            checkBoxPreference.onPreferenceChangeListener =
                Preference.OnPreferenceChangeListener { _ , newValue ->
                    lp.isEnabled = newValue as Boolean
                    viewModel.updateEnabledProviders()
                    true
            }
        }
    }
}
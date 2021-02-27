package org.pettersson.locationtester.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.delay
import org.pettersson.locationtester.R
import org.pettersson.locationtester.databinding.FragmentMeasurementsBinding
import org.pettersson.locationtester.viewModels.LocationProvidersViewModel
import java.util.*


class MeasurementsFragment : Fragment() {

    private val viewModel : LocationProvidersViewModel by activityViewModels()

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {

        val root = FragmentMeasurementsBinding.inflate(inflater, container, false)
            .also {
                it.viewModel = viewModel
                it.lifecycleOwner = viewLifecycleOwner
            }
            .root

        val recyclerView : RecyclerView = root.findViewById(R.id.measurements_list)
        recyclerView.addItemDecoration(DividerItemDecoration(recyclerView.context, LinearLayoutManager.VERTICAL))

        viewLifecycleOwner.lifecycleScope.launchWhenResumed {
            while(true) {
                delay(1000)
                viewModel.update()
            }
        }

        return root
    }
}
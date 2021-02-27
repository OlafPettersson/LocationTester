package org.pettersson.locationtester

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Toolbar
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.HasDefaultViewModelProviderFactory
import androidx.lifecycle.ViewModelProvider
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.viewpager.widget.ViewPager
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.tabs.TabLayout
import org.pettersson.locationtester.databinding.FragmentMeasurementRowBindingImpl
import org.pettersson.locationtester.ui.main.LocationTesterPagerAdapter
import org.pettersson.locationtester.viewModels.LocationProvidersViewModel


class MainActivity : AppCompatActivity(),
        HasDefaultViewModelProviderFactory // https://stackoverflow.com/questions/61370134/androidviewmodel-has-no-zero-argument-constructor
{
    val UPDATE_STATES_REQUEST_CODE = 1000

    private val viewModel: LocationProvidersViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val swipe = findViewById<SwipeRefreshLayout>(R.id.swipe_refresh)
        swipe.setOnRefreshListener {
            viewModel.refresh()
            swipe.isRefreshing = false
        }

        val pagerAdapter = LocationTesterPagerAdapter(this, supportFragmentManager)

        val viewPager: ViewPager = findViewById(R.id.view_pager)
        viewPager.adapter = pagerAdapter

        val tabs: TabLayout = findViewById(R.id.tabs)
        tabs.setupWithViewPager(viewPager)
        (0..pagerAdapter.count).forEach {
            tabs.getTabAt(it)?.icon = pagerAdapter.getIcon(it, theme)
        }

        tabs.addOnTabSelectedListener( object : TabLayout.OnTabSelectedListener{
            override fun onTabReselected(tab: TabLayout.Tab) {
                swipe.isEnabled = pagerAdapter.allowSwipeForRefresh(tab.position)
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }

            override fun onTabSelected(tab: TabLayout.Tab) {
                swipe.isEnabled = pagerAdapter.allowSwipeForRefresh(tab.position)
            }
        })

        val toolbar = findViewById<MaterialToolbar>(R.id.topAppBar)
        toolbar.setOnMenuItemClickListener {
            when (it.itemId){
                R.id.refresh -> {
                    viewModel.refresh()
                    true
                }
                R.id.menu_settings -> {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    val uri: Uri = Uri.fromParts("package", packageName, null)
                    intent.data = uri
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }
//        setSupportActionBar(toolbar)
    }

    override fun getDefaultViewModelProviderFactory(): ViewModelProvider.Factory {
        //return super.getDefaultViewModelProviderFactory()
        return ViewModelProvider.AndroidViewModelFactory.getInstance(application)
    }

    public fun onClickMeasurementRow(view: View) {
        val vm = DataBindingUtil.findBinding<FragmentMeasurementRowBindingImpl>(view)

        vm?.viewModel?.requestPermissions(this, UPDATE_STATES_REQUEST_CODE)
        vm?.viewModel?.requeryLocation() // clear and re-query location in any case
    }

    fun onClickMeasurementSettings(view: View) {
        val vm = DataBindingUtil.findBinding<FragmentMeasurementRowBindingImpl>(view)
        viewModel.selectedProvider.value = vm?.viewModel
        if(vm?.viewModel != null) {
            val tabs: TabLayout = findViewById(R.id.tabs)
            tabs.selectTab(tabs.getTabAt(3))
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        // does not seem to get called ?

        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if(requestCode == UPDATE_STATES_REQUEST_CODE) {
            viewModel.updateProviderStates()
            viewModel.requeryLocations()
        }
    }



}
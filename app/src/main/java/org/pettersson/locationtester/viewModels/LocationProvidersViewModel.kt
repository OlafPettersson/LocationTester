package org.pettersson.locationtester.viewModels

import android.app.Application
import android.content.SharedPreferences
import android.graphics.Color
import android.location.LocationManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.preference.PreferenceManager
import org.pettersson.locationtester.BR
import org.pettersson.locationtester.R
import org.pettersson.locationtester.helper.RecyclerItem
import org.pettersson.location.LocationProviderBuildin
import org.pettersson.location.MicroGUnifiedNlpBackend
import org.pettersson.location.PackageChangedReceiver

class LocationProvidersViewModel(application: Application)
                : AndroidViewModel(application) {


    private val _networkProvider =
        LocationProviderBuildin(
            application,
            LocationManager.NETWORK_PROVIDER
        )
    private val _gpsProvider     =
        LocationProviderBuildin(
            application,
            LocationManager.GPS_PROVIDER
        )
    private val _passiveProvider =
        LocationProviderBuildin(
            application,
            LocationManager.PASSIVE_PROVIDER
        )
    private val _fusedProvider   =
        LocationProviderBuildin(
            application,
            "fused"
        ) // does this exist?

    private val _lazyProviders = lazy {
        MutableLiveData<List<LocationProviderViewModel>>().also {
            PackageChangedReceiver.packagesChanged = { reloadProviders(it) }
            //PackageChangedReceiver.register(application) // does not seem to work...
            reloadProviders(it)
        }
    }
    val providers: MutableLiveData<List<LocationProviderViewModel>> by _lazyProviders

    val enabledProviderItems = MutableLiveData<List<RecyclerItem>>()
    val enabledProviders = MutableLiveData<List<LocationProviderViewModel>>()

    val selectedProvider = MutableLiveData<LocationProviderViewModel?>()

    fun updateEnabledProviders(){
        updateEnabledProviders(providers.value!!)
    }

    private fun updateEnabledProviders(list : List<LocationProviderViewModel>){
        val filtered = list.filter { it.isEnabled }

        val prevId = selectedProvider.value?.id

        enabledProviders.value = (filtered)
        enabledProviderItems.value = (filtered.map  { it.toRecyclerItem() })

        // update colors.
        val colorList = listOf(0x77be6e,0xf41a2f,0xf6ae3e,
                               0x623633,0x244759,0xfedb41)
        enabledProviders.value!!.filter { it !is LocationProviderBuildinViewModel }
                                .forEachIndexed { i, p ->
            p.displayColor.value = (0xFF000000u or colorList[i%colorList.count()].toUInt()).toInt()
        }


        selectedProvider.value = filtered.firstOrNull { it.id == prevId} ?: filtered.lastOrNull()
    }

    override fun onCleared() {
        PackageChangedReceiver.packagesChanged = null
        if(_lazyProviders.isInitialized()) // is there a race here?
            providers.value?.forEach { it.isEnabled = false }
        super.onCleared()
    }

    private fun LocationProviderViewModel.toRecyclerItem() = RecyclerItem(
        data = this,
        variableId = BR.viewModel,
        layoutId = R.layout.fragment_measurement_row
    )


    private fun reloadProviders(target : MutableLiveData<List<LocationProviderViewModel>> )  {

        // disable providers in previous list if any
        target.value?.forEach { it.isEnabled = false }

        val list = listOf<LocationProviderViewModel>(
            LocationProviderBuildinViewModel("Android Network", 0xC0C0C0, _networkProvider),
            LocationProviderBuildinViewModel("Android Passive", 0xfdf500,_passiveProvider),
            LocationProviderBuildinViewModel("Android GPS", 0x1388f5, _gpsProvider),
            LocationProviderBuildinViewModel("Android Fused", 0x880FDC,_fusedProvider)
        );



        val list2 = MicroGUnifiedNlpBackend.getLocationProviders(getApplication())
                                           .map { LocationProviderMicroGViewModel(it) }
                                           .sortedWith(compareBy({ it.javaClass.name}, {it.name} ))

        var combinedList = list + list2

        enableProvidersFromSettings(combinedList)

        target.value = (combinedList)
        updateEnabledProviders(combinedList)
    }

    private fun enableProvidersFromSettings(providers: List<LocationProviderViewModel>) {
        var ctx = getApplication<Application>()
        val pref: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(ctx)

        providers.forEach {
            it.isEnabled = pref.getBoolean(it.id, it.isEnabled)
        }
    }

    fun updateProviderStates() {
        providers.value?.forEach { it.updateState() }
    }

    fun requeryLocations() {
        providers.value?.forEach { it.requeryLocation() }
    }

    fun update(){
        providers.value?.forEach { it.updateTimeString() }
    }

    fun refresh() {
        reloadProviders(_lazyProviders.value)
        enabledProviders.value?.forEach { it.requeryLocation() }
        updateProviderStates()
    }
}
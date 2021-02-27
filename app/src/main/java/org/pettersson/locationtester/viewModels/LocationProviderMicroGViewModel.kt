package org.pettersson.locationtester.viewModels

import android.app.Activity
import android.content.Intent
import android.location.Location
import android.net.Uri
import android.provider.Settings
import androidx.lifecycle.MutableLiveData
import org.pettersson.locationtester.helper.RecyclerItem
import org.pettersson.location.LocationPermissions
import org.pettersson.location.LocationProviderMicroG
import org.pettersson.location.MicroGUnifiedNlpBackend


class LocationProviderMicroGViewModel(val backend : MicroGUnifiedNlpBackend)
    : LocationProviderViewModel(backend.displayName,
                            backend.serviceInfo.packageName + ":" + backend.serviceInfo.name){

    private val mProvider =
        LocationProviderMicroG(backend)

    private val mInitIntent     = MutableLiveData<Intent?>()
    private val mAboutIntent    = MutableLiveData<Intent?>()
    private val mSettingsIntent = MutableLiveData<Intent?>()

    init {
        mProvider.isConnected.observeForever { onIsConnectedChanged() }
        mProvider.lastError.observeForever { lastError.value = it }
        mProvider.registerLocationCallback { setLocation(it, true) }
        mInitIntent.value     = backend.initIntent
        mSettingsIntent.value = backend.settingsIntent
        mAboutIntent.value    = backend.aboutIntent
    }

    override fun onIsEnabledChanged() {
        if(isEnabled){
            mProvider.bindService()
        }else{
            mProvider.unbindService()
        }
        updateState();
    }

    private fun onIsConnectedChanged() {
        updateState();

        if(mProvider.isConnected.value == true){
            setLocation(mProvider.getLocation(), false)

            if(mInitIntent.value == null)
                mInitIntent.value = mProvider.getInitIntent()
            if(mAboutIntent.value == null)
                mAboutIntent.value = mProvider.getAboutIntent()
            if(mSettingsIntent.value == null)
                mSettingsIntent.value = mProvider.getSettingsIntent()
        }
    }

    override fun requeryLocation() {
        setLocation(null, false)

        if(mProvider.isConnected.value == true){
            setLocation(mProvider.getLocation(), false)
        }else{
            mProvider.bindService()
        }
        updateState()
    }

    override fun getState(): LocationProviderState {
        if(!isEnabled){
            return LocationProviderState.OFF
        }

        if(!LocationPermissions.checkSelfPermission(backend.context,
                                                    LocationPermissions.ACCESS_COARSE_LOCATION)){
            return LocationProviderState.NO_PERMISSION;
        }

        if(mProvider.isConnected.value != true){
            return LocationProviderState.BINDING
        }

        if(!hasLocationValue){
            return LocationProviderState.WAITING
        }

        if(!isPushedLocation){
            return LocationProviderState.POLLED_LOCATION
        }

        return LocationProviderState.PUSHED_LOCATION
    }

    override fun requestPermissions(requestActivity : Activity?, requestCompleteCode : Int) : Boolean{
        if(!LocationPermissions.checkSelfPermission(backend.context, LocationPermissions.ACCESS_COARSE_LOCATION,
                               false, requestActivity, requestCompleteCode))
            return false

        return true
    }

    private val appDetailsSettingsIntent : Intent
        get() {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            val uri = Uri.fromParts("package", backend.serviceInfo.packageName, null)
            intent.data = uri
            return intent
        }

    override val detailRows : List<RecyclerItem>
        get () = super.detailRows + listOf(
             DetailsTextRowViewModel.recyclerItem("package", backend.serviceInfo.packageName)
            ,DetailsTextRowViewModel.recyclerItem("class", backend.serviceInfo.name)
            ,DetailsTextRowViewModel.recyclerItem("system package", if (backend.isSystemPackage)  "yes" else "no")
            ,DetailsTextRowViewModel.recyclerItem("target sdk version", backend.serviceInfo.applicationInfo.targetSdkVersion.toString())
            ,DetailsTextRowViewModel.recyclerItem("intent priority", backend.resolvInfo.priority.toString())
            ,DetailsTextRowViewModel.recyclerItem("last error", lastError, true)
            ,DetailsButtonRowViewModel.recyclerItem("Configure", mSettingsIntent)
            ,DetailsButtonRowViewModel.recyclerItem("App Settings", appDetailsSettingsIntent)
            ,DetailsButtonRowViewModel.recyclerItem("About", mAboutIntent)
            ,DetailsButtonRowViewModel.recyclerItem("Initialize", mInitIntent)
//            ,DetailsTextRowViewModel.recyclerItem("target sdk version", backend.serviceInfo.applicationInfo.targetSdkVersion.toString())
        )
}


package org.pettersson.locationtester.viewModels

import android.app.Activity
import android.content.Intent
import android.location.Location
import android.provider.Settings
import androidx.lifecycle.Observer
import org.pettersson.locationtester.helper.RecyclerItem
import org.pettersson.location.LocationProviderBuildin


class LocationProviderBuildinViewModel(name : String,
                                       color : Int,
                                       private val _provider : LocationProviderBuildin
)
        : LocationProviderViewModel(name,"buildin-" + _provider.provider){

    private val _statusObserver         = Observer<Any> { updateState() }
    private val _locationObserver       = Observer<Location?> { this.setLocation(it, true) }
    private val _cachedLocationObserver = Observer<Location?> { this.setLocation(it, false) }

    init {
        displayColor.value = 0xFF000000.toInt() or color
    }

    override fun onIsEnabledChanged() {

        if(isEnabled){
            _provider.status.observeForever(_statusObserver)
            _provider.location.observeForever(_locationObserver)
            _provider.queriedLocation.observeForever(_cachedLocationObserver)
        }else{
            _provider.status.removeObserver(_statusObserver)
            _provider.location.removeObserver(_locationObserver)
            _provider.queriedLocation.removeObserver(_cachedLocationObserver)
        }

        _provider.enableListening(isEnabled)
        updateState();
    }

    override fun requeryLocation() {
        _provider.queryLocation()
        updateState()
    }

    override fun getState() : LocationProviderState {
        if(!isEnabled){
            return LocationProviderState.OFF
        }
        if(!_provider.checkPermission()){
            return LocationProviderState.NO_PERMISSION;
        }

        _provider.updateListeningState()

        if(!_provider.isEnabled()){
            return LocationProviderState.DISABLED
        }

        if(_provider.queriedLocation.value == null){
            return LocationProviderState.WAITING
        }

        if(_provider.location.value == null){
            return LocationProviderState.CACHED_LOCATION
        }

        return LocationProviderState.PUSHED_LOCATION
    }

    override fun setLocation(newLocation: Location?, isPushedLocation: Boolean) {
        super.setLocation(newLocation, isPushedLocation)

        if(newLocation == null || newLocation.provider == _provider.provider){
            displayName.postValue(name)
        }else{
            displayName.postValue("$name (${newLocation.provider})")
        }
    }

    override fun requestPermissions(requestActivity : Activity?, requestCompleteCode : Int) : Boolean{
        val hasPermission = _provider.checkPermission(requestActivity, requestCompleteCode)
        if (!hasPermission)
            return false

        if(_provider.isEnabled())
            return true;

        if(requestActivity == null)
            return false

        val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
        requestActivity.startActivityForResult(intent, requestCompleteCode)
        return false
    }

    override val detailRows : List<RecyclerItem>
        get () = super.detailRows + listOf(
            DetailsButtonRowViewModel.recyclerItem("Settings", Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
        )
}
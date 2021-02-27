package org.pettersson.location

import android.app.Activity
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import androidx.lifecycle.MutableLiveData

class LocationProviderBuildin(val context : Context, val provider : String){

    private var mListeningUsageCount : Int = 0;
    private var mIsRegistered : Boolean = false;

    private val mLocationManager : LocationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager;

    val location        = MutableLiveData<Location>()
    val queriedLocation = MutableLiveData<Location?>()
    val status          = MutableLiveData<Any>()


    private val mLocationListener: LocationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            this@LocationProviderBuildin.location.postValue(location)
        }
        override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {
            this@LocationProviderBuildin.status.postValue(Any())
        }
        override fun onProviderEnabled(provider: String) {
            this@LocationProviderBuildin.status.postValue(Any())
        }
        override fun onProviderDisabled(provider: String) {
            this@LocationProviderBuildin.status.postValue(Any())
        }
    }

    @Synchronized
    fun enableListening(enable : Boolean ){
        if(enable)
            mListeningUsageCount += 1;
        else
            mListeningUsageCount -= 1;

        updateListeningState()
    }

    @Synchronized
    fun updateListeningState() {
        if (mListeningUsageCount > 0 && !mIsRegistered) {
            try {
                queriedLocation.value = mLocationManager.getLastKnownLocation(provider)
                mLocationManager.requestLocationUpdates(provider, 0L, 0f, mLocationListener)
                mIsRegistered = true
            }
            catch(e: SecurityException){
                // no error, we just tried.
            }
        }

        if (mListeningUsageCount <= 0 && mIsRegistered) {
            mLocationManager.removeUpdates(mLocationListener)
            mIsRegistered = false
        }
    }

    fun queryLocation() {
        try {
            queriedLocation.value = mLocationManager.getLastKnownLocation(provider)
        }
        catch(e: SecurityException){
            // no error, we just tried.
        }
    }
    public fun isEnabled() : Boolean{
        return mLocationManager.isLocationEnabled && mLocationManager.isProviderEnabled(provider);
    }

    public fun checkPermission(requestGrantActivity : Activity? = null,
                               requestResultCode : Int = 0): Boolean {
        val permission = if (provider == LocationManager.NETWORK_PROVIDER)
                              LocationPermissions.ACCESS_COARSE_LOCATION
                         else LocationPermissions.ACCESS_FINE_LOCATION
        return LocationPermissions.checkSelfPermission(context, permission,
                                  false, requestGrantActivity, requestResultCode)
    }
}
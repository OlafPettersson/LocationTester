package org.pettersson.location

import android.content.Intent
import android.location.Location
import androidx.lifecycle.LiveData
import org.microg.nlp.api.LocationBackend
import org.microg.nlp.api.LocationCallback

class LocationProviderMicroG(private val backend : MicroGUnifiedNlpBackend)
{
    private val remoteService =
        RobustRemoteService<LocationBackend>(
            backend.context,
            backend.locationBackendBindingIntent,
            LocationBackend.Stub::asInterface,
            ::onServiceConnected,
            ::onServiceDisconnecting
        )


    val isConnected : LiveData<Boolean>
        get() = remoteService.isConnected

    val lastError : LiveData<String>
        get() = remoteService.lastError

    fun bindService() : Boolean{
        return remoteService.bindService()
    }

    fun unbindService() {
        remoteService.unbindService()
    }

    private fun onServiceConnected(backend : LocationBackend): Boolean {
        backend.open(unifiedNlpApiCallback)
        return true
    }

    private fun onServiceDisconnecting(backend : LocationBackend) {
        backend.close()
    }

    /**
     * Call the remote service to return is current location.
     * The service must have been bound with [bindService],
     * and [isConnected] must be true.
     */
    fun getLocation(): Location? {
        return remoteService.invoke(::getLocation.name) {
            val ret = it.update()
            if(ret != null)
                remoteService.resetReconnectCounter()
            ret
        }
    }

    private var registeredCallback : ((Location?) -> Unit)? = null;

    private val unifiedNlpApiCallback = object : LocationCallback.Stub() {
        override fun report(location: Location?) {
            remoteService.resetReconnectCounter()
            registeredCallback?.invoke(location)
        }
    }

    /**
     * Register a callback when the service pushes new locations.
     *
     * Locations will be received after a call to [bindService],
     * and [isConnected] changes to true, and if the bound service
     * actually publishes locations.
     */
    fun registerLocationCallback(callback : ((Location?) -> Unit)?) {
        registeredCallback = callback
    }

    fun getInitIntent() : Intent? {

        return remoteService.invoke(::getInitIntent.name) {
            it.initIntent
        }
    }

    fun getAboutIntent() : Intent?{
        return remoteService.invoke(::getAboutIntent.name) {
            it.aboutIntent
        }
    }
    fun getSettingsIntent() : Intent?{
        return remoteService.invoke(::getSettingsIntent.name) {
            it.settingsIntent
        }
    }
}


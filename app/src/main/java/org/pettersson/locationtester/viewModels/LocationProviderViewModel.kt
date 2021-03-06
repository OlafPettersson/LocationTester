package org.pettersson.locationtester.viewModels

import android.app.Activity
import android.location.Location
import android.text.format.DateUtils
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import org.pettersson.location.LocationFormatter
import org.pettersson.locationtester.helper.RecyclerItem
import org.pettersson.locationtester.helper.format
import kotlin.math.log10
import kotlin.math.roundToInt
import kotlin.properties.Delegates
import kotlin.reflect.KProperty

abstract class LocationProviderViewModel(val name : String, val id: String) {

    val displayName = MutableLiveData<String>(name)

    val locationValue = MutableLiveData<Location?>()
    val timeValue= MutableLiveData<Long?>()

    val location = MutableLiveData<String>()
    val accuracyAsMeters = MutableLiveData<String>()
    val accuracyValue   = MutableLiveData<Int?>()
    val time     = MutableLiveData<String>()

    val displayColor = MutableLiveData<Int>()

    protected var isPushedLocation = false
    protected var hasLocationValue = false

    val lastError = MutableLiveData<String>()
    val status = MutableLiveData<LocationProviderState>()

    var isEnabled : Boolean by Delegates.observable(false, ::onIsEnabledChanged2)

    protected open fun onIsEnabledChanged()  { }
    private fun onIsEnabledChanged2(kProperty: KProperty<*>, oldValue: Boolean, newValue: Boolean) {
        if(oldValue != newValue)
            onIsEnabledChanged()
    }

    open fun requestPermissions(requestActivity: Activity? = null, requestCompleteCode: Int = 0) : Boolean {
        return true
    }

    protected open fun setLocation(newLocation: Location?, isPushedLocation : Boolean) {
        this.isPushedLocation = isPushedLocation
        hasLocationValue = newLocation != null

        if(newLocation == null){
            locationValue.postValue(null)
            location.postValue("")
            accuracyAsMeters.postValue("")
            accuracyValue.postValue(null)
            timeValue.postValue(null)
        }
        else {
            locationValue.postValue(newLocation)
            location.postValue(
                    LocationFormatter.latitudeAsDMS(newLocation.latitude, 3)
                 + " " +  LocationFormatter.longitudeAsDMS(newLocation.longitude, 3)
                      )
            accuracyAsMeters.postValue("${newLocation.accuracy?.format(0)} m")
            accuracyValue.postValue(if (newLocation.accuracy == null)  null
                                    else log10(newLocation.accuracy.toDouble()).roundToInt())
            timeValue.postValue(newLocation.time)
        }

        updateTimeString()
        updateState()
    }

    fun updateTimeString() {
        if(timeValue.value == null){
            time.postValue("")
            return
        }

        val now = System.currentTimeMillis()
        if(now - timeValue.value!! <= 100){
            val ago = DateUtils.getRelativeTimeSpanString(now, now, 0)
            time.postValue(ago.toString())
        }else {
            val ago = DateUtils.getRelativeTimeSpanString(timeValue.value!!, now, 0)
            time.postValue(ago.toString())
        }
    }

    open fun requeryLocation()  { }


    abstract fun getState() : LocationProviderState
    fun updateState() {
        status.postValue(getState())
    }

    open val detailRows : List<RecyclerItem>
        get () = listOf(
                DetailsTextRowViewModel.recyclerItem("Name", name),
                DetailsTextRowViewModel.recyclerItem("Status", status.map { it.toString() })
            )

}
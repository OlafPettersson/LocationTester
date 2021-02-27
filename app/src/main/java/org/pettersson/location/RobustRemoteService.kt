package org.pettersson.location

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.DeadObjectException
import android.os.IBinder
import android.os.RemoteException
import android.util.Log
import androidx.lifecycle.MutableLiveData
import org.pettersson.locationtester.helper.TAG
import java.util.*
import kotlin.concurrent.schedule
import kotlin.math.pow

class RobustRemoteService<TService>(
    private val context: Context,
    private val intent: Intent,
    private val serviceFactory : (IBinder) -> TService,
    private val serviceConnected     : (TService) -> Boolean,
    private val serviceDisconnecting : (TService) -> Unit
) {

    val isConnected = MutableLiveData<Boolean>()
    val lastError   = MutableLiveData<String>()

    private var mTheService : TService? = null;

    private val mServiceName = intent.`package`

    private var mIsBinding      = false
    private var mIsReconnecting = false

    private var mReconnectTimer   = Timer("reconnect", true)
    private var mReconnectCounter = 0

    /**
     * base delay to apply when reconnecting to the service.
     * set to 0 to disable automatic reconnections
     */
    val reconnectBaseDelaySec = 10

    fun resetReconnectCounter(){
        mReconnectCounter = 0
    }

    private val _connection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            try {
                mTheService = serviceFactory(service)
            } catch (e: RemoteException) {
                lastError.postValue(e.message ?: e.javaClass.simpleName)
                return
            }

            Log.d(TAG(), "service connected: $mServiceName")

            var success = false
            try{
                 success = serviceConnected(mTheService!!)
                if(success)
                    isConnected.value = true
            }
            catch(e: Exception) {
                lastError.postValue(e.message ?: e.javaClass.simpleName)
                Log.i(TAG(), "service initial call error: ${mServiceName}")
            }

            if(!success)
                reconnectService()

        }

        override fun onServiceDisconnected(className: ComponentName) {
            mTheService = null
            Log.d(TAG(), "service disconnected: $mServiceName")

            isConnected.value = false

            if(mIsBinding){
                lastError.postValue("the service stopped unexpectedly")
                reconnectService()
            }
        }
    }

    /**
     * Opens a connection to the backend
     */
    fun bindService() : Boolean {

        synchronized (this){
            if(mIsBinding || mIsReconnecting)
                return true

            Log.d(TAG(), "service binding to: ${mServiceName}")

            lastError.postValue("")
            mIsReconnecting = false

            var success = false
            try{
                success = context.bindService(intent, _connection, Context.BIND_AUTO_CREATE)
                if(!success)
                    lastError.postValue("unable to bind to service")
            }
            catch(e : Exception){
                lastError.postValue(e.message ?: e.toString())
            }

            mIsBinding = success
            return success
        }
    }

    /**
     * Closes the connection to the backend
     */
    fun unbindService() {
        synchronized (this){
            if (mIsBinding) {
                Log.d(TAG(), "service unbinding: ${mServiceName}")

                if(isConnected.value == true){
                    try{
                        val s = mTheService
                        if(s != null)
                            serviceDisconnecting(s)
                    }
                    catch(e: Exception){
                        lastError.value = e.message ?: e.toString()
                    }
                }

                try {
                    context.unbindService(_connection)
                }
                catch(e: Exception) {
                    // ignore errors
                }
                mIsBinding = false
            }
            mIsReconnecting = false
        }
    }

    private fun reconnectService() {

        // not sure how elaborate we want to get to make the connection robust
        // possible error cases to consider.
        // - Out of memory on the service side
        // - service manually closed by user
        // - bug in the service

        synchronized (this){
            if(!mIsBinding || mIsReconnecting)
                return

            ++mReconnectCounter

            if(mReconnectCounter <= 3 && reconnectBaseDelaySec > 0) {
                mIsReconnecting = true
            }
        }

        if(!mIsReconnecting){
            unbindService()
            return
        }

        try {
            Log.i(TAG(), "service unbinding for reconnect: ${mServiceName}")
            context.unbindService(_connection)
        }
        catch(e: RemoteException){
        }

        val delay = reconnectBaseDelaySec * 1000 * 2.0.pow(mReconnectCounter)

        mReconnectTimer.schedule(delay.toLong()) {
            var doRebind : Boolean

            synchronized(this@RobustRemoteService) {
                doRebind = mIsBinding && mIsReconnecting
                mIsReconnecting = false
                if(doRebind){
                    mIsBinding = false
                }
            }

            if(doRebind){
                Log.i(TAG(), "service reconnect attempt rebind: ${mServiceName}")
                if(!bindService())
                    reconnectService()
            }
        }
    }

    public fun <R> invoke(logName : String, impl: (TService) -> R?): R?{
        val service = mTheService

        if(service == null)
            return null

        return try {
            impl(service)
        }catch (e: DeadObjectException) {
            // not sure if or when this is actually needed
            // or if this logic should better go into the "onServiceDisconnected"
            // method above
            val msg = "${logName}: service died, trying to reconnect: ${mServiceName}"
            Log.i(TAG(), msg)

            lastError.value = msg
            isConnected.value = false
            reconnectService()
            null
        }
        catch (e: Exception) {
            val msg = logName + ": " + (e.message ?: e.toString())
            Log.w(TAG(), "error in call to: ${mServiceName} method: $msg")
            lastError.value = msg
            null
        }
    }
}
package org.pettersson.location

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat

class LocationPermissions
{
    companion object{
        const val ACCESS_FINE_LOCATION = android.Manifest.permission.ACCESS_FINE_LOCATION
        const val ACCESS_COARSE_LOCATION = android.Manifest.permission.ACCESS_COARSE_LOCATION

        public fun checkSelfPermission(context : Context,
                                       permission : String,
                                       requireBackgroundAccess : Boolean = false,
                                       requestGrantActivity : Activity? = null,
                                       requestResultCode : Int = 0): Boolean {

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M)
                return true


            val checkBackgroundAccess = (requireBackgroundAccess
                                      && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)

            val permissions = mutableListOf(permission)

            if(checkBackgroundAccess)
                permissions.add(android.Manifest.permission.ACCESS_BACKGROUND_LOCATION)

            if (permissions.all { context.checkSelfPermission(it) == PackageManager.PERMISSION_GRANTED })
                return true

            if(requestGrantActivity == null)
                return false

            // Show the permission request
            ActivityCompat.requestPermissions(requestGrantActivity, arrayOf(permission),
                                              requestResultCode)
            return false
        }
    }
}
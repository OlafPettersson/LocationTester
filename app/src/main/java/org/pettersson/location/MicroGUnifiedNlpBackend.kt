package org.pettersson.location

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.content.pm.ServiceInfo

/**
 *  This class finds and references installed MicroG location providers on a system
 */
class MicroGUnifiedNlpBackend(val context : Context,
                              val resolvInfo: ResolveInfo){

    val serviceInfo : ServiceInfo = resolvInfo.serviceInfo

    val name = serviceInfo.name

    /**
     * true if this location provider lives in a system package
     */
    val isSystemPackage : Boolean = (serviceInfo.applicationInfo?.flags ?: 0) and ApplicationInfo.FLAG_SYSTEM > 0

    /**
     * priority of this package, as reported by the package manager
     */
    val packageManagerPriority : Int = resolvInfo.priority

    /**
     * true if this location provider lives in the same package as the application
     */
    val isSamePackage : Boolean = serviceInfo.packageName == context.packageName

    /**
     * true if this location provider lives in a package that shares at
     * least one signature with this application
     */
    val hasSameSignature : Boolean by lazy {
        (context.packageManager.checkSignatures(serviceInfo.packageName, context.packageName)
            == PackageManager.SIGNATURE_MATCH)
    }

    val backendSummary : String? by lazy {
        serviceInfo.metaData?.getString(METADATA_BACKEND_SUMMARY)
    }

    val displayName: String by lazy {
        val packageManager: PackageManager = context.packageManager
        var applicationInfo = serviceInfo.applicationInfo;
        val label = packageManager.getApplicationLabel(applicationInfo)
        label.toString()
    }

    val aboutIntent : Intent? by lazy {
        serviceInfo.metaData?.getString(METADATA_BACKEND_ABOUT_ACTIVITY)?.let {
            createViewIntent(serviceInfo.packageName, it)
        }
    }

    val settingsIntent : Intent? by lazy {
        serviceInfo.metaData?.getString(METADATA_BACKEND_SETTINGS_ACTIVITY)?.let {
            createViewIntent(serviceInfo.packageName, it)
        }
    }

    val initIntent : Intent? by lazy {
        serviceInfo.metaData?.getString(METADATA_BACKEND_INIT_ACTIVITY)?.let {
            createViewIntent(serviceInfo.packageName, it)
        }
    }

    private fun createViewIntent(packageName: String, activityName: String): Intent {
        val intent = Intent(Intent.ACTION_VIEW);
        intent.setPackage(packageName);
        intent.setClassName(packageName, activityName);
        return intent;
    }

    val locationBackendBindingIntent : Intent
        get () {
            val ret = Intent(ACTION_MICROG_LOCATION_BACKEND)
            ret.setPackage(serviceInfo.packageName)
            ret.setClassName(serviceInfo.packageName, serviceInfo.name)
            return ret
        }

    companion object {

        private val ACTION_MICROG_LOCATION_BACKEND = "org.microg.nlp.LOCATION_BACKEND"

        private const val METADATA_BACKEND_SETTINGS_ACTIVITY = "org.microg.nlp.BACKEND_SETTINGS_ACTIVITY"
        private const val METADATA_BACKEND_ABOUT_ACTIVITY = "org.microg.nlp.BACKEND_ABOUT_ACTIVITY"
        private const val METADATA_BACKEND_INIT_ACTIVITY = "org.microg.nlp.BACKEND_INIT_ACTIVITY"
        private const val METADATA_BACKEND_SUMMARY = "org.microg.nlp.BACKEND_SUMMARY"
        private const val METADATA_API_VERSION = "org.microg.nlp.API_VERSION"

        /**
         * Queries and returns all available location providers,
         * ordered by their relevance.
         * The first returned value has the highest relevance.
         */
        fun getLocationProviders(context : Context) : List<MicroGUnifiedNlpBackend>{
            val intent = Intent(ACTION_MICROG_LOCATION_BACKEND)
            val pm = context.packageManager

            return pm.queryIntentServices(intent, PackageManager.GET_META_DATA).map {
                MicroGUnifiedNlpBackend(context, it)
            }.sortedWith(compareBy (
                { !it.isSamePackage},           // prefer same package
                { !it.hasSameSignature},        // prefer same signature
                { !it.isSystemPackage },        // prefer system packages
                { -it.packageManagerPriority }  // prefer higher priority
            ))
        }
    }
}
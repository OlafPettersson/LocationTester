/*
 * SPDX-FileCopyrightText: 2019, microG Project Team
 * SPDX-License-Identifier: Apache-2.0
 */

package org.pettersson.location

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.Intent.*
import android.content.IntentFilter


public class PackageChangedReceiver : BroadcastReceiver() {

    companion object{
        var packagesChanged: (()->Unit)? = null

        val relevantActions = listOf(
            ACTION_PACKAGE_CHANGED,
            ACTION_PACKAGE_REMOVED,
            ACTION_PACKAGE_REPLACED,
            ACTION_PACKAGE_ADDED)


        @Volatile
        private var receiversRegistered = false

        fun register(contextIn: Context) {
            if (receiversRegistered) return

            val context = contextIn.applicationContext
            val intentFilter = IntentFilter()
            relevantActions.forEach { intentFilter.addAction(it) }
            context.registerReceiver(PackageChangedReceiver(), intentFilter)
            receiversRegistered = true
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        suspend { packagesChanged?.invoke() }
    }
}

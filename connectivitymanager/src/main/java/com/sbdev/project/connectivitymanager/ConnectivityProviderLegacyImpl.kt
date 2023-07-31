package com.sbdev.project.connectivitymanager

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.ConnectivityManager.CONNECTIVITY_ACTION
import android.net.ConnectivityManager.EXTRA_NETWORK_INFO
import android.net.NetworkInfo

@Suppress("DEPRECATION")
class ConnectivityProviderLegacyImpl(private val context: Context, private val cm: ConnectivityManager): ConnectivityProviderBaseImpl() {

    private val receiver = ConnectivityReceiver()


    override fun subscribe() {
        context.registerReceiver(receiver, IntentFilter(CONNECTIVITY_ACTION))
    }

    override fun unsubscribe() {
        context.unregisterReceiver(receiver)
    }


    override fun getNetworkState(): ConnectivityProvider.NetworkState {
        val activityNetworkInfo = cm.activeNetworkInfo
        return if(activityNetworkInfo != null) {
            ConnectivityProvider.NetworkState.ConnectedState.ConnectedLegacy(activityNetworkInfo)
        } else {
            ConnectivityProvider.NetworkState.NotConnectedState
        }
    }


    private inner class ConnectivityReceiver: BroadcastReceiver(){
        override fun onReceive(c: Context, i: Intent) {
            val networkInfo = cm.activeNetworkInfo
            val fallbackNetworkInfo: NetworkInfo? = i.getParcelableExtra(EXTRA_NETWORK_INFO)

            val state: ConnectivityProvider.NetworkState = if(networkInfo?.isConnectedOrConnecting == true) {
                ConnectivityProvider.NetworkState.ConnectedState.ConnectedLegacy(networkInfo)
            } else if (networkInfo != null && fallbackNetworkInfo != null && networkInfo.isConnectedOrConnecting != fallbackNetworkInfo.isConnectedOrConnecting) {
                ConnectivityProvider.NetworkState.ConnectedState.ConnectedLegacy(fallbackNetworkInfo)
            } else {
                val state = networkInfo ?: fallbackNetworkInfo
                if(state != null)
                    ConnectivityProvider.NetworkState.ConnectedState.ConnectedLegacy(state)
                else
                    ConnectivityProvider.NetworkState.NotConnectedState
            }

            dispatchChange(state)
        }
    }
}
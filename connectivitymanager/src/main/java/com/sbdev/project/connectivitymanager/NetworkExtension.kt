package com.sbdev.project.connectivitymanager

import android.net.NetworkCapabilities

fun NetworkCapabilities.networkType() : Int {
    return when {
        this.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> NetworkCapabilities.TRANSPORT_CELLULAR
        this.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> NetworkCapabilities.TRANSPORT_WIFI
        this.hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH) -> NetworkCapabilities.TRANSPORT_BLUETOOTH
        this.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> NetworkCapabilities.TRANSPORT_ETHERNET
        this.hasTransport(NetworkCapabilities.TRANSPORT_VPN) -> NetworkCapabilities.TRANSPORT_VPN
        this.hasTransport(NetworkCapabilities.TRANSPORT_WIFI_AWARE) -> NetworkCapabilities.TRANSPORT_WIFI_AWARE
        this.hasTransport(NetworkCapabilities.TRANSPORT_LOWPAN) -> NetworkCapabilities.TRANSPORT_LOWPAN
        this.hasTransport(NetworkCapabilities.TRANSPORT_USB) -> NetworkCapabilities.TRANSPORT_USB
        else -> -1
    }
}

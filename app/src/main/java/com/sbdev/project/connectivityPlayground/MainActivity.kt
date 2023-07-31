package com.sbdev.project.connectivityPlayground

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.sbdev.project.connectivitymanager.ConnectivityProvider

class MainActivity : AppCompatActivity(), ConnectivityProvider.ConnectivityStateListener {

    private val provider: ConnectivityProvider
            by lazy { ConnectivityProvider.createProvider(this) }

    private lateinit var networkStatusViaCallback: TextView
    private lateinit var networkStatusViaButton: TextView
    private lateinit var networkImageViaCallback: ImageView
    private lateinit var networkImageViaButton: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val button = findViewById<Button>(R.id.btnGetStatus)
        networkImageViaCallback = findViewById(R.id.ivAutomaticConnectionStatus)
        networkStatusViaCallback = findViewById(R.id.tvNetworkStatusViaCallback)
        networkImageViaButton = findViewById(R.id.ivManualConnectionStatus)
        networkStatusViaButton = findViewById(R.id.tvNetworkStatusViaButton)


        button.setOnClickListener {
            val hasInternet = provider.getNetworkState().hasInternet()
            val networkType = provider.getNetworkState().networkType()
            networkStatusViaButton.text =
                if(hasInternet) {
                    String.format("Connected via %s", getNetworkTypeName(networkType))
                } else {
                    String.format("Not Connected")
                }
            networkImageViaButton.setImageResource(if(hasInternet) getNetworkTypeImage(networkType) else R.drawable.no_connection)
        }
    }

    override fun onStart() {
        super.onStart()
        provider.addListener(this)
    }

    override fun onStop() {
        super.onStop()
        provider.removeListener(this)
    }

    override fun onStateChange(state: ConnectivityProvider.NetworkState) {
        val hasInternet = state.hasInternet()
        val networkType = state.networkType()
        Log.d("MainActivity", "onStateChange: $state")
        networkStatusViaCallback.text =
            if(hasInternet) {
                String.format("Connected via %s", getNetworkTypeName(networkType))
            } else {
                String.format("Not Connected")
            }
        networkImageViaCallback.setImageResource(if(hasInternet) getNetworkTypeImage(networkType) else R.drawable.no_connection)

        if (!hasInternet) {
            showNoConnectionNotification()
        } else {
            removeNoConnectionNotification()
        }
    }

    private fun ConnectivityProvider.NetworkState.hasInternet(): Boolean {
        return (this as? ConnectivityProvider.NetworkState.ConnectedState)?.hasInternet == true
    }

    private fun ConnectivityProvider.NetworkState.networkType(): Int {
        return (this as? ConnectivityProvider.NetworkState.ConnectedState)?.networkType ?: -1
    }

    @Suppress("DEPRECATION")
    private fun getNetworkTypeName(networkType: Int): String {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return when(networkType) {
                NetworkCapabilities.TRANSPORT_CELLULAR -> "Cellular Data"
                NetworkCapabilities.TRANSPORT_WIFI -> "Wi-Fi"
                NetworkCapabilities.TRANSPORT_BLUETOOTH -> "Bluetooth"
                NetworkCapabilities.TRANSPORT_ETHERNET -> "Ethernet"
                NetworkCapabilities.TRANSPORT_VPN -> "VPN"
                NetworkCapabilities.TRANSPORT_WIFI_AWARE -> "Wi-Fi Aware"
                NetworkCapabilities.TRANSPORT_LOWPAN -> "LoWPAN"
                NetworkCapabilities.TRANSPORT_USB -> "USB"
                else -> "Unknown source"
            }
        } else {
            return when(networkType) {
                ConnectivityManager.TYPE_MOBILE -> "Cellular Data"
                ConnectivityManager.TYPE_WIFI -> "Wi-Fi"
                ConnectivityManager.TYPE_BLUETOOTH -> "Bluetooth"
                ConnectivityManager.TYPE_ETHERNET -> "Ethernet"
                ConnectivityManager.TYPE_VPN -> "VPN"
                ConnectivityManager.TYPE_WIMAX -> "WiMAX"
                ConnectivityManager.TYPE_MOBILE_DUN -> "Mobile DUN"
                ConnectivityManager.TYPE_MOBILE_HIPRI -> "Mobile HIPRI"
                ConnectivityManager.TYPE_MOBILE_MMS -> "Mobile MMS"
                ConnectivityManager.TYPE_MOBILE_SUPL -> "Mobile Supl"
                ConnectivityManager.TYPE_DUMMY -> "Dummy"
                else -> "Unknown source"
            }
        }
    }

    @Suppress("DEPRECATION")
    private fun getNetworkTypeImage(networkType: Int): Int {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return when(networkType) {
                NetworkCapabilities.TRANSPORT_CELLULAR -> R.drawable.cellular
                NetworkCapabilities.TRANSPORT_WIFI -> R.drawable.router
                NetworkCapabilities.TRANSPORT_BLUETOOTH -> R.drawable.bluetooth
                NetworkCapabilities.TRANSPORT_ETHERNET -> R.drawable.ethernet
                NetworkCapabilities.TRANSPORT_VPN -> R.drawable.vpn
                else -> R.drawable.ic_launcher_monochrome
            }
        } else {
            return when(networkType) {
                ConnectivityManager.TYPE_MOBILE -> R.drawable.cellular
                ConnectivityManager.TYPE_WIFI -> R.drawable.router
                ConnectivityManager.TYPE_BLUETOOTH -> R.drawable.bluetooth
                ConnectivityManager.TYPE_ETHERNET -> R.drawable.ethernet
                ConnectivityManager.TYPE_VPN -> R.drawable.vpn
                else -> R.drawable.ic_launcher_monochrome
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun showNoConnectionNotification() {
        // Check for notification permission & request for permission if not allowed
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (!isNotificationAllowed()) {
                notificationPermissionRequester.launch(Manifest.permission.POST_NOTIFICATIONS)
                return
            }
        }

        // Create notification channel if required
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            createNotificationChannel()

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent
            .getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("No connectivity")
            .setContentText("Not connected to any Wifi or mobile network")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("Looks like you are not connected to any Wifi or mobile network!"))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(false)

        with(NotificationManagerCompat.from(this)) {
            notify(NOTIFICATION_ID, builder.build())
        }
    }

    private fun removeNoConnectionNotification() {
        with(NotificationManagerCompat.from(this)) {
            cancel(NOTIFICATION_ID)
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun isNotificationAllowed(): Boolean {
        return ActivityCompat.checkSelfPermission(
                this@MainActivity,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
    }

    private val notificationPermissionRequester =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { result ->
            if (result) {
                val hasInternet = provider.getNetworkState().hasInternet()

                if (!hasInternet) {
                    showNoConnectionNotification()
                }
            }
        }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel() {
        val channelName = "Connectivity Channel"
        val channelDescription = "This channel is used for showing notification about connectivity"
        val importance = NotificationManager.IMPORTANCE_HIGH

        val notificationChannel = NotificationChannel(CHANNEL_ID, channelName, importance)
        notificationChannel.description = channelDescription
        notificationChannel.vibrationPattern = longArrayOf(0,300,200,100)
        notificationChannel.lightColor = Color.YELLOW
        notificationChannel.enableVibration(true)
        notificationChannel.enableLights(true)
        notificationChannel.setBypassDnd(true)
        notificationChannel.setShowBadge(true)

        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(notificationChannel)
    }

    companion object {
        private const val CHANNEL_ID = "connectivity_notification"
        private const val NOTIFICATION_ID = 5251
    }
}
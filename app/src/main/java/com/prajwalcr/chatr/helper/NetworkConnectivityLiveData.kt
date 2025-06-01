package com.prajwalcr.chatr.helper

import androidx.lifecycle.LiveData
import com.prajwalcr.chatr.BuildConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import timber.log.Timber
import java.net.InetSocketAddress
import java.net.Socket

class NetworkConnectivityLiveData: LiveData<NetworkConnectivityLiveData.NetworkStatus>() {

    companion object {
        private const val TAG = "internetReceiverLiveData"
        private const val INTERNET_CHECK_DELAY = 30000L
    }

    enum class NetworkStatus {
        NETWORK_CONNECTIVITY_AVAILABLE,
        NETWORK_CONNECTIVITY_NOT_AVAILABLE,
        NETWORK_CONNECTIVITY_DISCONNECTED,
        UNKNOWN_STATE
    }

    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    private var pingGoogleDNSEventJob: Job? = null
    private var isInternetAvailable: Boolean = false

    override fun onActive() {
        super.onActive()
        Timber.tag(TAG).i("onActive: ")
        scheduleInternetConnectivityCheckJob()
    }

    override fun onInactive() {
        super.onInactive()
        Timber.tag(TAG).i("onInactive: ")
        pingGoogleDNSEventJob?.cancel()
    }

    private fun scheduleInternetConnectivityCheckJob() {
        pingGoogleDNSEventJob?.cancel()

        pingGoogleDNSEventJob = coroutineScope.launch {
            while (isActive) {
                val connectionStatus = getInternetConnectivityStatus()
                if (value != connectionStatus) postValue(connectionStatus)

                isInternetAvailable = (connectionStatus == NetworkStatus.NETWORK_CONNECTIVITY_AVAILABLE)

                delay(INTERNET_CHECK_DELAY)
            }
        }
    }

    fun getInternetAvailable() = isInternetAvailable

    private fun getInternetConnectivityStatus(): NetworkStatus {
        return try {
            val socket = Socket()
            socket.connect(InetSocketAddress("  ",53),1500)
            socket.close()
            NetworkStatus.NETWORK_CONNECTIVITY_AVAILABLE
        } catch (ex: Exception) {
            Timber.tag(TAG).e("Network is not available")
            NetworkStatus.NETWORK_CONNECTIVITY_NOT_AVAILABLE
        }
    }
}
package com.zj.data.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class NetworkChecker(context: Context) {

    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> get() = _isConnected

    private val connectivityManager: ConnectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    init {
        registerNetworkCallback()
    }

    private fun registerNetworkCallback() {
        connectivityManager.registerDefaultNetworkCallback(object :
            ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                Log.d(TAG, "onAvailable")
                _isConnected.value = true
            }

            override fun onLost(network: Network) {
                Log.d(TAG, "onLost")
                _isConnected.value = false
            }
        })
    }

    companion object {
        private const val TAG = "NetworkChecker"
    }

}

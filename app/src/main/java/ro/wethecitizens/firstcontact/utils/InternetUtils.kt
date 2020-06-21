// Copyright (c) 2020 Noi, Cetatenii

package ro.wethecitizens.firstcontact.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo

object InternetUtils {

    fun hasInternetConnection(context: Context) : Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork: NetworkInfo? = cm.activeNetworkInfo
        val isConnected: Boolean = activeNetwork?.isConnected == true

        return isConnected
    }
}
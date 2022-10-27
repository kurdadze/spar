package ge.mark.sparemployee.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities

object NetworkUtil {
    fun getConnectivityStatusString(context: Context): String {
        val status: String?
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = cm.activeNetwork
        if (activeNetwork != null) {
            val actNw = cm.getNetworkCapabilities(activeNetwork) ?: return false.toString()
            status = when {
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> {
                    "Wifi enabled"
                }
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> {
                    "Mobile network enabled"
                }
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> {
                    "Ethernet enabled"
                }
                else -> "Unknown network"
            }
        } else {
            status = "No internet is available"
        }
        return status
    }
}
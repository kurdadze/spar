package ge.mark.sparemployee.services

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager

class MyReceiver : BroadcastReceiver() {

    @SuppressLint("UnsafeProtectedBroadcastReceiver")
    override fun onReceive(context: Context?, intent: Intent?) {
//        var status = getConnectivityStatusString(context)
//        if (status.isEmpty() || status == "No internet is available" || status == "No Internet Connection") {
//            status = "No Internet Connection"
//            Toast.makeText(context, "Place enable internet connection", Toast.LENGTH_LONG).show()
//        } else {
//            Toast.makeText(context, status, Toast.LENGTH_LONG).show()
//        }
//        val status = intent.getBooleanExtra("state", false) ?: return
//        if (status) {
//            Toast.makeText(context, "Mode Enabled", Toast.LENGTH_LONG).show()
//        } else {
//            Toast.makeText(context, "Mode disabled", Toast.LENGTH_LONG).show()
//        }
        if (connectivityReceiverListener != null) {
            connectivityReceiverListener!!.onNetworkConnectionChanged(
                isConnectedOrConnecting(
                    context!!
                )
            )
        }
    }

    private fun isConnectedOrConnecting(context: Context): Boolean {
        val connMgr = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connMgr.activeNetworkInfo
        return networkInfo != null && networkInfo.isConnectedOrConnecting
    }

    interface ConnectivityReceiverListener {
        fun onNetworkConnectionChanged(isConnected: Boolean)
    }

    companion object {
        var connectivityReceiverListener: ConnectivityReceiverListener? = null
    }
}
package ge.mark.sparemployee.services

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import ge.mark.sparemployee.network.NetworkUtil.getConnectivityStatusString

class MyReceiver : BroadcastReceiver() {

    @SuppressLint("UnsafeProtectedBroadcastReceiver")
    override fun onReceive(context: Context, intent: Intent) {
        var status = getConnectivityStatusString(context)
        Log.i("network", status!!)
        if (status.isEmpty() || status == "No internet is available" || status == "No Internet Connection") {
            status = "No Internet Connection"
//            Toast.makeText(context, "Place enable internet connection", Toast.LENGTH_LONG).show()
        } else {
//            Toast.makeText(context, status, Toast.LENGTH_LONG).show()
        }
    }
}
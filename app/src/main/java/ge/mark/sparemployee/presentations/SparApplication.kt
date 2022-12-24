package ge.mark.sparemployee.presentations

import android.app.Application

class SparApplication: Application() {

    companion object {
        var pingState = true
    }

    override fun onCreate() {
        super.onCreate()
    }
}
package ge.mark.sparemployee.models

import java.lang.System.currentTimeMillis

data class Worker(
    var id: Long = getTimeStamp(),
    var code: String = "",
    var photo_path: String = "",
    var photo: String = "",
    var date_time: String = "",
    var forwarded: String = ""
) {
    companion object {
        fun getTimeStamp(): Long {
            return currentTimeMillis() / 1000
        }
    }
}

package ge.mark.sparemployee.services

import android.app.job.JobParameters
import android.app.job.JobService
import android.util.Log
import ge.mark.sparemployee.helpers.DbHelper
import ge.mark.sparemployee.helpers.SysHelper
import ge.mark.sparemployee.models.User
import ge.mark.sparemployee.models.Worker
import ge.mark.sparemployee.network.ApiClient
import ge.mark.sparemployee.network.calls.ApiCalls
import kotlinx.coroutines.Runnable
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SparSendOfflineDataJobService : JobService() {

    private var jobCancelled = false

    override fun onStartJob(params: JobParameters): Boolean {
        Log.i(TAG, "Job started")
        doBackgroundWork(params)
        return true
    }

    override fun onStopJob(params: JobParameters): Boolean {
        Log.i(TAG, "Job cancelled before completion")
        jobCancelled = true
        return true
    }

    private fun doBackgroundWork(params: JobParameters) {
        val dbHelper = DbHelper(context = applicationContext)
        Thread(Runnable {
            var i = 0
            do {
                i++
                Log.i(TAG, "run: $i")
                if (jobCancelled) {
                    return@Runnable
                }
                try {
                    val workers: ArrayList<Worker> = dbHelper.getAllWorker()
                    if(workers.size >0){
                        for (wrk in workers){
                            ApiCalls.sendDataToServer(
                                context = applicationContext,
                                controller_code = wrk.controller_code,
                                pin = wrk.pin,
                                datetime = wrk.datetime,
                                picture = wrk.picture,
                                hash = wrk.hash,
                                photo_name = wrk.photo_name
                            )
                        }
                    }
                    Thread.sleep(15000)
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
            } while (!jobCancelled)
            Log.i(TAG, "Job finished")
            jobFinished(params, false)
        }).start()
    }

    companion object {
        private const val TAG = "JobService"
    }
}
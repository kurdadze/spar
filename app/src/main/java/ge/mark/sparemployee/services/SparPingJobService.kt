package ge.mark.sparemployee.services

import android.app.job.JobParameters
import android.app.job.JobService
import android.util.Log
import ge.mark.sparemployee.network.calls.ApiCalls
import kotlinx.coroutines.Runnable

class SparPingJobService : JobService() {

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
        Thread(Runnable {
            var i = 0
            do {
                i++
                Log.i(TAG, "run: $i")
                if (jobCancelled) {
                    return@Runnable
                }
                try {
                    ApiCalls.ping(context = applicationContext)
                    Thread.sleep(60000)
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
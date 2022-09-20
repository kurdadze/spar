package ge.mark.sparemployee.services

import android.app.job.JobParameters
import android.app.job.JobService
import android.util.Log
import ge.mark.sparemployee.helpers.DbHelper
import ge.mark.sparemployee.helpers.SysHelper
import ge.mark.sparemployee.models.User
import ge.mark.sparemployee.network.ApiClient
import ge.mark.sparemployee.network.calls.ApiCalls
import kotlinx.coroutines.Runnable
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SparGetUserJobService : JobService() {

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
    } //    

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
//                    Handler(Looper.getMainLooper()).postDelayed({
//                        getUsers()
//                    }, 60000)
                    ApiCalls.getUsers(context = applicationContext)
                    Thread.sleep(60000*60)
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
            } while (!jobCancelled)
            Log.i(TAG, "Job finished")
            jobFinished(params, false)
        }).start()
    }
//
//    private fun getUsers() {
//        val sysHelper = SysHelper(context = applicationContext)
//        val dbHelper = DbHelper(context = applicationContext)
//        val getUsersCall =
//            ApiClient.getService()?.getUsers(CONTROLLER_CODE = sysHelper.getDeviceID())
//        getUsersCall?.enqueue(object : Callback<List<User>> {
//            override fun onResponse(
//                call: Call<List<User>>,
//                response: Response<List<User>>
//            ) {
//                dbHelper.deleteAllUsers()
//                val res = response.body()
//                res?.forEach {
//                    dbHelper.insertUser(
//                        User(
//                            first_name = it.first_name,
//                            last_name = it.last_name,
//                            pass_code = it.pass_code
//                        )
//                    )
//                }
//                Log.v("retrofit", "call failed")
//            }
//
//            override fun onFailure(call: Call<List<User>>, t: Throwable) {
//                Log.v("retrofit", "call failed")
//            }
//
//        })
//    }

    companion object {
        private const val TAG = "JobService"
    }
}
package ge.mark.sparemployee.services

import android.app.job.JobParameters
import android.app.job.JobService
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.google.gson.JsonObject
import ge.mark.sparemployee.helpers.DbHelper
import ge.mark.sparemployee.helpers.SysHelper
import ge.mark.sparemployee.models.User
import ge.mark.sparemployee.network.ApiClient
import kotlinx.coroutines.Runnable
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SparJobService : JobService() {

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
//                        ping()
//                    }, 20000)

                    Thread.sleep(30000)
                    ping()
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
                try {
                    Thread.sleep(30000)
                    getUsers()
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
            } while (!jobCancelled)
            Log.i(TAG, "Job finished")
            jobFinished(params, false)
        }).start()
    }

    private fun getUsers() {
        val sysHelper = SysHelper(context = applicationContext)
        val dbHelper = DbHelper(context = applicationContext)
        val getUsersCall =
            ApiClient.getService()?.getUsers(CONTROLLER_CODE = sysHelper.getDeviceID())
        getUsersCall?.enqueue(object : Callback<List<User>> {
            override fun onResponse(
                call: Call<List<User>>,
                response: Response<List<User>>
            ) {
                dbHelper.deleteAllWorkers()
                val res = response.body()
                res?.forEach {
                    dbHelper.insertUser(User(first_name = it.first_name, last_name = it.last_name, pass_code = it.pass_code))
                }
                Log.v("retrofit", "call failed")
            }

            override fun onFailure(call: Call<List<User>>, t: Throwable) {
                Log.v("retrofit", "call failed")
            }

        })
    }

    private fun ping() {
        val sysHelper = SysHelper(context = applicationContext)
        val pingCall = ApiClient.getService()
            ?.ping(
                CONTROLLER_CODE = sysHelper.getDeviceID(),
                CONTROLLER_MODEL = "Tablet",
                CONTROLLER_TIME = sysHelper.getDateTimeNow()
            )

        pingCall?.enqueue(object : Callback<JsonObject> {
            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                Log.v("retrofit", "call failed")
            }

            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                val resCode: Int? = response.code()
                val apiResponse: JsonObject? = response.body()
                Log.v("retrofit", "call success")
            }
        })
    }

    companion object {
        private const val TAG = "JobService"
    }
}
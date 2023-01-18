package ge.mark.sparemployee.network.calls

import android.content.Context
import android.os.Environment
import android.util.Log
import com.google.gson.JsonObject
import ge.mark.sparemployee.helpers.DbHelper
import ge.mark.sparemployee.helpers.SysHelper
import ge.mark.sparemployee.models.User
import ge.mark.sparemployee.network.ApiClient
import ge.mark.sparemployee.presentations.MainActivity
import ge.mark.sparemployee.presentations.SparApplication
import ge.mark.sparemployee.utils.NetworkUtil
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

object ApiCalls {

    fun getUsers(context: Context) {
        if (NetworkUtil.isNetworkAvailable(context = context)) {
            val sysHelper = SysHelper(context = context)
            val dbHelper = DbHelper(context = context)
            val getUsersCall =
                ApiClient.getService()?.getUsers(CONTROLLER_CODE = sysHelper.getDeviceID())
            getUsersCall?.enqueue(object : Callback<List<User>> {
                override fun onResponse(
                    call: Call<List<User>>,
                    response: Response<List<User>>
                ) {
                    if(response.code() == 200){
                        val res = response.body()
                        if (res != null) {
                            if(res.isNotEmpty()) {
                                dbHelper.deleteAllUsers()
                                res.forEach {
                                    dbHelper.insertUser(
                                        User(
                                            first_name = it.first_name,
                                            last_name = it.last_name,
                                            pass_code = it.pass_code
                                        )
                                    )
                                }
                            }
                        }
                        Log.v("retrofit", "call failed")
                    }
                }

                override fun onFailure(call: Call<List<User>>, t: Throwable) {
                    Log.v("retrofit", "call failed")
                }

            })
        }
    }

    fun sendDataToServer(
        context: Context,
        controller_code: String,
        pin: String,
        datetime: String,
        picture: String,
        hash: String,
        photo_name: String
    ) {
        if (NetworkUtil.isNetworkAvailable(context = context)) {
            val dbHelper = DbHelper(context = context)
            val workerCall = ApiClient.getService()
                ?.sendWorkerStatus(
                    CONTROLLER_CODE = controller_code,
                    PIN = pin,
                    DATETIME = datetime,
                    PICTURE = picture,
                    HASH = hash,
                    PHOTO_NAME = photo_name
                )
            workerCall?.enqueue(object : Callback<JsonObject> {
                override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                    Log.v("retrofit", "call failed")
                }

                override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                    val resCode: Int? = response.code()
                    val apiResponse: JsonObject? = response.body()
                    dbHelper.deleteWorker(hash)
                    Log.v("retrofit", "call success")
                }
            })
        }
    }

}
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
                    dbHelper.deleteAllUsers()
                    val res = response.body()
                    res?.forEach {
                        dbHelper.insertUser(
                            User(
                                first_name = it.first_name,
                                last_name = it.last_name,
                                pass_code = it.pass_code
                            )
                        )
                    }
                    Log.v("retrofit", "call failed")
                }

                override fun onFailure(call: Call<List<User>>, t: Throwable) {
                    Log.v("retrofit", "call failed")
                }

            })
        }
    }

    fun ping(context: Context) {
        if (NetworkUtil.isNetworkAvailable(context = context)) {
            val sysHelper = SysHelper(context = context)
            val pingCall = ApiClient.getService()
                ?.ping(
                    CONTROLLER_CODE = sysHelper.getDeviceID(),
                    CONTROLLER_MODEL = "Tablet",
                    CONTROLLER_TIME = sysHelper.getDateTimeNow()
                )

            pingCall?.enqueue(object : Callback<JsonObject> {
                override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                    Log.v("retrofit", "call failed")
                    SparApplication.pingState = false
                }

                override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                    val resCode = response.code()
                    SparApplication.pingState = true
                    if (resCode == 400) {
                        SparApplication.pingState = false
                    }
                    val apiResponse: JsonObject? = response.body()
                    Log.v("retrofit", "call success")
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
                    val filePathImg =
                        Environment.getExternalStoragePublicDirectory("Pictures/Spar").toString()
                    val file = File("$filePathImg/$photo_name")
                    file.delete()
                    Log.v("retrofit", "call success")
                }
            })
        }
    }

}
package ge.mark.sparemployee.network.services

import com.google.gson.JsonObject
import ge.mark.sparemployee.models.User
import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST
import retrofit2.http.Query

interface ApiServices {
    @FormUrlEncoded
    @POST("api/Device/InsertDeviceData")
    fun ping(
        @Field("CONTROLLER_CODE") CONTROLLER_CODE: String?,
        @Field("CONTROLER_MODEL") CONTROLLER_MODEL: String?,
        @Field("CONTROLER_TIME") CONTROLLER_TIME: String?
    ): Call<JsonObject>

    @FormUrlEncoded
    @POST("api/Device/GetPins/")
    fun getUsers(
        @Field("CONTROLLER_CODE") CONTROLLER_CODE: String?
    ): Call<List<User>>
}

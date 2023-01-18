package ge.mark.sparemployee.helpers

import android.R.attr.data
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.provider.Settings
import androidx.camera.core.ImageCapture
import java.io.*
import java.math.BigInteger
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.*


class SysHelper(private var context: Context) {

    @SuppressLint("HardwareIds")
    fun getDeviceID(): String {
        return Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
    }

    @SuppressLint("SimpleDateFormat")
    fun getDateTimeNow(format: String = "yyyy-MM-dd HH:mm:ss"): String {
        return SimpleDateFormat(format).format(Date())
    }

    fun fileToBase64(op: ImageCapture.OutputFileResults): String {
        val imgStream: InputStream?
        val bitmap: Bitmap
        try {
            imgStream  = context.contentResolver.openInputStream(op.savedUri!!)
            bitmap = BitmapFactory.decodeStream(imgStream)
        } catch (np: NullPointerException) {
            return ""
        }

        var encodedImage = ""
        try {
            val baos = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 15, baos) // bm is the bitmap object
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                encodedImage = Base64.getEncoder().encodeToString(baos.toByteArray())
            }
        } catch (e: FileNotFoundException) {
            println("File not found$e")
        } catch (ioe: IOException) {
            println("Exception while reading the file $ioe")
        }
        return encodedImage
    }

    fun stringToMD5(str: String): String {
        val md = MessageDigest.getInstance("MD5")
        return BigInteger(1, md.digest(str.toByteArray())).toString(16).padStart(32, '0')
    }
}
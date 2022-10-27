package ge.mark.sparemployee.helpers

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.provider.Settings
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.IOException
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

    fun fileToBase64(filePath: String?): String? {
        var base64File: String? = ""
        val file = File(filePath!!)
        try {
            FileInputStream(file).use { imageInFile ->
                // Reading a file from file system
                val fileData = ByteArray(file.length().toInt())
                imageInFile.read(fileData)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    base64File = Base64.getEncoder().encodeToString(fileData)
                }
            }
        } catch (e: FileNotFoundException) {
            println("File not found$e")
        } catch (ioe: IOException) {
            println("Exception while reading the file $ioe")
        }
        return base64File
    }

    fun stringToMD5(str: String): String {
        val md = MessageDigest.getInstance("MD5")
        return BigInteger(1, md.digest(str.toByteArray())).toString(16).padStart(32, '0')
    }
}
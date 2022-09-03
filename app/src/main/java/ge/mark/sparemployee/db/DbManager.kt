package ge.mark.sparemployee.db

import ge.mark.sparemployee.db.DbHelper
import android.database.sqlite.SQLiteDatabase
import android.content.ContentValues
import android.annotation.SuppressLint
import android.content.Context
import ge.mark.sparemployee.db.models.Worker
import java.util.ArrayList

class DbManager(context: Context?) {
    private val dbHelper: DbHelper
    private var db: SQLiteDatabase? = null
    fun openDb() {
        db = dbHelper.writableDatabase
    }

    fun insertToDb(employee_code: String?, photo_path: String?, forwarded: String?) {
        val cv = ContentValues()
        cv.put(Constants.EMPLOYEE_CODE, employee_code)
        cv.put(Constants.PHOTO_PATH, photo_path)
        cv.put(Constants.FORWARDED, forwarded)
        db!!.insert(Constants.TABLE_NAME, null, cv)
    }

    val fromDb: List<Worker>
        get() {
            val worker = Worker()
            val tempList: MutableList<Worker> = ArrayList()
            val cursor = db!!.query(
                Constants.TABLE_NAME,
                null,
                null,
                null,
                null,
                null,
                null
            )
            while (cursor.moveToNext()) {
                @SuppressLint("Range") val employeeCode = cursor.getString(
                    cursor.getColumnIndex(
                        Constants.EMPLOYEE_CODE
                    )
                )
                @SuppressLint("Range") val photoPath = cursor.getString(
                    cursor.getColumnIndex(
                        Constants.PHOTO_PATH
                    )
                )
                @SuppressLint("Range") val forwarded = cursor.getString(
                    cursor.getColumnIndex(
                        Constants.FORWARDED
                    )
                )
                worker.code = employeeCode
                worker.photoPath = photoPath
                worker.forwarded = forwarded
                tempList.add(worker)
            }
            cursor.close()
            return tempList
        }

    fun deleteFromDb(employeeCode: String): Boolean {
        return db!!.delete(
            Constants.TABLE_NAME,
            Constants.EMPLOYEE_CODE + "=" + employeeCode,
            null
        ) > 0
    }

    fun closeDb() {
        dbHelper.close()
    }

    init {
        dbHelper = DbHelper(context)
    }
}
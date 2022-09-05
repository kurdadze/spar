package ge.mark.sparemployee.helpers

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import ge.mark.sparemployee.models.User
import ge.mark.sparemployee.models.Worker

class DbHelper(context: Context) :

    SQLiteOpenHelper(context, DB_NAME, null, DB_VERSION) {

    companion object {
        private const val DB_VERSION = 2
        private const val DB_NAME = "spar.db"
        private const val TABLE_NAME = "spar_table"
        private const val TABLE_USERS = "spar_users_table"

        // Table fields
        private const val ID = "id"
        private const val CODE = "code"
        private const val PHOTO_PATH = "photo_path"
        private const val PHOTO = "photo"
        private const val DATE_TIME = "date_time"
        private const val FORWARDED = "forwarded"

        // Users table fields
        private const val FIRST_NAME = "first_name"
        private const val LAST_NAME = "last_name"
        private const val PASS_CODE = "pass_code"

        private const val TABLE_STRUCTURE = ("CREATE TABLE IF NOT EXISTS " +
                TABLE_NAME + " ("
                + ID + " INTEGER PRIMARY KEY, "
                + CODE + " TEXT,"
                + PHOTO_PATH + " TEXT,"
                + PHOTO + " TEXT,"
                + DATE_TIME + " TEXT,"
                + FORWARDED + " TEXT)"
                )
        private const val DROP_TABLE = "DROP TABLE IF EXISTS $TABLE_NAME"

        private const val USERS_TABLE_STRUCTURE = ("CREATE TABLE IF NOT EXISTS " +
                TABLE_USERS + " ("
                + FIRST_NAME + " TEXT,"
                + LAST_NAME + " TEXT,"
                + PASS_CODE + " TEXT)"
                )
        private const val DROP_USERS_TABLE = "DROP TABLE IF EXISTS $TABLE_USERS"
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(TABLE_STRUCTURE)
        db.execSQL(USERS_TABLE_STRUCTURE)
    }

    override fun onUpgrade(db: SQLiteDatabase, i: Int, i1: Int) {
        db.execSQL(DROP_TABLE)
        db.execSQL(DROP_USERS_TABLE)
        onCreate(db)
    }

    fun insertUser(user: User): Long {

        val db = this.writableDatabase

        val cv = ContentValues()
        cv.put(FIRST_NAME, user.first_name)
        cv.put(LAST_NAME, user.last_name)
        cv.put(PASS_CODE, user.pass_code)

        val success = db.insert(TABLE_USERS, null, cv)
        db.close()
        return success
    }

    @SuppressLint("Range", "Recycle")
    fun checkUsers(pass_code: String): User {
        val db = this.writableDatabase
        val selectQuery = "SELECT * FROM $TABLE_USERS WHERE pass_code ='$pass_code'"
        var cursor: Cursor? = null
        var usr: User? = null
        val first_name: String
        val last_name: String
        val pass_code: String
        try {
            cursor = db.rawQuery(selectQuery, null)
            if (cursor.getCount() > 0) {
                cursor.moveToFirst()
                first_name = cursor.getString(cursor.getColumnIndex("first_name"))
                last_name = cursor.getString(cursor.getColumnIndex("last_name"))
                pass_code = cursor.getString(cursor.getColumnIndex("pass_code"))

                usr = User(
                    first_name = first_name,
                    last_name = last_name,
                    pass_code = pass_code
                )
            }
            return usr!!
        } catch (e: Exception) {
            return User()
        } finally {
            cursor!!.close()
        }
    }

    @SuppressLint("Range")
    fun getAllWorker(): ArrayList<Worker> {
        val wrkList: ArrayList<Worker> = ArrayList()
        val selectQuery = "SELECT * FROM $TABLE_NAME"
        val db = this.writableDatabase

        val cursor: Cursor?

        try {
            cursor = db.rawQuery(selectQuery, null)
        } catch (e: Exception) {
            e.printStackTrace()
            db.execSQL(selectQuery)
            return ArrayList()
        }

        var id: Long
        var code: String
        var photo_path: String
        var photo: String
        var date_time: String
        var forwarded: String

        if (cursor.moveToFirst()) {
            do {
                id = cursor.getLong(cursor.getColumnIndex("id"))
                code = cursor.getString(cursor.getColumnIndex("code"))
                photo_path = cursor.getString(cursor.getColumnIndex("photo_path"))
                photo = cursor.getString(cursor.getColumnIndex("photo"))
                date_time = cursor.getString(cursor.getColumnIndex("date_time"))
                forwarded = cursor.getString(cursor.getColumnIndex("forwarded"))

                val wrk = Worker(
                    id = id,
                    code = code,
                    photo_path = photo_path,
                    photo = photo,
                    date_time = date_time,
                    forwarded = forwarded
                )
                wrkList.add(wrk)
            } while (cursor.moveToNext())
        }
        return wrkList
    }

    fun insertWorker(worker: Worker): Long {

        val db = this.writableDatabase

        val cv = ContentValues()
        cv.put(ID, worker.id)
        cv.put(CODE, worker.code)
        cv.put(PHOTO_PATH, worker.photo_path)
        cv.put(PHOTO, worker.photo)
        cv.put(DATE_TIME, worker.date_time)
        cv.put(FORWARDED, worker.forwarded)

        val success = db.insert(TABLE_NAME, null, cv)
        db.close()
        return success
    }

    fun deleteWorker(photo: String) {
        val db = this.writableDatabase

        val cv = ContentValues()
        cv.put(PHOTO, photo)
        db.execSQL("DELETE FROM $TABLE_NAME WHERE photo ='$photo'")
        db.close()
    }
}
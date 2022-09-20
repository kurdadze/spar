package ge.mark.sparemployee.helpers

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import ge.mark.sparemployee.models.User
import ge.mark.sparemployee.models.Worker

class DbHelper(context: Context) : SQLiteOpenHelper(context, DB_NAME, null, DB_VERSION) {

    companion object {
        private const val DB_VERSION = 121
        private const val DB_NAME = "spar.db"
        private const val TABLE_NAME = "spar_table"
        private const val TABLE_USERS = "spar_users_table"

        // Table fields
        private const val CONTROLLER_CODE = "controller_code"
        private const val PIN = "pin"
        private const val DATETIME = "datetime"
        private const val PICTURE = "picture"
        private const val HASH = "hash"
        private const val PHOTO_NAME = "photo_name"

        // Users table fields
        private const val FIRST_NAME = "first_name"
        private const val LAST_NAME = "last_name"
        private const val PASS_CODE = "pass_code"

        private const val TABLE_STRUCTURE = ("CREATE TABLE IF NOT EXISTS " +
                TABLE_NAME + " ("
                + CONTROLLER_CODE + " TEXT, "
                + PIN + " TEXT,"
                + DATETIME + " TEXT,"
                + PICTURE + " TEXT,"
                + HASH + " TEXT,"
                + PHOTO_NAME + " TEXT)"
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
            if (cursor.count > 0) {
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

        var controller_code: String
        var pin: String
        var datetime: String
        var picture: String
        var hash: String
        var photo_name: String

        if (cursor.moveToFirst()) {
            do {
                controller_code = cursor.getString(cursor.getColumnIndex("controller_code"))
                pin = cursor.getString(cursor.getColumnIndex("pin"))
                datetime = cursor.getString(cursor.getColumnIndex("datetime"))
                picture = cursor.getString(cursor.getColumnIndex("picture"))
                hash = cursor.getString(cursor.getColumnIndex("hash"))
                photo_name = cursor.getString(cursor.getColumnIndex("photo_name"))

                val wrk = Worker(
                    controller_code = controller_code,
                    pin = pin,
                    datetime = datetime,
                    picture = picture,
                    hash = hash,
                    photo_name = photo_name
                )
                wrkList.add(wrk)
                cursor.moveToNext()
            } while (!cursor.isAfterLast)
        }
        cursor.close()
        db.close()
        return wrkList
    }

    fun insertWorker(worker: Worker): Boolean {

        val db = this.writableDatabase
        db.beginTransaction()
        try {
            val cv = ContentValues()
            cv.put(CONTROLLER_CODE, worker.controller_code)
            cv.put(PIN, worker.pin)
            cv.put(DATETIME, worker.datetime)
            cv.put(PICTURE, worker.picture)
            cv.put(HASH, worker.hash)
            cv.put(PHOTO_NAME, worker.photo_name)

            val result = db.insert(TABLE_NAME, null, cv)

            return if (result.equals(0)) {
                false;
            } else {
                db.setTransactionSuccessful();
                true;
            }
        } finally {
            db.endTransaction()
            db.close()
        }
    }

    fun deleteWorker(hash: String) {
        val db = this.writableDatabase

        val cv = ContentValues()
        cv.put(HASH, hash)
        db.execSQL("DELETE FROM $TABLE_NAME WHERE hash ='$hash'")
        db.close()
    }

    fun deleteAllWorkers() {
        val db = this.writableDatabase

        db.execSQL("DELETE FROM $TABLE_NAME")
        db.close()
    }

    fun deleteAllUsers() {
        val db = this.writableDatabase

        db.execSQL("DELETE FROM $TABLE_USERS")
        db.close()
    }
}
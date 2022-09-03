package ge.mark.sparemployee.db

object Constants {
    const val TABLE_NAME = "spar_table"
    const val _ID = "_id"
    const val EMPLOYEE_CODE = "employee_code"
    const val PHOTO_PATH = "photo_path"
    const val FORWARDED = "forwarded"
    const val DB_NAME = "spar.db"
    const val DB_VERSION = 1
    const val TABLE_STRUCTURE = ("CREATE TABLE IF NOT EXISTS " +
            TABLE_NAME + " ("
            + _ID + " INTEGER PRIMARY KEY, "
            + EMPLOYEE_CODE + " TEXT,"
            + PHOTO_PATH + " TEXT,"
            + FORWARDED + " TEXT)")
    const val DROP_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME
}
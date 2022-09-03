package ge.mark.sparemployee.db;

public class Constants {
    public static final String TABLE_NAME = "spar_table";
    public static final String _ID = "_id";
    public static final String EMPLOYEE_CODE = "employee_code";
    public static final String PHOTO_PATH = "photo_path";
    public static final String FORWARDED = "forwarded";
    public static final String DB_NAME = "spar.db";
    public static final int DB_VERSION = 1;
    public static final String TABLE_STRUCTURE = "CREATE TABLE IF NOT EXISTS " +
            TABLE_NAME + " ("
            + _ID + " INTEGER PRIMARY KEY, "
            + EMPLOYEE_CODE + " TEXT,"
            + PHOTO_PATH + " TEXT,"
            + FORWARDED + " TEXT)";
    public static final String DROP_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;
}

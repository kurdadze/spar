package ge.mark.sparemployee.db;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

import ge.mark.sparemployee.db.models.Worker;

public class DbManager {



    private Context context;
    private DbHelper dbHelper;
    private SQLiteDatabase db;

    public DbManager(Context context) {
        this.context = context;
        dbHelper = new DbHelper(context);
    }

    public void openDb(){
        db = dbHelper.getWritableDatabase();
    }

    public void insertToDb(String employee_code, String photo_path, String forwarded){

        ContentValues cv = new ContentValues();
        cv.put(Constants.EMPLOYEE_CODE, employee_code);
        cv.put(Constants.PHOTO_PATH, photo_path);
        cv.put(Constants.FORWARDED, forwarded);
        db.insert(Constants.TABLE_NAME, null, cv);
    }

    public List<Worker> getFromDb(){

        Worker worker = new Worker();
        List<Worker> tempList = new ArrayList<>();
        Cursor cursor = db.query(
          Constants.TABLE_NAME,
                null,
                null,
                null,
                null,
                null,
                null
                );
        while (cursor.moveToNext()){
            @SuppressLint("Range")
            String employeeCode = cursor.getString(cursor.getColumnIndex(Constants.EMPLOYEE_CODE));
            @SuppressLint("Range")
            String photoPath = cursor.getString(cursor.getColumnIndex(Constants.PHOTO_PATH));
            @SuppressLint("Range")
            String forwarded = cursor.getString(cursor.getColumnIndex(Constants.FORWARDED));

            worker.setCode(employeeCode);
            worker.setPhotoPath(photoPath);
            worker.setForwarded(forwarded);
            tempList.add(worker);
        }
        cursor.close();
        return tempList;
    }

    public boolean deleteFromDb(String employeeCode){
            return db.delete(Constants.TABLE_NAME, Constants.EMPLOYEE_CODE + "=" + employeeCode,null)>0;
    }

    public void closeDb(){
        dbHelper.close();
    }
}

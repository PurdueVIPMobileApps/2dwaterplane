package com.precisionag.lib;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by steve on 6/3/13.
 */
public class DatabaseHandler extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "demManager";
    private static final String TABLE_NAME = "dems";

    private static final String KEY_ID = "id";
    private static final String KEY_SW_LAT = "sw_lat";
    private static final String KEY_SW_LONG = "sw_long";
    private static final String KEY_NE_LAT = "ne_lat";
    private static final String KEY_NE_LONG = "ne_long";
    private static final String KEY_FILENAME = "filename";
    private static final String KEY_TIMESTAMP = "timestamp";

    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + "("
                + KEY_ID + " INTEGER PRIMARY KEY,"
                + KEY_SW_LAT + " REAL,"
                + KEY_SW_LONG + " REAL,"
                + KEY_NE_LAT + " REAL,"
                + KEY_NE_LONG + " REAL,"
                + KEY_FILENAME + " TEXT,"
                + KEY_TIMESTAMP + " TEXT"
                + ")";
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public void addDem(Dem dem) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_SW_LAT, dem.getSw_lat());
        values.put(KEY_SW_LONG, dem.getSw_long());
        values.put(KEY_NE_LAT, dem.getNe_lat());
        values.put(KEY_NE_LONG, dem.getNe_long());
        values.put(KEY_FILENAME, dem.getFilename());
        values.put(KEY_TIMESTAMP, dem.getTimestamp());

        db.insert(TABLE_NAME, null, values);
        db.close();
    }

    public Dem getDem(int id) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_NAME, new String[] {KEY_ID, KEY_SW_LAT, KEY_SW_LONG,
                KEY_NE_LAT, KEY_NE_LONG, KEY_FILENAME, KEY_TIMESTAMP}, KEY_ID + "=?",
                new String[] {String.valueOf(id)}, null, null, null, null);

        if (cursor != null)
            cursor.moveToFirst();

        Dem dem = new Dem(Integer.parseInt(cursor.getString(0)),
                Float.parseFloat(cursor.getString(1)),
                Float.parseFloat(cursor.getString(2)),
                Float.parseFloat(cursor.getString(3)),
                Float.parseFloat(cursor.getString(4)),
                cursor.getString(5),
                cursor.getString(6)
                );

        return dem;
    }

    public List<Dem> getAllDems() {
        List<Dem> demList = new ArrayList<Dem>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_NAME;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                Dem dem = new Dem(Integer.parseInt(cursor.getString(0)),
                        Float.parseFloat(cursor.getString(1)),
                        Float.parseFloat(cursor.getString(2)),
                        Float.parseFloat(cursor.getString(3)),
                        Float.parseFloat(cursor.getString(4)),
                        cursor.getString(5),
                        cursor.getString(6)
                );

                demList.add(dem);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();

        return demList;
    }

    public int getDemCount() {
        String countQuery = "SELECT  * FROM " + TABLE_NAME;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        int count = cursor.getCount();
        cursor.close();
        db.close();
        return count;
    }

    public int updateDem(Dem dem) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_SW_LAT, dem.getSw_lat());
        values.put(KEY_SW_LONG, dem.getSw_long());
        values.put(KEY_NE_LAT, dem.getNe_lat());
        values.put(KEY_NE_LONG, dem.getNe_long());
        values.put(KEY_FILENAME, dem.getFilename());
        values.put(KEY_TIMESTAMP, dem.getTimestamp());

        // updating row
        int result = db.update(TABLE_NAME, values, KEY_ID + " = ?",
                new String[] { String.valueOf(dem.getId()) });
        db.close();
        return result;
    }

    public void deleteDem(Dem dem) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NAME, KEY_ID + " = ?",
                new String[] { String.valueOf(dem.getId()) });
        db.close();
    }
}

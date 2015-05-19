package com.rmathur.cumtd.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.rmathur.cumtd.data.model.Stop;

import java.util.ArrayList;

public class StopDataSource {

    // Database fields
    private SQLiteDatabase database;
    private MySQLiteHelper dbHelper;
    private String[] allColumns = {MySQLiteHelper.COLUMN_ID, MySQLiteHelper.COLUMN_STOPID, MySQLiteHelper.COLUMN_STOPNAME};

    public StopDataSource(Context context) {
        dbHelper = new MySQLiteHelper(context);
    }

    public void open() throws SQLException {
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    public Stop createStop(String stopid, String stopname) {
        ContentValues values = new ContentValues();
        values.put(MySQLiteHelper.COLUMN_STOPID, stopid);
        values.put(MySQLiteHelper.COLUMN_STOPNAME, stopname);
        long insertId = database.insert(MySQLiteHelper.TABLE_STOPS, null,
                values);
        Cursor cursor = database.query(MySQLiteHelper.TABLE_STOPS,
                allColumns, MySQLiteHelper.COLUMN_ID + " = " + insertId, null,
                null, null, null);
        cursor.moveToFirst();
        Stop newStop = cursorToStop(cursor);
        cursor.close();
        return newStop;
    }

    public void deleteStop(Stop stop) {
        long id = stop.getId();
        database.delete(MySQLiteHelper.TABLE_STOPS, MySQLiteHelper.COLUMN_ID + " = " + id, null);
    }

    public void findStopByName(String stopName) {
        Cursor cursor = database.query(MySQLiteHelper.TABLE_STOPS, allColumns, null, null, null, null, null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            Stop stop = cursorToStop(cursor);
            if (stop.getStopName().equals(stopName)) {
                deleteStop(stop);
            }
            cursor.moveToNext();
        }
        cursor.close();
    }

    public ArrayList<Stop> getAllStops() {
        ArrayList<Stop> stops = new ArrayList<Stop>();

        Cursor cursor = database.query(MySQLiteHelper.TABLE_STOPS, allColumns, null, null, null, null, null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            Stop stop = cursorToStop(cursor);
            stops.add(stop);
            cursor.moveToNext();
        }
        cursor.close();
        return stops;
    }

    public boolean stopExists(String stopName) {
        boolean stopFound = false;
        Cursor cursor = database.query(MySQLiteHelper.TABLE_STOPS, allColumns, null, null, null, null, null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            Stop currStop = cursorToStop(cursor);
            if (currStop.getStopName().equals(stopName)) {
                stopFound = true;
            }
            cursor.moveToNext();
        }
        cursor.close();
        return stopFound;
    }

    private Stop cursorToStop(Cursor cursor) {
        Stop stop = new Stop();
        stop.setId(cursor.getLong(0));
        stop.setStopId(cursor.getString(1));
        stop.setStopName(cursor.getString(2));
        return stop;
    }
} 
package com.face_chtj.base_iotutils.download;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class DownloadDatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "downloads.db";
    private static final int DATABASE_VERSION = 1;

    public static final String TABLE_DOWNLOADS = "downloads";
    public static final String COLUMN_URL = "url";
    public static final String COLUMN_FILE_PATH = "file_path";
    public static final String COLUMN_PROGRESS = "progress";
    public static final String COLUMN_IS_PAUSED = "is_paused";

    public DownloadDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + TABLE_DOWNLOADS + " (" +
                COLUMN_URL + " TEXT PRIMARY KEY, " +
                COLUMN_FILE_PATH + " TEXT, " +
                COLUMN_PROGRESS + " INTEGER, " +
                COLUMN_IS_PAUSED + " INTEGER)";
        db.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_DOWNLOADS);
        onCreate(db);
    }

    public void addDownloadTask(DownloadTask task) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_URL, task.getUrl());
        values.put(COLUMN_FILE_PATH, task.getFilePath());
        values.put(COLUMN_PROGRESS, task.getProgress());
        values.put(COLUMN_IS_PAUSED, task.isPaused() ? 1 : 0);
        db.insertWithOnConflict(TABLE_DOWNLOADS, null, values, SQLiteDatabase.CONFLICT_REPLACE);
        db.close();
    }

    public List<DownloadTask> getAllDownloadTasks() {
        List<DownloadTask> tasks = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_DOWNLOADS, null);
        if (cursor.moveToFirst()) {
            do {
                String url = cursor.getString(cursor.getColumnIndex(COLUMN_URL));
                String filePath = cursor.getString(cursor.getColumnIndex(COLUMN_FILE_PATH));
                int progress = cursor.getInt(cursor.getColumnIndex(COLUMN_PROGRESS));
                boolean isPaused = cursor.getInt(cursor.getColumnIndex(COLUMN_IS_PAUSED)) == 1;
                tasks.add(new DownloadTask(url, filePath, progress, isPaused));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return tasks;
    }

    public void deleteDownloadTask(String url) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_DOWNLOADS, COLUMN_URL + " = ?", new String[]{url});
        db.close();
    }
}


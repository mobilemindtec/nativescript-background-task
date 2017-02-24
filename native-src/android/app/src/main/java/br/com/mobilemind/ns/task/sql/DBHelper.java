package br.com.mobilemind.ns.task.sql;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {

    public DBHelper(Context context, String dbName, int dbVersion)
    {
        super(context, dbName , null, dbVersion);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    public Cursor getData(String query){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor =  db.rawQuery(query, null);
        return cursor;
    }

    public Long getDataId(String query, String[] args){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor =  db.rawQuery(query, args);
        
        try{        

            if (cursor.moveToNext())
                return cursor.getLong(0);

        }finally{
            cursor.close();
        }

        return null;
    }

    public void executeQuery(String query, Object[] args, SQLiteDatabase db){
        db.execSQL(query, args);
    }
}
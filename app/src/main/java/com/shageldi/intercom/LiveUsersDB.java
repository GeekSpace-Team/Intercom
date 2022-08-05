package com.shageldi.intercom;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class LiveUsersDB extends SQLiteOpenHelper {
    private static final String DBNAME="liveUsers";
    private static final String TBNAME="liveU";
    public LiveUsersDB(@Nullable Context context) {
        super(context, DBNAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL("CREATE TABLE "+TBNAME+" (ID INTEGER PRIMARY KEY AUTOINCREMENT,name TEXT,ip TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS "+TBNAME);
        onCreate(sqLiteDatabase);
    }

    public boolean insertData(String name,String ip){
        SQLiteDatabase db=this.getWritableDatabase();
        ContentValues values=new ContentValues();
        values.put("name",name);
        values.put("ip",ip);

        long result=db.insert(TBNAME,null,values);
        if(result==-1){
            return false;
        } else{
            return true;
        }
    }

    public Cursor getAll(){
        SQLiteDatabase db=this.getWritableDatabase();
        Cursor cursor=db.rawQuery("SELECT * FROM "+TBNAME, null);
        return cursor;
    }

    public Cursor getSelect(String ip){
        SQLiteDatabase db=this.getWritableDatabase();
        Cursor cursor=db.rawQuery("SELECT * FROM "+TBNAME+" WHERE ip = '"+ip+"'", null);
        return cursor;
    }

    public boolean updateData(String name,String ip){
        SQLiteDatabase db=this.getWritableDatabase();
        ContentValues values=new ContentValues();
        values.put("name",name);
        values.put("ip",ip);
        db.update(TBNAME,values,"ip=?",new String[]{ip});
        return true;
    }



    public Integer deleteData(String ip){
        SQLiteDatabase db=this.getWritableDatabase();
        return db.delete(TBNAME,"ip=?",new String[]{ip});

    }

    public void truncate(){
        SQLiteDatabase db=this.getWritableDatabase();
        db.execSQL("DROP TABLE IF EXISTS "+TBNAME);
        onCreate(db);
    }
}

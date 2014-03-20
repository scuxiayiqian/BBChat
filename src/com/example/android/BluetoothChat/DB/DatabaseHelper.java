package com.example.android.BluetoothChat.DB;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase.CursorFactory;


public class DatabaseHelper extends SQLiteOpenHelper {
    //类没有实例化,是不能用作父类构造器的参数,必须声明为静态
    private static final String name = "history.db"; //数据库名称
    private static final int version = 1; //数据库版本
    public DatabaseHelper(Context context) {
        //第三个参数CursorFactory指定在执行查询时获得一个游标实例的工厂类,设置为null,代表使用系统默认的工厂类
        super(context, name, null, version);
        
    }
    
    public DatabaseHelper(Context context,String name,CursorFactory factory,int version){
		super(context,name,factory,version);
	}
    @Override 
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE if not exists history(id integer primary key autoincrement, mac varchar(50), text varchar(500), date varchar(20), fromto integer)");
        //db.execSQL("CREATE TABLE IF NOT EXISTS myPhoto (id integer primary key autoincrement, add varchar(100))");
        db.execSQL("CREATE TABLE if not exists config(id integer primary key, flag integer)");
    }
    @Override 
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		onCreate(db);
    }
    
    
}

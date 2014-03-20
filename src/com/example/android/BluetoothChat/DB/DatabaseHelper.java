package com.example.android.BluetoothChat.DB;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase.CursorFactory;


public class DatabaseHelper extends SQLiteOpenHelper {
    //��û��ʵ����,�ǲ����������๹�����Ĳ���,��������Ϊ��̬
    private static final String name = "history.db"; //���ݿ�����
    private static final int version = 1; //���ݿ�汾
    public DatabaseHelper(Context context) {
        //����������CursorFactoryָ����ִ�в�ѯʱ���һ���α�ʵ���Ĺ�����,����Ϊnull,����ʹ��ϵͳĬ�ϵĹ�����
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

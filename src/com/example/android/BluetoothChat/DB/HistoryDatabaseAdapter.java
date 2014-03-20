package com.example.android.BluetoothChat.DB;

import java.io.IOException;
import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

public class HistoryDatabaseAdapter {
	private SQLiteDatabase db;
	private final Context context;
	private DatabaseHelper dbOpenHelper;
	
	private ArrayList<History> ConvertToHistory(Cursor cursor){
		int resultCounts=cursor.getCount();
		if(resultCounts==0||!cursor.moveToFirst()){
			return null;
		}
		ArrayList<History> h=new ArrayList<History>();
		//History[] h=new History[resultCounts];
		for(int i=0;i<resultCounts;i++){
			History temp=new History();
			temp.setId(cursor.getInt(0));
			temp.setMac(cursor.getString(cursor.getColumnIndex("mac")));
			temp.setText(cursor.getString(cursor.getColumnIndex("text")));
			temp.setDate(cursor.getString(cursor.getColumnIndex("date")));
			temp.setFrom(cursor.getInt(cursor.getColumnIndex("fromto")));
			h.add(temp);
			
			cursor.moveToNext();
		}
		return h;
		
	}
	


	public HistoryDatabaseAdapter(Context _context){
		context=_context;
		dbOpenHelper=new DatabaseHelper(context);
	}

	public void open() throws SQLiteException{
		try{
			db=dbOpenHelper.getWritableDatabase();
		}catch(SQLiteException ex){
			db=dbOpenHelper.getReadableDatabase();
		}
	}
	
	public void close(){
		if(db!=null){
			db.close();
			db=null;
		}
	}
	
	public long insert(History h){
		ContentValues newValues=new ContentValues();
		
		//newValues.put("id",h.getId());
		newValues.put("mac", h.getMac());
		newValues.put("text", h.getText());
		newValues.put("date", h.getDate());
		newValues.put("fromto", h.getFrom());
		
		return db.insert("history", null, newValues);		
	}
	
	public long deleteAllData(){
		return db.delete("history", null, null);
	}
	
	public long deleteOneData(long id){
		return db.delete("history","id="+id, null);
	}
	
	public ArrayList<History> getOneData(long id){
		Cursor results=db.query("history", new String[]{"id","mac","text","date","fromto"}, "id="+id,null,null,null,null);
		return ConvertToHistory(results); 
	
	}
	
	public ArrayList<History> getAllData(){
		Cursor results=db.query("history", new String[]{"id","mac","text","date","fromto"}, null,null,null,null,null);
		return ConvertToHistory(results); 		
	}
	
	public ArrayList<History> getDataFromMac(String mac){
		Cursor results=db.query("history", new String[]{"id","mac","text","date","fromto"},"mac="+mac,null,null,null,null);
		return ConvertToHistory(results); 		
	}
	
	public int getFlag(){
		Cursor results;
		try {
			results=db.query("config", new String[]{"id","flag"},"id="+"1",null,null,null,null);
		}catch(Exception e){
			return -1;
		}
		if(results.getCount() == 0)
			return -1;
		int x = results.getCount();
		results.moveToFirst();
		return results.getInt(results.getColumnIndex("flag"));
	}
	
	public int setFlag(int flag){
		ContentValues newValues = new ContentValues();
		newValues.put("id", 1);
		newValues.put("flag", flag);
		return db.update("config",newValues, "id="+"1", null);	
	}
	
	public long insertFlag(int flag){
		ContentValues newValues=new ContentValues();
		newValues.put("id", 1);
		newValues.put("flag", flag);
		
		return db.insert("config", null, newValues);	
	}


/*
	public long Modify(long id, History news) {
		ContentValues newValues = new ContentValues();
		newValues.put("title", news.getTitle());
		newValues.put("content", news.getContent());
		newValues.put("date", news.getDate());
		newValues.put("address", news.getAddress());
		if(news.getIsAlarm()==false)
			newValues.put("isAlarm",0);
		else
			newValues.put("isAlarm",1);
		newValues.put("alarmContent", news.getAlarmContent());
		newValues.put("alarmDate", news.getAlarmDate());
		newValues.put("alarmType", news.getAlarmType());
		
		return db.update("history",newValues, "id="+id, null);
	}*/



	
}

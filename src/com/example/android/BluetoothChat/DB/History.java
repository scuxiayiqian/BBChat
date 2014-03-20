package com.example.android.BluetoothChat.DB;

import java.sql.Date;
import java.sql.Time;

public class History {
	private int id;
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	private String mac;
	private String text;
	private String date;
	private int from;
	public int getFrom() {
		return from;
	}
	public void setFrom(int from) {
		this.from = from;
	}
	public String getMac() {
		return mac;
	}
	public void setMac(String mac) {
		this.mac = mac;
	}
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
	public String getDate() {
		return date;
	}
	public void setDate(String date) {
		this.date = date;
	}
	
	public History(int id,String mac, String text, String date,int from) {
		super();
		this.from=from;
		this.id=id;
		this.mac = mac;
		this.text = text;
		this.date = date;
	}
	public History() {
		super();
	}	

}

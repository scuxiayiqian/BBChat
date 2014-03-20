package com.example.android.BluetoothChat;

import java.io.File;
import java.io.IOException;

import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.media.MediaRecorder.OutputFormat;
import android.os.Environment;

public class SmallRecorder {
	private MediaRecorder recorder;
	private MediaPlayer player ;
	
	private File SDPATH;
	private File recordFile;
	public SmallRecorder(MediaRecorder recorder,MediaPlayer player) {
		
		this.recorder = recorder;
		this.player = player;
		if(Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED)){
			//创建源文件存储目录
			SDPATH = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+File.separator+"myRecorder");
			//初始化录音设备
			initRecord();
		}
	}
	public void initRecord() {
		try {
			SDPATH.mkdir();
			//创建可录制文件
			recordFile = File.createTempFile("record", ".amr",SDPATH);
			//设置录音来源为麦克风
			recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
			//设置录制格式为amr
			//工作断点
			recorder.setOutputFormat(OutputFormat.RAW_AMR);
			//设置音频编码格式为默认
			recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
			//设置录制文件路径
			recorder.setOutputFile(recordFile.getAbsolutePath());
			recorder.prepare();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public void delete() {
		recordFile.delete();
	}
	public String getFilePath() {
		return recordFile.getAbsolutePath();
	}
	public void playRecord() {
		try {
			if(!player.isPlaying()) {
				player.setDataSource(getFilePath());
				player.prepare();
				player.start();
				player.setLooping(false);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		} 
	}
}

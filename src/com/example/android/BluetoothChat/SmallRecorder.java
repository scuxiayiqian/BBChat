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
			//����Դ�ļ��洢Ŀ¼
			SDPATH = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+File.separator+"myRecorder");
			//��ʼ��¼���豸
			initRecord();
		}
	}
	public void initRecord() {
		try {
			SDPATH.mkdir();
			//������¼���ļ�
			recordFile = File.createTempFile("record", ".amr",SDPATH);
			//����¼����ԴΪ��˷�
			recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
			//����¼�Ƹ�ʽΪamr
			//�����ϵ�
			recorder.setOutputFormat(OutputFormat.RAW_AMR);
			//������Ƶ�����ʽΪĬ��
			recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
			//����¼���ļ�·��
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

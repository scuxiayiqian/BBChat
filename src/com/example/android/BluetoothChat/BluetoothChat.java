/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.BluetoothChat;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import com.example.android.BluetoothChat.DB.History;
import com.example.android.BluetoothChat.DB.HistoryDatabaseAdapter;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * This is the main Activity that displays the current chat session.
 */
public class BluetoothChat extends Activity {
	// Debugging
	private static final String TAG = "BluetoothChat";
	private static final boolean D = true;

	// Message types sent from the BluetoothChatService Handler
	public static final int MESSAGE_STATE_CHANGE = 1;
	public static final int MESSAGE_READ = 2;
	public static final int MESSAGE_WRITE = 3;
	public static final int MESSAGE_DEVICE_NAME = 4;
	public static final int MESSAGE_TOAST = 5;
	public static final int ICO_READ = 6;
	public static final int FILE_READ = 7;
	public static final int FILE_GET = 8;
	public static final int TITLE_READ = 9;
	public static final int SOUND_READ = 10;
	
	private static File DIR=new File(Environment.getExternalStorageDirectory()+"/TEST/");
	

	private final int IMAGE_CODE = 25;
	private final String IMAGE_TYPE = "image/*";
	private String path;
	private ImageView im;
	private Bitmap bm = null;
	private Bitmap compressBm = null;
	private Bitmap bm3 = null;
	
	//语音部分相关的变量
	private MediaRecorder mr;
	private MediaPlayer player = new MediaPlayer();
	private SmallRecorder recorder;
	private String filePath ;
	private String recordFilePath ;
	private File SDPATH;
	private int first=0,second=0,thrid = 0;

	// Key names received from the BluetoothChatService Handler
	public static final String DEVICE_NAME = "device_name";
	public static final String TOAST = "toast";
	private final static String ALBUM_PATH = Environment
			.getExternalStorageDirectory() + "/BluetoothChat/";

	// Intent request codes
	private static final int REQUEST_CONNECT_DEVICE_SECURE = 1;
	private static final int REQUEST_CONNECT_DEVICE_INSECURE = 2;
	private static final int REQUEST_ENABLE_BT = 3;

	// Layout Views
	private TextView mTitle;
	private ListView mConversationView;
	private EditText mOutEditText;
	private Button mSendButton;
	private View v;
	private int flag;
	
	private Button recordButton;
	private ImageView iv_dialog;

	// Name of the connected device
	private String mConnectedDeviceName = null;
	// Array adapter for the conversation thread
	private ArrayAdapter<String> mConversationArrayAdapter;
	// String buffer for outgoing messages
	private StringBuffer mOutStringBuffer;
	// Local Bluetooth adapter
	private BluetoothAdapter mBluetoothAdapter = null;
	// Member object for the chat services
	private BluetoothChatService mChatService = null;

	private List<Map<String, Object>> list;// 声明列表容器
	private static MySimpleAdapterChat myadapter;// 声明适配器对象

	private HistoryDatabaseAdapter hdAdapter;

	private finalStatic fs;
	private byte[] fileBuffer;
	private String fileTitle;
	private String receiveFileTitle;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (D)
			Log.e(TAG, "+++ ON CREATE +++");

		// Set up the window layout
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.main);

		hdAdapter = new HistoryDatabaseAdapter(this);
		fs = new finalStatic();
		hdAdapter.open();
		fs.flag = hdAdapter.getFlag();
		hdAdapter.close();
		flag = fs.flag;
		if (flag == -1) {
			hdAdapter.open();
			hdAdapter.insertFlag(0);
			hdAdapter.close();
			flag = 0;
		}
		// if(flag == 0)
		// {
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,
				R.layout.custom_title);
		// Set up the custom title
		mTitle = (TextView) findViewById(R.id.title_left_text);
		mTitle.setText(R.string.app_name);
		mTitle = (TextView) findViewById(R.id.title_right_text);
		// }
		/*
		 * else if(flag == 1) {
		 * getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,
		 * R.layout.custom_title_common); // Set up the custom title mTitle =
		 * (TextView) findViewById(R.id.title_left_text_common);
		 * mTitle.setText(R.string.app_name); mTitle = (TextView)
		 * findViewById(R.id.title_right_text_common); }
		 */

		v = findViewById(R.id.main_id);
		fs.f_backgroud = BitmapFactory.decodeFile(ALBUM_PATH
				+ "f_background.jpg");
		if (fs.f_backgroud == null)
			fs.f_backgroud = BitmapFactory.decodeResource(getResources(),
					R.drawable.ic_launcher);
		fs.f_icon = BitmapFactory.decodeFile(ALBUM_PATH + "f_icon.jpg");
		if(fs.f_icon == null)
			fs.f_icon = BitmapFactory.decodeResource(getResources(),R.drawable.ic_launcher);
		BitmapDrawable bd = new BitmapDrawable(fs.f_backgroud);
		v.setBackgroundDrawable(bd);

		// Get local Bluetooth adapter
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

		// If the adapter is null, then Bluetooth is not supported
		if (mBluetoothAdapter == null) {
			Toast.makeText(this, "Bluetooth is not available",
					Toast.LENGTH_LONG).show();
			finish();
			return;
		}
	}

	@Override
	public void onStart() {
		super.onStart();
		if (D)
			Log.e(TAG, "++ ON START ++");

		// If BT is not on, request that it be enabled.
		// setupChat() will then be called during onActivityResult
		if (!mBluetoothAdapter.isEnabled()) {
			Intent enableIntent = new Intent(
					BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
			// Otherwise, setup the chat session
		} else {
			if (mChatService == null)
				setupChat();
		}
	}

	@Override
	public synchronized void onResume() {
		super.onResume();
		if (D)
			Log.e(TAG, "+ ON RESUME +");

		beginChat();
		BitmapDrawable bd = new BitmapDrawable(fs.f_backgroud);
		v.setBackgroundDrawable(bd);

		// Performing this check in onResume() covers the case in which BT was
		// not enabled during onStart(), so we were paused to enable it...
		// onResume() will be called when ACTION_REQUEST_ENABLE activity
		// returns.
		if (mChatService != null) {
			// Only if the state is STATE_NONE, do we know that we haven't
			// started already
			if (mChatService.getState() == BluetoothChatService.STATE_NONE) {
				// Start the Bluetooth chat services
				mChatService.start();
			}
		}
	}

	private void beginChat() {

		flag = fs.flag;
		if (flag == 0) {
			// Initialize the array adapter for the conversation thread
			mConversationArrayAdapter = new ArrayAdapter<String>(this,
					R.layout.message);
			mConversationView = (ListView) findViewById(R.id.in);
			mConversationView.setAdapter(mConversationArrayAdapter);
		} else if (flag == 1) {
			// 实例化listView
			list = new ArrayList<Map<String, Object>>();
			myadapter = new MySimpleAdapterChat(this, list,
					R.layout.custom_title_2, new String[] { "item1_icoleft",
							"item1_icoright", "item1_sendleft",
							"item1_sendright", "item1_dateleft",
							"item1_dateright" }, new int[] { R.id.icoleft,
							R.id.icoright, R.id.title_left_text,
							R.id.title_right_text, R.id.dateleft,
							R.id.dateright });
			// 为列表视图设置适配器，将数据映射到列表视图中
			mConversationView = (ListView) findViewById(R.id.in);
			mConversationView.setAdapter(myadapter);
		}
	}

	private void setupChat() {
		Log.d(TAG, "setupChat()");

		beginChat();

		// Initialize the compose field with a listener for the return key
		mOutEditText = (EditText) findViewById(R.id.edit_text_out);
		mOutEditText.setOnEditorActionListener(mWriteListener);

		// Initialize the send button with a listener that for click events
		mSendButton = (Button) findViewById(R.id.button_send);
		mSendButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				// Send a message using content of the edit text widget
				TextView view = (TextView) findViewById(R.id.edit_text_out);
				String message = view.getText().toString();
				sendMessage(message);
			}
		});

		//初始化录音，发送语音相关的响应
		//初始化录音按钮
				recordButton = (Button) findViewById(R.id.recorder);
				iv_dialog = (ImageView) findViewById(R.id.dialog_talk);
				iv_dialog.setVisibility(View.GONE);
				
				recordButton.setOnTouchListener(new OnTouchListener() {
					//是否开始录制
					private boolean isStart = false;
					//是否录制中
					private boolean isRecording = false;
					//录音点击事件
					@Override
					public boolean onTouch(View v, MotionEvent event) {
						switch(event.getAction()) {
						case MotionEvent.ACTION_DOWN:
								first = (int)(System.currentTimeMillis()/1000);
								mr = new MediaRecorder();
								player = new MediaPlayer();
								//初始化录音和播放设备
								recorder = new SmallRecorder(mr,player);
								filePath = recorder.getFilePath();
								Log.i("录音调试", filePath);
								Log.i("录音调试", "down");
								
							iv_dialog.setVisibility(View.VISIBLE);
							if(recorder!=null&&!player.isPlaying()) {
								new Thread() {
									public void run() {
										mr.start();
										Log.i("录音调试", "开始了");
										isStart = true;
									}
								}.start();
							}
							break;
						case MotionEvent.ACTION_UP:
							second = (int)(System.currentTimeMillis()/1000);
							thrid = second - first;
							//判斷錄製時間是否超過一秒
							if(thrid>=1) {
								isRecording = true;
								isStart = true;
								System.out.println((second-first)+"time");
								//将声音文件转化为字符码病发送
								sendSound(readFileSdcard(filePath));
								mr.stop();
								
							} else {
								System.out.println("reset");
								Toast.makeText(BluetoothChat.this, "录音错误，请按紧保持一秒以上", Toast.LENGTH_LONG).show();
								recorder.delete();
								isStart = false;
								mr.reset();
								isRecording = false;
							}
							Log.i("录音调试", "up");
							iv_dialog.setVisibility(View.GONE);
							break;
						}
						return false;
					}
				});
			

		
		// Initialize the BluetoothChatService to perform bluetooth connections
		mChatService = new BluetoothChatService(this, mHandler);

		// Initialize the buffer for outgoing messages
		mOutStringBuffer = new StringBuffer("");

	}

	// 该函数用于预处理需要压缩的图片，参数分别为新的大小，单位像素
	public static Bitmap zoomImage(Bitmap bgimage, int newWidth, int newHeight) {
		// 获取这个图片的宽和高
		int width = bgimage.getWidth();
		int height = bgimage.getHeight();
		// 创建操作图片用的matrix对象
		Matrix matrix = new Matrix();
		// 计算缩放率，新尺寸除原始尺寸
		float scaleWidth = ((float) newWidth) / width;
		float scaleHeight = ((float) newHeight) / height;
		// 缩放图片动作
		matrix.postScale(scaleWidth, scaleHeight);
		Bitmap bitmap = Bitmap.createBitmap(bgimage, 0, 0, width, height,
				matrix, true);
		return bitmap;

	}

	// 该函数用于压缩图片的大小
	public static Bitmap compressImage(Bitmap image) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		image = zoomImage(image, 20, 20);
		// 质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
		image.compress(Bitmap.CompressFormat.PNG, 100, baos);
		int options = 100;
		while (baos.toByteArray().length > 6000) { // 循环判断如果压缩后图片是否大于,大于继续压缩

			if (options == 0)
				break;
			int debug = baos.toByteArray().length;
			baos.reset(); // 重置baos即清空baos
			image.compress(Bitmap.CompressFormat.PNG, options, baos);// 这里压缩options%，把压缩后的数据存放到baos中
			options -= 5;// 每次都减少1
		}

		ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());// 把压缩后的数据baos存放到ByteArrayInputStream中
		Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, null);// 把ByteArrayInputStream数据生成图片
		return bitmap;
	}

	
	//将收到的字符流转换成文件并返回路径
		public String getRecorderToPath(byte[] record){
			String filePath_get = null;
			SDPATH = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "myRecorder");		
			Log.i("录音调试", "sdpath"+SDPATH);
			
			try {
				File file = File.createTempFile("record", ".amr", SDPATH);
				filePath_get = file.getAbsolutePath();
				FileOutputStream output= new FileOutputStream(file);
				Log.i("录音调试", "recorder大小为"+record.length);
				output.write(record,0,record.length);
				Log.i("录音调试", "少年写入中");
				output.flush();
				output.close();
				Log.i("录音调试", "转换后名称"+filePath_get);
				return filePath_get;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return filePath_get;
		}

		public void playAudio(String path) {
			player = new MediaPlayer();
			try {
				player.reset();
				player.setDataSource(path);
				player.prepare();
				player.start();
			} catch (Exception e) {
				e.printStackTrace();
			}
			}
		
	
	
	
	
	@Override
	public synchronized void onPause() {
		super.onPause();
		if (D)
			Log.e(TAG, "- ON PAUSE -");
	}

	@Override
	public void onStop() {
		super.onStop();
		if (D)
			Log.e(TAG, "-- ON STOP --");
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		// Stop the Bluetooth chat services
		if (mChatService != null)
			mChatService.stop();
		if (D)
			Log.e(TAG, "--- ON DESTROY ---");
	}

	private void getIcon() {
		Intent getAlbum = new Intent(Intent.ACTION_GET_CONTENT);
		getAlbum.setType(IMAGE_TYPE);
		startActivityForResult(getAlbum, IMAGE_CODE);
	}

	private void ensureDiscoverable() {
		if (D)
			Log.d(TAG, "ensure discoverable");
		if (mBluetoothAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
			Intent discoverableIntent = new Intent(
					BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
			discoverableIntent.putExtra(
					BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
			startActivity(discoverableIntent);
		}
	}

	/**
	 * Sends a message.
	 * 
	 * @param message
	 *            A string of text to send.
	 */
	private void sendMessage(String message) {
		// Check that we're actually connected before trying anything
		if (mChatService.getState() != BluetoothChatService.STATE_CONNECTED) {
			Toast.makeText(this, R.string.not_connected, Toast.LENGTH_SHORT)
					.show();
			return;
		}

		if (flag == 0) {
			// Check that there's actually something to send
			if (message.length() > 0) {
				// Get the message bytes and tell the BluetoothChatService to
				// write
				byte[] send = message.getBytes();
				mChatService.writeM(send);

				// Reset out string buffer to zero and clear the edit text field
				mOutStringBuffer.setLength(0);
				mOutEditText.setText(mOutStringBuffer);
			}
		} else if (flag == 1) {
			if (message.length() > 0) {

				// 获取当前时间
				String datedata = getDate();
				Map<String, Object> map = new HashMap<String, Object>();

				// 这一行屏蔽空背景
				map.put("item1_icoright", fs.f_icon);
				map.put("item1_sendright", message);
				map.put("item1_dateright", datedata);
				map.put("item1_icoleft", null);// 这一行屏蔽空背景
				map.put("item1_sendleft", null);
				map.put("item1_dateleft", null);
				list.add(map);
				myadapter.notifyDataSetChanged();

				// Get the message bytes and tell the BluetoothChatService to
				// write
				byte[] send = message.getBytes();
				mChatService.writeM(send);

				// Reset out string buffer to zero and clear the edit text field
				mOutStringBuffer.setLength(0);
				mOutEditText.setText(mOutStringBuffer);
			}
		}
	}
	
private void sendFile(byte[] buffer) {
		
		// 获取头像
		//String fileName = "/sdcard/Y.txt";

		//也可以用String fileName = "mnt/sdcard/Y.txt";
		mChatService.writeT(fileTitle.getBytes());
		mChatService.write(buffer);
		
		

	}

private void sendSound(byte[] buffer) {
	
	// 获取头像
	//String fileName = "/sdcard/Y.txt";

	//也可以用String fileName = "mnt/sdcard/Y.txt";
	//mChatService.writeT(fileTitle.getBytes());
	mChatService.writeS(buffer);
	
	

}

	private void sendPicture(String path) {
		byte[] pic;
		// 获取头像
		bm = BitmapFactory.decodeFile(path);
		compressBm = bm;
		// compressBm = compressImage(bm);
		// im.setImageBitmap(compressBm);
		// 将位图转化为byte[]

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		compressBm.compress(Bitmap.CompressFormat.PNG, 100, baos);
		pic = baos.toByteArray();
		// if(pic.length>1024)
		// Toast.makeText(getApplicationContext(),
		// "图片超过大小，传输失败",Toast.LENGTH_SHORT).show();

		// if (pic.length != 0) {
		// bm2 = BitmapFactory.decodeByteArray(pic, 0, pic.length);
		// }
		// im2.setImageBitmap(bm2);

		if (mChatService.getState() != BluetoothChatService.STATE_CONNECTED) {
			Toast.makeText(this, R.string.not_connected, Toast.LENGTH_SHORT)
					.show();
			return;
		}

		mChatService.writeP(pic);
		// Reset out string buffer to zero and clear the edit text field

		mOutStringBuffer.setLength(0);
		mOutEditText.setText(mOutStringBuffer);
	}

	// The action listener for the EditText widget, to listen for the return key
	private TextView.OnEditorActionListener mWriteListener = new TextView.OnEditorActionListener() {
		public boolean onEditorAction(TextView view, int actionId,
				KeyEvent event) {
			// If the action is a key-up event on the return key, send the
			// message
			if (actionId == EditorInfo.IME_NULL
					&& event.getAction() == KeyEvent.ACTION_UP) {
				String message = view.getText().toString();
				sendMessage(message);
			}
			if (D)
				Log.i(TAG, "END onEditorAction");
			return true;
		}
	};

	// The Handler that gets information back from the BluetoothChatService
	private final Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MESSAGE_STATE_CHANGE:
				if (D)
					Log.i(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
				switch (msg.arg1) {
				case BluetoothChatService.STATE_CONNECTED:
					mTitle.setText(R.string.title_connected_to);
					mTitle.append(mConnectedDeviceName);
					if (flag == 0)
						mConversationArrayAdapter.clear();
					sendPicture(ALBUM_PATH + "f_icon.jpg");
					break;
				case BluetoothChatService.STATE_CONNECTING:
					mTitle.setText(R.string.title_connecting);
					break;
				case BluetoothChatService.STATE_LISTEN:
				case BluetoothChatService.STATE_NONE:
					mTitle.setText(R.string.title_not_connected);
					break;
				}
				break;
			case MESSAGE_WRITE:
				byte[] writeBuf = (byte[]) msg.obj;
				// construct a string from the buffer
				String writeMessage = new String(writeBuf);
				if (flag == 0)
					mConversationArrayAdapter.add("Me:  " + writeMessage);
				String datedata = getDate();
				History ht = new History(0, fs.macad, writeMessage, datedata, 0);
				hdAdapter.open();
				hdAdapter.insert(ht);
				hdAdapter.close();
				break;
			case MESSAGE_READ:
				byte[] readBuf = (byte[]) msg.obj;
				// construct a string from the valid bytes in the buffer
				String readMessage = new String(readBuf, 0, msg.arg1);
				// 得到收到消息的時間
				String rcvDate = getDate();
				if (flag == 0)
					mConversationArrayAdapter.add(mConnectedDeviceName + ":  "
							+ readMessage);
				else if (flag == 1) {
					Map<String, Object> map = new HashMap<String, Object>();

					// 这一行屏蔽空背景
					map.put("item1_icoright", null);
					map.put("item1_sendright", null);
					map.put("item1_dateright", null);
					map.put("item1_icoleft", fs.y_icon);// 这一行屏蔽空背景
					map.put("item1_sendleft", readMessage);
					map.put("item1_dateleft", rcvDate);
					list.add(map);
					myadapter.notifyDataSetChanged();
				}
				History ht_2 = new History(0, fs.macad, readMessage, rcvDate, 1);
				hdAdapter.open();
				hdAdapter.insert(ht_2);
				hdAdapter.close();
				break;
			case ICO_READ:
				byte[] readBuf2 = (byte[]) msg.obj;
				bm3 = BitmapFactory.decodeByteArray(readBuf2, 0,
						readBuf2.length);
				fs.y_icon = bm3;
				break;
			case FILE_READ:
				byte[] fileBuf = (byte[]) msg.obj;
				writeFileSdcard(receiveFileTitle,fileBuf);
				
				


				break;
			case SOUND_READ:
				byte[] soundBuf = (byte[]) msg.obj;
				//writeFileSdcard(receiveFileTitle,fileBuf);
						Log.i("录音调试", "收到需要的东西");
						//根据录音的名称来播放
						player.reset();
						String reSoundPath = getRecorderToPath(soundBuf);
						playAudio(reSoundPath);
						
						/*
						if(recorder!=null) {
							recorder.playRecord();
							iv_dialog.setVisibility(View.GONE);
						}*/


				
				


				break;
			case TITLE_READ:
				byte[] fileTitleBuf = (byte[]) msg.obj;
				String titleMessage = new String(fileTitleBuf, 0, msg.arg1);
				receiveFileTitle=titleMessage;
				Toast.makeText(getApplicationContext(),
						"传输完成"+receiveFileTitle,
						Toast.LENGTH_SHORT).show();


				break;
			case MESSAGE_DEVICE_NAME:
				// save the connected device's name
				mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
				Toast.makeText(getApplicationContext(),
						"Connected to " + mConnectedDeviceName,
						Toast.LENGTH_SHORT).show();
				break;
			case MESSAGE_TOAST:
				Toast.makeText(getApplicationContext(),
						msg.getData().getString(TOAST), Toast.LENGTH_SHORT)
						.show();
				break;
			}
		}
	};

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(D) Log.d(TAG, "onActivityResult " + resultCode);
        switch (requestCode) {
        case REQUEST_CONNECT_DEVICE_SECURE:
            // When DeviceListActivity returns with a device to connect
            if (resultCode == Activity.RESULT_OK) {
                connectDevice(data, true);
            }
            break;
        case REQUEST_CONNECT_DEVICE_INSECURE:
            // When DeviceListActivity returns with a device to connect
            if (resultCode == Activity.RESULT_OK) {
                connectDevice(data, false);
            }
            break;
        case REQUEST_ENABLE_BT:
            // When the request to enable Bluetooth returns
            if (resultCode == Activity.RESULT_OK) {
                // Bluetooth is now enabled, so set up a chat session
                setupChat();
            } else {
                // User did not enable Bluetooth or an error occured
                Log.d(TAG, "BT not enabled");
                Toast.makeText(this, R.string.bt_not_enabled_leaving, Toast.LENGTH_SHORT).show();
                finish();
            }
        case FILE_GET:
			if(resultCode==RESULT_OK){

				Uri originalUri = data.getData(); // 获得图片的uri
				String[] proj = { MediaStore.Images.Media.DATA };
				// 最后根据索引值获取图片路径
				path =originalUri.getPath();

				fileBuffer=readFileSdcard(path);
				
				fileTitle=getFileName(path);
				sendFile(fileBuffer);
				
			}
			else{
				return;
			}
			break;
        case IMAGE_CODE:
        	try {
			if(resultCode==RESULT_OK){
				ContentResolver resolver = getContentResolver();

				Uri originalUri = data.getData(); // 获得图片的uri
				bm = MediaStore.Images.Media.getBitmap(resolver, originalUri); // 显得到bitmap图片
				// 这里开始的第二部分，获取图片的路径：
				String[] proj = { MediaStore.Images.Media.DATA };
				Cursor cursor = managedQuery(originalUri, proj, null, null,
						null);
				// 获得用户选择的图片的索引值
				int column_index = cursor
						.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
				cursor.moveToFirst();
				// 最后根据索引值获取图片路径
				path = cursor.getString(column_index);
				
				bm = BitmapFactory.decodeFile(path);

				Log.e("Lostinai", path);
			}
			else{
				return;
			}		
		} catch (IOException e) {
			Log.e("Lostinai", e.toString());
		}}
	}

	private void connectDevice(Intent data, boolean secure) {
		// Get the device MAC address
		String address = data.getExtras().getString(
				DeviceListActivity.EXTRA_DEVICE_ADDRESS);
		// Get the BLuetoothDevice object
		BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
		// Attempt to connect to the device
		mChatService.connect(device, secure);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.option_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent serverIntent = null;
		switch (item.getItemId()) {
		case R.id.secure_connect_scan:
			// Launch the DeviceListActivity to see devices and do scan
			serverIntent = new Intent(this, DeviceListActivity.class);
			startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_SECURE);
			return true;
		case R.id.discoverable:
			// Ensure this device is discoverable by others
			ensureDiscoverable();
			return true;
		case R.id.setting:
			Intent intent1 = new Intent();
			intent1.setClass(BluetoothChat.this, SetActivity.class);
			startActivity(intent1);
			return true;
		case R.id.sendFile:
			Intent getAlbum = new Intent(Intent.ACTION_GET_CONTENT);
			getAlbum.setType(IMAGE_TYPE);
			startActivityForResult(getAlbum,FILE_GET);
			return true;
		case R.id.history:
			// Check that we're actually connected before trying anything
			if (mChatService.getState() != BluetoothChatService.STATE_CONNECTED) {
				Toast.makeText(this, R.string.not_connected, Toast.LENGTH_SHORT)
						.show();
				return true;
			}
			Intent intent2 = new Intent();
			intent2.setClass(BluetoothChat.this, HistoryActivity.class);
			startActivity(intent2);
			return true;
		}
		return false;
	}

	/*
	 * 日期转换格式函数
	 */
	public static Date StrToDate(String str) {

		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date date = null;
		try {
			date = format.parse(str);
		} catch (java.text.ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return date;
	}

	public static String DateToStr(Date date) {

		SimpleDateFormat format = new SimpleDateFormat("yyyy年MM月dd日  HH:mm:ss");
		String str = format.format(date);
		return str;
	}

	/*
	 * 得到當前時間
	 */
	public String getDate() {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

		Date curDate = new Date(System.currentTimeMillis());
		String datestr = formatter.format(curDate);
		String datedata = DateToStr(StrToDate(datestr));

		StringTokenizer strToken = new StringTokenizer(datedata, " ");
		String showDate = null;
		while (strToken.hasMoreTokens())
			showDate = strToken.nextToken();
		return showDate;
	}

	byte[] arraycat(byte[] buf1, byte[] buf2) {
		byte[] bufret = null;
		int len1 = 0;
		int len2 = 0;
		if (buf1 != null)
			len1 = buf1.length;
		if (buf2 != null)
			len2 = buf2.length;
		if (len1 + len2 > 0)
			bufret = new byte[len1 + len2];
		if (len1 > 0)
			System.arraycopy(buf1, 0, bufret, 0, len1);
		if (len2 > 0)
			System.arraycopy(buf2, 0, bufret, len1, len2);
		return bufret;
	}
	
	public String getFileName(String apath){  

        

        int start=apath.lastIndexOf("/");

        if(start!=-1){  

            return apath.substring(start+1);    

        }else{  

            return null;  

        }  

          
	}
	
	public void writeFileSdcard(String fileName,byte[] buffer){ 

		String	name=fileName;

		File file1=new File(DIR,name);
		FileOutputStream fileOutputStream = null;
		
		if(!DIR.exists()){
			DIR.mkdirs();
		}

		if(!file1.exists()){
			try {
				file1.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		try {
			fileOutputStream=new FileOutputStream(file1);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	       try{
	    	   fileOutputStream.write(buffer);
	    	   
	    	   fileOutputStream.close(); 
	        } 
	       catch(Exception e){ 
	        e.printStackTrace(); 
	       } 
	       
	       Toast.makeText(getApplicationContext(),
					"传输完成"+receiveFileTitle,
					Toast.LENGTH_SHORT).show();
	   }
	
	public byte[] readFileSdcard(String fileName){
		 
		 
		 
		 byte [] buffer;

		 
	        try{ 
	         FileInputStream fin = new FileInputStream(fileName); 
	         int length = fin.available(); 
	         buffer = new byte[length]; 
	         fin.read(buffer);
	         fin.close();
	         
	         
	        } 
	        catch(Exception e){ 
	         e.printStackTrace();
	         return null;
	        }

	         return buffer;
	       
	        
	   }
}

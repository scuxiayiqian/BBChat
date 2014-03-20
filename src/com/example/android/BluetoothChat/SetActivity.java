package com.example.android.BluetoothChat;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import com.example.android.BluetoothChat.DB.HistoryDatabaseAdapter;

import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.drawable.BitmapDrawable;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;
import android.widget.Toast;

public class SetActivity extends Activity {

	private final String IMAGE_TYPE = "image/*";
	private final int IMAGE_CODE = 0;
	private Button iconBtn;
	private Button backgroundBtn;
	private EditText nameEt;
	private TextView macAd;
	private ImageView im;
	private RadioGroup rg;
	private View v;
	private String path;
	private int flag;
	private BluetoothAdapter mBtAdapter = null;
	private final static String ALBUM_PATH = Environment
			.getExternalStorageDirectory() + "/BluetoothChat/";
	private HistoryDatabaseAdapter hdAdapter;
	private finalStatic fs;
	Photo tempPhoto=new Photo();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.activity_sett);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,
				R.layout.custom_title_set);
		
		iconBtn = (Button) findViewById(R.id.set_btn_ico);
		backgroundBtn = (Button) findViewById(R.id.set_btn_background);
		nameEt = (EditText) findViewById(R.id.set_et_name);
		macAd = (TextView) findViewById(R.id.set_tv_macad_info);
		im = (ImageView) findViewById(R.id.set_iv_ico);
		rg = (RadioGroup) findViewById(R.id.set_rg_theme);
		v = findViewById(R.id.set_id);
		
		im.setImageBitmap(fs.f_icon);
		
		BitmapDrawable bd = new BitmapDrawable(fs.f_backgroud);
		v.setBackgroundDrawable(bd);

		mBtAdapter = BluetoothAdapter.getDefaultAdapter();
		
		nameEt.setText(mBtAdapter.getName());
		macAd.setText(mBtAdapter.getAddress());
		
		fs = new finalStatic();
		hdAdapter = new HistoryDatabaseAdapter(this);
		flag = fs.flag;

		iconBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				//弹出对话框
				final AlertDialog.Builder builder = new AlertDialog.Builder(SetActivity.this);
				builder.setTitle("请选择头像");
				builder.setItems(R.array.photo_upload_item, new DialogInterface.OnClickListener() {					
					public void onClick(DialogInterface dialog, int which) {
						if(which==0){//选择拍照
							//下面指定调用相机拍照后的照片存储的路径
							//若路径文件夹不存在则新建
							if(!Photo.getPhotoDir().exists()){
								Photo.getPhotoDir().mkdirs();
							}
							//指定路径
							tempPhoto.getPhotoFileName();
							//建立Intent并发送
							Intent intent = new Intent(
									MediaStore.ACTION_IMAGE_CAPTURE);
							intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri
									.fromFile(tempPhoto.getFile()));
							startActivityForResult(intent,Photo.FROM_CAMERA); 					
						}
						else{//启动从文件上传
							Intent intent = new Intent(Intent.ACTION_PICK, null);
							intent.setDataAndType(
									MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
									"image/*");
							startActivityForResult(intent,Photo.FROM_FILE);
						}					
					}
				});

				//点击返回之后,操作为空，对话框消失
				builder.setPositiveButton("返回", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) { 

					} 
				}) ;

				builder.show();	
				//Intent getAlbum = new Intent(Intent.ACTION_GET_CONTENT);
				//getAlbum.setType(IMAGE_TYPE);
				//startActivityForResult(getAlbum, IMAGE_CODE);
			}
		});
		backgroundBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent getAlbum = new Intent(Intent.ACTION_GET_CONTENT);
				getAlbum.setType(IMAGE_TYPE);
				startActivityForResult(getAlbum, IMAGE_CODE + 1);
			}
		});
		RadioButton rb1 = (RadioButton) findViewById(R.id.radio1);
		RadioButton rb2 = (RadioButton) findViewById(R.id.radio2);
		if(flag == 0)
			rb1.setChecked(true);
		else if(flag == 1)
			rb2.setChecked(true);
        rg.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            
            @Override
            public void onCheckedChanged(RadioGroup arg0, int arg1) {
            	if (arg1 == R.id.radio1) { 
            		flag = 0;
            	}
            	else if (arg1 == R.id.radio2) {
            		flag = 1;
            	}
            }});

	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode != RESULT_OK) {
			return;
		}

		Bitmap bm = null;
		ContentResolver resolver = getContentResolver();
		if (requestCode ==Photo.FROM_CUT) {
			Bundle extras = data.getExtras();
			if (extras != null) {

				fs.f_icon = extras.getParcelable("data");
				//将按钮图片设置为照片
				im.setImageBitmap(fs.f_icon);
				tempPhoto.setBitmap(fs.f_icon);
			}
			//im.setImageBitmap(bm);
		} else if (requestCode == IMAGE_CODE + 1) {
			try {
				Uri originalUri = data.getData();
				bm = MediaStore.Images.Media.getBitmap(resolver, originalUri);
				String[] proj = { MediaStore.Images.Media.DATA };
				Cursor cursor = managedQuery(originalUri, proj, null, null,
						null);
				int column_index = cursor
						.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
				cursor.moveToFirst();
				path = cursor.getString(column_index);
				Log.e("Lostinai", path);

				BitmapFactory.Options opts = new Options();
				// 不读取像素数组到内存中，仅读取图片的信息
				opts.inJustDecodeBounds = true;
				int imageHeight = bm.getHeight();
				int imageWidth = bm.getWidth();

				// 获取Android屏幕的服务
				WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);
				// 获取屏幕的分辨率，getHeight()、getWidth已经被废弃掉了
				// 应该使用getSize()，但是这里为了向下兼容所以依然使用它们
				int windowHeight = wm.getDefaultDisplay().getHeight();
				int windowWidth = wm.getDefaultDisplay().getWidth();

				// 计算采样率
				int scaleX = imageWidth / windowWidth;
				int scaleY = imageHeight / windowHeight;
				int scale = 1;
				// 采样率依照最大的方向为准
				if (scaleX > scaleY && scaleY >= 1) {
					scale = scaleX;
				}
				if (scaleX < scaleY && scaleX >= 1) {
					scale = scaleY;
				}

				// false表示读取图片像素数组到内存中，依照设定的采样率
				opts.inJustDecodeBounds = false;
				// 采样率
				opts.inSampleSize = scale;

				bm = BitmapFactory.decodeFile(path, opts);
				fs.f_backgroud = BitmapFactory.decodeFile(path, opts);
				BitmapDrawable bd = new BitmapDrawable(bm);
				v.setBackgroundDrawable(bd);
			} catch (IOException e) {
				Log.e("Lostinai", e.toString());
			}
		}
		else if (requestCode == Photo.FROM_CAMERA) {
			if(resultCode==RESULT_OK){
				startPhotoZoom(Uri.fromFile(tempPhoto.getFile()));
			}
			else{
				return;
			}
		}
		else if (requestCode == Photo.FROM_FILE) {
			if(resultCode==RESULT_OK){
				startPhotoZoom(data.getData());
			}
			else{
				return;
			}
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.set, menu);
		return true;
	}
	
	public void startPhotoZoom(Uri uri) {		
		Intent intent = new Intent("com.android.camera.action.CROP");
		intent.setDataAndType(uri, "image/*");
		//下面这个crop=true是设置在开启的Intent中设置显示的VIEW可裁剪
		intent.putExtra("crop", "true");
		// aspectX aspectY 是宽高的比例
		intent.putExtra("aspectX", 1);
		intent.putExtra("aspectY", 1);
		// outputX outputY 是裁剪图片宽高
		intent.putExtra("outputX", 150);
		intent.putExtra("outputY", 150);
		intent.putExtra("return-data", true);
		startActivityForResult(intent,Photo.FROM_CUT);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_settings:
			saveBitmap(fs.f_icon, fs.f_backgroud);
			mBtAdapter.setName(nameEt.getText().toString());
			hdAdapter.open();
			hdAdapter.setFlag(flag);
			hdAdapter.close();
			fs.flag = flag;
			Toast.makeText(this, "保存成功！", Toast.LENGTH_LONG).show();
			finish();
			return true;
		}
		Toast.makeText(this, "保存失败！", Toast.LENGTH_LONG).show();
		return false;
	}

	/** 保存方法 */
	public void saveBitmap(Bitmap bm1, Bitmap bm2) {
		File dirFile = new File(ALBUM_PATH);
		if (!dirFile.exists()) {
			dirFile.mkdir();
		}
		File myCaptureFile_1 = new File(ALBUM_PATH + "f_icon.jpg");
		File myCaptureFile_2 = new File(ALBUM_PATH + "f_background.jpg");
		if (myCaptureFile_1.exists()) {
			myCaptureFile_1.delete();
			myCaptureFile_1 = new File(ALBUM_PATH + "f_icon.jpg");
		}
		if (myCaptureFile_2.exists()) {
			myCaptureFile_2.delete();
			myCaptureFile_2 = new File(ALBUM_PATH + "f_background.jpg");
		}
		try {
			FileOutputStream out1 = new FileOutputStream(myCaptureFile_1);
			FileOutputStream out2 = new FileOutputStream(myCaptureFile_2);
			bm1.compress(Bitmap.CompressFormat.JPEG, 100, out1);
			bm2.compress(Bitmap.CompressFormat.JPEG, 100, out2);
			out1.flush();
			out1.close();
			out2.flush();
			out2.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}

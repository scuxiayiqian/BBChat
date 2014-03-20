package com.example.android.BluetoothChat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.graphics.Bitmap;
import android.os.Environment;
import android.util.Log;

public class Photo {

	
	//定义拍照后照片保存地�?
	private static final File PHOTO_DIR=new File(Environment.getExternalStorageDirectory()+"/DCIM/Babyface"); 
	//定义照片裁剪后的保存地址
	private static final File PHOTO_SAVE=new File(Environment.getExternalStorageDirectory()+"/Babyface/Photo");
	//定义请求码常�?
	public static final int FROM_CAMERA=20;
	public static final int FROM_FILE = 21;
	public static final int FROM_CUT = 22;
	
	//调试�?
	private static final String TAG = "Photo";
	
	//用于存放临时文件路径，不用管�?
	private File file=null;
	//用于存放�?��的图片路�?
	private String path="noPath";
	//用于存放宝宝的姓名，亦是决定存储的文件名的重要参�?
	private String name="noName";
	//用于存放裁剪后的图片数据
	private Bitmap bitmap=null;
	
	
    //get以及set方法
	public File getFile(){
		return file;
	}
	
	
	public void setBitmap(Bitmap mBitmap){
		bitmap=mBitmap;
	}

	public Bitmap getBitmap(){
		return bitmap;
	}
	
	public void setPath(String mPath){
		path=mPath;
	}

	public String getPath(){
		return path;
	}
	
	public void setName(String mName){
		name=mName;
	}
	
	public String getName(){
		return name;
	}
    
	
	//构�?方法
	public Photo(){
		file=null;
		bitmap=null;
		path="noPath";
		name="noName";
	}

	public Photo(String mPath,String mName,Bitmap mBitmap){
		file=null;
		bitmap=mBitmap;
		path=mPath;
		name=mName;
	}
		
	//保存图片至文�?接收的参数为要存的Bitmap，结果将存在定义的路径下，文件名�?name.jpg"
	//在savedata前请先调用setname()函数设置宝宝姓名
	public void savedata(Bitmap photo) throws IOException {
		if(!PHOTO_SAVE.exists()){
			PHOTO_SAVE.mkdirs();
			}
		File f = new File(PHOTO_SAVE,name+".jpg");
		if(f.exists()){
			f.delete();
		}
        FileOutputStream fOut =new FileOutputStream(f);
        photo.compress(Bitmap.CompressFormat.JPEG, 100, fOut);
        fOut.flush();
        fOut.close();
        
        //使用File.getPath()将保存路径存至path中，之后可调用Photo.getPath()得到存储路径
        path=f.getAbsolutePath();
        Log.d(TAG,path);
        return;
	}
	
	
	//用于确定�?��照片名字，这里是基于拍摄时间而定，返回String类型的文件名
	public void getPhotoFileName() {
        Log.d(TAG, "getPhotoFileName()");
        Date date = new Date(System.currentTimeMillis());  
        SimpleDateFormat dateFormat = new SimpleDateFormat(  
                "yyyy-MM-dd HH-mm-ss");
        file=new File(getPhotoDir(),dateFormat.format(date) + ".jpg");
    }


	public static File getPhotoDir() {
		return PHOTO_DIR;
	}
	
}
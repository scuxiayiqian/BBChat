package com.example.android.BluetoothChat;

import java.util.List;
import java.util.Map;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class MySimpleAdapterChat extends BaseAdapter {
	// 声明一个LayoutInflater对象（其作用是用来实例化布局）
	private LayoutInflater mInflater;
	private List<Map<String, Object>> list;// 声明List容器对象
	private int layoutID; // 声明布局ID
	private String flag[];// 声明ListView项中所有组件映射索引
	private int ItemIDs[];// 声明ListView项中所有组件ID数组

	public MySimpleAdapterChat(Context context, List<Map<String, Object>> list,
			int layoutID, String flag[], int ItemIDs[]) {
		// 利用构造来实例化成员变量对象
		this.mInflater = LayoutInflater.from(context);
		this.list = list;
		this.layoutID = layoutID;
		this.flag = flag;
		this.ItemIDs = ItemIDs;
	}

	public int getCount() {
		return list.size();// 返回ListView项的长度
	}

	public Object getItem(int arg0) {
		return 0;
	}

	public long getItemId(int arg0) {
		return 0;
	}

	// 实例化布局与组件以及设置组件数据
	// getView(int position, View convertView, ViewGroup parent)
	// 第一个参数：绘制的行数
	// 第二个参数：绘制的视图这里指的是ListView中每一项的布局
	// 第三个参数：view的合集，这里不需要

	public View getView( int position, View convertView, ViewGroup parent) {
		// 将布局通过mInflater对象实例化为一个view
		convertView = mInflater.inflate(layoutID, null);
		for (int i = 0; i < flag.length; i++) {// 遍历每一项的所有组件
			// 每个组件都做匹配判断，得到组件的正确类型
			if (convertView.findViewById(ItemIDs[i]) instanceof ImageView) {
				// findViewById()函数作用是实例化布局中的组件
				// 当组件为ImageView类型，则为其实例化一个ImageView对象
				ImageView iv = (ImageView) convertView.findViewById(ItemIDs[i]);
				// 为其组件设置数据
//				iv.setBackgroundResource((Integer) list.get(position).get(
//						flag[i]));
				iv.setImageBitmap((Bitmap) list.get(position).get(
						flag[i]));
			} else if (convertView.findViewById(ItemIDs[i]) instanceof TextView) {
				// 当组件为TextView类型，则为其实例化一个TextView对象
				TextView tv = (TextView) convertView.findViewById(ItemIDs[i]);
				// 为其组件设置数据
				tv.setText((String) list.get(position).get(flag[i]));
				if (tv.getText().length() == 0) {
					tv.setBackgroundDrawable(null);//这一行屏蔽空背景
				}
			}
		}
		return convertView;
	}

}
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
	// ����һ��LayoutInflater����������������ʵ�������֣�
	private LayoutInflater mInflater;
	private List<Map<String, Object>> list;// ����List��������
	private int layoutID; // ��������ID
	private String flag[];// ����ListView�����������ӳ������
	private int ItemIDs[];// ����ListView�����������ID����

	public MySimpleAdapterChat(Context context, List<Map<String, Object>> list,
			int layoutID, String flag[], int ItemIDs[]) {
		// ���ù�����ʵ������Ա��������
		this.mInflater = LayoutInflater.from(context);
		this.list = list;
		this.layoutID = layoutID;
		this.flag = flag;
		this.ItemIDs = ItemIDs;
	}

	public int getCount() {
		return list.size();// ����ListView��ĳ���
	}

	public Object getItem(int arg0) {
		return 0;
	}

	public long getItemId(int arg0) {
		return 0;
	}

	// ʵ��������������Լ������������
	// getView(int position, View convertView, ViewGroup parent)
	// ��һ�����������Ƶ�����
	// �ڶ������������Ƶ���ͼ����ָ����ListView��ÿһ��Ĳ���
	// ������������view�ĺϼ������ﲻ��Ҫ

	public View getView( int position, View convertView, ViewGroup parent) {
		// ������ͨ��mInflater����ʵ����Ϊһ��view
		convertView = mInflater.inflate(layoutID, null);
		for (int i = 0; i < flag.length; i++) {// ����ÿһ����������
			// ÿ���������ƥ���жϣ��õ��������ȷ����
			if (convertView.findViewById(ItemIDs[i]) instanceof ImageView) {
				// findViewById()����������ʵ���������е����
				// �����ΪImageView���ͣ���Ϊ��ʵ����һ��ImageView����
				ImageView iv = (ImageView) convertView.findViewById(ItemIDs[i]);
				// Ϊ�������������
//				iv.setBackgroundResource((Integer) list.get(position).get(
//						flag[i]));
				iv.setImageBitmap((Bitmap) list.get(position).get(
						flag[i]));
			} else if (convertView.findViewById(ItemIDs[i]) instanceof TextView) {
				// �����ΪTextView���ͣ���Ϊ��ʵ����һ��TextView����
				TextView tv = (TextView) convertView.findViewById(ItemIDs[i]);
				// Ϊ�������������
				tv.setText((String) list.get(position).get(flag[i]));
				if (tv.getText().length() == 0) {
					tv.setBackgroundDrawable(null);//��һ�����οձ���
				}
			}
		}
		return convertView;
	}

}
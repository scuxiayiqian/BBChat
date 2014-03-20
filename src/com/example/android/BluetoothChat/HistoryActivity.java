package com.example.android.BluetoothChat;

import java.util.ArrayList;

import com.example.android.BluetoothChat.DB.History;
import com.example.android.BluetoothChat.DB.HistoryDatabaseAdapter;

import android.os.Bundle;
import android.os.Environment;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class HistoryActivity extends Activity {

    // Array adapter for the conversation thread
    private ArrayAdapter<String> mConversationArrayAdapter;
    private ArrayList<History> allHistory;
	private View v;
    private ListView mConversationView;
    
    private HistoryDatabaseAdapter hdAdapter;
    
	private final static String ALBUM_PATH = Environment.getExternalStorageDirectory() + "/BluetoothChat/";
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.activity_history);
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.custom_title_history);
        
        v = findViewById(R.id.history_id);
		Bitmap bm = BitmapFactory.decodeFile(ALBUM_PATH + "f_background.jpg");
		BitmapDrawable bd = new BitmapDrawable(bm);
		v.setBackgroundDrawable(bd);
		
        // Initialize the array adapter for the conversation thread
        mConversationArrayAdapter = new ArrayAdapter<String>(this, R.layout.message);
        mConversationView = (ListView) findViewById(R.id.in_history);
        mConversationView.setAdapter(mConversationArrayAdapter);
		
        hdAdapter = new HistoryDatabaseAdapter(this);
        hdAdapter.open();
        allHistory = hdAdapter.getAllData();
        hdAdapter.close();
        
        for(History temp:allHistory)
        {
        	if(temp.getFrom() == 0)
        		mConversationArrayAdapter.add("Me:  " + temp.getText() + "(" + temp.getDate() + ")");
        	else if(temp.getFrom() == 1)
        		mConversationArrayAdapter.add("You:  " + temp.getText() + "(" + temp.getDate() + ")");
        }
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.history, menu);
		return true;
	}

}

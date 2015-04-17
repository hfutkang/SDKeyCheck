package com.android.simt;

import java.io.File;

import com.sctek.sdkeycheck.R;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends Activity {

	private static final String TAG = "MainActivity";
	
	private static final int TEST_ERROR_NOERR  = 0;
	private static final int TEST_ERROR_O_DIRECT = -1;
	private static final int TEST_ERROR_NOPERMISSION = -2;
	private static final int TEST_ERROR_CACHED = -3;
	private static final int TEST_ERROR_DIR  = -4;
	private static final int TEST_ERROR_READONLY = -5;
	private static final int TEST_ERROR_UNKNOWN = -100;
	
	private static final String[] msgs = {"TEST_ERROR_NOERR 0","TEST_ERROR_O_DIRECT -1",
					"TEST_ERROR_NOPERMISSION -2","TEST_ERROR_CACHED -3","TEST_ERROR_DIR -4","TEST_ERROR_READONLY -5","TEST_ERROR_UNKNOWN -100"};
	
	private TextView msgTv;
	private EditText pathEt;
	
	private Simt13ApduJni apduJni;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		msgTv = (TextView)findViewById(R.id.msg_tv);
		pathEt = (EditText)findViewById(R.id.path_et);
		
		apduJni = new Simt13ApduJni();
	}
	
	public void onClearBtClicked(View v) {
		
		msgTv.setText("");
		
	}
	
	public void onExternalPathBtClicked(View v) {
		
		String path = getExternalSDcardRootPath();
		pathEt.setText(path);
	}
	
	public void onAppPathBtClicked(View v) {
		String path = getAppPath();
		pathEt.setText(path);
	}
	
	public void onTestBtClicked(View v) {
		
		String path = pathEt.getText().toString();
		int res;
		if("".equals(path)) {
			printMsg("No Path Specified");
			return;
		}
		
		if(!path.endsWith("/"))
			path = path + "/";
		Log.e(TAG, path);
		res = apduJni.testMySD(path.getBytes());

		printTestMsg(res);
	}
	
	private void printMsg(String msg) {
		msgTv.append(msg + "\n");
	}
	
	private void printTestMsg(int res) {
		
		switch(res) {
		case -1:
			printMsg(msgs[0]);
			break;
		case -2:
			printMsg(msgs[1]);
			break;
		case -3:
			printMsg(msgs[2]);
			break;
		case -4:
			printMsg(msgs[3]);
			break;
		case -5:
			printMsg(msgs[4]);
			break;
		case -100:
			printMsg(msgs[5]);
			break;
			default:
				break;
		}
	}
	
	@SuppressLint("NewApi")
	private String getExternalSDcardRootPath() {
		
		String path = null;
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			
			File[] files = getExternalFilesDirs(null);
			if(files.length >= 2 && files[1] != null) {
				String appPath = files[1].getAbsolutePath();
				int end = appPath.indexOf("/Android");
				path = appPath.substring(0, end);
				return path;
			}
		}
		
		path = System.getenv("SECONDARY_STORAGE");
		if(path != null)
			return path;
		
		File sdDir = getExternalSdcardDirectory();
		return sdDir.getAbsolutePath();
	}
	
	private String getAppPath() {
		String sdRootPath = getExternalSDcardRootPath();
		String appPath = sdRootPath + "/Android/data/" + getApplicationContext().getPackageName() +"/files";
		File file = new File(appPath);
		if(!file.exists() || !file.isDirectory())
			file.mkdirs();
		return sdRootPath + "/Android/data/" + getApplicationContext().getPackageName() +"/files";
	}
	private File getExternalSdcardDirectory() {

		File rootDir = null;
		File primaryStorage = Environment.getExternalStorageDirectory();
		Log.e(TAG, primaryStorage.getAbsolutePath());
		if(primaryStorage.toString().toLowerCase().contains("emulated"))
			rootDir = primaryStorage.getParentFile().getParentFile();
		else
			rootDir = primaryStorage.getParentFile();
		
		File[] files = rootDir.listFiles();
		for(File file : files) {
			String path = file.toString().toLowerCase();
			if(path.contains("sdcard") && !path.contains("sdcard0"))
				return file;
		}
		return primaryStorage;
	}
	
}

package my.test.cpuloading;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

public class WriteFile2SD {

	private Context mContext;
	private String mFileName;
	private FileWriter mFileWriter;

	public WriteFile2SD(Context context, long fileName, String filename) {
		mContext = context;
		if (filename.equals(""))
			mFileName = String.valueOf(fileName);
		else 
			mFileName = filename;

		try {
			if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
				File loadingDirectory = new File(Environment.getExternalStorageDirectory()+"/Loading/");
				Log.v(Loading.TAG,"File Path:"+Environment.getExternalStorageDirectory()+"/Loading/"+mFileName+".csv");
				loadingDirectory.mkdirs();
				File outputFile = new File(loadingDirectory, mFileName+".csv");
				mFileWriter = new FileWriter(outputFile);
				Log.v(Loading.TAG, "Create Success. Filename: "+mFileName);
				
			} else {
				Log.v(Loading.TAG, "No SD card!");
			}
		} catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

	}
	
	protected void write(String s) {
		if (Loading.DEBUG && Loading.W2SD_DEBUG) Log.v(Loading.TAG,"W: "+s);
		try {
			mFileWriter.write(s+"\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
    public void close(){
    	try {
			if (mFileWriter!=null) {
				Log.v(Loading.TAG, "File Close:"+mFileName+".txt");
				mFileWriter.close();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
}

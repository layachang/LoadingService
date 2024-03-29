package my.test.cpuloading;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import android.os.Environment;
import android.util.Log;

public class WriteFile2SD {

    private String mFileName;
    private FileWriter mFileWriter;

    public WriteFile2SD(String filename) {
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
        if (Loading.W2SD_DEBUG) Log.v(Loading.TAG,"W: "+s);
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

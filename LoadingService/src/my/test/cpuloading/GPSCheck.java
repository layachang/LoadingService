package my.test.cpuloading;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.util.Log;

public class GPSCheck {
	private Context mContext;
	private WriteFile2SD wfile;
	private String mPID;
	private String mProcessName;
	private int mStatus = 0;
	private long mCurrentTime=0;
	private int mLogTime=0;
	
	public GPSCheck(LoadingService context, WriteFile2SD file) {
		mContext = context;
		wfile = file; 
		//mProcessName = procName;
		Log.v(Loading.TAG, "GPSCheck");
	}

	public void getAudio(long ctime) {
		mCurrentTime = ctime;
		final StringBuilder cpuinfo = new StringBuilder();
		boolean flag = false;
		CharSequence cs = mPID;
		try {
            final List<String> commandLine = new ArrayList<String>();
            commandLine.add("dumpsys");
            commandLine.add("audio");
            //commandLine.add(Integer.toString(android.os.Process.myPid()));

            final Process process = Runtime.getRuntime().exec(commandLine.toArray(new String[commandLine.size()]));
            final BufferedReader bufferedReader = new BufferedReader(
	            		new InputStreamReader(process.getInputStream()), 
	            		1024*1024);
            bufferedReader.readLine();
            bufferedReader.readLine();
            String line = bufferedReader.readLine();
            if (line!=null)
            	mStatus = 1;
            else 
            	mStatus = 0;

            bufferedReader.close();
        } catch (IOException e) {
            //Log.e(Loading.TAG, "DumpSysCollector.meminfo could not retrieve data", e);
        }
	}
	
	protected void printAudio(boolean withValue){
		int cur_log_time=((LoadingService) mContext).getTime(mCurrentTime);
		mLogTime = cur_log_time;
		String input = "Audio," + mLogTime ;
		if (withValue) {
			input = "Audio," + mLogTime + "," + mStatus;
		}
		if(Loading.DEBUG && Loading.AUDIO_DEBUG) Log.v(Loading.TAG, "input: "+input);
        wfile.write(input);
	}
}

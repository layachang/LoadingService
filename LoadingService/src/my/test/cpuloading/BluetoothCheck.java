package my.test.cpuloading;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.util.Log;

public class BluetoothCheck {
	private Context mContext;
	private WriteFile2SD wfile;
	private String mPID;
	private String mProcessName;
	private int mStatus = 0;
	private long mCurrentTime=0;
	private int mLogTime=0;
	
	public BluetoothCheck(LoadingService context, WriteFile2SD file, int pid) {
		mContext = context;
		wfile = file; 
		//mProcessName = procName;
		mPID = String.valueOf(pid);
		Log.v(Loading.TAG, "Bluetooth/ mPID="+mPID);
	}

	public void getBluetooth(long ctime) {
		mCurrentTime = ctime;
		final StringBuilder cpuinfo = new StringBuilder();
		boolean flag = false;
		CharSequence cs = mPID;
		try {
            final List<String> commandLine = new ArrayList<String>();
            commandLine.add("dumpsys");
            commandLine.add("bluetooth");
            //commandLine.add(Integer.toString(android.os.Process.myPid()));

            final Process process = Runtime.getRuntime().exec(commandLine.toArray(new String[commandLine.size()]));
            final BufferedReader bufferedReader = new BufferedReader(
	            		new InputStreamReader(process.getInputStream()), 
	            		1024*1024);
            String line = bufferedReader.readLine();
        	String[] s = line.split(" ");
        	if (s[1].equals("OFF")) {
        		mStatus = -1;
        	} else {
	        	for(int i=0;i<30;i++) {
		        	line = bufferedReader.readLine();
		            if (line!=null && line.contains(mPID)) {
		            	Log.v(Loading.TAG, "Bluetooth--"+line);
		            	mStatus = 1;
		            	break;
		            }
	        	}
        	}
            bufferedReader.close();
        } catch (IOException e) {
            //Log.e(Loading.TAG, "DumpSysCollector.meminfo could not retrieve data", e);
        }
	}
	
	protected void printBluetooth(boolean withValue){
		//int cur_log_time=((LoadingService) mContext).getTime(mCurrentTime);
		//mLogTime = cur_log_time;
		String input = "Bluetooth," + mLogTime ;
		if (withValue) {
			input = "Bluetooth," + mLogTime + "," + mStatus;
		}
		if(Loading.DEBUG && Loading.BLUE_DEBUG) Log.v(Loading.TAG, "input: "+input);
        wfile.write(input);
	}
	public void printAll() {
		int cur_log_time=((LoadingService) mContext).getTime(mCurrentTime);

		while (cur_log_time-mLogTime>1) {
			mLogTime++;
			//printBattCurrentNow(false);
			//printBattVol(false);
			//printCapacity(false);
			//printVoltageNow(false);
			//printTemp(false);
			//printCurrentNow(false);
			printBluetooth(false);
		}
		mLogTime = cur_log_time;
		//printBattCurrentNow(true);
		//printBattVol(true);
		//printCapacity(true);
		//printVoltageNow(true);
		//printTemp(true);
		//printCurrentNow(true);
		printBluetooth(true);
	}
}

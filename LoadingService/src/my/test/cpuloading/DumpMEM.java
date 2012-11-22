package my.test.cpuloading;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.util.Log;

public class DumpMEM {
	private Context mContext;
	private WriteFile2SD wfile;
	private int mPID;
	private String mProcessName;
	private String mMemAloc;
	private long mCurrentTime=0;
	private int mLogTime=0;
	private int mAlocMem;
	private int mInit=-1;
	
	private int mInitTime = -1;
	private int mLastTime = -1;
	private int mAmount = 0;
	
	public DumpMEM(LoadingService context, WriteFile2SD file,
			String procName, int pid) {
		mContext = context;
		wfile = file; 
		mProcessName = procName;
		mPID = pid;
		Log.v(Loading.TAG, "DumpMEM/ mProcessName="+mProcessName);
	}

	public void getALOC(long ctime) {
		mCurrentTime = ctime;
		ArrayList<String> result = null;
		
		try {
            final List<String> commandLine = new ArrayList<String>();
            commandLine.add("dumpsys");
            commandLine.add("meminfo");
            commandLine.add(Integer.toString(mPID));

            final Process process = Runtime.getRuntime().exec(commandLine.toArray(new String[commandLine.size()]));
            final BufferedReader bufferedReader = new BufferedReader(
	            		new InputStreamReader(process.getInputStream()), 
	            		1024*1024);
            String line = null;
            for (int count=0;count<7;count++) {
                line = bufferedReader.readLine();
                //Log.v(Loading.TAG, count+" DumpMEM--"+line);
            }
            if (line!=null) {
	            result = ((LoadingService) mContext).dataPasing(line);
	            mAlocMem = Integer.parseInt(result.get(4));
	            //Log.v(Loading.TAG, "mAlocMem: "+mAlocMem);
            }
            bufferedReader.close();
        } catch (IOException e) {
            Log.e(Loading.TAG, "DumpSysCollector.meminfo could not retrieve data", e);
        }
	}

	protected void printALOC(boolean withValue){
        if (mInit==-1) {
        	mInit = mAlocMem; 
        }
        int variation = mAlocMem - mInit;
        String input = "2,MEM," + mLogTime + "," + String.valueOf(variation);
        /*
        String input = "2,MEM," + mLogTime ;
		if (withValue) {
			input = "1,CPU," + mLogTime + "," + String.valueOf(variation);
		}
		*/
		if(Loading.DEBUG && Loading.MEM_DEBUG) Log.v(Loading.TAG, "input: "+input);
        wfile.write(input);
        
        meanValue(withValue, variation);
	}

	public void printAll() {
		int cur_log_time=((LoadingService) mContext).getTime(mCurrentTime);
		while (cur_log_time-mLogTime>1) {
			mLogTime++;
			//Log.i("LoadingService", "===="+mLogTime+"===");
			printALOC(false);
		}
		mLogTime = cur_log_time;
		//Log.i("LoadingService", "===="+mLogTime+"===");
		printALOC(true);
	}
	
	private void meanValue(boolean withValue, int variation) {
		// TODO Auto-generated method stub
        if (mInitTime==-1 && withValue) {
        	mInitTime = mLogTime;
        }
        mLastTime = mLogTime;
        mAmount +=variation;
	}
	public void printMean(){
		if(Loading.DEBUG && Loading.MEM_DEBUG) 
			Log.v(Loading.TAG, "mAmount= "+mAmount+"; mLastTime="+mLastTime+"; mInitTime="+mInitTime);
		String input = "2,MEM, mean," + (float)mAmount/(float)(mLastTime-mInitTime+1);
		wfile.write(input);
	}
}

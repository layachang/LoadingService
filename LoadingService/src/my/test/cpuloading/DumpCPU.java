package my.test.cpuloading;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.util.Log;

public class DumpCPU {
	private Context mContext;
	private WriteFile2SD wfile;
	private String mPID;
	private String mProcessName;
	private String mCurrCpu;
	private long mCurrentTime=0;
	private int mLogTime=0;
	
	private int mInitTime = -1;
	private int mLastTime = -1;
	private float mAmount = 0;

	public DumpCPU(LoadingService context, WriteFile2SD file,
			String procName, int pid) {
		mContext = context;
		wfile = file; 
		mProcessName = procName;
		mPID = String.valueOf(pid);
		Log.v(Loading.TAG, "DumpCPU/ mProcessName="+mProcessName);
	}

	public void getCPULoaging(long ctime) {
		mCurrentTime = ctime;
		final StringBuilder cpuinfo = new StringBuilder();

		try {
            final List<String> commandLine = new ArrayList<String>();
            commandLine.add("dumpsys");
            commandLine.add("cpuinfo");
            //commandLine.add(Integer.toString(android.os.Process.myPid()));

            final Process process = Runtime.getRuntime().exec(commandLine.toArray(new String[commandLine.size()]));
            final BufferedReader bufferedReader = new BufferedReader(
	            		new InputStreamReader(process.getInputStream()), 
	            		1024*1024);
            int line_count = 0;
            for(;;) {
            	line_count++;
                String line = bufferedReader.readLine();
                //Log.v(Loading.TAG, line_count+" DumpCPU--"+line);
                if ( line_count==1 || line_count==2) {
                	if (line.startsWith("Permission Denial")) {
                		Log.e(Loading.TAG, line);
                		break;
                	}
                	continue;
                }

                String s[] = line.split(" ");
                if (s.length<2) {
                	continue;
                }
                
                if (line == null || s[1].equals("TOTAL:") || line_count>50) {
                	break;
                }
                String pid_proc = s[3];
                //Log.v(Loading.TAG, "DumpCPU:"+pid_proc+"; mPID.length()="+mPID.length());
                if (pid_proc!=null && 
                		pid_proc.length() >= mPID.length() &&
                		pid_proc.substring(0,mPID.length()).equals(mPID)) {
                	mCurrCpu = s[2].substring(0, s[2].length()-1);
                	//Log.v(Loading.TAG, "DumpCPU-------"+mCurrCpu);
                	break;
                }
                //cpuinfo.append(line);
                //cpuinfo.append("\n");
            }
            bufferedReader.close();
        } catch (IOException e) {
            //Log.e(Loading.TAG, "DumpSysCollector.meminfo could not retrieve data", e);
        }
	}
	
	private void meanValue(boolean withValue, float cpu) {
		// TODO Auto-generated method stub
        if (mInitTime==-1&&withValue) {
        	mInitTime = mLogTime;
        }
        mLastTime = mLogTime;
        mAmount +=cpu;
	}
	public void printMean(){
		if(Loading.DEBUG && Loading.CPU_DEBUG) 
			Log.v(Loading.TAG, "mAmount= "+mAmount+"; mLastTime="+mLastTime+"; mInitTime="+mInitTime);
		String input = "1,CPU, mean," + mAmount/(float)(mLastTime-mInitTime+1);
		wfile.write(input);
	}
	protected void printCPU(boolean withValue){
		String input = "1,CPU," + mLogTime + "," + mCurrCpu;
		/*
		String input = "1,CPU," + mLogTime;
		if (withValue) {
			input = "1,CPU," + mLogTime + "," + mCurrCpu;
		}
		*/
		if(Loading.DEBUG && Loading.CPU_DEBUG) Log.v(Loading.TAG, "input: "+input);
        wfile.write(input);
        
        if (mCurrCpu!=null)  meanValue(withValue, Float.parseFloat(mCurrCpu));
	}


	public void printAll() {
		int cur_log_time=((LoadingService) mContext).getTime(mCurrentTime);
		while (cur_log_time-mLogTime>1) {
			mLogTime++;
			//Log.i("LoadingService", "===="+mLogTime+"===");
	    	printCPU(false);
		}
		mLogTime = cur_log_time;
		//Log.i("LoadingService", "===="+mLogTime+"===");
    	printCPU(true);
	}
}

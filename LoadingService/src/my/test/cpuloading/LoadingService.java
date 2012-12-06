package my.test.cpuloading;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.BatteryManager;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;

public class LoadingService extends Service  {
	private Handler handler = new Handler();
	
	private String KEY_CACHE = "";
	private String KEY_SYSTEM = "";
	private String KEY_DATA = "";
	private int KEY_FILESYSTEM = 4; //Sector size
	private int READ_LINE= 0;
	private int NUM_CPU= 0;
	private String mFileName;
	private String mProcessName;
	private int mPID;
	private int mUID;
	private long mCount=0;
	private boolean mFirst = true;
	private static long mStartTime=0;

	private static WriteFile2SD wfile;

	private ProcStat mCpuAll;
	private SysCpuLoad mCpuAll15;
	private DumpCPU mCpuPid;
	private MemInfo mMemAll;
	private DumpMEM mMemPid;
	private Diskstats mDiskAll;
	private ProcNetDev mNetAll;
	private NetStats mNetUid;
	private BattInfoProc mBattAll;
	private VisualizerLinster mVolAll;

	private static String[] mBattState = {"n/a","UNKNOWN","CHARGING","DISCHARGING","NOT_CHARGING","FULL"} ; 
	private static String[] mBattHealth = {"n/a","UNKNOWN","GOOD","OVERHEAT","DEAD","OVER_VOLTAGE","UNSPECIFIED_FAILURE","COLD"} ;

    @Override
    public void onStart(Intent intent, int startId) {
	    handleCommand(intent);
    	final int pid = mPID;
    	final int uid = mUID;

	    Log.v(Loading.TAG,"!!!!!!!!!! onStart !!!!!!!!!!VERSION.SDK_INT="+VERSION.SDK_INT);
        handler.postDelayed(catchData, 800);

        wfile = new WriteFile2SD(mFileName);

        mCpuAll = new ProcStat(pid,uid,NUM_CPU);
        mCpuPid = new DumpCPU(pid,uid);
        mMemAll = new MemInfo();
        mMemPid = new DumpMEM(pid);
        mDiskAll = new Diskstats(KEY_CACHE, KEY_SYSTEM, KEY_DATA, KEY_FILESYSTEM);
        mNetAll = new ProcNetDev(READ_LINE);
        mNetUid = new NetStats(uid);
        mBattAll = new BattInfoProc();
        mVolAll = new VisualizerLinster();
	}
    
    private void handleCommand(Intent intent) {
    	if (intent!=null) {
	    	Bundle b = intent.getExtras();
	    	READ_LINE =b.getInt("READ_LINE");
	    	NUM_CPU = b.getInt("NUM_CPU");
	    	KEY_CACHE = b.getString("KEY_CACHE");
	    	KEY_SYSTEM = b.getString("KEY_SYSTEM");
	    	KEY_DATA = b.getString("KEY_DATA");
	    	KEY_FILESYSTEM = b.getInt("KEY_FILESYSTEM");
	    	mFileName = b.getString("FILE_NAME");
	    	mProcessName = b.getString("PROCESS_NAME");
	    	mPID = b.getInt("PID");
	    	mUID = b.getInt("UID");
    	} else {
    		this.stopSelf();
    		Log.v(Loading.TAG,"!!!!!!!!!! KILL SERVICE !!!!!!!!!!");
    	}
	}

	@Override
    public void onDestroy() {
		//print mean value
		mVolAll.destory();
		handler.removeCallbacks(catchData);
        if(wfile!=null) {
        	wfile.close();
        }
        super.onDestroy();
    }
     
    private Runnable catchData = new Runnable() {
        public void run() {
        	long ctime = System.currentTimeMillis();
        	//Log.v(Loading.TAG,String.valueOf(ctime));
            //log�ثe�ɶ�
            dumpValues();
            printValues();
            handler.postDelayed(this, 300);
        }
    };

	protected void dumpValues() {
    	if (mFirst) {
    		mStartTime = System.currentTimeMillis();
            //printProcCpuinfo();
    		mCpuAll.dumpValues();   	//CPU
            mMemAll.dumpValues();      	//MEM
        	mDiskAll.dumpValues();      //Flash
            mNetAll.dumpValues();    	//Network
            try {
                Thread.sleep(360);
            } catch (Exception e) {}
        } else {
        	mCpuAll.dumpValues();
        	mCpuPid.dumpValues();
        	mMemAll.dumpValues();
        	mMemPid.dumpValues();
        	mDiskAll.dumpValues();
        	mNetAll.dumpValues();
        	mNetUid.dumpValues();
        	mBattAll.dumpValues();
        	mVolAll.dumpValues();
        }
	}


	protected void printValues() {
		if (mFirst) {
			mFirst = false;
			StringBuffer input = new StringBuffer();
			input.append(",CPU ALL,CPU PID,MEM ALL, MEM PID,DISK ALL,DISK READ,DISK WRITE,NET ALL, NET UID,BATT,AUDIO ");
			wfile.write(input.toString());
			return;
		}
		long currTime = System.currentTimeMillis();
		StringBuffer input = new StringBuffer();
		input.append(currTime-mStartTime); input.append(",");
		input.append(mCpuAll.getValues(BasicFunc.CPU_ALL_INDEX)); input.append(",");
		input.append(mCpuPid.getValues(BasicFunc.CPU_PID_INDEX)); input.append(",");
		input.append(mMemAll.getValues(BasicFunc.MEM_ALL_INDEX)); input.append(",");
		input.append(mMemPid.getValues(BasicFunc.MEM_PID_INDEX)); input.append(",");
		input.append(mDiskAll.getValues(BasicFunc.DISK_ALL_INDEX)); input.append(",");
		input.append(mDiskAll.getValues(BasicFunc.DISK_READ_INDEX)); input.append(",");
		input.append(mDiskAll.getValues(BasicFunc.DISK_WRITE_INDEX)); input.append(",");
		input.append(mNetAll.getValues(BasicFunc.NET_ALL_INDEX)); input.append(",");
		input.append(mNetUid.getValues(BasicFunc.NET_UID_INDEX)); input.append(",-");
		input.append(mBattAll.getValues(BasicFunc.BATT_ALL_INDEX)); input.append(",");
		input.append(mVolAll.getValues(BasicFunc.AUDIO_ALL_INDEX)); input.append(",");

		wfile.write(input.toString());
	}
	protected void printProcCpuinfo() {
	    try {
	        String load ;
	        RandomAccessFile reader = new RandomAccessFile(Loading.STR_CPU_CPUINFO, "r");
	        for(;;) {
	            load = reader.readLine();
	            if (load!=null) {
	                wfile.write(load);
	            } else {
	                reader.close();
	                break;
	            }
	        }
	    }
	    catch (IOException ex) {
	    	ex.printStackTrace();
	    }
	}

	protected int getTime(long time){
        return (int)(time-mStartTime);
    }

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}
	
	private class CpuAll {
		private Object mMatchObject;
		public CpuAll(int pid, int uid, int cpu_num) {
			if (VERSION.SDK_INT!=15)
				mMatchObject = new ProcStat(pid,uid,cpu_num);
			else
				mMatchObject = new SysCpuLoad();
		}
		public void dumpValues(){
			if (VERSION.SDK_INT!=15)
				((ProcStat)mMatchObject).dumpValues();
			else
				((SysCpuLoad)mMatchObject).dumpValues();
		}
		public String getValues(int index) {
			if (VERSION.SDK_INT!=15)
				return ((ProcStat)mMatchObject).getValues(index);
			else
				return ((SysCpuLoad)mMatchObject).getValues(index);
		}
	}
}

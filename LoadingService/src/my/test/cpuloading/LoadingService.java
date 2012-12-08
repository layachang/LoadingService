package my.test.cpuloading;

import android.app.ActivityManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.BatteryManager;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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
	private int mPID=0;
	private int mUID=0;
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
	    Log.v(Loading.TAG,"onStart() SDK="+VERSION.SDK_INT);
        handler.postDelayed(catchData, 800);

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
    	} else {
    		this.stopSelf();
    		Log.v(Loading.TAG,"!!!!!!!!!! KILL SERVICE !!!!!!!!!!");
    	}
	}

	@Override
    public void onDestroy() {
		stopAll();

        super.onDestroy();
    }
     
    private void stopAll() {
		//print mean value
		printMean();
		if(mVolAll!=null) mVolAll.destory();
		handler.removeCallbacks(catchData);
        if(wfile!=null) {
        	wfile.close();
        }
		
	}

	private Runnable catchData = new Runnable() {
        public void run() {
        	long ctime = System.currentTimeMillis();
            //log�ثe�ɶ�
    		if (mPID!=0 && mUID!=0) {
                dumpValues();
                printValues();
                if (isPkgAvaliable(mProcessName)) {
                	handler.postDelayed(this, 300);
                } else {
                	stopSelf();
                }
    		} else {
    			getPIDUIDByPKG(mProcessName);
    			handler.postDelayed(this, 300);
    		}
        }
    };
    private void startCacheData() {
        wfile = new WriteFile2SD(mFileName);

        mCpuAll = new ProcStat(mPID,mUID,NUM_CPU);
        mCpuPid = new DumpCPU(mPID,mPID);
        mMemAll = new MemInfo();
        mMemPid = new DumpMEM(mPID);
        mDiskAll = new Diskstats(KEY_CACHE, KEY_SYSTEM, KEY_DATA, KEY_FILESYSTEM);
        mNetAll = new ProcNetDev(READ_LINE);
        mNetUid = new NetStats(mPID);
        mBattAll = new BattInfoProc();
        mVolAll = new VisualizerLinster();
	}
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
	protected void printMean() {
		StringBuffer input = new StringBuffer();
		input.append(",");
		input.append(mCpuAll.getMean(BasicFunc.CPU_ALL_INDEX)); input.append(",");
		input.append(mCpuPid.getMean(BasicFunc.CPU_PID_INDEX)); input.append(",");
		input.append(mMemAll.getMean(BasicFunc.MEM_ALL_INDEX)); input.append(",");
		input.append(mMemPid.getMean(BasicFunc.MEM_PID_INDEX)); input.append(",");
		input.append(mDiskAll.getMean(BasicFunc.DISK_ALL_INDEX)); input.append(",");
		input.append(mDiskAll.getMean(BasicFunc.DISK_READ_INDEX)); input.append(",");
		input.append(mDiskAll.getMean(BasicFunc.DISK_WRITE_INDEX)); input.append(",");
		input.append(mNetAll.getMean(BasicFunc.NET_ALL_INDEX)); input.append(",");
		input.append(mNetUid.getMean(BasicFunc.NET_UID_INDEX)); input.append(",-");
		input.append(mBattAll.getMean(BasicFunc.BATT_ALL_INDEX)); input.append(",");
		input.append(mVolAll.getMean(BasicFunc.AUDIO_ALL_INDEX)); input.append(",");

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

	private void getPIDUIDByPKG(String pkgName) {
    	ActivityManager am = (ActivityManager)this.getApplicationContext().getSystemService(ACTIVITY_SERVICE);
        Iterator i = am.getRunningAppProcesses().iterator();
        while(i.hasNext()) {
        	ActivityManager.RunningAppProcessInfo info = (ActivityManager.RunningAppProcessInfo)(i.next());
        	try {
        		if(info.processName.equals(pkgName)) {
        			Log.v(Loading.TAG,"pkg="+pkgName+"; pid="+info.pid+"; uid="+info.uid);
        			if (info.uid!=0 &&info.pid!=0) {
        				mPID = info.pid;
        				mUID = info.uid;
        				startCacheData();
        				return ;
        			}
        		}
        	} catch(Exception e) {
        		Log.d(Loading.TAG, "Error>> :"+ e.toString());
        	}
        }
    }

    private boolean isPkgAvaliable(String pkgName) {
    	ActivityManager am = (ActivityManager)this.getApplicationContext().getSystemService(ACTIVITY_SERVICE);
        Iterator i = am.getRunningAppProcesses().iterator();
        while(i.hasNext())  {
            ActivityManager.RunningAppProcessInfo info = (ActivityManager.RunningAppProcessInfo)(i.next());
            try { 
                if(info.processName.equals(pkgName)) {
                    return true;
                }
            } catch(Exception e) {
            	Log.d(Loading.TAG, "Error>> :"+ e.toString());
            }
        }
        return false;
    }
}

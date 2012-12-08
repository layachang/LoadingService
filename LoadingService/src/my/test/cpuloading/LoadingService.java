package my.test.cpuloading;

import android.app.ActivityManager;
import android.app.Service;
import android.content.Intent;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Iterator;

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
	private boolean mFirst = true;
	private static long mStartTime=0;

	private static WriteFile2SD wfile;
	private ProcStat mCpuAll;
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
		printMedian();
		printVar();		//Must after printMean()
		printSD();		//Must after printVar()
		StringBuffer input = new StringBuffer();
		input.append(",CPU ALL,CPU PID,MEM ALL, MEM PID,DISK ALL,DISK READ,DISK WRITE,NET ALL,NET REC,NET TRA, NET UID,BATT,AUDIO ");
		input.append(",%,%,%,%,KB,KB,KB,KB,KB,KB,KB,%, ");
		wfile.write(input.toString());
		if(mVolAll!=null) mVolAll.destory();
		handler.removeCallbacks(catchData);
        if(wfile!=null) {
        	wfile.close();
        }
		
	}

	private Runnable catchData = new Runnable() {
        public void run() {
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
			StringBuffer input1 = new StringBuffer();
			input1.append(",CPU ALL,CPU PID,MEM ALL, MEM PID,DISK READ,DISK WRITE,DISK ALL,NET ALL,NET REC,NET TRA, NET UID,AUDIO,BATT");
			wfile.write(input1.toString());
			StringBuffer input2 = new StringBuffer();
			input2.append(",%,%,%,%,KB,KB,KB,KB,KB,KB,KB,%, ");
			wfile.write(input2.toString());
			return;
		}
		long currTime = System.currentTimeMillis();
		StringBuffer input = new StringBuffer();
		input.append(currTime-mStartTime); input.append(",");
		input.append(mCpuAll.getValues(BasicFunc.CPU_ALL_INDEX)); input.append(",");
		input.append(mCpuPid.getValues(BasicFunc.CPU_PID_INDEX)); input.append(",");
		input.append(mMemAll.getValues(BasicFunc.MEM_ALL_INDEX)); input.append(",");
		input.append(mMemPid.getValues(BasicFunc.MEM_PID_INDEX)); input.append(",");
		input.append(mDiskAll.getValues(BasicFunc.DISK_READ_INDEX)); input.append(",");
		input.append(mDiskAll.getValues(BasicFunc.DISK_WRITE_INDEX)); input.append(",");
		input.append(mDiskAll.getValues(BasicFunc.DISK_ALL_INDEX)); input.append(",");
		input.append(mNetAll.getValues(BasicFunc.NET_ALL_INDEX)); input.append(",");
		input.append(mNetAll.getValues(BasicFunc.NET_REC_INDEX)); input.append(",");
		input.append(mNetAll.getValues(BasicFunc.NET_TRA_INDEX)); input.append(",");
		input.append(mNetUid.getValues(BasicFunc.NET_UID_INDEX)); input.append(",-");
		input.append(mVolAll.getValues(BasicFunc.AUDIO_ALL_INDEX)); input.append(",");
		input.append(mBattAll.getValues(BasicFunc.BATT_ALL_INDEX)); input.append(",");

		wfile.write(input.toString());
	}

	protected void printMean() {
		StringBuffer input = new StringBuffer();
		input.append("Mean,");
		input.append(mCpuAll.getMean(BasicFunc.CPU_ALL_INDEX)); input.append(",");
		input.append(mCpuPid.getMean(BasicFunc.CPU_PID_INDEX)); input.append(",");
		input.append(mMemAll.getMean(BasicFunc.MEM_ALL_INDEX)); input.append(",");
		input.append(mMemPid.getMean(BasicFunc.MEM_PID_INDEX)); input.append(",");
		input.append(mDiskAll.getMean(BasicFunc.DISK_READ_INDEX)); input.append(",");
		input.append(mDiskAll.getMean(BasicFunc.DISK_WRITE_INDEX)); input.append(",");
		input.append(mDiskAll.getMean(BasicFunc.DISK_ALL_INDEX)); input.append(",");
		input.append(mNetAll.getMean(BasicFunc.NET_ALL_INDEX)); input.append(",");
		input.append(mNetAll.getMean(BasicFunc.NET_REC_INDEX)); input.append(",");
		input.append(mNetAll.getMean(BasicFunc.NET_TRA_INDEX)); input.append(",");
		input.append(mNetUid.getMean(BasicFunc.NET_UID_INDEX)); input.append(",-");
		input.append(mVolAll.getMean(BasicFunc.AUDIO_ALL_INDEX)); input.append(",");
		input.append(mBattAll.getMean(BasicFunc.BATT_ALL_INDEX)); input.append(",");
		wfile.write(input.toString());
	}

	private void printMedian() {
		StringBuffer input = new StringBuffer();
		input.append("Median,");
		input.append(mCpuAll.getMedian(BasicFunc.CPU_ALL_INDEX)); input.append(",");
		input.append(mCpuPid.getMedian(BasicFunc.CPU_PID_INDEX)); input.append(",");
		input.append(mMemAll.getMedian(BasicFunc.MEM_ALL_INDEX)); input.append(",");
		input.append(mMemPid.getMedian(BasicFunc.MEM_PID_INDEX)); input.append(",");
		input.append(mDiskAll.getMedian(BasicFunc.DISK_READ_INDEX)); input.append(",");
		input.append(mDiskAll.getMedian(BasicFunc.DISK_WRITE_INDEX)); input.append(",");
		input.append(mDiskAll.getMedian(BasicFunc.DISK_ALL_INDEX)); input.append(",");
		input.append(mNetAll.getMedian(BasicFunc.NET_ALL_INDEX)); input.append(",");
		input.append(mNetAll.getMedian(BasicFunc.NET_REC_INDEX)); input.append(",");
		input.append(mNetAll.getMedian(BasicFunc.NET_TRA_INDEX)); input.append(",");
		input.append(mNetUid.getMedian(BasicFunc.NET_UID_INDEX)); input.append(",-");
		input.append(mVolAll.getMedian(BasicFunc.AUDIO_ALL_INDEX)); input.append(",");
		input.append(mBattAll.getMedian(BasicFunc.BATT_ALL_INDEX)); input.append(",");
		wfile.write(input.toString());
	}

	private void printVar() {
		StringBuffer input = new StringBuffer();
		input.append("Variance,");
		input.append(mCpuAll.getVariance(BasicFunc.CPU_ALL_INDEX)); input.append(",");
		input.append(mCpuPid.getVariance(BasicFunc.CPU_PID_INDEX)); input.append(",");
		input.append(mMemAll.getVariance(BasicFunc.MEM_ALL_INDEX)); input.append(",");
		input.append(mMemPid.getVariance(BasicFunc.MEM_PID_INDEX)); input.append(",");
		input.append(mDiskAll.getVariance(BasicFunc.DISK_READ_INDEX)); input.append(",");
		input.append(mDiskAll.getVariance(BasicFunc.DISK_WRITE_INDEX)); input.append(",");
		input.append(mDiskAll.getVariance(BasicFunc.DISK_ALL_INDEX)); input.append(",");
		input.append(mNetAll.getVariance(BasicFunc.NET_ALL_INDEX)); input.append(",");
		input.append(mNetAll.getVariance(BasicFunc.NET_REC_INDEX)); input.append(",");
		input.append(mNetAll.getVariance(BasicFunc.NET_TRA_INDEX)); input.append(",");
		input.append(mNetUid.getVariance(BasicFunc.NET_UID_INDEX)); input.append(",-");
		input.append(mBattAll.getVariance(BasicFunc.BATT_ALL_INDEX)); input.append(",");
		input.append(mVolAll.getVariance(BasicFunc.AUDIO_ALL_INDEX)); input.append(",");
		wfile.write(input.toString());
	}
	
	private void printSD() {
		StringBuffer input = new StringBuffer();
		input.append("SD,");
		input.append(mCpuAll.getSD(BasicFunc.CPU_ALL_INDEX)); input.append(",");
		input.append(mCpuPid.getSD(BasicFunc.CPU_PID_INDEX)); input.append(",");
		input.append(mMemAll.getSD(BasicFunc.MEM_ALL_INDEX)); input.append(",");
		input.append(mMemPid.getSD(BasicFunc.MEM_PID_INDEX)); input.append(",");
		input.append(mDiskAll.getSD(BasicFunc.DISK_READ_INDEX)); input.append(",");
		input.append(mDiskAll.getSD(BasicFunc.DISK_WRITE_INDEX)); input.append(",");
		input.append(mDiskAll.getSD(BasicFunc.DISK_ALL_INDEX)); input.append(",");
		input.append(mNetAll.getSD(BasicFunc.NET_ALL_INDEX)); input.append(",");
		input.append(mNetAll.getSD(BasicFunc.NET_REC_INDEX)); input.append(",");
		input.append(mNetAll.getSD(BasicFunc.NET_TRA_INDEX)); input.append(",");
		input.append(mNetUid.getSD(BasicFunc.NET_UID_INDEX)); input.append(",-");
		input.append(mBattAll.getSD(BasicFunc.BATT_ALL_INDEX)); input.append(",");
		input.append(mVolAll.getSD(BasicFunc.AUDIO_ALL_INDEX)); input.append(",");
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
		return null;
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

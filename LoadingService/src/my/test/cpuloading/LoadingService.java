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
import java.util.ArrayList;
import java.util.HashMap;
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
    private boolean hasValue = false;
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

    private HashMap<String,ArrayList<Integer>> mResources = new HashMap<String,ArrayList<Integer>>();

    @Override
    public void onStart(Intent intent, int startId) {
        handleCommand(intent);
        startCacheData();
        Log.v(Loading.TAG,"onStart() SDK="+VERSION.SDK_INT);
        handler.postDelayed(catchData, 800);
        estResTree();
    }

    private void estResTree() {
        ArrayList<Integer> cpu_all = new ArrayList<Integer>();
        cpu_all.add(BasicFunc.CPU_ALL_INDEX);
        mResources.put("CpuAll", cpu_all);

        ArrayList<Integer> cpu_pid = new ArrayList<Integer>();
        cpu_pid.add(BasicFunc.CPU_PID_INDEX);
        mResources.put("CpuPid", cpu_pid);

        ArrayList<Integer> mem_all = new ArrayList<Integer>();
        mem_all.add(BasicFunc.MEM_USED_INDEX);
        mem_all.add(BasicFunc.MEM_FREE_INDEX);
        mem_all.add(BasicFunc.MEM_BUFF_INDEX);
        mem_all.add(BasicFunc.MEM_CACHED_INDEX);
        mem_all.add(BasicFunc.MEM_ACTIVE_INDEX);
        mem_all.add(BasicFunc.MEM_INACTIVE_INDEX);
        mResources.put("MemAll", mem_all);

        ArrayList<Integer> mem_pid = new ArrayList<Integer>();
        mem_pid.add(BasicFunc.MEM_PID_INDEX);
        mResources.put("MemPid", mem_pid);

        ArrayList<Integer> disk_all = new ArrayList<Integer>();
        disk_all.add(BasicFunc.DISK_READ_INDEX);
        disk_all.add(BasicFunc.DISK_WRITE_INDEX);
        disk_all.add(BasicFunc.DISK_ALL_INDEX);
        mResources.put("DiskAll", disk_all);

        ArrayList<Integer> net_all = new ArrayList<Integer>();
        net_all.add(BasicFunc.NET_REC_INDEX);
        net_all.add(BasicFunc.NET_TRA_INDEX);
        net_all.add(BasicFunc.NET_ALL_INDEX);
        mResources.put("NetAll", net_all);

        ArrayList<Integer> net_uid = new ArrayList<Integer>();
        net_uid.add(BasicFunc.NET_UID_INDEX);
        mResources.put("NetUid", net_uid);

        ArrayList<Integer> voice_all = new ArrayList<Integer>();
        voice_all.add(BasicFunc.AUDIO_ALL_INDEX);
        mResources.put("VolAll", voice_all);

        ArrayList<Integer> batt_all = new ArrayList<Integer>();
        batt_all.add(BasicFunc.BATT_ALL_INDEX);
        mResources.put("BattAll", batt_all);
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
        printVar();        //Must after printMean()
        printSD();        //Must after printVar()
        printHeadLine();
        if(mVolAll!=null) mVolAll.destory();
        handler.removeCallbacks(catchData);
        if(wfile!=null) {
            wfile.close();
        }
    }

    private void printHeadLine() {
        StringBuffer input = new StringBuffer();
        for (int i=0;i<BasicFunc.mClassName.length;i++) {
            input.append(BasicFunc.mClassName[i]+",");
        }
        wfile.write(input.toString());
        input = new StringBuffer();
        for (int i=0;i<BasicFunc.mResourceName.length;i++) {
            input.append(BasicFunc.mResourceName[i]+",");
        }
        wfile.write(input.toString());
        input = new StringBuffer();
        for (int i=0;i<BasicFunc.Unit.length;i++) {
            input.append(BasicFunc.Unit[i]+",");
        }
        wfile.write(input.toString());
    }

    private Runnable catchData = new Runnable() {
        public void run() {
            if (hasValue) {
                dumpValues();
                printValues();
                if (isPkgAvaliable(mProcessName)) {
                    handler.postDelayed(this, 300);
                } else {
                    stopSelf();
                }
            } else {
                getPIDUIDByPKG(mProcessName);
                dumpValues();
                printValues();
                handler.postDelayed(this, 300);
            }
        }
    };
    private void startCacheData() {
        wfile = new WriteFile2SD(mFileName);
        mCpuAll = new ProcStat(NUM_CPU);
        mCpuPid = new DumpCPU(mPID);
        mMemAll = new MemInfo();
        mMemPid = new DumpMEM(mPID);
        mDiskAll = new Diskstats(KEY_CACHE, KEY_SYSTEM, KEY_DATA, KEY_FILESYSTEM);
        mNetAll = new ProcNetDev(READ_LINE);
        mNetUid = new NetStats(mUID);
        mBattAll = new BattInfoProc();
        mVolAll = new VisualizerLinster();
    }
    protected void dumpValues() {
        if (mFirst) {
            mStartTime = System.currentTimeMillis();
            //printProcCpuinfo();
            mCpuAll.dumpValues();       //CPU
            mMemAll.dumpValues();       //MEM
            mDiskAll.dumpValues();      //Flash
            mNetAll.dumpValues();       //Network
            try {
                Thread.sleep(360);
            } catch (Exception e) {}
        } else {
            mCpuAll.dumpValues();
            mMemAll.dumpValues();
            mDiskAll.dumpValues();
            mNetAll.dumpValues();
            mBattAll.dumpValues();
            if (hasValue) {
                mCpuPid.dumpValues();
                mMemPid.dumpValues();
                mNetUid.dumpValues();
            }
        }
    }

    protected void printValues() {
        if (mFirst) {
            mFirst = false;
            printHeadLine();
            return;
        }
        long currTime = System.currentTimeMillis();
        StringBuffer input = new StringBuffer();
        input.append(currTime-mStartTime); input.append(",");

        ArrayList<Integer> cpu_all = mResources.get("CpuAll");
        for (int i: cpu_all) {
            input.append(mCpuAll.getValues(i)); input.append(",");
        }
        ArrayList<Integer> cpu_pid = mResources.get("CpuPid");
        for (int i: cpu_pid) {
            input.append(mCpuPid.getValues(i)); input.append(",");
        }
        ArrayList<Integer> mem_all = mResources.get("MemAll");
        for (int i: mem_all) {
            input.append(mMemAll.getValues(i)); input.append(",");
        }
        ArrayList<Integer> mem_pid = mResources.get("MemPid");
        for (int i: mem_pid) {
            input.append(mMemPid.getValues(i)); input.append(",");
        }
        ArrayList<Integer> disk_all = mResources.get("DiskAll");
        for (int i: disk_all) {
            input.append(mDiskAll.getValues(i)); input.append(",");
        }
        ArrayList<Integer> net_all = mResources.get("NetAll");
        for (int i: net_all) {
            Log.v(Loading.TAG,"mNetAll.getValues("+i+")");
            input.append(mNetAll.getValues(i)); input.append(",");
        }
        ArrayList<Integer> net_uid = mResources.get("NetUid");
        for (int i: net_uid) {
            input.append(mNetUid.getValues(i)); input.append(",");
        }
        ArrayList<Integer> voice_all = mResources.get("VolAll");
        for (int i: voice_all) {
            input.append(mVolAll.getValues(i)); input.append(",");
        }
        ArrayList<Integer> batt_all = mResources.get("BattAll");
        for (int i: batt_all) {
            input.append(mBattAll.getValues(i)); input.append(",");
        }

        wfile.write(input.toString());
    }

    protected void printMean() {
        StringBuffer input = new StringBuffer();
        input.append("Mean,");
        //ProcStat
        if (hasValue) {
            ArrayList<Integer> cpu_all = mResources.get("CpuAll");
            for (int i: cpu_all) {
                input.append(mCpuAll.getMean(i)); input.append(",");
            }
            ArrayList<Integer> cpu_pid = mResources.get("CpuPid");
            for (int i: cpu_pid) {
                input.append(mCpuPid.getMean(i)); input.append(",");
            }
            ArrayList<Integer> mem_all = mResources.get("MemAll");
            for (int i: mem_all) {
                input.append(mMemAll.getMean(i)); input.append(",");
            }
            ArrayList<Integer> mem_pid = mResources.get("MemPid");
            for (int i: mem_pid) {
                input.append(mMemPid.getMean(i)); input.append(",");
            }
            ArrayList<Integer> disk_all = mResources.get("DiskAll");
            for (int i: disk_all) {
                input.append(mDiskAll.getMean(i)); input.append(",");
            }
            ArrayList<Integer> net_all = mResources.get("NetAll");
            for (int i: net_all) {
                input.append(mNetAll.getMean(i)); input.append(",");
            }
            ArrayList<Integer> net_uid = mResources.get("NetUid");
            for (int i: net_uid) {
                input.append(mNetUid.getMean(i)); input.append(",");
            }
            ArrayList<Integer> voice_all = mResources.get("VolAll");
            for (int i: voice_all) {
                input.append(mVolAll.getMean(i)); input.append(",");
            }
            ArrayList<Integer> batt_all = mResources.get("BattAll");
            for (int i: batt_all) {
                input.append(mBattAll.getMean(i)); input.append(",");
            }
        }
        wfile.write(input.toString());
    }

    private void printMedian() {
        StringBuffer input = new StringBuffer();
        input.append("Median,");
        if (hasValue) {
            ArrayList<Integer> cpu_all = mResources.get("CpuAll");
            for (int i: cpu_all) {
                input.append(mCpuAll.getMedian(i)); input.append(",");
            }
            ArrayList<Integer> cpu_pid = mResources.get("CpuPid");
            for (int i: cpu_pid) {
                input.append(mCpuPid.getMedian(i)); input.append(",");
            }
            ArrayList<Integer> mem_all = mResources.get("MemAll");
            for (int i: mem_all) {
                input.append(mMemAll.getMedian(i)); input.append(",");
            }
            ArrayList<Integer> mem_pid = mResources.get("MemPid");
            for (int i: mem_pid) {
                input.append(mMemPid.getMedian(i)); input.append(",");
            }
            ArrayList<Integer> disk_all = mResources.get("DiskAll");
            for (int i: disk_all) {
                input.append(mDiskAll.getMedian(i)); input.append(",");
            }
            ArrayList<Integer> net_all = mResources.get("NetAll");
            for (int i: net_all) {
                input.append(mNetAll.getMedian(i)); input.append(",");
            }
            ArrayList<Integer> net_uid = mResources.get("NetUid");
            for (int i: net_uid) {
                input.append(mNetUid.getMedian(i)); input.append(",");
            }
            ArrayList<Integer> voice_all = mResources.get("VolAll");
            for (int i: voice_all) {
                input.append(mVolAll.getMedian(i)); input.append(",");
            }
            ArrayList<Integer> batt_all = mResources.get("BattAll");
            for (int i: batt_all) {
                input.append(mBattAll.getMedian(i)); input.append(",");
            }
        }
        wfile.write(input.toString());
    }

    private void printVar() {
        StringBuffer input = new StringBuffer();
        input.append("Variance,");
        if (hasValue) {
            ArrayList<Integer> cpu_all = mResources.get("CpuAll");
            for (int i: cpu_all) {
                input.append(mCpuAll.getVariance(i)); input.append(",");
            }
            ArrayList<Integer> cpu_pid = mResources.get("CpuPid");
            for (int i: cpu_pid) {
                input.append(mCpuPid.getVariance(i)); input.append(",");
            }
            ArrayList<Integer> mem_all = mResources.get("MemAll");
            for (int i: mem_all) {
                input.append(mMemAll.getVariance(i)); input.append(",");
            }
            ArrayList<Integer> mem_pid = mResources.get("MemPid");
            for (int i: mem_pid) {
                input.append(mMemPid.getVariance(i)); input.append(",");
            }
            ArrayList<Integer> disk_all = mResources.get("DiskAll");
            for (int i: disk_all) {
                input.append(mDiskAll.getVariance(i)); input.append(",");
            }
            ArrayList<Integer> net_all = mResources.get("NetAll");
            for (int i: net_all) {
                input.append(mNetAll.getVariance(i)); input.append(",");
            }
            ArrayList<Integer> net_uid = mResources.get("NetUid");
            for (int i: net_uid) {
                input.append(mNetUid.getVariance(i)); input.append(",");
            }
            ArrayList<Integer> voice_all = mResources.get("VolAll");
            for (int i: voice_all) {
                input.append(mVolAll.getVariance(i)); input.append(",");
            }
            ArrayList<Integer> batt_all = mResources.get("BattAll");
            for (int i: batt_all) {
                input.append(mBattAll.getVariance(i)); input.append(",");
            }
        }
        wfile.write(input.toString());
    }

    private void printSD() {
        StringBuffer input = new StringBuffer();
        input.append("SD,");
        if (hasValue) {
            ArrayList<Integer> cpu_all = mResources.get("CpuAll");
            for (int i: cpu_all) {
                input.append(mCpuAll.getSD(i)); input.append(",");
            }
            ArrayList<Integer> cpu_pid = mResources.get("CpuPid");
            for (int i: cpu_pid) {
                input.append(mCpuPid.getSD(i)); input.append(",");
            }
            ArrayList<Integer> mem_all = mResources.get("MemAll");
            for (int i: mem_all) {
                input.append(mMemAll.getSD(i)); input.append(",");
            }
            ArrayList<Integer> mem_pid = mResources.get("MemPid");
            for (int i: mem_pid) {
                input.append(mMemPid.getSD(i)); input.append(",");
            }
            ArrayList<Integer> disk_all = mResources.get("DiskAll");
            for (int i: disk_all) {
                input.append(mDiskAll.getSD(i)); input.append(",");
            }
            ArrayList<Integer> net_all = mResources.get("NetAll");
            for (int i: net_all) {
                input.append(mNetAll.getSD(i)); input.append(",");
            }
            ArrayList<Integer> net_uid = mResources.get("NetUid");
            for (int i: net_uid) {
                input.append(mNetUid.getSD(i)); input.append(",");
            }
            ArrayList<Integer> voice_all = mResources.get("VolAll");
            for (int i: voice_all) {
                input.append(mVolAll.getSD(i)); input.append(",");
            }
            ArrayList<Integer> batt_all = mResources.get("BattAll");
            for (int i: batt_all) {
                input.append(mBattAll.getSD(i)); input.append(",");
            }
        }
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
                        clearAllRecord();
                        hasValue = true;
                        mPID = info.pid;
                        mUID = info.uid;
                        mCpuPid.setPid(mPID);
                        mMemPid.setPid(mPID);
                        mNetUid.setUid(mUID);
                        return ;
                    }
                }
            } catch(Exception e) {
                Log.d(Loading.TAG, "Error>> :"+ e.toString());
            }
        }
    }

    private void clearAllRecord() {
        ArrayList<Integer> cpu_all = mResources.get("CpuAll");
        for (int i: cpu_all) {
            mCpuAll.clearAllRecord(i);
        }
        ArrayList<Integer> cpu_pid = mResources.get("CpuPid");
        for (int i: cpu_pid) {
            mCpuPid.clearAllRecord(i);
        }
        ArrayList<Integer> mem_all = mResources.get("MemAll");
        for (int i: mem_all) {
            mMemAll.clearAllRecord(i);
        }
        ArrayList<Integer> mem_pid = mResources.get("MemPid");
        for (int i: mem_pid) {
            mMemPid.clearAllRecord(i);
        }
        ArrayList<Integer> disk_all = mResources.get("DiskAll");
        for (int i: disk_all) {
            mDiskAll.clearAllRecord(i);
        }
        ArrayList<Integer> net_all = mResources.get("NetAll");
        for (int i: net_all) {
            mNetAll.clearAllRecord(i);
        }
        ArrayList<Integer> net_uid = mResources.get("NetUid");
        for (int i: net_uid) {
            mNetUid.clearAllRecord(i);
        }
        ArrayList<Integer> voice_all = mResources.get("VolAll");
        for (int i: voice_all) {
            mVolAll.clearAllRecord(i);
        }
        ArrayList<Integer> batt_all = mResources.get("BattAll");
        for (int i: batt_all) {
            mBattAll.clearAllRecord(i);
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

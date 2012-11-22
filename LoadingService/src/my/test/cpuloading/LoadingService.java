package my.test.cpuloading;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Date;

public class LoadingService extends Service  {
	private Handler handler = new Handler();
	
	private String KEY_CACHE = "";
	private String KEY_SYSTEM = "";
	private String KEY_DATA = "";
	private int READ_LINE= 0;
	private int NUM_CPU= 0;
	private String mFileName;
	private String mProcessName;
	private int mPID;
	private int mUID;
	private long mCount=0;
	private static long mStartTime=0;

	private MemInfo memInfo;
	//private ProcStat procStat;
	private ProcNetDev procNetDev;
	private BattInfoProc battInfoProc;
	private static WriteFile2SD wfile;
	private Diskstats diskstats;

	private DumpCPU mDumpCpu;
	private DumpMEM mDumpMem;
	private NetStats mNetStats;

	private static String[] mBattState = {"n/a","UNKNOWN","CHARGING","DISCHARGING","NOT_CHARGING","FULL"} ; 
	private static String[] mBattHealth = {"n/a","UNKNOWN","GOOD","OVERHEAT","DEAD","OVER_VOLTAGE","UNSPECIFIED_FAILURE","COLD"} ; 
	private BluetoothCheck mBluetooth;

	//private AudioCheck mAudio;
	private VisualizerLinster mVisualizer;

	private GPSCheck mGPS;
	//private final IBinder mBinder = new LocalBinder();

	//private LoadingService mBoundService;
	
    //public class LocalBinder extends Binder {
    //	LoadingService getService() {
    //        return LoadingService.this;
    //    }
    //}
    
    //public void onServiceConnected(ComponentName className, IBinder service) {
    //	mBoundService = ((LoadingService.LocalBinder)service).getService();
    //}
    
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public void onStart(Intent intent, int startId) {
	    handleCommand(intent);
	    Log.v(Loading.TAG,"!!!!!!!!!! onStart !!!!!!!!!!");
        handler.postDelayed(showTime, 800);
        mStartTime = System.currentTimeMillis();
        wfile = new WriteFile2SD(LoadingService.this, mStartTime, mFileName);
        
        /*
        
        /** 1 - CPU **/
        //procStat = new ProcStat(LoadingService.this,wfile, NUM_CPU);
        mDumpCpu = new DumpCPU(LoadingService.this,wfile, mProcessName, mPID);
        
        /** 2 - Memory **/
        //memInfo = new MemInfo(LoadingService.this,wfile);
        mDumpMem = new DumpMEM(LoadingService.this,wfile, mProcessName, mPID);
        
        /** 3 - Storage **/
        diskstats = new Diskstats(LoadingService.this,wfile,KEY_CACHE,KEY_SYSTEM,KEY_DATA );
        
        /** 4 - Network **/
        //procNetDev = new ProcNetDev(LoadingService.this,wfile, READ_LINE);
        mNetStats = new NetStats(LoadingService.this,wfile, mProcessName, mUID);
        
        /** * - Bluetooth **/
        //mBluetooth = new BluetoothCheck(LoadingService.this,wfile, mPID);
        
        /** 5 - Battery **/
        //battInfoProc = new BattInfoProc(LoadingService.this,wfile);
        


        
        /** 6 - Audio **/
        //mAudio = new AudioCheck(LoadingService.this,wfile);
        mVisualizer = new VisualizerLinster(LoadingService.this,wfile);
        
        /** * - GPS **/
        //mGPS = new GPSCheck(LoadingService.this,wfile);
        /** * - LCD **/
        /** 7 - User interaction **/
	    // We want this service to continue running until it is explicitly
	    // stopped, so return sticky.
        
        super.onStart(intent, startId);
	}
    
    private void handleCommand(Intent intent) {
    	if (intent!=null) {
	    	Bundle b = intent.getExtras();
	    	READ_LINE =b.getInt("READ_LINE");
	    	NUM_CPU = b.getInt("NUM_CPU");
	    	KEY_CACHE = b.getString("KEY_CACHE");
	    	KEY_SYSTEM = b.getString("KEY_SYSTEM");
	    	KEY_DATA = b.getString("KEY_DATA");
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
		mDumpCpu.printMean();
		mDumpMem.printMean();
		diskstats.printReadMean();
		diskstats.printWriteMean();
		mNetStats.printRxMean();
		mNetStats.printTxMean();
		mVisualizer.printMean();
        handler.removeCallbacks(showTime);
        mVisualizer.destory();
        if(wfile!=null) {
        	wfile.close();
        }
        super.onDestroy();
    }
     
    private Runnable showTime = new Runnable() {
        public void run() {
        	long ctime = System.currentTimeMillis();
        	//Log.v(Loading.TAG,String.valueOf(ctime));
        	
            //log目前時間
            if (mCount==0) {
                //printProcCpuinfo();
                //procStat.initProcStat(ctime);        //CPU
                //memInfo.initProcMeminfo(ctime);      //MEM
                //procNetDev.initProcNetDev(ctime);    //Network
            	diskstats.initDiskstats(ctime);      //Flash
                try {
                    Thread.sleep(360);
                } catch (Exception e) {}
            } else {
            	//procStat.initProcStat(ctime);
            	//procStat.printAll();
            	mDumpCpu.getCPULoaging(ctime);
            	mDumpCpu.printAll();
            	
            	//memInfo.initProcMeminfo(ctime);
            	//memInfo.printAll();
            	mDumpMem.getALOC(ctime);
            	mDumpMem.printAll();

            	diskstats.initDiskstats(ctime);
            	diskstats.printAll();
            	
            	//procNetDev.initProcNetDev(ctime);
            	//procNetDev.printAll();
            	mNetStats.getTxRx(ctime);
            	mNetStats.printAll();

            	//battInfoProc.initBatteryInfo(ctime);
            	//battInfoProc.printAll();
            	//battInfoProc.printMW(true);
            	
            	//mBluetooth.getBluetooth(ctime);
            	//mBluetooth.printAll();
            	//mBluetooth.printBluetooth(true);
            	
            	//mAudio.getAudio(ctime);
            	//mAudio.printAll();
            	//mAudio.printAudio(true);
            	mVisualizer.getVolumn(ctime);
            	mVisualizer.printAll();
            }
            handler.postDelayed(this, 1000);
            mCount++;
        }
    };


	protected void printProcCpuinfo() {
	    try {
	        String load ;
	        RandomAccessFile reader = new RandomAccessFile(Loading.STR_CPU_CPUINFO, "r");
	        for(;;) {
	            load = reader.readLine();
	            if (load!=null) {
	            	if (Loading.DEBUG && Loading.SERVICE_DEBUG) Log.v(Loading.TAG,"W: "+load);
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

	protected ArrayList<String> dataPasing(String load) {
		ArrayList<String> result = new ArrayList<String>();
		String[] toks = load.split(" ");
		int idx=0;
        for (int i=0;i<toks.length;i++)  {
        	String s = toks[i];
        	if(s.length()>0) {
        		result.add(idx, s);
        		if (Loading.DEBUG && Loading.SERVICE_DEBUG) Log.v(Loading.TAG,"data: result["+idx+"]="+s);
        		idx++;
        	}
        }
		return result;
	}

	protected int getTime(long time){
        return (int)(time-mStartTime)/1000;
    }
	
	protected static void printBatLevel(int time, double level, String status){
		//int cur_log_time=((LoadingService) mContext).getTime(mCurrentTime);
		//mLogTime = cur_log_time;
		String input = "5,"+status+"," +  time + "," + level;

		if(Loading.DEBUG && Loading.BATT_DEBUG) Log.v(Loading.TAG, "input: "+input);
        wfile.write(input);
	}


	public static BroadcastReceiver getBatInfoReceiver() {
        return new BroadcastReceiver() {
            @Override
            public void onReceive(final Context context, Intent intent) {
            	String action = intent.getAction();
            	if (action.equals(Intent.ACTION_BATTERY_CHANGED)) {
	            	long current_time = System.currentTimeMillis();
	            	int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, 0);
	                int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
	                int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, 100);
	                
	                int health = intent.getIntExtra("health", 0);
	                int icon_small = intent.getIntExtra("icon-small", 0);
	                int plugged = intent.getIntExtra("plugged", 0);
	                int voltage = intent.getIntExtra("voltage", 0);
	                int temperature = intent.getIntExtra("temperature",0);
	                String technology = intent.getStringExtra("technology");
	                
	                final int time = (int)(current_time-mStartTime)/1000;
	                //Log.i(Loading.TAG, "("+time+")level: " + level + "; scale: " + scale);
	                if (Loading.DEBUG && Loading.BATT_DEBUG) {
	                Log.i(Loading.TAG, "("+time+"),"+mBattState[status]+","+
	                								level+","+
	                								scale+","+
	                								mBattHealth[health]+","+
	                								icon_small+","+
	                								plugged+","+
	                								voltage+","+
	                								temperature+","+
	                								technology);
	                }
	                final double percent = (level*100)/scale;
	
	                //final String text = String.valueOf(percent) + "%";
	                printBatLevel(time, percent, mBattState[status]);
	                //mBatInfoHandler.post( new Runnable() {
	                //    public void run() {
	                //    	printBatLevel(time, percent, mBattState[status]);
	                //        //Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
	                //    }
	                //});

            	}
            }
        };
	}
}

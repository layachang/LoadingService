package my.test.cpuloading;

import java.io.IOException;
import java.io.RandomAccessFile;

import android.content.Context;
import android.util.Log;

public class BattInfoProc {

	private WriteFile2SD wfile;
	private long mCurrentTime=0;
	private Context mContext;
	private int mLogTime=0;

	private int mBattCurrentNow;
	private int mBattVol;
	private int mCapacity;
	private int mVoltageNow;
	private int mTemp;
	private int mCurrentNow;
	private int mW;
	
	public final static String STR_BATT_BATT_CURRENT_NOW = "/sys/class/power_supply/battery/batt_current_now"; //500
	public final static String STR_BATT_BATT_VOL = "/sys/class/power_supply/battery/batt_vol"; //4163 = 4163 mV
	public final static String STR_BATT_CAPACITY = "/sys/class/power_supply/battery/capacity"; //99 = 99%
	public final static String STR_BATT_VOLTAGE_NOW = "/sys/class/power_supply/battery/voltage_now";  //4166 = 4166 mV
	public final static String STR_BATT_TEMP = "/sys/class/power_supply/battery/temp"; //312 = 31.2C
	public final static String STR_BATT_CURRENT_NOW = "/sys/class/power_supply/battery/current_now"; // 0 = 0 mA (Microamps)
	
	public BattInfoProc(Context context, WriteFile2SD file) {
		mContext = context;
		wfile = file; 
	}
	
	protected void initBatteryInfo(long ctime) {
		mCurrentTime = ctime;
	    //try {
	    //    RandomAccessFile reader = new RandomAccessFile(Loading.STR_BATT_BATT_CURRENT_NOW, "r");
	    //    String load = reader.readLine();
	    //    mBattCurrentNow = Integer.parseInt(load);
	    //    if(Loading.DEBUG && Loading.BATT_DEBUG) Log.v(Loading.TAG,"batt_current_now:"+load+"("+mBattCurrentNow+")");
	    //    reader.close();
	    //} catch (IOException ex) {
	    //	ex.printStackTrace();
	    //}
	    
	    try {
	        RandomAccessFile reader = new RandomAccessFile(Loading.STR_BATT_BATT_VOL, "r");
	        String load = reader.readLine();
	        mBattVol = Integer.parseInt(load);
	        if(Loading.DEBUG && Loading.BATT_DEBUG) Log.v(Loading.TAG,"batt_vol:"+load+"("+mBattVol+")");
	        reader.close();
	    } catch (IOException ex) {
	    	ex.printStackTrace();
	    }
	    
	    try {
	        RandomAccessFile reader = new RandomAccessFile(Loading.STR_BATT_CAPACITY, "r");
	        String load = reader.readLine();
	        mCapacity = Integer.parseInt(load);
	        if(Loading.DEBUG && Loading.BATT_DEBUG) Log.v(Loading.TAG,"capacity:"+load+"("+mCapacity+")");
	        reader.close();
	    } catch (IOException ex) {
	    	ex.printStackTrace();
	    }
	    
	    try {
	        RandomAccessFile reader = new RandomAccessFile(Loading.STR_BATT_VOLTAGE_NOW, "r");
	        String load = reader.readLine();
	        mVoltageNow = Integer.parseInt(load);
	        if(Loading.DEBUG && Loading.BATT_DEBUG) Log.v(Loading.TAG,"voltage_now:"+load+"("+mVoltageNow+")");
	        reader.close();
	    } catch (IOException ex) {
	    	ex.printStackTrace();
	    }
	    try {
	        RandomAccessFile reader = new RandomAccessFile(Loading.STR_BATT_TEMP, "r");
	        String load = reader.readLine();
	        mTemp = Integer.parseInt(load);
	        if(Loading.DEBUG && Loading.BATT_DEBUG) Log.v(Loading.TAG,"temp:"+load+"("+mTemp+")");
	        reader.close();
	    } catch (IOException ex) {
	    	ex.printStackTrace();
	    }
	    
	    try {
	        RandomAccessFile reader = new RandomAccessFile(Loading.STR_BATT_CURRENT_NOW, "r");
	        String load = reader.readLine();
	        mCurrentNow = Integer.parseInt(load);
	        if(Loading.DEBUG && Loading.BATT_DEBUG) Log.v(Loading.TAG,"current_now:"+load+"("+mCurrentNow+")");
	        reader.close();
	    } catch (IOException ex) {
	    	ex.printStackTrace();
	    }
	    
	    mW = mCurrentNow * mVoltageNow *1000;
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
			printMW(false);
		}
		mLogTime = cur_log_time;
		//printBattCurrentNow(true);
		//printBattVol(true);
		//printCapacity(true);
		//printVoltageNow(true);
		//printTemp(true);
		//printCurrentNow(true);
		printMW(true);
	}

	public void printMW(boolean withValue) {
		String input = "BATT mW," + mLogTime;
		if (withValue) {
			input = "BATT mW," + mLogTime + "," + mW;
		}
		if(Loading.DEBUG && Loading.BATT_DEBUG) Log.v(Loading.TAG, "input: "+input);
		wfile.write(input);
	}
	
	private void printCurrentNow(boolean withValue) {
		String input = "BATT CurrentNow," + mLogTime;
		if (withValue) {
			input = "BATT CurrentNow," + mLogTime + "," + mCurrentNow;
		}
		if(Loading.DEBUG && Loading.BATT_DEBUG) Log.v(Loading.TAG, "input: "+input);
		wfile.write(input);
	}

	private void printTemp(boolean withValue) {
		String input = "BATT Temp," + mLogTime;
		if (withValue) {
			input = "BATT Temp," + mLogTime + "," + mTemp;
		}
		if(Loading.DEBUG && Loading.BATT_DEBUG) Log.v(Loading.TAG, "input: "+input);
		wfile.write(input);
		
	}

	private void printVoltageNow(boolean withValue) {
		String input = "BATT VoltageNow," + mLogTime;
		if (withValue) {
			input = "BATT VoltageNow," + mLogTime + "," + mVoltageNow;
		}
		if(Loading.DEBUG && Loading.BATT_DEBUG) Log.v(Loading.TAG, "input: "+input);
		wfile.write(input);
		
	}

	private void printCapacity(boolean withValue) {
		String input = "BATT Capacity," + mLogTime;
		if (withValue) {
			input = "BATT Capacity," + mLogTime + "," + mCapacity;
		}
		if(Loading.DEBUG && Loading.BATT_DEBUG) Log.v(Loading.TAG, "input: "+input);
		wfile.write(input);
		
	}

	private void printBattVol(boolean withValue) {
		String input = "BATT BattVol," + mLogTime;
		if (withValue) {
			input = "BATT BattVol," + mLogTime + "," + mBattVol;
		}
		if(Loading.DEBUG && Loading.BATT_DEBUG) Log.v(Loading.TAG, "input: "+input);
		wfile.write(input);
		
	}

	private void printBattCurrentNow(boolean withValue) {
		String input = "BATT BattCurrentNow," + mLogTime;
		if (withValue) {
			input = "BATT BattCurrentNow," + mLogTime + "," + mBattCurrentNow;
		}
		if(Loading.DEBUG && Loading.BATT_DEBUG) Log.v(Loading.TAG, "input: "+input);
		wfile.write(input);
	}
}

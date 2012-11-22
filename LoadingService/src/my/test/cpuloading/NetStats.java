package my.test.cpuloading;

import android.content.Context;
import android.net.TrafficStats;
import android.util.Log;

public class NetStats {
	private Context mContext;
	private WriteFile2SD wfile;
	private static int mUID;
	private String mProcessName;
	private static long mRx;
	private static long mTx;
	private long mCurrentTime=0;
	private int mLogTime=0;
	private long mRxLast=-1;
	private long mTxLast=-1;
	private int mRxInitTime=-1;
	private int mRxLastTime=-1;
	private int mRxAmount=0;
	private int mTxInitTime=-1;
	private int mTxLastTime=-1;
	private int mTxAmount=0;
	
	public NetStats(LoadingService context, WriteFile2SD file,
			String procName, int uid) {
		mContext = context;
		wfile = file; 
		mProcessName = procName;
		mUID = uid;
		Log.v(Loading.TAG, "NetStats/ mProcessName="+mProcessName+"; mUID="+mUID);
	}

	public void getTxRx(long ctime) {
		mCurrentTime = ctime;
		getTotalTxBytes();
		getTotalRxBytes();
	}

	public static void getTotalTxBytes() {
		//mTx = (TrafficStats.getUidTxBytes(mUID)/1024);
		//mTx = (TrafficStats.getTotalTxBytes()/1024);
		mTx = TrafficStats.getUidTxBytes(mUID);
	}

	public static void getTotalRxBytes(){
		//mRx = (TrafficStats.getUidRxBytes(mUID)/1024);
		//mRx = (TrafficStats.getTotalRxBytes()/1024);
		mRx = TrafficStats.getUidRxBytes(mUID);
    }  

	protected void printRx(boolean withValue){
    	if (mRxLast==-1) {
    		mRxLast = mRx ;
    	}
    	int variation = (int) (mRx-mRxLast);
    	mRxLast = mRx ;
    	String input = "4.1,NET Rx," + mLogTime + "," + String.valueOf(variation);
    	/*
		String input = "4.1,NET Rx," + mLogTime ;
		if (withValue) {
			input = "4.1,NET Rx," + mLogTime + "," + String.valueOf(mRx);
		}
		*/
		if(Loading.DEBUG && Loading.NET_DEBUG) Log.v(Loading.TAG, "input: "+input);
        wfile.write(input);
        meanRxValue(withValue,variation);
	}
	protected void printTx(boolean withValue){
    	if (mTxLast==-1) {
    		mTxLast = mTx ;
    	}
    	int variation = (int) (mTx-mTxLast);
    	mTxLast = mTx ;
    	String input = "4.2,NET Tx," + mLogTime + "," + String.valueOf(variation);
    	/*
		String input = "4.2,NET Tx," + mLogTime ;
		if (withValue) {
			input = "4.2,NET Tx," + mLogTime + "," + String.valueOf(mTx);
		}
		*/
		if(Loading.DEBUG && Loading.NET_DEBUG) Log.v(Loading.TAG, "input: "+input);
        wfile.write(input);
        meanTxValue(withValue,variation);
	}
	
	public void printAll() {
		int cur_log_time=((LoadingService) mContext).getTime(mCurrentTime);
		while (cur_log_time-mLogTime>1) {
			mLogTime++;
			printTx(false);
			printRx(false);
		}
		mLogTime = cur_log_time;
		printTx(true);
		printRx(true);
	}
	
	private void meanRxValue(boolean withValue, int variation) {
		// TODO Auto-generated method stub
        if (mRxInitTime==-1 && withValue) {
        	mRxInitTime = mLogTime;
        }
        mRxLastTime = mLogTime;
        mRxAmount +=variation;
	}
	public void printRxMean(){
		if(Loading.DEBUG && Loading.NET_DEBUG) 
			Log.v(Loading.TAG, "mRxAmount= "+mRxAmount+"; mRxLastTime="+mRxLastTime+"; mRxInitTime="+mRxInitTime);
		String input = "4.1,NET Rx, mean," + (float)mRxAmount/(float)(mRxLastTime-mRxInitTime+1);
		wfile.write(input);
	}
	private void meanTxValue(boolean withValue, int variation) {
		// TODO Auto-generated method stub
        if (mTxInitTime==-1 && withValue) {
        	mTxInitTime = mLogTime;
        }
        mTxLastTime = mLogTime;
        mTxAmount +=variation;
	}
	public void printTxMean(){
		if(Loading.DEBUG && Loading.NET_DEBUG) 
			Log.v(Loading.TAG, "mTxAmount= "+mTxAmount+"; mTxLastTime="+mTxLastTime+"; mTxInitTime="+mTxInitTime);
		String input = "4.2,NET Tx, mean," + (float)mTxAmount/(float)(mTxLastTime-mTxInitTime+1);
		wfile.write(input);
	}
}

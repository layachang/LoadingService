package my.test.cpuloading;

import android.content.Context;
import android.net.TrafficStats;
import android.util.Log;

public class NetStats  extends BasicFunc {
	private static long mRx;
	private static long mTx;
	private long mRxLast=-1;
	private long mTxLast=-1;
	private int mRxInitTime=-1;
	private int mRxLastTime=-1;
	private int mRxAmount=0;
	private int mTxInitTime=-1;
	private int mTxLastTime=-1;
	private int mTxAmount=0;
	
	public NetStats(int uid) {
		mUID = uid;
	}
	@Override
	protected void dumpValues() {
		final long r = TrafficStats.getUidRxBytes(mUID);
		final long t = TrafficStats.getUidTxBytes(mUID);
		
    	if (mRxLast==-1) { mRxLast = r ; }
    	if (mTxLast==-1) { mTxLast = t ; }
    
    	mRx= (int) (r-mRxLast);
    	mTx = (int) (t-mTxLast);
    	mRxLast = r ;
    	mTxLast = t ;
	}
	
	private int getRx(){
		final long r = TrafficStats.getUidRxBytes(mUID);
    	if (mRxLast==-1) {
    		mRxLast = r ;
    	}
    	int variation = (int) (r-mRxLast);
    	mRxLast = r ;
    	return variation;
	}

	private int getTx(){
		final long t = TrafficStats.getUidTxBytes(mUID);
    	if (mTxLast==-1) {
    		mTxLast = t ;
    	}
    	int variation = (int) (t-mTxLast);
    	mTxLast = t ;
    	return variation;
	}

	@Override
	protected String getValues(int index) {
		if (index==NET_UID_INDEX) {
			final int total = getTx()+getRx();
			recordMaxMin(NET_UID_INDEX, total);
			recordMean(NET_UID_INDEX, total);
			return String.valueOf(total);
		}
		return null;
	}

}

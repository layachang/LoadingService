package my.test.cpuloading;

import android.net.TrafficStats;
import android.util.Log;

public class NetStats  extends BasicFunc {
    private static long mRx;
    private static long mTx;
    private long mRxLast=-1;
    private long mTxLast=-1;
    private long mRxCurr=-1;
    private long mTxCurr=-1;
    private int mRxVar=-1;
    private int mTxVar=-1;

    private int mRxInitTime=-1;
    private int mRxLastTime=-1;
    private int mRxAmount=0;
    private int mTxInitTime=-1;
    private int mTxLastTime=-1;
    private int mTxAmount=0;

    public NetStats(int uid) {
        if(Loading.NET_UID_DEBUG)
            Log.v(Loading.TAG, "NetStats, mUID--"+uid);
        mUID = uid;
    }

    @Override
    protected void dumpValues() {
        if(Loading.NET_UID_DEBUG)
            Log.v(Loading.TAG, "[--ProcNetDev--], hasValue="+hasValue);
        if (!hasValue) return;

        mRxCurr = TrafficStats.getUidRxBytes(mUID);
        mTxCurr = TrafficStats.getUidTxBytes(mUID);
        if(Loading.NET_UID_DEBUG)
            Log.v(Loading.TAG, "mRxCurr="+mRxCurr+"; mTxCurr="+mTxCurr);
    }
    
    private int getRx(){
        if (mRxLast==-1) {
            mRxLast = mRxCurr ;
        }
        mRxVar = (int) (mRxCurr-mRxLast);
        if(Loading.NET_UID_DEBUG)
            Log.v(Loading.TAG, "getRx():"+mRxVar+" = (int) ("+mRxCurr+"-"+mRxLast+")");
        mRxLast = mRxCurr ;
        return mRxVar;
    }
    private int getTx(){
        if (mTxLast==-1) {
            mTxLast = mTxCurr ;
        }
        mTxVar = (int) (mTxCurr-mTxLast);
        if(Loading.NET_UID_DEBUG)
            Log.v(Loading.TAG, "getTx():"+mTxVar+" = (int) ("+mTxCurr+"-"+mTxLast+")");
        mTxLast = mTxCurr ;
        return mTxVar;
    }

    @Override
    protected String getValues(int index) {
        if(Loading.NET_UID_DEBUG)
            Log.v(Loading.TAG,BasicFunc.mClassName[index]+"/"+BasicFunc.mResourceName[index]+" getValues("+index+"), hasValue="+hasValue);
        if (index==NET_UID_INDEX && hasValue) {
            int total = (getTx()+getRx())/1024;
            if(Loading.NET_UID_DEBUG)
                Log.v(Loading.TAG,"("+mTxVar+"+"+mRxVar+")= "+(mTxVar+mRxVar)+"/1024="+total);
            recordMaxMin(NET_UID_INDEX, total);
            recordMean(NET_UID_INDEX, total);
            return String.valueOf(total);
        }
        return "--";
    }
    public void setUid(int uid) {
        mUID= uid;
        hasValue = true;
    }
}

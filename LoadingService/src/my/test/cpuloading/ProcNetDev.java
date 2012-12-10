package my.test.cpuloading;


import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;

import android.net.TrafficStats;
import android.util.Log;

public class ProcNetDev extends BasicFunc {
    private int READ_LINE= 0;
/*
 *bytes��ܦ��o���줸�ռ�
 *packets��ܦ��o���T���]�q
 *errs��ܦ��o��~���]�q
 *drop��ܦ��o��󪺥]�q
 *
 *====SAMSUNG S2====
 *Inter-|   Receive                                                |  Transmit
 *face  | bytes   packets errs drop fifo frame compressed multicast|  bytes    packets errs drop fifo colls carrier compressed
 *lo:    4809281   4129    0    0    0     0          0         0    4809281    4129    0    0    0     0       0          0
 *sit0:     0       0      0    0    0     0          0         0       0         0     0    0    0     0       0          0
 *ip6tnl0:  0       0      0    0    0     0          0         0       0         0     0    0    0     0       0          0
 *rmnet0:   0       0      0    0    0     0          0         0       0         0     0    0    0     0       0          0
 *rmnet1:   0       0      0    0    0     0          0         0       0         0     0    0    0     0       0          0
 *rmnet2:   0       0      0    0    0     0          0         0       0         0     0    0    0     0       0          0
 *wlan0: 55078037 39049    0   926   0     0          0         0    1508878    18431   0    0    0     0       0          0
 *
 *====SAMSUNG GALAXY Note 10.1====
 *Inter-|   Receive                                                |  Transmit
 * face |bytes    packets errs drop fifo frame compressed multicast|bytes    packets errs drop fifo colls carrier compressed
 *    lo:   
 *svnet0:   
 *  ifb0:
 *  ifb1:
 *  usb0:
 *  sit0:
 *ip6tnl0:
 *  eth0:
 *  
 * ===SAMSUNG GALAXY Tab
 * Inter-|   Receive                                                |  Transmit
 * face  |  bytes  packets errs drop fifo frame compressed multicast|bytes    packets errs drop fifo colls carrier compressed
 *  lo:      34106  457     0    0    0     0          0         0    34106     457    0    0    0     0       0          0
 * sit0:       0     0      0    0    0     0          0         0        0 0    0    0    0     0       0          0
 * ip6tnl0:    0     0      0    0    0     0          0         0        0  0    0    0    0     0       0          0
 * rmnet0:     0     0      0    0    0     0          0         0        0 0    0    0    0     0       0          0
 * rmnet1:     0     0      0    0    0     0          0         0        0 0    0    0    0     0       0          0
 * rmnet2:     0     0      0    0    0     0          0         0        0 0    0    0    0     0       0          0
 * wlan0:   284342  1220    0    0    0     0          0         0    1497886    0    0    0     0       0          0 
 *  
 *  
 *  
 */
    private int initReceiveLOBytes=-1;
    private int initReceiveLOPackets=-1;
    private int initReceiveLOErrs=-1;
    private int initReceiveLODrop=-1;
    
    private int initTransmitLOBytes=-1;
    private int initTransmitLOPackets=-1;
    private int initTransmitLOErrs=-1;
    private int initTransmitLODrop=-1;
    
    private int initReW0Bytes=-1;
    private int initReW0Packets=-1;
    private int initReW0Errs=-1;
    private int initReW0Drop=-1;
    
    private int initTranW0Bytes=-1;
    private int initTranW0Packets=-1;
    private int initTranW0Errs=-1;
    private int initTranW0Drop=-1;
    
    private int mReceiveLOBytes=0;
    private int mReceiveLOPackets=0;
    private int mReceiveLOErrs=0;
    private int mReceiveLODrop=0;
    
    private int mTransmitLOBytes=0;
    private int mTransmitLOPackets=0;
    private int mTransmitLOErrs=0;
    private int mTransmitLODrop=0;
    
    private int mLaReW0Bytes=-1;
    private int mLaReW0Packets=-1;
    private int mLaReW0Errs=-1;
    private int mLaReW0Drop=-1;
    
    private int mCuReW0Bytes=-1;
    private int mCuReW0Packets=-1;
    private int mCuReW0Errs=-1;
    private int mCuReW0Drop=-1;
    private int mLaTranW0Bytes=-1;
    private int mLaTranW0Packets=-1;
    private int mLaTranW0Errs=-1;
    private int mLaTranW0Drop=-1;
    private int mCuTranW0Bytes=-1;
    private int mCuTranW0Packets=-1;
    private int mCuTranW0Errs=-1;
    private int mCuTranW0Drop=-1;
    
    private int mVarReW0Bytes;
    private int mVarTranW0Bytes;

    public ProcNetDev(int line_num) {
        READ_LINE= line_num;
        Log.v(Loading.TAG, "ProcNetDev, line_num"+line_num); 
    }

    public long getTotalRxBytes(){  
        return TrafficStats.getTotalRxBytes()==TrafficStats.UNSUPPORTED?0:(TrafficStats.getTotalRxBytes()/1024);
    }
    public long getTotalTxBytes(){
        return TrafficStats.getTotalTxBytes()==TrafficStats.UNSUPPORTED?0:(TrafficStats.getTotalTxBytes()/1024);
    }
    public long getMobileRxBytes(){
        return TrafficStats.getMobileRxBytes()==TrafficStats.UNSUPPORTED?0:(TrafficStats.getMobileRxBytes()/1024);
    }

    private void moveValues() {
        mLaReW0Bytes = mCuReW0Bytes;
        mLaReW0Packets = mCuReW0Packets;
        mLaReW0Errs = mCuReW0Errs;
        mLaReW0Drop = mCuReW0Drop;
        
        mLaTranW0Bytes = mCuTranW0Bytes;
        mLaTranW0Packets = mCuTranW0Packets;
        mLaTranW0Errs = mCuTranW0Errs;
        mLaTranW0Drop = mCuTranW0Drop;
    }

    protected void dumpValues() {
        if(Loading.NET_DEBUG)
            Log.v(Loading.TAG, "[--NetStats--]");
        try {
            moveValues();
            
            String[] toks;
            String load = null;
            ArrayList<String> result;
            RandomAccessFile reader = new RandomAccessFile(Loading.STR_NET_DEV, "r");
            for (int i=0; i< READ_LINE;i++ ) {
                load = reader.readLine();
                if (Loading.NET_DEBUG) Log.v(Loading.TAG, i+":"+load); 
            }
            if (load.length()>0) {
                result = dataPasing(load);
                mCuReW0Bytes = Integer.parseInt(result.get(1));
                mCuReW0Packets = Integer.parseInt(result.get(2));
                mCuReW0Errs = Integer.parseInt(result.get(3));
                mCuReW0Drop = Integer.parseInt(result.get(4));

                mCuTranW0Bytes = Integer.parseInt(result.get(9));
                mCuTranW0Packets = Integer.parseInt(result.get(10));
                mCuTranW0Errs = Integer.parseInt(result.get(11));
                mCuTranW0Drop = Integer.parseInt(result.get(12));

                if (Loading.NET_DEBUG) {
                    Log.v(Loading.TAG, "mCuReW0Bytes: "+mCuReW0Bytes+"; mCuTranW0Bytes="+mCuTranW0Bytes);
                    Log.v(Loading.TAG, "mCuReW0Packets: "+mCuReW0Packets+"; mCuTranW0Packets="+mCuTranW0Packets); 
                    Log.v(Loading.TAG, "mCuReW0Errs: "+mCuReW0Errs+"; mCuTranW0Errs="+mCuTranW0Errs); 
                    Log.v(Loading.TAG, "mCuReW0Drop: "+mCuReW0Drop+"; mCuTranW0Drop="+mCuTranW0Drop); 
                }

                if (initReW0Packets==-1) initReW0Packets = mCuReW0Packets;
                if (initReW0Errs==-1) initReW0Errs = mCuReW0Errs;
                if (initReW0Drop==-1) initReW0Drop = mCuReW0Drop;
                if (initTranW0Packets==-1) initTranW0Packets = mCuTranW0Packets;
                if (initTranW0Errs==-1) initTranW0Errs = mCuTranW0Errs;
                if (initTranW0Drop==-1) initTranW0Drop = mCuTranW0Drop;

                if (initReW0Bytes==-1) initReW0Bytes = mCuReW0Bytes;
                if (initTranW0Bytes==-1) initTranW0Bytes = mCuTranW0Bytes;
            }
            reader.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    private int getReceiveLOBytes() { return mReceiveLOBytes; }
    private int getReceiveLOPackets() { return mReceiveLOPackets; }
    private int getReceiveLOErrs() { return mReceiveLOErrs; }
    private int getReceiveLODrop() { return mReceiveLODrop; }
    private int getTransmitLOBytes() { return mTransmitLOBytes; }
    private int getTransmitLOPackets() { return mTransmitLOPackets; }
    private int getTransmitLOErrs() { return mTransmitLOErrs; }
    private int getTransmitLODrop() { return mTransmitLODrop; }
    private int getReceiveWlan0Packets() { return mCuReW0Packets-initReW0Packets; }
    private int getReceiveWlan0Errs() { return mCuReW0Errs-initReW0Errs; }
    private int getReceiveWlan0Drop() { return mCuReW0Drop-initReW0Drop; }
    private int getTransmitWlan0Packets() { return mCuTranW0Packets-initTranW0Packets; }
    private int getTransmitWlan0Errs() { return mCuTranW0Errs-initTranW0Errs; }
    private int getTransmitWlan0Drop() { return mCuTranW0Drop-initTranW0Drop; }

    private int getReceiveWlan0Bytes() {
        if (mLaReW0Bytes==-1) {
            mLaReW0Bytes = mCuReW0Bytes;
        }
        mVarReW0Bytes = mCuReW0Bytes - mLaReW0Bytes;
        if(Loading.NET_DEBUG)
            Log.v(Loading.TAG,
                "getReceiveWlan0Bytes: "+mVarReW0Bytes+" = "+mCuReW0Bytes+" - "+mLaReW0Bytes);
        return mVarReW0Bytes;
    }
    private int getTransmitWlan0Bytes() {
        if (mLaTranW0Bytes==-1) {
            mLaTranW0Bytes = mCuTranW0Bytes;
        }
        mVarTranW0Bytes = mCuTranW0Bytes - mLaTranW0Bytes;
        if(Loading.NET_DEBUG)
            Log.v(Loading.TAG,
                "getTransmitWlan0Bytes: "+mVarTranW0Bytes+" = "+mCuTranW0Bytes+" - "+mLaTranW0Bytes);
        return mVarTranW0Bytes;
    }
    @Override
    protected String getValues(int index) {
        if(Loading.NET_DEBUG)
            Log.v(Loading.TAG,BasicFunc.mClassName[index]+"/"+BasicFunc.mResourceName[index]+" getValues("+index+")");
        if (index==NET_ALL_INDEX) {
            float total = round((mVarTranW0Bytes+mVarReW0Bytes)/1024,3);
            if(Loading.NET_DEBUG)
                Log.v(Loading.TAG,
                    "NET_ALL_INDEX: ("+mVarTranW0Bytes+"+"+mVarReW0Bytes+")/1024="+total);
            recordMaxMin(NET_ALL_INDEX, total);
            recordMean(NET_ALL_INDEX, total);
            return String.valueOf(total);
        } else if (index==NET_REC_INDEX) {
            float total = round(getReceiveWlan0Bytes()/1024,3);
            if(Loading.NET_DEBUG)
                Log.v(Loading.TAG,
                    "NET_REC_INDEX: "+mVarReW0Bytes+"/1024="+total);
            recordMaxMin(NET_REC_INDEX, total);
            recordMean(NET_REC_INDEX, total);
            return String.valueOf(total);
        } else if (index==NET_TRA_INDEX) {
            float total = round(getTransmitWlan0Bytes()/1024,3);
            if(Loading.NET_DEBUG)
                Log.v(Loading.TAG,
                    "NET_TRA_INDEX: "+mVarTranW0Bytes+"/1024="+total);
            recordMaxMin(NET_TRA_INDEX, total);
            recordMean(NET_TRA_INDEX, total);
            return String.valueOf(total);
        }
        return "--";
    }
    public int getAmount() {
        int result = (mCuReW0Bytes-initReW0Bytes) - (mCuTranW0Bytes-initTranW0Bytes);
        if(Loading.AMOUNT_MEAN)
            Log.v(Loading.TAG,
                        "result:"+result+", ("+mCuReW0Bytes+"-"+initReW0Bytes+") - ("+mCuTranW0Bytes+"-"+initTranW0Bytes+")");
        return result;
    }
}

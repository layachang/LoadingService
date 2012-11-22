package my.test.cpuloading;


import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;

import android.content.Context;
import android.net.TrafficStats;
import android.util.Log;

public class ProcNetDev {

	private WriteFile2SD wfile;
	private long mCurrentTime=0;
	private int mLogTime=0;
	private int READ_LINE= 0;
	private Context mContext;
/*
 *bytes表示收發的位元組數
 *packets表示收發正確的包量
 *errs表示收發錯誤的包量
 *drop表示收發丟棄的包量
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
	
	public ProcNetDev(Context context, WriteFile2SD file, int line_num) {
		mContext = context;
		wfile = file;
		READ_LINE= line_num;
	}
	protected void initProcNetDev(long ctime) {
		mCurrentTime = ctime;
	    try {
	    	moveValues();
	    	
	    	String[] toks;
	    	String load = null;
	    	ArrayList<String> result;
	        RandomAccessFile reader = new RandomAccessFile(Loading.STR_NET_DEV, "r");
	        for (int i=0; i< READ_LINE;i++ ) {
	        	load = reader.readLine();
	        	if (Loading.DEBUG && Loading.NET_DEBUG) Log.v(Loading.TAG, i+":"+load); 
	        }
	        if (load.length()>0) {
		        result = ((LoadingService) mContext).dataPasing(load);
		    	mCuReW0Bytes = Integer.parseInt(result.get(1));
		    	mCuReW0Packets = Integer.parseInt(result.get(2));
		    	mCuReW0Errs = Integer.parseInt(result.get(3));
		    	mCuReW0Drop = Integer.parseInt(result.get(4));
		    	if (initReW0Bytes==-1) initReW0Bytes = mCuReW0Bytes;
		    	if (initReW0Packets==-1) initReW0Packets = mCuReW0Packets;
		    	if (initReW0Errs==-1) initReW0Errs = mCuReW0Errs;
		    	if (initReW0Drop==-1) initReW0Drop = mCuReW0Drop;

		    	mCuTranW0Bytes = Integer.parseInt(result.get(9));
		    	mCuTranW0Packets = Integer.parseInt(result.get(10));
		    	mCuTranW0Errs = Integer.parseInt(result.get(11));
		    	mCuTranW0Drop = Integer.parseInt(result.get(12));
		    	if (initTranW0Bytes==-1) initTranW0Bytes = mCuTranW0Bytes;
		    	if (initTranW0Packets==-1) initTranW0Packets = mCuTranW0Packets;
		    	if (initTranW0Errs==-1) initTranW0Errs = mCuTranW0Errs;
		    	if (initTranW0Drop==-1) initTranW0Drop = mCuTranW0Drop;

	        }
	        reader.close();
	    } catch (IOException ex) {
	    	ex.printStackTrace();
	    }
	}

	public void printAll() {
		int cur_log_time=((LoadingService) mContext).getTime(mCurrentTime);

		while (cur_log_time-mLogTime>1) {
			mLogTime++;
			printReceiveWlan0Bytes(false);
			printReceiveWlan0Drop(false);
			printReceiveWlan0Errs(false);
			printReceiveWlan0Packets(false);
	    	printTransmitWlan0Bytes(false);
	    	printTransmitWlan0Drop(false);
	    	printTransmitWlan0Errs(false);
	    	printTransmitWlan0Packets(false);
		}
		mLogTime = cur_log_time;
    	printReceiveWlan0Bytes(true);
    	printReceiveWlan0Drop(true);
    	printReceiveWlan0Errs(true);
    	printReceiveWlan0Packets(true);
    	printTransmitWlan0Bytes(true);
    	printTransmitWlan0Drop(true);
    	printTransmitWlan0Errs(true);
    	printTransmitWlan0Packets(true);
		
	}
	/*
	protected void printReceiveLOBytes(boolean withValue) {
		String input = "REC LO Bytes," + mLogTime;
		if (withValue) 
			input = "REC LO Bytes," + mLogTime + "," + mReceiveLOBytes;
		if(Loading.DEBUG && Loading.NET_DEBUG) Log.v(Loading.TAG, "input: "+input);
		wfile.write(input);
	}
	protected void printReceiveLOPackets(boolean withValue) {
		String input = "REC LO Packets," + mLogTime;
		if (withValue)
			input = "REC LO Packets," + mLogTime + "," + mReceiveLOPackets;
		if(Loading.DEBUG && Loading.NET_DEBUG) Log.v(Loading.TAG, "input: "+input);
		wfile.write(input);
	}
	protected void printReceiveLOErrs(boolean withValue) {
		String input = "REC LO Errs," + mLogTime;
		if (withValue)
			input = "REC LO Errs," + mLogTime + "," + mReceiveLOErrs;
		if(Loading.DEBUG && Loading.NET_DEBUG) Log.v(Loading.TAG, "input: "+input);
		wfile.write(input);
	}
	protected void printReceiveLODrop(boolean withValue) {
		String input = "REC LO Drop," + mLogTime;
		if (withValue)
			input = "REC LO Drop," + mLogTime + "," + mReceiveLODrop;
		if(Loading.DEBUG && Loading.NET_DEBUG) Log.v(Loading.TAG, "input: "+input);
		wfile.write(input);
	}
	protected void printTransmitLOBytes(boolean withValue) {
		String input = "TRAS LO Bytes," + mLogTime;
		if (withValue)
			input = "TRAS LO Bytes," + mLogTime + "," + mTransmitLOBytes;
		if(Loading.DEBUG && Loading.NET_DEBUG) Log.v(Loading.TAG, "input: "+input);
		wfile.write(input);
	}
	protected void printTransmitLOPackets(boolean withValue) {
		String input = "TRAS LO Packets," + mLogTime;
		if (withValue)
			input = "TRAS LO Packets," + mLogTime + "," + mTransmitLOPackets;
		if(Loading.DEBUG && Loading.NET_DEBUG) Log.v(Loading.TAG, "input: "+input);
		wfile.write(input);
	}
	protected void printTransmitLOErrs(boolean withValue) {
		String input = "TRAS LO Errs," + mLogTime;
		if (withValue)
			input = "TRAS LO Errs," + mLogTime + "," + mTransmitLOErrs;
		if(Loading.DEBUG && Loading.NET_DEBUG) Log.v(Loading.TAG, "input: "+input);
		wfile.write(input);
	}
	protected void printTransmitLODrop(boolean withValue) {
		String input = "TRAS LO Drop," + mLogTime;
		if (withValue)
			input = "TRAS LO Drop," + mLogTime + "," + mTransmitLODrop;
		if(Loading.DEBUG && Loading.NET_DEBUG) Log.v(Loading.TAG, "input: "+input);
		wfile.write(input);
	}
	*/
	protected void printReceiveWlan0Bytes(boolean withValue) {
		String input = "REC W0 Bytes," + mLogTime;
		if (withValue)
			input = "REC W0 Bytes," + mLogTime + "," + (mCuReW0Bytes-initReW0Bytes)/1024;
		if(Loading.DEBUG && Loading.NET_DEBUG) Log.v(Loading.TAG, "input: "+input+ "("+mCuReW0Bytes+"-"+initReW0Bytes+")/1024");
		wfile.write(input);
	}
	protected void printReceiveWlan0Packets(boolean withValue) {
		String input = "REC W0 Packets," + mLogTime;
		if (withValue)
			input = "REC W0 Packets," + mLogTime + "," + (mCuReW0Packets-initReW0Packets);
		if(Loading.DEBUG && Loading.NET_DEBUG) Log.v(Loading.TAG, "input: "+input+ "("+mCuReW0Packets+"-"+initReW0Packets+")");
		wfile.write(input);
	}
	protected void printReceiveWlan0Errs(boolean withValue) {
		String input = "REC W0 Errs," + mLogTime;
		if (withValue)
			input = "REC W0 Errs," + mLogTime + "," + (mCuReW0Errs-initReW0Errs);
		if(Loading.DEBUG && Loading.NET_DEBUG) Log.v(Loading.TAG, "input: "+input+ "("+mCuReW0Errs+"-"+initReW0Errs+")");
		wfile.write(input);
	}
	protected void printReceiveWlan0Drop(boolean withValue) {
		String input = "REC W0 Drop," + mLogTime;
		if (withValue)
			input = "REC W0 Drop," + mLogTime + "," + (mCuReW0Drop-initReW0Drop);
		if(Loading.DEBUG && Loading.NET_DEBUG) Log.v(Loading.TAG, "input: "+input+ "("+mCuReW0Drop+"-"+initReW0Drop+")");
		wfile.write(input);
	}
	protected void printTransmitWlan0Bytes(boolean withValue) {
		String input = "TRAS W0 Bytes," + mLogTime;
		if (withValue)
			input = "TRAS W0 Bytes," + mLogTime + "," + (mCuTranW0Bytes-initTranW0Bytes)/1024;
		if(Loading.DEBUG && Loading.NET_DEBUG) Log.v(Loading.TAG, "input: "+input+ "("+mCuTranW0Bytes+"-"+initTranW0Bytes+")/1024");
		wfile.write(input);
	}
	protected void printTransmitWlan0Packets(boolean withValue) {
		String input = "TRAS W0 Packets," + mLogTime;
		if (withValue)
			input = "TRAS W0 Packets," + mLogTime + "," + (mCuTranW0Packets-initTranW0Packets);
		if(Loading.DEBUG && Loading.NET_DEBUG) Log.v(Loading.TAG, "input: "+input+ "("+mCuTranW0Packets+"-"+initTranW0Packets+")");
		wfile.write(input);
	}
	protected void printTransmitWlan0Errs(boolean withValue) {
		String input = "TRAS W0 Errs," + mLogTime;
		if (withValue)
			input = "TRAS W0 Errs," + mLogTime + "," + (mCuTranW0Errs-initTranW0Errs);
		if(Loading.DEBUG && Loading.NET_DEBUG) Log.v(Loading.TAG, "input: "+input+ "("+mCuTranW0Errs+"-"+initTranW0Errs+")");
		wfile.write(input);
	}
	protected void printTransmitWlan0Drop(boolean withValue) {
		String input = "TRAS W0 Drop," + mLogTime;
		if (withValue)
			input = "TRAS W0 Drop," + mLogTime + "," + (mCuTranW0Drop-initTranW0Drop);
		if(Loading.DEBUG && Loading.NET_DEBUG) Log.v(Loading.TAG, "input: "+input+ "("+mCuTranW0Drop+"-"+initTranW0Drop+")");
		wfile.write(input);
	}

}

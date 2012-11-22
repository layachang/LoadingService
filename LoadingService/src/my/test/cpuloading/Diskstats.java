package my.test.cpuloading;


import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;

import android.content.Context;
import android.util.Log;

public class Diskstats {

	private long mCurrentTime=0;
	private int mLogTime=0;
	private String KEY_CACHE = "";
	private String KEY_SYSTEM = "";
	private String KEY_DATA = "";
	private Context mContext;
	private WriteFile2SD wfile;
/*
 *	Field 1 -- # of reads issued [3]
 *	Field 2 -- # of reads merged [4], field 6 -- # of writes merged [8]
 *	Field 3 -- # of sectors read [5]
 *	Field 4 -- # of milliseconds spent reading [6]
 *	Field 5 -- # of writes completed [7]
 *	Field 7 -- # of sectors written [9]
 *	Field 8 -- # of milliseconds spent writing [10]
 *	Field 9 -- # of I/Os currently in progress [11]
 *	Field 10 -- # of milliseconds spent doing I/Os [12]
 *	Field 11 -- weighted # of milliseconds spent doing I/Os [13] 
 */
	private int mLastReadsIssued;  //[3]
	private int mCurrReadsIssued;
	private int initReadsIssued;
	
	private int mLastReadsMerged;  //[4]
	private int mCurrReadsMerged;
	private int initReadsMerged;
	
    private int mLastWriteMerged;  //[8]
    private int mCurrWriteMerged;
    private int initWriteMerged;
    
	private int mLastSectorsRead;  //[5]
	private int mCurrSectorsRead;
	private int initSectorsRead;
	
	private int mLastSectorsWrite; //[9]
	private int mCurrSectorsWrite;
	private int initSectorsWrite;
	
	private int mSectorReadLast=-1;
	private int mSectorWriteLast=-1;
	private int mReadInitTime=-1;
	private int mReadLastTime=-1;
	private int mReadAmount=0;
	private int mWriteInitTime=-1;
	private int mWriteLastTime=-1;
	private int mWriteAmount=0;

	public Diskstats(Context context, WriteFile2SD file, String cache, String system, String data) {
		mContext = context;
		wfile = file;
		KEY_CACHE = cache;
		KEY_SYSTEM = system;
		KEY_DATA = data;
	}
	protected void initDiskstats(long ctime) {
		mCurrentTime = ctime;
		ArrayList<String> result = null;
		mCurrReadsIssued= mCurrReadsMerged= mCurrWriteMerged= mCurrSectorsRead= mCurrSectorsWrite=0;
	    try {
	        RandomAccessFile reader = new RandomAccessFile(Loading.STR_DISK_DISKSTATE, "r");
	        for(;;){
	            //moveValues();
		        String load = reader.readLine();
		        //if(Loading.DEBUG && Loading.DISK_DEBUG) Log.v(Loading.TAG, "load: "+load); 
		        if(load==null) {
		        	reader.close();
		        	break;
		        }
		        result = ((LoadingService) mContext).dataPasing(load);
		        if (result.get(2).equals(KEY_CACHE) || 
		                result.get(2).equals(KEY_SYSTEM) ||
		                result.get(2).equals(KEY_DATA)) {
		        	if(Loading.DEBUG && Loading.DISK_DEBUG) Log.v(Loading.TAG,load);
		            mCurrReadsIssued += Integer.parseInt(result.get(3));
		            if(Loading.DEBUG && Loading.DISK_DEBUG) Log.v(Loading.TAG, "result.get(3)="+result.get(3)+"+="+mCurrReadsIssued);
		            mCurrReadsMerged += Integer.parseInt(result.get(4));
		            if(Loading.DEBUG && Loading.DISK_DEBUG) Log.v(Loading.TAG, "result.get(4)="+result.get(4)+"+="+mCurrReadsMerged);
		            mCurrWriteMerged += Integer.parseInt(result.get(8));
		            if(Loading.DEBUG && Loading.DISK_DEBUG) Log.v(Loading.TAG, "result.get(8)="+result.get(8)+"+="+mCurrWriteMerged);
		            mCurrSectorsRead += Integer.parseInt(result.get(5));
		            if(Loading.DEBUG && Loading.DISK_DEBUG) Log.v(Loading.TAG, "result.get(5)="+result.get(5)+"+="+mCurrSectorsRead);
		            mCurrSectorsWrite += Integer.parseInt(result.get(9));
		            if(Loading.DEBUG && Loading.DISK_DEBUG) Log.v(Loading.TAG, "result.get(9)="+result.get(9)+"+="+mCurrSectorsWrite);
		        }
	        } 

	    	if(initReadsIssued==-1) initReadsIssued = mCurrReadsIssued;
	    	if(initReadsMerged==-1) initReadsMerged = mCurrReadsMerged;
	    	if(initWriteMerged==-1) initWriteMerged = mCurrWriteMerged;
	    	if(initSectorsRead==-1) initSectorsRead = mCurrSectorsRead;
	    	if(initSectorsWrite==-1) initSectorsWrite = mCurrSectorsWrite;
	    } 
	    
	    catch (IOException ex) {
	    	ex.printStackTrace();
	    }
	}
    private void moveValues() {
        mLastReadsIssued = mCurrReadsIssued;
        mLastReadsMerged = mCurrReadsMerged;
        mLastWriteMerged = mCurrWriteMerged;
        mLastSectorsRead = mCurrSectorsRead;
        mLastSectorsWrite = mCurrSectorsWrite;
        mCurrReadsIssued = mCurrReadsMerged = mCurrWriteMerged = mCurrSectorsRead = mCurrSectorsWrite = 0;
    }
    
    protected void printReadsIssued(boolean withValue) {
        String input = "FLASH READ ISSUED," + mLogTime;
        if (withValue)
        	input = "FLASH READ ISSUED," + mLogTime + ","+ (mCurrReadsIssued);
        if(Loading.DEBUG && Loading.DISK_DEBUG) Log.v(Loading.TAG, "input: "+input);
        wfile.write(input);
    }
    
    protected void printReadsMerged(boolean withValue) {
        String input = "FLASH READ MERGED," + mLogTime;
        if (withValue)
        	input = "FLASH READ MERGED," + mLogTime + ","+ (mCurrReadsMerged);
        if(Loading.DEBUG && Loading.DISK_DEBUG) Log.v(Loading.TAG, "input: "+input);
        wfile.write(input);
    }
    
    protected void printWriteMerged(boolean withValue) {
        String input = "FLASH WRITE MERGED," + mLogTime;
        if (withValue)
        	input = "FLASH WRITE MERGED," + mLogTime + ","+ (mCurrWriteMerged);
        if(Loading.DEBUG && Loading.DISK_DEBUG) Log.v(Loading.TAG, "input: "+input);
        wfile.write(input);
    }
    
    protected void printSectorsRead(boolean withValue) {
    	if (mSectorReadLast==-1) {
    		mSectorReadLast = mCurrSectorsRead ;
    	}
    	int variation = mCurrSectorsRead-mSectorReadLast;
    	mSectorReadLast = mCurrSectorsRead ;
    	String input = "3.1,FLASH SECTORS READ," + mLogTime + ","+ String.valueOf((variation));
    	/*
        String input = "3.1,FLASH SECTORS READ," + mLogTime;
        if (withValue) {
        	input = "3.1,FLASH SECTORS READ," + mLogTime + ","+ String.valueOf((variation));
        }
        */
        if(Loading.DEBUG && Loading.DISK_DEBUG) Log.v(Loading.TAG, "input: "+input);
        wfile.write(input);
        meanReadValue(withValue,variation);
    }
    
    protected void printSectorsWrite(boolean withValue) {
    	if (mSectorWriteLast==-1) {
    		mSectorWriteLast = mCurrSectorsRead ;
    	}
    	int variation = mCurrSectorsRead-mSectorWriteLast;
    	mSectorWriteLast = mCurrSectorsRead ;
    	String input = "3.2,FLASH SECTORS WRITE," + mLogTime + ","+ String.valueOf((variation));
    	/*
        String input = "3.2,FLASH SECTORS WRITE," + mLogTime;
        if (withValue)
        	input = "3.2,FLASH SECTORS WRITE," + mLogTime + ","+ (mCurrSectorsWrite);
        */
        if(Loading.DEBUG && Loading.DISK_DEBUG) Log.v(Loading.TAG, "input: "+input);
        wfile.write(input);
        meanWriteValue(withValue,variation);
    }
    
    public void printAll() {
		int cur_log_time=((LoadingService) mContext).getTime(mCurrentTime);

		while (cur_log_time-mLogTime>1) {
			mLogTime++;
			//printReadsIssued(false);
			//printReadsMerged(false);
			//printWriteMerged(false);
			printSectorsRead(false);
			printSectorsWrite(false);
		}
		mLogTime = cur_log_time;
        //printReadsIssued(true);
        //printReadsMerged(true);
        //printWriteMerged(true);
        printSectorsRead(true);
        printSectorsWrite(true);
    }
    
	private void meanReadValue(boolean withValue, int variation) {
		// TODO Auto-generated method stub
        if (mReadInitTime==-1 && withValue) {
        	mReadInitTime = mLogTime;
        }
        mReadLastTime = mLogTime;
        mReadAmount +=variation;
	}
	public void printReadMean(){
		if(Loading.DEBUG && Loading.DISK_DEBUG) 
			Log.v(Loading.TAG, "mReadAmount= "+mReadAmount+"; mReadLastTime="+mReadLastTime+"; mReadInitTime="+mReadInitTime);
		String input = "3.1,FLASH SECTORS READ, mean," + (float)mReadAmount/(float)(mReadLastTime-mReadInitTime+1);
		wfile.write(input);
	}
	private void meanWriteValue(boolean withValue, int variation) {
		// TODO Auto-generated method stub
        if (mWriteInitTime==-1 && withValue) {
        	mWriteInitTime = mLogTime;
        }
        mWriteLastTime = mLogTime;
        mWriteAmount +=variation;
	}
	public void printWriteMean(){
		if(Loading.DEBUG && Loading.DISK_DEBUG) 
			Log.v(Loading.TAG, "mWriteAmount= "+mWriteAmount+"; mWriteLastTime="+mWriteLastTime+"; mWriteInitTime="+mWriteInitTime);
		String input = "3.2,FLASH SECTORS WRITE, mean," + (float)mWriteAmount/(float)(mWriteLastTime-mWriteInitTime+1);
		wfile.write(input);
	}
}

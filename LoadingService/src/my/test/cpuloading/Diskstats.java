package my.test.cpuloading;


import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;

import android.util.Log;

public class Diskstats extends BasicFunc {

	private String KEY_CACHE = "";
	private String KEY_SYSTEM = "";
	private String KEY_DATA = "";
	private int mSize = 4; //Kb
/*
 *	  1       0 ram0 0 0 0 0 0 0 0 0 0 0 0
 *	  1       1 ram1 0 0 0 0 0 0 0 0 0 0 0
 *	  1       2 ram2 0 0 0 0 0 0 0 0 0 0 0
 *	  1       3 ram3 0 0 0 0 0 0 0 0 0 0 0
 *	  1       4 ram4 0 0 0 0 0 0 0 0 0 0 0
 *	  1       5 ram5 0 0 0 0 0 0 0 0 0 0 0
 *	  1       6 ram6 0 0 0 0 0 0 0 0 0 0 0
 *	  1       7 ram7 0 0 0 0 0 0 0 0 0 0 0
 *	  1       8 ram8 0 0 0 0 0 0 0 0 0 0 0
 *	  1       9 ram9 0 0 0 0 0 0 0 0 0 0 0
 *	  1      10 ram10 0 0 0 0 0 0 0 0 0 0 0
 *	  1      11 ram11 0 0 0 0 0 0 0 0 0 0 0
 *	  1      12 ram12 0 0 0 0 0 0 0 0 0 0 0
 *	  1      13 ram13 0 0 0 0 0 0 0 0 0 0 0
 *	  1      14 ram14 0 0 0 0 0 0 0 0 0 0 0
 *	  1      15 ram15 0 0 0 0 0 0 0 0 0 0 0
 *	  7       0 loop0 0 0 0 0 0 0 0 0 0 0 0
 *	  7       1 loop1 0 0 0 0 0 0 0 0 0 0 0
 *	  7       2 loop2 0 0 0 0 0 0 0 0 0 0 0
 *	  7       3 loop3 0 0 0 0 0 0 0 0 0 0 0
 *	  7       4 loop4 0 0 0 0 0 0 0 0 0 0 0
 *	  7       5 loop5 0 0 0 0 0 0 0 0 0 0 0
 *	  7       6 loop6 0 0 0 0 0 0 0 0 0 0 0
 *	  7       7 loop7 0 0 0 0 0 0 0 0 0 0 0
 *	179       0 mmcblk0 6935 4544 652520 18645 11628 33428 360000 81815 0 43580 100370
 *	179       1 mmcblk0p1 35 32 4474 65 84 68 1216 1205 0 880 1270
 *	179       2 mmcblk0p2 0 0 0 0 0 0 0 0 0 0 0
 *	179       3 mmcblk0p3 0 0 0 0 0 0 0 0 0 0 0
 *	179       4 mmcblk0p4 22 105 1016 25 0 0 0 0 0 25 25
 *	179       5 mmcblk0p5 0 0 0 0 0 0 0 0 0 0 0
 *	179       6 mmcblk0p6 0 0 0 0 0 0 0 0 0 0 0
 *	179       7 mmcblk0p7 11 64 618 10 11 11 176 430 0 235 440
 *	179       8 mmcblk0p8 64 2554 20944 420 0 0 0 0 0 340 420
 *	179       9 mmcblk0p9 6427 229 600850 16885 0 0 0 0 0 10290 16845
 *	179      10 mmcblk0p10 366 1560 24538 1235 11473 33349 358608 80055 0 35780 81240
 *	179      11 mmcblk0p11 0 0 0 0 0 0 0 0 0 0 0
 *	179      12 mmcblk0p12 0 0 0 0 0 0 0 0 0 0 0
 *
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
	private int mInitReadsIssued;

	private int mLastReadsMerged;  //[4]
	private int mCurrReadsMerged;
	private int mInitReadsMerged;

    private int mLastWriteMerged;  //[8]
    private int mCurrWriteMerged;
    private int mInitWriteMerged;

	private int mLastSectorsRead;  //[5]
	private int mCurrSectorsRead;
	private int mInitSectorsRead;

	private int mLastSectorsWrite; //[9]
	private int mCurrSectorsWrite;
	private int mInitSectorsWrite;

	private int mSectorReadLast=-1;
	private int mSectorWriteLast=-1;
	private int mReadInitTime=-1;
	private int mReadLastTime=-1;
	private int mReadAmount=0;
	private int mWriteInitTime=-1;
	private int mWriteLastTime=-1;
	private int mWriteAmount=0;
	private int mSectorReadVar = 0;
	private int mSectorWriteVar = 0;

	public Diskstats(String cache, String system, String data, int size) {
		KEY_CACHE = cache;
		KEY_SYSTEM = system;
		KEY_DATA = data;
		mSize = size;
		Log.v(Loading.TAG, "Diskstats, cache="+cache+"; system="+system+"; data="+data+"; size="+size);
	}

	protected void dumpValues() {
		ArrayList<String> result = null;
	    try {
	    	moveValues();
	        RandomAccessFile reader = new RandomAccessFile(Loading.STR_DISK_DISKSTATE, "r");
	        for(;;){

		        String load = reader.readLine();
		        if(Loading.DEBUG && Loading.DISK_DEBUG) Log.v(Loading.TAG, "load: "+load); 
		        if(load==null) {
		        	reader.close();
		        	break;
		        }
		        result = dataPasing(load);
		        if (result.get(2).equals(KEY_SYSTEM) || result.get(2).equals(KEY_DATA)) {
		        	if(Loading.DEBUG && Loading.DISK_DEBUG) Log.v(Loading.TAG,"Parsing Data!!");

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
		        else if(result.get(2).equals(KEY_CACHE)) {
		        	if(Loading.DEBUG && Loading.DISK_DEBUG) Log.v(Loading.TAG,"Parsing Data!! (First)");

		            mCurrReadsIssued = Integer.parseInt(result.get(3));
		            if(Loading.DEBUG && Loading.DISK_DEBUG) Log.v(Loading.TAG, "result.get(3)="+result.get(3)+"+="+mCurrReadsIssued);

		            mCurrReadsMerged = Integer.parseInt(result.get(4));
		            if(Loading.DEBUG && Loading.DISK_DEBUG) Log.v(Loading.TAG, "result.get(4)="+result.get(4)+"+="+mCurrReadsMerged);

		            mCurrWriteMerged = Integer.parseInt(result.get(8));
		            if(Loading.DEBUG && Loading.DISK_DEBUG) Log.v(Loading.TAG, "result.get(8)="+result.get(8)+"+="+mCurrWriteMerged);

		            mCurrSectorsRead = Integer.parseInt(result.get(5));
		            if(Loading.DEBUG && Loading.DISK_DEBUG) Log.v(Loading.TAG, "result.get(5)="+result.get(5)+"+="+mCurrSectorsRead);

		            mCurrSectorsWrite = Integer.parseInt(result.get(9));
		            if(Loading.DEBUG && Loading.DISK_DEBUG) Log.v(Loading.TAG, "result.get(9)="+result.get(9)+"+="+mCurrSectorsWrite);
		        }
	        } 
	    	if(mInitReadsIssued==-1) mInitReadsIssued = mCurrReadsIssued;
	    	if(mInitReadsMerged==-1) mInitReadsMerged = mCurrReadsMerged;
	    	if(mInitWriteMerged==-1) mInitWriteMerged = mCurrWriteMerged;
	    	if(mInitSectorsRead==-1) mInitSectorsRead = mCurrSectorsRead;
	    	if(mInitSectorsWrite==-1) mInitSectorsWrite = mCurrSectorsWrite;
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
    }
    
    private int getReadIssued() { return mCurrReadsIssued; }
    private int getReadReadMerged() { return mCurrReadsMerged; }
    private int getWriteMerged() { return mCurrWriteMerged; }

    private int getSectorsRead() {
    	if (mSectorReadLast==-1) {
    		mSectorReadLast = mCurrSectorsRead ;
    	}
    	mSectorReadVar = mCurrSectorsRead-mSectorReadLast;
    	if(Loading.DEBUG && Loading.DISK_DEBUG)
    		Log.v(Loading.TAG,
    			"getSectorsRead: "+mSectorReadVar+" = "+mCurrSectorsRead+" - "+mSectorReadLast);
    	return mSectorReadVar;
    }

    private int getSectorsWrite() {
    	if (mSectorWriteLast==-1) {
    		mSectorWriteLast = mCurrSectorsWrite ;
    	}
    	mSectorWriteVar = mCurrSectorsWrite - mSectorWriteLast;
    	if(Loading.DEBUG && Loading.DISK_DEBUG)
    		Log.v(Loading.TAG,
    			"getSectorsWrite: "+mSectorWriteVar+" = "+mCurrSectorsWrite+" - "+mSectorWriteLast);
    	return mSectorWriteVar;
    }
 
    protected String getValues(int index){
		if (index==DISK_READ_INDEX) {
			final int read = getSectorsRead() * mSize;
			recordMaxMin(DISK_READ_INDEX, read);
			recordMean(DISK_READ_INDEX, read);
			return String.valueOf(read);
		} else if (index==DISK_WRITE_INDEX) {
			final int write = getSectorsWrite() * mSize;
			recordMaxMin(DISK_WRITE_INDEX, write);
			recordMean(DISK_WRITE_INDEX, write);
			return String.valueOf(write);
		} else if (index==DISK_ALL_INDEX) {
			final int totla = (mSectorWriteVar + mSectorReadVar) * mSize;
			recordMaxMin(DISK_ALL_INDEX, totla);
			recordMean(DISK_ALL_INDEX, totla);
			return String.valueOf(totla);
		}
		return null;
	}
}

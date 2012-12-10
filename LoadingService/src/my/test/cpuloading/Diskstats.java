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
 *      1       0 ram0 0 0 0 0 0 0 0 0 0 0 0
 *      1       1 ram1 0 0 0 0 0 0 0 0 0 0 0
 *      1       2 ram2 0 0 0 0 0 0 0 0 0 0 0
 *      1       3 ram3 0 0 0 0 0 0 0 0 0 0 0
 *      1       4 ram4 0 0 0 0 0 0 0 0 0 0 0
 *      1       5 ram5 0 0 0 0 0 0 0 0 0 0 0
 *      1       6 ram6 0 0 0 0 0 0 0 0 0 0 0
 *      1       7 ram7 0 0 0 0 0 0 0 0 0 0 0
 *      1       8 ram8 0 0 0 0 0 0 0 0 0 0 0
 *      1       9 ram9 0 0 0 0 0 0 0 0 0 0 0
 *      1      10 ram10 0 0 0 0 0 0 0 0 0 0 0
 *      1      11 ram11 0 0 0 0 0 0 0 0 0 0 0
 *      1      12 ram12 0 0 0 0 0 0 0 0 0 0 0
 *      1      13 ram13 0 0 0 0 0 0 0 0 0 0 0
 *      1      14 ram14 0 0 0 0 0 0 0 0 0 0 0
 *      1      15 ram15 0 0 0 0 0 0 0 0 0 0 0
 *      7       0 loop0 0 0 0 0 0 0 0 0 0 0 0
 *      7       1 loop1 0 0 0 0 0 0 0 0 0 0 0
 *      7       2 loop2 0 0 0 0 0 0 0 0 0 0 0
 *      7       3 loop3 0 0 0 0 0 0 0 0 0 0 0
 *      7       4 loop4 0 0 0 0 0 0 0 0 0 0 0
 *      7       5 loop5 0 0 0 0 0 0 0 0 0 0 0
 *      7       6 loop6 0 0 0 0 0 0 0 0 0 0 0
 *      7       7 loop7 0 0 0 0 0 0 0 0 0 0 0
 *    179       0 mmcblk0 6935 4544 652520 18645 11628 33428 360000 81815 0 43580 100370
 *    179       1 mmcblk0p1 35 32 4474 65 84 68 1216 1205 0 880 1270
 *    179       2 mmcblk0p2 0 0 0 0 0 0 0 0 0 0 0
 *    179       3 mmcblk0p3 0 0 0 0 0 0 0 0 0 0 0
 *    179       4 mmcblk0p4 22 105 1016 25 0 0 0 0 0 25 25
 *    179       5 mmcblk0p5 0 0 0 0 0 0 0 0 0 0 0
 *    179       6 mmcblk0p6 0 0 0 0 0 0 0 0 0 0 0
 *    179       7 mmcblk0p7 11 64 618 10 11 11 176 430 0 235 440
 *    179       8 mmcblk0p8 64 2554 20944 420 0 0 0 0 0 340 420
 *    179       9 mmcblk0p9 6427 229 600850 16885 0 0 0 0 0 10290 16845
 *    179      10 mmcblk0p10 366 1560 24538 1235 11473 33349 358608 80055 0 35780 81240
 *    179      11 mmcblk0p11 0 0 0 0 0 0 0 0 0 0 0
 *    179      12 mmcblk0p12 0 0 0 0 0 0 0 0 0 0 0
 *
 *    Field 1 -- # of reads issued [3]
 *    Field 2 -- # of reads merged [4], field 6 -- # of writes merged [8]
 *    Field 3 -- # of sectors read [5]
 *    Field 4 -- # of milliseconds spent reading [6]
 *    Field 5 -- # of writes completed [7]
 *    Field 7 -- # of sectors written [9]
 *    Field 8 -- # of milliseconds spent writing [10]
 *    Field 9 -- # of I/Os currently in progress [11]
 *    Field 10 -- # of milliseconds spent doing I/Os [12]
 *    Field 11 -- weighted # of milliseconds spent doing I/Os [13]
 */
    private int mLastReadsIssued=-1;  //[3]
    private int mCurrReadsIssued;
    private int mInitReadsIssued=-1;

    private int mLastReadsMerged=-1;  //[4]
    private int mCurrReadsMerged;
    private int mInitReadsMerged=-1;

    private int mLastWriteMerged=-1;  //[8]
    private int mCurrWriteMerged;
    private int mInitWriteMerged=-1;

    private int mLastSectorsRead=-1;  //[5]
    private int mCurrSectorsRead;
    private int mInitSectorsRead=-1;

    private int mLastSectorsWrite=-1; //[9]
    private int mCurrSectorsWrite;
    private int mInitSectorsWrite=-1;

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
    
    private long mLastTime=-1;
    private long mCurrTime=-1;

    public Diskstats(String cache, String system, String data, int size) {
        KEY_CACHE = cache;
        KEY_SYSTEM = system;
        KEY_DATA = data;
        mSize = size;
        Log.v(Loading.TAG, "Diskstats, cache="+cache+"; system="+system+"; data="+data+"; size="+size);
    }

    protected void dumpValues() {
        if(Loading.DISK_DEBUG)
            Log.v(Loading.TAG, "[--Diskstats--]");
        ArrayList<String> result = null;
        try {
            RandomAccessFile reader = new RandomAccessFile(Loading.STR_DISK_DISKSTATE, "r");
            long curr = System.currentTimeMillis();
            if (mLastTime==-1) {
            	mCurrTime = mLastTime = curr;
            } else {
            	mLastTime = mCurrTime;
            	mCurrTime = curr;
            }
            for(;;){
                String load = reader.readLine();
                if(Loading.DISK_DEBUG) Log.v(Loading.TAG, "load: "+load); 
                if(load==null) {
                    reader.close();
                    break;
                }
                result = dataPasing(load);
                if (result.get(2).equals(KEY_SYSTEM) || result.get(2).equals(KEY_DATA)) {
                    if(Loading.DISK_DEBUG) Log.v(Loading.TAG,"Parsing Data!!");

                    int ReadsIssued = Integer.parseInt(result.get(3));
                    if(Loading.DISK_DEBUG)
                       Log.v(Loading.TAG,"ReadsIssued(3)="+ReadsIssued+"+"+mCurrReadsIssued+"="+(mCurrReadsIssued+ReadsIssued));
                    mCurrReadsIssued += ReadsIssued;

                    int ReadsMerged = Integer.parseInt(result.get(4));
                    if(Loading.DISK_DEBUG)
                        Log.v(Loading.TAG,"ReadsMerged(4)="+ReadsMerged+"+"+mCurrReadsMerged+"="+(mCurrReadsMerged+ReadsMerged));
                    mCurrReadsMerged += ReadsMerged;

                    int WriteMerged = Integer.parseInt(result.get(8));
                    if(Loading.DISK_DEBUG)
                        Log.v(Loading.TAG,"WriteMerged(8)="+WriteMerged+"+"+mCurrWriteMerged+"="+(mCurrWriteMerged+WriteMerged));
                    mCurrWriteMerged += WriteMerged;

                    int SectorsRead = Integer.parseInt(result.get(5));
                    if(Loading.DISK_DEBUG)
                        Log.v(Loading.TAG, "SectorsRead(5)="+SectorsRead+"+"+mCurrSectorsRead+"="+(mCurrSectorsRead+SectorsRead));
                    mCurrSectorsRead += SectorsRead;

                    int SectorsWrite = Integer.parseInt(result.get(9));
                    if(Loading.DISK_DEBUG)
                        Log.v(Loading.TAG,"SectorsWrite(9)="+SectorsWrite+"+"+mCurrSectorsWrite+"="+(mCurrSectorsWrite+SectorsWrite));
                    mCurrSectorsWrite += SectorsWrite;
                }
                else if(result.get(2).equals(KEY_CACHE)) {
                    if(Loading.DISK_DEBUG) Log.v(Loading.TAG,"Parsing Data!! (First)");

                    int ReadsIssued = Integer.parseInt(result.get(3));
                    mCurrReadsIssued = ReadsIssued;
                    if(Loading.DISK_DEBUG) Log.v(Loading.TAG, "ReadsIssued(3)="+ReadsIssued);

                    int ReadsMerged = Integer.parseInt(result.get(4));
                    mCurrReadsMerged = ReadsMerged;
                    if(Loading.DISK_DEBUG) Log.v(Loading.TAG, "ReadsMerged(4)="+ReadsMerged);

                    int WriteMerged = Integer.parseInt(result.get(8));
                    mCurrWriteMerged = WriteMerged;
                    if(Loading.DISK_DEBUG) Log.v(Loading.TAG, "WriteMerged(8)="+WriteMerged);

                    int SectorsRead = Integer.parseInt(result.get(5));
                    mCurrSectorsRead = SectorsRead;
                    if(Loading.DISK_DEBUG) Log.v(Loading.TAG, "SectorsRead(5)="+SectorsRead);

                    int SectorsWrite = Integer.parseInt(result.get(9));
                    mCurrSectorsWrite = SectorsWrite;
                    if(Loading.DISK_DEBUG) Log.v(Loading.TAG, "SectorsWrite(9)="+SectorsWrite);
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

    private int getReadIssued() { return mCurrReadsIssued; }
    private int getReadReadMerged() { return mCurrReadsMerged; }
    private int getWriteMerged() { return mCurrWriteMerged; }

    private int getSectorsRead() {
        if (mLastSectorsRead==-1) {
            mLastSectorsRead = mCurrSectorsRead ;
        }
        mSectorReadVar = mCurrSectorsRead-mLastSectorsRead;
        if(Loading.DISK_DEBUG)
            Log.v(Loading.TAG,
                    "getSectorsRead: "+mSectorReadVar+" = "+mCurrSectorsRead+" - "+mLastSectorsRead);
        mLastSectorsRead = mCurrSectorsRead;
        return mSectorReadVar;
    }

    private int getSectorsWrite() {
        if (mLastSectorsWrite==-1) {
            mLastSectorsWrite = mCurrSectorsWrite ;
        }
        mSectorWriteVar = mCurrSectorsWrite - mLastSectorsWrite;
        if(Loading.DISK_DEBUG)
            Log.v(Loading.TAG,
                    "getSectorsWrite: "+mSectorWriteVar+" = "+mCurrSectorsWrite+" - "+mLastSectorsWrite);
        mLastSectorsWrite = mCurrSectorsWrite;
        return mSectorWriteVar;
    }
 
    protected String getValues(int index){
    	int value = 0;
    	float fvalue = 0;
    	float time = (float) (mCurrTime-mLastTime);
    	if (LoadingService.mFloatList.contains(index) && time==0) return "--";
    	
        if(Loading.DISK_DEBUG)
            Log.v(Loading.TAG,BasicFunc.mClassName[index]+"/"+BasicFunc.mResourceName[index]+" getValues("+index+")");
        switch(index) {
            case DISK_READ_INDEX:
	            value = getSectorsRead() * mSize;
	            break;
            case DISK_WRITE_INDEX:
                value = getSectorsWrite() * mSize;
                break;
            case DISK_ALL_INDEX:
                value = (mSectorWriteVar + mSectorReadVar) * mSize;
                break;
            case DISK_READ_RATE_INDEX:
                if(Loading.DISK_DEBUG)
                    Log.v(Loading.TAG,"DISK_READ_RATE_INDEX: "+mSectorReadVar+" * "+mSize+" *1024 / "+time+")");
                if (mSectorReadVar>0) {
                	fvalue = round(mSectorReadVar * mSize *1024 / time,2);
                } else {
                	fvalue = 0;
                }
                break;
            case DISK_WRITE_RATE_INDEX:
                if(Loading.DISK_DEBUG)
                    Log.v(Loading.TAG,"DISK_WRITE_RATE_INDEX: "+mSectorWriteVar+" * "+mSize+" *1024 / "+time+")");
                if (mSectorWriteVar>0) {
                	fvalue = round(mSectorWriteVar * mSize *1024 / time,2);
                } else {
                	fvalue = 0;
                }
                
                break;
            case DISK_ALL_RATE_INDEX:
                if(Loading.DISK_DEBUG)
                    Log.v(Loading.TAG,"DISK_ALL_RATE_INDEX: "+(mSectorWriteVar + mSectorReadVar)+" * "+mSize+" *1024 / "+time+")");
                if (mSectorWriteVar>0 || mSectorReadVar>0) {
                	fvalue = round((mSectorWriteVar + mSectorReadVar) * mSize *1024 / time,2);
                } else {
                	fvalue = 0;
                }

                break;
            default:
            	return "--";
        }
        
        if (LoadingService.mFloatList.contains(index)) {
        	recordMaxMin(index, fvalue);
        	recordMean(index, fvalue);
        	return String.valueOf(fvalue);
        } else {
        	recordMaxMin(index, value);
        	recordMean(index, value);
        	return String.valueOf(value);
        }
    }
}

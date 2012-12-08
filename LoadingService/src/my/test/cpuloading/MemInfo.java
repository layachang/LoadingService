package my.test.cpuloading;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.HashMap;

import android.util.Log;

public class MemInfo extends BasicFunc {

/*
 * MemTotal:         850532 kB
 * MemFree:          229588 kB
 * Buffers:           41252 kB
 * Cached:           219484 kB
 * SwapCached:            0 kB
 * Active:           392364 kB
 * Inactive:         148784 kB
 * Active(anon):     281492 kB
 * Inactive(anon):     1040 kB
 * Active(file):     110872 kB
 * Inactive(file):   147744 kB
 * Unevictable:        1028 kB
 * Mlocked:               0 kB
 * HighTotal:        229376 kB
 * HighFree:            424 kB
 * LowTotal:         621156 kB
 * LowFree:          229164 kB
 * SwapTotal:             0 kB
 * SwapFree:              0 kB
 * Dirty:                 0 kB
 * Writeback:             0 kB
 * AnonPages:        281468 kB
 * Mapped:            81120 kB
 * Shmem:              1108 kB
 * Slab:              18036 kB
 * SReclaimable:       9452 kB
 * SUnreclaim:         8584 kB
 * KernelStack:        6640 kB
 * PageTables:        11844 kB
 * NFS_Unstable:          0 kB
 * Bounce:                0 kB
 * WritebackTmp:          0 kB
 * CommitLimit:      425264 kB
 * Committed_AS:    6324924 kB
 * VmallocTotal:     139264 kB
 * VmallocUsed:       62252 kB
 * VmallocChunk:      46020 kB
 */
	HashMap<String,Integer> mData = new HashMap<String,Integer>();
	
	private int mInitTime = -1;
	private int mLastTime = -1;

	protected void dumpValues() {
		ArrayList<String> result = null;
	    try {
	        RandomAccessFile reader = new RandomAccessFile(Loading.STR_MEM_MEMINFO, "r");
	        for(;;){
		        String load = reader.readLine();
		        if(load==null) {
		        	reader.close();
		        	break;
		        }
		        result = dataPasing(load);
		        if(Loading.DEBUG && Loading.MEM_DEBUG) Log.v(Loading.TAG, "load: "+load);
		        String key = result.get(0).replace(":", "");
		        mData.put(key, Integer.parseInt(result.get(1)));
	        } 

	        
	    } 
	    catch (IOException ex) {
	    	ex.printStackTrace();
	    }
	}

	private int getMemUsed() {
		return Math.round(
				(mData.get("MemTotal")-mData.get("MemFree"))*100 / mData.get("MemTotal")
				);
	}

	private int getMemBuffers() { return mData.get("Buffers"); }
	
	private int getMemCached() { return mData.get("Cached"); }

	@Override
	protected String getValues(int index) {
		if (index==MEM_ALL_INDEX) {
			final int used = getMemUsed();
			recordMaxMin(MEM_ALL_INDEX, used);
			recordMean(MEM_ALL_INDEX, used);
			return String.valueOf(used);
		}
		return null;
	}

}

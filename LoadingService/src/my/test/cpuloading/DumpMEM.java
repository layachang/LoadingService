package my.test.cpuloading;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import android.util.Log;

public class DumpMEM extends BasicFunc {
    private int mAllMem=0;
    private int mAlocMem=0;
    private int mFreeMem=0;
    private int mInit=-1;
    private int mInitTime = -1;
    private int mLastTime = -1;
/*
1:    Applications Memory Usage (kB):
2:        Uptime: 11685093 Realtime: 26245584
3:
4:        ** MEMINFO in pid 9690 [my.test.cpuloading] **
5:                            native   dalvik    other    total
6:                    size:     3228     9735      N/A    12963
7:               allocated:     3195     8839      N/A    12034
8:                    free:       20      896      N/A      916
9:                   (Pss):      819      470     6930     8219
10:          (shared dirty):      948     1904     7864    10716
11:            (priv dirty):      796      360     6288     7444
12:
13:         Objects
14:                   Views:       23        ViewRoots:        1
15:             AppContexts:        4       Activities:        1
16:                  Assets:        3    AssetManagers:        3
17:           Local Binders:       11    Proxy Binders:       18
18:        Death Recipients:        1
19:         OpenSSL Sockets:        0
20:
21:         SQL
22:                       heap:        0         MEMORY_USED:        0
23:         PAGECACHE_OVERFLOW:        0         MALLOC_SIZE:        0
*/
         
    public DumpMEM(int pid) {
        mPID = String.valueOf(pid);
        Log.v(Loading.TAG, "DumpMEM, mPID="+mPID);
    }

    public void dumpValues() {
        if(Loading.MEM_PID_DEBUG)
            Log.v(Loading.TAG, "[--DumpMEM--], hasValue="+hasValue);
        if (!hasValue) return;
        ArrayList<String> result = null;

        try {
            final List<String> commandLine = new ArrayList<String>();
            commandLine.add("dumpsys");
            commandLine.add("meminfo");
            commandLine.add(mPID);

            final Process process = Runtime.getRuntime().exec(commandLine.toArray(new String[commandLine.size()]));
            final BufferedReader bufferedReader = new BufferedReader(
                        new InputStreamReader(process.getInputStream()),
                        1024*1024);
            String line = null;
            for (int count=0;count<8;count++) {
                line = bufferedReader.readLine();
                if(Loading.MEM_PID_DEBUG)
                    Log.v(Loading.TAG, count+" DumpMEM--"+line);
                //size
                if (count==(6-1) && line!=null) {
                    result = dataPasing(line);
                    mAllMem = Integer.parseInt(result.get(4));
                    if(Loading.MEM_PID_DEBUG)
                        Log.v(Loading.TAG, "mAllMem: "+mAllMem);
                }
                //allocated
                else if (count==(7-1) && line!=null) {
                    result = dataPasing(line);
                    mAlocMem = Integer.parseInt(result.get(4));
                    if(Loading.MEM_PID_DEBUG)
                        Log.v(Loading.TAG, "mAlocMem: "+mAlocMem);
                }
                //free
                else if (count==(8-1) && line!=null) {
                    result = dataPasing(line);
                    mFreeMem = Integer.parseInt(result.get(4));
                    if(Loading.MEM_PID_DEBUG)
                        Log.v(Loading.TAG, "mFreeMem: "+mFreeMem);
                }
            }
            bufferedReader.close();
        } catch (IOException e) {
            Log.e(Loading.TAG, "DumpSysCollector.meminfo could not retrieve data", e);
        }
    }

    private int getALOC() { return mAlocMem; }
    private int getMemUsed() {
        return Math.round( (mAllMem-mFreeMem)*100 / mAllMem );
    }

    protected String getValues(int index){
        if(Loading.MEM_PID_DEBUG)
            Log.v(Loading.TAG,BasicFunc.mClassName[index]+"/"+BasicFunc.mResourceName[index]+" getValues("+index+"), hasValue="+hasValue);
        if (index==MEM_PID_INDEX && hasValue) {
            final int used = getMemUsed();
            recordMaxMin(MEM_PID_INDEX, used);
            recordMean(MEM_PID_INDEX, used);
            return String.valueOf(used);
        }
        return "--";
    }

    public void setPid(int pid) {
        mPID = String.valueOf(pid);
        hasValue = true;
    }
}

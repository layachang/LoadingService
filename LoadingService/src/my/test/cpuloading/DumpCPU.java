package my.test.cpuloading;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import android.util.Log;

public class DumpCPU extends BasicFunc {
    private float mCurrCpu=0;
    private long mCurrentTime=0;
    private int mLogTime=0;
    private int mInitTime = -1;
    private int mLastTime = -1;

    public DumpCPU(int pid) {
        mPID = String.valueOf(pid);
        if(Loading.CPU_PID_DEBUG)
            Log.v(Loading.TAG, "DumpCPU, mPID--"+mPID);
    }

    public void dumpValues() {
        if(Loading.CPU_PID_DEBUG)
            Log.v(Loading.TAG, "[--DumpCPU--], hasValue="+hasValue);
        if (!hasValue) return;
        //final StringBuilder cpuinfo = new StringBuilder();

        try {
            final List<String> commandLine = new ArrayList<String>();
            commandLine.add("dumpsys");
            commandLine.add("cpuinfo");
            //commandLine.add(Integer.toString(android.os.Process.myPid()));

            final Process process = Runtime.getRuntime().exec(commandLine.toArray(new String[commandLine.size()]));
            final BufferedReader bufferedReader = new BufferedReader(
                        new InputStreamReader(process.getInputStream()),
                        1024*1024);
            int line_count = 0;
            for(;;) {
                line_count++;
                String line = bufferedReader.readLine();
                if(Loading.CPU_PID_DEBUG)
                    Log.v(Loading.TAG, line_count+" DumpCPU--"+line);
                if ( line_count==1 || line_count==2) {
                    if (line.startsWith("Permission Denial")) {
                        Log.e(Loading.TAG, line);
                        break;
                    }
                    continue;
                }

                String s[] = line.split(" ");
                if (s.length<2) {
                    continue;
                }
                
                if (line == null || s[1].equals("TOTAL:") || line_count>50) {
                    break;
                }
                String pid_proc = s[3];
                if(Loading.CPU_PID_DEBUG)
                    Log.v(Loading.TAG, "DumpCPU:"+pid_proc+"; mPID.length()="+mPID.length());
                if (pid_proc!=null && 
                        pid_proc.length() >= mPID.length() &&
                        pid_proc.substring(0,mPID.length()).equals(mPID)) {
                    mCurrCpu = Float.valueOf(s[2].substring(0, s[2].length()-1));
                    if(Loading.CPU_PID_DEBUG)
                        Log.v(Loading.TAG, "DumpCPU-------"+mCurrCpu);
                    break;
                }
                //cpuinfo.append(line);
                //cpuinfo.append("\n");
            }
            bufferedReader.close();
        } catch (IOException e) {
            Log.e(Loading.TAG, "DumpSysCollector.meminfo could not retrieve data", e);
        }
    }
    private int getCPU() {
        return Math.round(mCurrCpu);
    }
    protected String getValues(int index){
        if(Loading.CPU_PID_DEBUG)
            Log.v(Loading.TAG,
                    BasicFunc.mClassName[index]+"/"+BasicFunc.mResourceName[index]+" getValues("+index+"), hasValue="+hasValue);
        if (index==CPU_PID_INDEX && hasValue) {
            final int utilization = getCPU();
            recordMaxMin(CPU_PID_INDEX, utilization);
            recordMean(CPU_PID_INDEX, utilization);
            return String.valueOf(utilization);
        }
        return "--";
    }

    public void setPid(int pid) {
        hasValue = true;
        mPID = String.valueOf(pid);
    }
}

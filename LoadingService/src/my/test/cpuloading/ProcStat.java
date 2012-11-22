package my.test.cpuloading;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.math.BigDecimal;

import android.content.Context;
import android.util.Log;

public class ProcStat {

	private int mLastCtxt = -1;
	private int mCurrCtxt = -1;
	private int mLastProcesses = -1;
	private int mCurrProcesses = -1;
	private int mProcs_running;
	private int mProcs_blocked;
	private long mLastIdle = -1;
	private long mCurrIdle = -1;
	private long mLastCpu = -1;
	private long mCurrCpu = -1;
	private long mCurrentTime=0;
	private int mLogTime=0;
	private Context mContext;
	
/* user: 一般跑在user mode下的processes
 * nice: 跑在user mode下的nice processes
 * system: 執行在kernel mode下的processes
 * idle: cpu閒閒沒事的累計數量
 * iowait: 等待I/O完成的時間
 * irq: 執行中斷服務的時間
 * softirq: 執行軟體中斷服務的時間
 * intr後面接續著很多行數字（包括很多的0），其計算從開機（boot time）以來每一種中斷的服務次數，其中第一欄數字是所有中斷的總合
 * ctxt累計所有cpus的context switches次數
 * btime紀錄系統開機時距離Unix epoch多少時間
 * processes紀錄有多少processes與threads被建立
 * procs_running紀錄cpu正執行多少個processes
 * procs_blocked記錄當下有多少process被block住等待I/O服務完成
 * 
 *      [user][nice][system][idle] [iowait][irq][softirq]
 * cpu  92674  3321   53937 2090481  5710   53    1292 
 * cpu0 87275  2630   51703 984429   4714   53    1289
 * intr 3455384 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 123 0 3576 0 0 0 1 0 0 0 0 .....
 * ctxt 6435833
 * btime 1345392835
 * processes 8456
 * procs_running 1
 * procs_blocked 0
 * 
 */
	private int init_ctxt=-1;
	private int init_processes=-1;
	private int init_procs_running=-1;
	private int init_procs_blocked=-1;
	private int CPU_NUM=0;
	
	private WriteFile2SD wfile;
	
	public ProcStat(Context context, WriteFile2SD file, int cpu_num) {
		mContext = context;
		wfile = file; 
		CPU_NUM = cpu_num;
		Log.v(Loading.TAG, "CPU_NUM="+CPU_NUM);
	}

	protected void initProcStat(long ctime) {
		mCurrentTime = ctime;
		
	    try {
	    	boolean isIntrLoad = false;
	    	moveValues();
	    	
	        RandomAccessFile reader = new RandomAccessFile(Loading.STR_CPU_STAT, "r");
	        if(Loading.DEBUG && Loading.CPU_DEBUG)  Log.v(Loading.TAG, "--"+Loading.STR_CPU_STAT+"--");
	        
	        String load = reader.readLine();
	        if(Loading.DEBUG && Loading.CPU_DEBUG)  Log.v(Loading.TAG, "1:"+load);
	        
	        String[] toks = load.split(" ");
	        mCurrIdle = getIdle(toks);
	        mCurrCpu = getCpu(toks);
	        load = reader.readLine(); //cpu0
	        if(Loading.DEBUG && Loading.CPU_DEBUG)  Log.v(Loading.TAG, "cpu0:"+load);
	        if (CPU_NUM>1) {
	        	for (int j=1; j<CPU_NUM;j++) {
	    	        load = reader.readLine(); //cpu-n
	    	        toks = load.split(" ");
	    	        String field =toks[0];
	    	        if (field.equals("intr")) {
	    	        	isIntrLoad = true;
	    	        	break;
	    	        } else {
	    	        	if(Loading.DEBUG && Loading.CPU_DEBUG)  Log.v(Loading.TAG, "cpu"+j+":"+load);
	    	        }
	        	}
	        }
	        if(!isIntrLoad) load = reader.readLine(); //intr
	        if(Loading.DEBUG && Loading.CPU_DEBUG)  Log.v(Loading.TAG, "intr:"+load);
	        load = reader.readLine(); //ctxt
	        if(Loading.DEBUG && Loading.CPU_DEBUG)  Log.v(Loading.TAG, "ctxt:"+load);
	        toks = load.split(" ");
	        mCurrCtxt = Integer.parseInt(toks[1]);
	        if (init_ctxt==-1) init_ctxt = mCurrCtxt;
	        
	        load = reader.readLine(); //btime
	        if(Loading.DEBUG && Loading.CPU_DEBUG)  Log.v(Loading.TAG, "btime:"+load);
	        load = reader.readLine(); //processes -- 累計所有cpus的context switches次數
	        if(Loading.DEBUG && Loading.CPU_DEBUG)  Log.v(Loading.TAG, "processes:"+load);
	        toks = load.split(" ");
	        mCurrProcesses = Integer.parseInt(toks[1]);
	        if (init_processes==-1) init_processes = mCurrProcesses;
	        
	        load = reader.readLine(); //procs_running -- 紀錄cpu正執行多少個processes
	        if(Loading.DEBUG && Loading.CPU_DEBUG)  Log.v(Loading.TAG, "procs_running:"+load);
	        toks = load.split(" ");
	        mProcs_running = Integer.parseInt(toks[1]);
	        
	        load = reader.readLine(); //procs_blocked -- 記錄當下有多少process被block住等待I/O服務完成
	        if(Loading.DEBUG && Loading.CPU_DEBUG)  Log.v(Loading.TAG, "procs_blocked:"+load);
	        
	        toks = load.split(" ");
	        mProcs_blocked = Integer.parseInt(toks[1]);
	        if(Loading.DEBUG && Loading.CPU_DEBUG)  Log.v(Loading.TAG, "-------------------------");
	        reader.close();
	    } catch (IOException ex) {
	        ex.printStackTrace();
	    }
	}
	
	private void moveValues() {
		mLastIdle = mCurrIdle;
		mLastCpu = mCurrCpu;
		init_ctxt = mCurrCtxt;
		init_processes = mCurrProcesses;
	}

	private long getIdle(String[] toks) {
		return Long.parseLong(toks[5]);
	}

	private long getCpu(String[] toks) {
		return Long.parseLong(toks[2]) + Long.parseLong(toks[3]) + Long.parseLong(toks[4])
	              + Long.parseLong(toks[6]) + Long.parseLong(toks[7]) + Long.parseLong(toks[8]);
	}

	protected void printCPU(boolean withValue){
		String input = "CPU," + mLogTime ;
		if (withValue) {
			float u = (float) (mCurrCpu - mLastCpu) / ((mCurrCpu + mCurrIdle) - (mLastCpu + mLastIdle)) *100;
	        input = "CPU," + mLogTime + "," + Math.round(u);
		}
		if(Loading.DEBUG && Loading.CPU_DEBUG) Log.v(Loading.TAG, "input: "+input);
        wfile.write(input);

	}

	protected void printCtxt(boolean withValue){
		String input = "CPU ctxt," + mLogTime;
		if (withValue) {
			input = "CPU ctxt," + mLogTime + "," + (mCurrCtxt-init_ctxt);
		}
		if(Loading.DEBUG && Loading.CPU_DEBUG) Log.v(Loading.TAG, "input: "+input+"("+mCurrCtxt+"-"+init_ctxt+")");
		wfile.write(input);
	}
	protected void printProcesses(boolean withValue){
		String input = "CPU Processes," + mLogTime;
		if (withValue) {
		    input = "CPU Processes," + mLogTime + "," + (mCurrProcesses);
		}
		if(Loading.DEBUG && Loading.CPU_DEBUG) Log.v(Loading.TAG, "input: "+input+"("+mCurrProcesses+"-"+init_processes+")");
		wfile.write(input);
	}
	protected void printProcsRunning(boolean withValue){
		String input = "CPU ProcsRunning," + mLogTime;
		if (withValue) {
		    input = "CPU ProcsRunning," + mLogTime + "," + mProcs_running;
		}
		if(Loading.DEBUG && Loading.CPU_DEBUG) Log.v(Loading.TAG, "input: "+input);
		wfile.write(input);
	}
	
	protected void printProcsBlocked(boolean withValue){
		String input = "CPU ProcsBlocked," + mLogTime;
		if (withValue) {
		    input = "CPU ProcsBlocked," + mLogTime + "," + mProcs_blocked;
		}
		if(Loading.DEBUG && Loading.CPU_DEBUG) Log.v(Loading.TAG, "input: "+input);
		wfile.write(input);
	}
	

	public void printAll() {
		int cur_log_time=((LoadingService) mContext).getTime(mCurrentTime);
		while (cur_log_time-mLogTime>1) {
			mLogTime++;
			Log.i("LoadingService", "===="+mLogTime+"===");
	    	printCPU(false);
	    	printCtxt(false);
	    	printProcesses(false);
	    	printProcsBlocked(false);
	    	printProcsRunning(false);
		}
		mLogTime = cur_log_time;
		Log.i("LoadingService", "===="+mLogTime+"===");
    	printCPU(true);
    	printCtxt(true);
    	printProcesses(true);
    	printProcsBlocked(true);
    	printProcsRunning(true);
	}
}

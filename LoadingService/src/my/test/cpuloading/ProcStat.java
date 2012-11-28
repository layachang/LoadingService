package my.test.cpuloading;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.math.BigDecimal;
import java.util.ArrayList;

import android.content.Context;
import android.util.Log;

public class ProcStat extends BasicFunc {
	private int mLastCtxt = -1;
	private int mCurrCtxt = -1;
	private int mLastProcesses = -1;
	private int mCurrProcesses = -1;
	private int mProcs_running;
	private int mProcs_blocked;
	private long mLastIdle = 0;
	private long mCurrIdle = 0;
	private long mInitIdle = 0;
	private long mLastCpu = 0;
	private long mCurrCpu = 0;
	private long mInitCpu = 0;

	
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

	public ProcStat(int pid, int uid, int cpu_num) {
		CPU_NUM = cpu_num;
		Log.v(Loading.TAG, "ProcStat, pid="+pid+"; uid="+uid+"; cpu_num="+cpu_num);
	}

	protected void dumpValues() {
	    try {
	    	boolean isIntrLoad = false;
	    	moveValues();
	    	
	        RandomAccessFile reader = new RandomAccessFile(Loading.STR_CPU_STAT, "r");
	        if(Loading.DEBUG && Loading.CPU_DEBUG)
	        	Log.v(Loading.TAG, "--"+Loading.STR_CPU_STAT+"--");
	        
	        String load = reader.readLine();
	        if(Loading.DEBUG && Loading.CPU_DEBUG)
	        	Log.v(Loading.TAG, "1:"+load);
	        
	        String[] toks = load.split(" ");
	        mCurrIdle = getIdle(toks);
	        mCurrCpu = getCpu(toks);
	        if(Loading.DEBUG && Loading.CPU_DEBUG) {
	            Log.v(Loading.TAG, "mCurrIdle:"+mCurrIdle);
	            Log.v(Loading.TAG, "mCurrCpu:"+mCurrCpu);
	        }
	        load = reader.readLine(); //cpu0
	        if(Loading.DEBUG && Loading.CPU_DEBUG)
	        	Log.v(Loading.TAG, "cpu0:"+load);
	        if (CPU_NUM>1) {
	        	for (int j=1; j<CPU_NUM;j++) {
	    	        load = reader.readLine(); //cpu-n
	    	        toks = load.split(" ");
	    	        String field =toks[0];
	    	        if (field.equals("intr")) {
	    	        	isIntrLoad = true;
	    	        	break;
	    	        } else {
	    	        	if(Loading.DEBUG && Loading.CPU_DEBUG)
	    	        		Log.v(Loading.TAG, "cpu"+j+":"+load);
	    	        }
	        	}
	        }
	        if(!isIntrLoad) load = reader.readLine(); //intr
	        if(Loading.DEBUG && Loading.CPU_DEBUG)
	        	Log.v(Loading.TAG, "intr:"+load);
	        load = reader.readLine(); //ctxt
	        if(Loading.DEBUG && Loading.CPU_DEBUG)
	        	Log.v(Loading.TAG, "ctxt:"+load);
	        toks = load.split(" ");
	        mCurrCtxt = Integer.parseInt(toks[1]);
	        if (init_ctxt==-1) init_ctxt = mCurrCtxt;
	        
	        load = reader.readLine(); //btime
	        if(Loading.DEBUG && Loading.CPU_DEBUG)
	        	Log.v(Loading.TAG, "btime:"+load);
	        load = reader.readLine(); //processes -- 累計所有cpus的context switches次數
	        if(Loading.DEBUG && Loading.CPU_DEBUG)
	        	Log.v(Loading.TAG, "processes:"+load);
	        toks = load.split(" ");
	        mCurrProcesses = Integer.parseInt(toks[1]);
	        if (init_processes==-1) init_processes = mCurrProcesses;
	        
	        load = reader.readLine(); //procs_running -- 紀錄cpu正執行多少個processes
	        if(Loading.DEBUG && Loading.CPU_DEBUG)
	        	Log.v(Loading.TAG, "procs_running:"+load);
	        toks = load.split(" ");
	        mProcs_running = Integer.parseInt(toks[1]);
	        
	        load = reader.readLine(); //procs_blocked -- 記錄當下有多少process被block住等待I/O服務完成
	        if(Loading.DEBUG && Loading.CPU_DEBUG)
	        	Log.v(Loading.TAG, "procs_blocked:"+load);
	        
	        toks = load.split(" ");
	        mProcs_blocked = Integer.parseInt(toks[1]);
	        if(Loading.DEBUG && Loading.CPU_DEBUG)
	        	Log.v(Loading.TAG, "-------------------------");
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
		long l = Long.parseLong(toks[5]);
		if (mInitIdle!=0) {
			return l;
		} else {
			mInitIdle = l;
			return l;
		}

	}

	private long getCpu(String[] toks) {
		long l = Long.parseLong(toks[2]) + Long.parseLong(toks[3]) + Long.parseLong(toks[4])
	              + Long.parseLong(toks[6]) + Long.parseLong(toks[7]) + Long.parseLong(toks[8]);
		if(Loading.DEBUG && Loading.CPU_DEBUG)
			Log.v(Loading.TAG, 	Long.parseLong(toks[2]) +"+"+
								Long.parseLong(toks[3]) +"+"+
								Long.parseLong(toks[4]) +"+"+
								Long.parseLong(toks[6]) +"+"+
								Long.parseLong(toks[7]) +"+"+
								Long.parseLong(toks[8])+" = " + String.valueOf(l));
		if (mInitCpu!=0) {
			return l;
		} else {
			mInitCpu = l;
			return l;
		}
	}

	private int getUtil() {
		float input = (mCurrCpu - mLastCpu)*100 /
						((mCurrCpu + mCurrIdle) - (mLastCpu + mLastIdle)) ;
		int result = Math.round(input) ; 
		return result;
	}

	private int getCtxt() { return mCurrCtxt-init_ctxt; }
	private int getProcesses() { return mCurrProcesses; }
	private int getProcsRunning() { return mProcs_running; }
	private int getProcsBlocked() { return mProcs_blocked; 	}

	@Override
	public String getValues(int index) {
		if (index==CPU_ALL_INDEX) {
			final int utilization = getUtil();
			recordMaxMin(CPU_ALL_INDEX, utilization);
			//recordMean(CPU_ALL_INDEX, u);
			return String.valueOf(utilization);
		}
		return null;
	}
	public String getMena() {
		float input = (mCurrCpu - mInitCpu)*100 /
				((mCurrCpu + mCurrIdle) - (mInitCpu + mInitIdle)) ;
		return String.valueOf(Math.round(input));
	}
}

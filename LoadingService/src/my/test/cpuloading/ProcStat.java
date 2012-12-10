package my.test.cpuloading;

import java.io.IOException;
import java.io.RandomAccessFile;

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
    private long mLastCpu = 0;
    private long mCurrCpu = 0;
    private long mLastUser = -1;
    private long mCurrUser = 0;
    private long mLastNice = -1;
    private long mCurrNice = 0;
    private long mLastSys = -1;
    private long mCurrSys = 0;
    private long mLastIdl = -1;
    private long mCurrIdl = 0;
    private long mLastIOW = -1;
    private long mCurrIOW = 0;
    private long mLastIRQ = -1;
    private long mCurrIRQ = 0;
    private long mLastSIRQ = -1;
    private long mCurrSIRQ = 0;
    private long mLastIntr = -1;
    private long mCurrIntr = 0;
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
    private long init_cpu=-1;
    private long inti_idle=-1;
    private long init_intr=-1;
    private int init_ctxt=-1;
    private int init_processes=-1;
    private int init_procs_running=-1;
    private int init_procs_blocked=-1;
    private int CPU_NUM=0;

    public ProcStat(int cpu_num) {
        CPU_NUM = cpu_num;
        Log.v(Loading.TAG, "ProcStat, cpu_num="+cpu_num);
    }

    protected void dumpValues() {
        try {
            if(Loading.CPU_DEBUG)
                Log.v(Loading.TAG, "[--ProcStat--]");
            boolean isIntrLoad = false;
            moveValues();
            RandomAccessFile reader = new RandomAccessFile(Loading.STR_CPU_STAT, "r");
            if(Loading.CPU_DEBUG)
                Log.v(Loading.TAG, "--"+Loading.STR_CPU_STAT+"--");
            
            String load = reader.readLine();
            if(Loading.CPU_DEBUG)
                Log.v(Loading.TAG, "1:"+load);

            String[] toks = load.split(" ");
            mCurrIdle = getIdle(toks);
            if (inti_idle==-1) inti_idle = mCurrIdle;

            mCurrCpu = getCpu(toks);
            if (init_cpu==-1) init_cpu = mCurrIdle;

            if(Loading.CPU_DEBUG) {
                Log.v(Loading.TAG, "mCurrIdle:"+mCurrIdle);
                Log.v(Loading.TAG, "mCurrCpu:"+mCurrCpu);
            }

            load = reader.readLine(); //cpu0
            if(Loading.CPU_DEBUG)
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
                        if(Loading.CPU_DEBUG)
                            Log.v(Loading.TAG, "cpu"+j+":"+load);
                    }
                }
            }

            if(!isIntrLoad) load = reader.readLine(); //intr
            if(Loading.CPU_DEBUG)
                Log.v(Loading.TAG, "intr:"+load);
            toks = load.split(" ");
            mCurrIntr = Integer.parseInt(toks[1]);
            if (init_intr==-1) init_intr = mCurrIntr;

            load = reader.readLine(); //ctxt
            if(Loading.CPU_DEBUG)
                Log.v(Loading.TAG, "ctxt:"+load);
            toks = load.split(" ");
            mCurrCtxt = Integer.parseInt(toks[1]);
            if (init_ctxt==-1) init_ctxt = mCurrCtxt;
            
            load = reader.readLine(); //btime
            if(Loading.CPU_DEBUG)
                Log.v(Loading.TAG, "btime:"+load);

            load = reader.readLine(); //processes -- 累計所有cpus的context switches次數
            if(Loading.CPU_DEBUG)
                Log.v(Loading.TAG, "processes:"+load);
            toks = load.split(" ");
            mCurrProcesses = Integer.parseInt(toks[1]);
            if (init_processes==-1) init_processes = mCurrProcesses;
            
            load = reader.readLine(); //procs_running -- 紀錄cpu正執行多少個processes
            if(Loading.CPU_DEBUG)
                Log.v(Loading.TAG, "procs_running:"+load);
            toks = load.split(" ");
            mProcs_running = Integer.parseInt(toks[1]);
            if (init_procs_running==-1) init_procs_running = mProcs_running;

            load = reader.readLine(); //procs_blocked -- 記錄當下有多少process被block住等待I/O服務完成
            if(Loading.CPU_DEBUG)
                Log.v(Loading.TAG, "procs_blocked:"+load);
            toks = load.split(" ");
            mProcs_blocked = Integer.parseInt(toks[1]);
            if (init_procs_blocked==-1) init_procs_blocked = mProcs_blocked;

            if(Loading.CPU_DEBUG)
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
        mCurrIdl = Long.parseLong(toks[5]);
        if (mLastIdl==-1) {
            mLastIdl = mCurrIdl;
        }
        int var =  (int) (mCurrIdl - mLastIdl);
        mLastIdl = mCurrIdl;
        return mCurrIdl;
    }

    private long getCpu(String[] toks) {
        mCurrUser = Long.parseLong(toks[2]);
        mCurrNice = Long.parseLong(toks[3]);
        mCurrSys = Long.parseLong(toks[4]);
        mCurrIOW = Long.parseLong(toks[6]);
        mCurrIRQ = Long.parseLong(toks[7]);
        mCurrSIRQ = Long.parseLong(toks[8]);
        long l = mCurrUser + mCurrNice + mCurrSys + mCurrIOW + mCurrIRQ + mCurrSIRQ;
        if(Loading.CPU_DEBUG)
            Log.v(Loading.TAG,  mCurrUser +"+"+mCurrNice +"+"+mCurrSys +"+"+mCurrIOW +"+"+
                                mCurrIRQ +"+"+mCurrSIRQ +" = " + String.valueOf(l));
        return l;

    }

    private int getUtil() {
        float input = (mCurrCpu - mLastCpu)*100 /
                        ((mCurrCpu + mCurrIdle) - (mLastCpu + mLastIdle)) ;
        int result = Math.round(input) ; 
        return result;
    }

    private int getUser() {
        if (mLastUser==-1) {
            mLastUser = mCurrUser;
        }
        int var =  (int) (mCurrUser - mLastUser);
        mLastUser = mCurrUser;
        return var;
    }
    private int getNice() {
        if (mLastNice==-1) {
            mLastNice = mCurrNice;
        }
        int var =  (int) (mCurrNice - mLastNice);
        mLastNice = mCurrNice;
        return var;
    }
    private int getSys() {
        if (mLastSys==-1) {
            mLastSys = mCurrSys;
        }
        int var =  (int) (mCurrSys - mLastSys);
        mLastSys = mCurrSys;
        return var;
    }
    private int getIdle() {
        if (mLastIdl==-1) {
            mLastIdl = mCurrIdl;
        }
        int var =  (int) (mCurrIdl - mLastIdl);
        mLastIdl = mCurrIdl;
        return var;
    }
    private int getIOW() {
        if (mLastIOW==-1) {
            mLastIOW = mCurrIOW;
        }
        int var =  (int) (mCurrIOW - mLastIOW);
        mLastIOW = mCurrIOW;
        return var;
    }
    private int getIRQ() {
        if (mLastIRQ==-1) {
            mLastIRQ = mCurrIRQ;
        }
        int var =  (int) (mCurrIRQ - mLastIRQ);
        mLastIRQ = mCurrIRQ;
        return var;
    }
    private int getSIRQ() {
        if (mLastSIRQ==-1) {
            mLastSIRQ = mCurrSIRQ;
        }
        int var =  (int) (mCurrSIRQ - mLastSIRQ);
        mLastSIRQ = mCurrSIRQ;
        return var;
    }
    private int getIntr() {
        if (mLastIntr==-1) {
            mLastIntr = mCurrIntr;
        }
        int var =  (int) (mCurrIntr - mLastIntr);
        mLastIntr = mCurrIntr;
        return var;
    }
    private int getCtxt() {
        if (mLastCtxt==-1) {
            mLastCtxt = mCurrCtxt;
        }
        int var =  (int) (mCurrCtxt - mLastCtxt);
        mLastCtxt = mCurrCtxt;
        return var;
    }
    private int getProcesses() {
        if (mLastProcesses==-1) {
            mLastProcesses = mCurrProcesses;
        }
        int var =  (int) (mCurrProcesses - mLastProcesses);
        mLastProcesses = mCurrProcesses;
        return var;
    }
    private int getProcsRunning() { return mProcs_running; }
    private int getProcsBlocked() { return mProcs_blocked; }

    @Override
    public String getValues(int index) {
        if(Loading.CPU_DEBUG)
            Log.v(Loading.TAG,BasicFunc.mClassName[index]+"/"+BasicFunc.mResourceName[index]+" getValues("+index+")");
        if (index==CPU_ALL_INDEX) {
            int value = getUtil();
            recordMaxMin(CPU_ALL_INDEX, value);
            recordMean(CPU_ALL_INDEX, value);
            return String.valueOf(value);

        } else if (index==CPU_USER_INDEX) {
            int value = getUser();
            recordMaxMin(CPU_USER_INDEX, value);
            recordMean(CPU_USER_INDEX, value);
            return String.valueOf(value);

        } else if (index==CPU_NICE_INDEX) {
            int value = getNice();
            recordMaxMin(CPU_NICE_INDEX, value);
            recordMean(CPU_NICE_INDEX, value);
            return String.valueOf(value);

        } else if (index==CPU_SYS_INDEX) {
            int value = getSys();
            recordMaxMin(CPU_SYS_INDEX, value);
            recordMean(CPU_SYS_INDEX, value);
            return String.valueOf(value);

        } else if (index==CPU_IDLE_INDEX) {
            int value = getIdle();
            recordMaxMin(CPU_IDLE_INDEX, value);
            recordMean(CPU_IDLE_INDEX, value);
            return String.valueOf(value);

        } else if (index==CPU_IOWAIT_INDEX) {
            int value = getIOW();
            recordMaxMin(CPU_IOWAIT_INDEX, value);
            recordMean(CPU_IOWAIT_INDEX, value);
            return String.valueOf(value);

        } else if (index==CPU_IRQ_INDEX) {
            int value = getIRQ();
            recordMaxMin(CPU_IRQ_INDEX, value);
            recordMean(CPU_IRQ_INDEX, value);
            return String.valueOf(value);

        } else if (index==CPU_SOFTIQR_INDEX) {
            int value = getSIRQ();
            recordMaxMin(CPU_SOFTIQR_INDEX, value);
            recordMean(CPU_SOFTIQR_INDEX, value);
            return String.valueOf(value);

        } else if (index==CPU_INTR_INDEX) {
            int value = getIntr();
            recordMaxMin(CPU_INTR_INDEX, value);
            recordMean(CPU_INTR_INDEX, value);
            return String.valueOf(value);

        } else if (index==CPU_CTXT_INDEX) {
            int value = getCtxt();
            recordMaxMin(CPU_CTXT_INDEX, value);
            recordMean(CPU_CTXT_INDEX, value);
            return String.valueOf(value);

        } else if (index==CPU_PROC_INDEX ) {
            int value = getProcesses();
            recordMaxMin(CPU_PROC_INDEX , value);
            recordMean(CPU_PROC_INDEX , value);
            return String.valueOf(value);

        } else if (index==CPU_PROC_R_INDEX) {
            int value = getProcsRunning();
            recordMaxMin(CPU_PROC_R_INDEX, value);
            recordMean(CPU_PROC_R_INDEX, value);
            return String.valueOf(value);

        } else if (index==CPU_PROC_B_INDEX) {
            int value = getProcsBlocked();
            recordMaxMin(CPU_PROC_B_INDEX, value);
            recordMean(CPU_PROC_B_INDEX, value);
            return String.valueOf(value);

        }
        return "--";
    }
    public String getMena() {
        float input = (mCurrCpu - init_cpu)*100 /
                ((mCurrCpu + mCurrIdle) - (init_cpu + inti_idle)) ;
        if(Loading.AMOUNT_MEAN)
        Log.v(Loading.TAG,
                    "input:"+input+", ("+mCurrCpu+"-"+init_cpu+")*100 / " +
                            "(("+mCurrCpu+"-"+mCurrIdle+")-("+init_cpu+" + "+inti_idle+"))");
        return String.valueOf(Math.round(input));
    }
}

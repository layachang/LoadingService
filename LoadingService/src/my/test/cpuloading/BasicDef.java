package my.test.cpuloading;

public class BasicDef {
    public static final int CPU_ALL_INDEX          = 1;
    public static final int CPU_USER_INDEX         = 2;
    public static final int CPU_NICE_INDEX         = 3;
    public static final int CPU_SYS_INDEX          = 4;
    public static final int CPU_IDLE_INDEX         = 5;
    public static final int CPU_IOWAIT_INDEX       = 6;
    public static final int CPU_IRQ_INDEX          = 7;
    public static final int CPU_SOFTIQR_INDEX      = 8;
    public static final int CPU_INTR_INDEX         = 9;
    public static final int CPU_CTXT_INDEX         = 10;
    public static final int CPU_PROC_INDEX         = 11;
    public static final int CPU_PROC_R_INDEX       = 12;
    public static final int CPU_PROC_B_INDEX       = 13;

    public static final int CPU_PID_INDEX          = 14;

    public static final int MEM_USED_INDEX         = 15;
    public static final int MEM_FREE_INDEX         = 16;
    public static final int MEM_BUFF_INDEX         = 17;
    public static final int MEM_CACHED_INDEX       = 18;
    public static final int MEM_ACTIVE_INDEX       = 19;
    public static final int MEM_INACTIVE_INDEX     = 20;

    public static final int MEM_PID_INDEX          = 21;

    public static final int DISK_READ_INDEX        = 22;
    public static final int DISK_WRITE_INDEX       = 23;
    public static final int DISK_ALL_INDEX         = 24;

    public static final int NET_REC_INDEX          = 25;
    public static final int NET_TRA_INDEX          = 26;
    public static final int NET_ALL_INDEX          = 27;

    public static final int NET_UID_INDEX          = 28;

    public static final int AUDIO_ALL_INDEX        = 29;

    public static final int BATT_ALL_INDEX         = 30;
    
    public static String[] mClassName = 
    	{"",
        "CPU","CPU","CPU","CPU","CPU", "CPU","CPU","CPU","CPU","CPU", "CPU","CPU","CPU",
        "CPU",
        "MEM","MEM","MEM","MEM","MEM","MEM",
        "MEM",
        "DISK","DISK","DISK",
        "NET","NET","NET",
        "NET",
        "AUDIO",
        "BATT"};
    public static String[] mResourceName = 
    	{"",
        "ALL","USER","NICE","SYS","IDLE", "IOWAIT","IRQ","SOFTIQR","INTR","CTXT", "PROC","PROC R","PROC B",
        "PID",
        "USED","FREE","BUFF","CACHED","ACTIVE","INACTIVE",
        "PID",
        "READ","WRITE","ALL",
        "REC","TRA","ALL",
        "UID",
        "AUDIO",
        "BATT"};
    public static String[] Unit = 
    	{"",
        "%","ms","ms","ms","ms", "ms","ms","ms","ms","ms", "ms","ms","ms",
        "%",
        "%","%","%","%","%","%",
        "%",
        "KB","KB","KB",
        "KB","KB","KB",
        "KB",
        "%",
        "mW"};
}

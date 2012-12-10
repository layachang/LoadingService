package my.test.cpuloading;


import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.RadioGroup;
import android.widget.Toast;

public class Loading extends Activity {
    /** Called when the activity is first created. */
    public final static String TAG = "LoadingService";
    public final static boolean DEBUG = true;

    public final static boolean W2SD_DEBUG        = DEBUG && true;

    public final static boolean CPU_DEBUG         = DEBUG && false; //ProcState
    public final static boolean CPU_PID_DEBUG     = DEBUG && false; //ProcState
    public final static boolean MEM_DEBUG         = DEBUG && false; //MemInfo
    public final static boolean MEM_PID_DEBUG     = DEBUG && false; //MemInfo
    public final static boolean DISK_DEBUG        = DEBUG && false; //DiskState
    public final static boolean NET_DEBUG         = DEBUG && false; //ProcNetDev
    public final static boolean NET_UID_DEBUG     = DEBUG && false; //ProcNetDev
    public final static boolean BATT_DEBUG        = DEBUG && false; //BattInfoProc
    public final static boolean BLUE_DEBUG        = DEBUG && false; //Bluetooth
    public final static boolean AUDIO_DEBUG       = DEBUG && false; //Audio
    public final static boolean MAX_MIN           = DEBUG && false; //MAX MIN
    public final static boolean AMOUNT_MEAN       = DEBUG && false; //Amount
    public final static boolean MEDIAN            = DEBUG && false; //Median
    public final static boolean SD                = DEBUG && false; //Standard Deviation
    public final static boolean VARIANCE          = DEBUG && false; //VARIANCE

    /**Battery**/
    public final static String STR_BATT_BATT_CURRENT_NOW = "/sys/class/power_supply/battery/batt_current_now"; //500
    public final static String STR_BATT_BATT_VOL = "/sys/class/power_supply/battery/batt_vol"; //4163 = 4163 mV
    public final static String STR_BATT_CAPACITY = "/sys/class/power_supply/battery/capacity"; //99 = 99%
    public final static String STR_BATT_VOLTAGE_NOW = "/sys/class/power_supply/battery/voltage_now";  //4166 = 4166 mV
    public final static String STR_BATT_TEMP = "/sys/class/power_supply/battery/temp"; //312 = 31.2C
    public final static String STR_BATT_CURRENT_NOW = "/sys/class/power_supply/battery/current_now"; // 0 = 0 mA (Microamps)
    /**Network**/
    public final static String STR_NET_DEV = "/proc/net/dev";
    /**DISK (Flash)**/
    public final static String STR_DISK_DISKSTATE = "/proc/diskstats";
    /**Memory**/
    public final static String STR_MEM_MEMINFO = "/proc/meminfo";
    /**CPU**/
    public final static String STR_CPU_STAT = "/proc/stat";
    public final static String STR_CPU_CPUINFO = "/proc/cpuinfo";

    private final static int DEVICE_NUM_SAMSUNG_S2 = 1;
    private final static int DEVICE_NUM_GALAXY_NOTE_GTP6800 = 2;
    private final static int DEVICE_NUM_GALAXY_NOTE_GTP6810 = 3;
    private final static int DEVICE_NUM_GALAXY_TAB_7_7 = 4;
    private final static int DEVICE_NUM_PICASA_MF = 5;
    private String[] APP = {"",
    						"LoadingService",
    						"Vimeo",
    						"Youtube",
    						"DailyMotion",
    						"MySpace", 
    						"BreakApp"};
    private String[] PAKNAME = {"my.test.cpuloading",
					             "com.vimeo.android.videoapp",
					             "com.google.android.youtube",
					             "com.dailymotion.dailymotion",
					             "com.myspace.android",
					             "com.breakapp.dreakdotcom"};
    private String KEY_CACHE = "";
    private String KEY_SYSTEM = "";
    private String KEY_DATA = "";
    private int READ_LINE = 0;
    private int NUM_CPU = 0;
    private int KEY_FILESYSTEM = 4;
    private Button startButton;
    private Button stopButton;
    private RadioButton device1;
    private RadioButton device2;
    private RadioButton device3;
    private RadioButton device4;
    private RadioButton device5;
    private Spinner mPSSpinner;
    long total = 0;
    long idle = 0;
    int mPID;
    int mUID;
    private String mFileName;
    String mProcessName;
    EditText mFilenameFiled;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView();
        setSharedPreferences();
    }

    private void setContentView() {
        setContentView(R.layout.main);
        startButton = (Button) findViewById(R.id.startButton);
        stopButton = (Button) findViewById(R.id.stopButton);
        startButton.setOnClickListener(startClickListener);
        stopButton.setOnClickListener(stopClickListener);
        device1 = (RadioButton) findViewById(R.id.device1);
        device2 = (RadioButton) findViewById(R.id.device2);
        device3 = (RadioButton) findViewById(R.id.device3);
        device4 = (RadioButton) findViewById(R.id.device4);
        device5 = (RadioButton) findViewById(R.id.device5);
        mFilenameFiled = (EditText)findViewById(R.id.filename);
        mPSSpinner = (Spinner)findViewById(R.id.ps_spinnner);
    }

    private void setSharedPreferences() {
        SharedPreferences pref = getSharedPreferences("LOADING_DEVICE_NUMBER", 0);
        setCheckboutton(pref);
        RadioGroup rdg = (RadioGroup)findViewById(R.id.rdg1);
        rdg.setOnCheckedChangeListener(
            new RadioGroup.OnCheckedChangeListener() {
                public void onCheckedChanged(RadioGroup group, final int checkedId) {
                    switch (checkedId) {
                        case R.id.device1:
                            memSetting(DEVICE_NUM_SAMSUNG_S2);
                            break;
                        case R.id.device2:
                            memSetting(DEVICE_NUM_GALAXY_NOTE_GTP6800);
                            break;
                        case R.id.device3:
                            memSetting(DEVICE_NUM_GALAXY_NOTE_GTP6810);
                            break;
                        case R.id.device4:
                            memSetting(DEVICE_NUM_GALAXY_TAB_7_7);
                            break;
                        case R.id.device5:
                            memSetting(DEVICE_NUM_PICASA_MF);
                            break;
                    }
                }
                private void memSetting(int device_num) {
                    SharedPreferences pref = getSharedPreferences("LOADING_DEVICE_NUMBER", device_num);
                    SharedPreferences.Editor PE = pref.edit();
                    PE.putInt("LOADING_DEVICE_NUMBER", device_num);
                    PE.commit();
                    loadSetting(device_num);
                }
            });
    }

    @Override
    public void onResume(){
        super.onResume();
        ArrayAdapter<String> adapter = new ArrayAdapter<String>( this,android.R.layout.simple_spinner_item,APP);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mPSSpinner.setAdapter(adapter);
        mPSSpinner.setOnItemSelectedListener(new Spinner.OnItemSelectedListener(){
            public void onItemSelected(AdapterView adapterView, View view, int position, long id){
                if (position!=0) {
                    Toast.makeText(Loading.this, "You selected: "+adapterView.getSelectedItem().toString()+" ("+position+")", Toast.LENGTH_LONG).show();
                    String[] select= adapterView.getSelectedItem().toString().split(";");
                    mProcessName = PAKNAME[position-1];
                    mFilenameFiled.setText(adapterView.getSelectedItem().toString()+"_");
                }
            }
            public void onNothingSelected(AdapterView arg0) {
                Toast.makeText(Loading.this, "You did not select anything.", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void setCheckboutton(SharedPreferences pref) {
        int def_device = pref.getInt("LOADING_DEVICE_NUMBER", 0);
        Log.v(TAG, "setCheckboutton, def_device="+def_device);
        loadSetting(def_device);
        switch(def_device) {
        case DEVICE_NUM_SAMSUNG_S2:
            device1.setChecked(true);
            break;
        case DEVICE_NUM_GALAXY_NOTE_GTP6800:
            device2.setChecked(true);
            break;
        case DEVICE_NUM_GALAXY_NOTE_GTP6810:
            device3.setChecked(true);
            break;
        case DEVICE_NUM_GALAXY_TAB_7_7:
            device4.setChecked(true);
            break;
        case DEVICE_NUM_PICASA_MF:
            device5.setChecked(true);
            break;
            default: break;
        }
    }

    private void loadSetting(int device_num) {
        switch (device_num) {
        case DEVICE_NUM_SAMSUNG_S2:
            /*--/proc/net/dev--*/
            setREAD_LINE(9);
            /*--/proc/diskstats--*/
            setKEY_CACHE("mmcblk0p7");
            setKEY_SYSTEM("mmcblk0p9");
            setKEY_DATA("mmcblk0p10");
            /*--/proc/stat--*/
            setNUM_CPU(1);
            break;
        case DEVICE_NUM_GALAXY_NOTE_GTP6800:
            setREAD_LINE(7);
            setKEY_CACHE("mmcblk0p7");
            setKEY_SYSTEM("mmcblk0p9");
            setKEY_DATA("mmcblk0p10");
            setNUM_CPU(2);
            setKEY_FILESYSTEM(4);
            break;
        case DEVICE_NUM_GALAXY_NOTE_GTP6810:
            setREAD_LINE(6);
            setKEY_CACHE("mmcblk0p7");
            setKEY_SYSTEM("mmcblk0p8");
            setKEY_DATA("mmcblk0p9");
            setNUM_CPU(2);
            break;
        case DEVICE_NUM_GALAXY_TAB_7_7:
            setREAD_LINE(9);
            setKEY_CACHE("mmcblk0p7");
            setKEY_SYSTEM("mmcblk0p9");
            setKEY_DATA("mmcblk0p10");
            setNUM_CPU(2);
        case DEVICE_NUM_PICASA_MF:
            setREAD_LINE(9);
            setKEY_CACHE("mmcblk0p7");
            setKEY_SYSTEM("mmcblk0p9");
            setKEY_DATA("mmcblk0p10");
            setNUM_CPU(2);
            break;
        }
    }

    private Button.OnClickListener startClickListener = new Button.OnClickListener() {
        public void onClick(View arg0) {
            mFileName = mFilenameFiled.getText().toString();
            stopButton.setEnabled(true);
            Intent intent = new Intent(Loading.this, LoadingService.class);
            intent.putExtra("KEY_CACHE", getKEY_CACHE());
            intent.putExtra("KEY_SYSTEM", getKEY_SYSTEM());
            intent.putExtra("KEY_DATA", getKEY_DATA());
            intent.putExtra("READ_LINE", getREAD_LINE());
            intent.putExtra("NUM_CPU", getNUM_CPU());
            intent.putExtra("KEY_FILESYSTEM", getKEY_FILESYSTEM());
            intent.putExtra("FILE_NAME", mFileName);
            intent.putExtra("PROCESS_NAME", mProcessName);
            device1.setEnabled(false);
            device2.setEnabled(false);
            Log.v(Loading.TAG,"!!!!!!!!!! startService !!!!!!!!!!");
            startService(intent);
        }

    };
    
    private Button.OnClickListener stopClickListener = new Button.OnClickListener() {
        public void onClick(View arg0) {
            Log.v(Loading.TAG,"!!!!!!!!!! soptService !!!!!!!!!!");
            soptService();
        }
    };

    public void onUserInteraction() {
        super.onUserInteraction();
    }

    protected void soptService() {
        Intent intent = new Intent(Loading.this, LoadingService.class);
        stopService(intent);
        device1.setEnabled(true);
        device2.setEnabled(true);
        device3.setEnabled(true);
        device4.setEnabled(true);
        device5.setEnabled(true);
    }

    public int getNUM_CPU() { return NUM_CPU; }
    public int getREAD_LINE() { return READ_LINE; }
    public int getKEY_FILESYSTEM() { return KEY_FILESYSTEM; }
    public String getKEY_DATA() { return KEY_DATA; }
    public String getKEY_CACHE() { return KEY_CACHE; }
    public String getKEY_SYSTEM() { return KEY_SYSTEM; }
    public void setNUM_CPU(int num_cpu) { NUM_CPU = num_cpu; }
    public void setREAD_LINE(int rEAD_LINE) { READ_LINE = rEAD_LINE; }
    public void setKEY_FILESYSTEM(int kEY_FILESYSTEM) { KEY_FILESYSTEM = kEY_FILESYSTEM; }
    public void setKEY_DATA(String kEY_DATA) { KEY_DATA = kEY_DATA; }
    public void setKEY_CACHE(String kEY_CACHE) { KEY_CACHE = kEY_CACHE; }
    public void setKEY_SYSTEM(String kEY_SYSTEM) { KEY_SYSTEM = kEY_SYSTEM; }
}
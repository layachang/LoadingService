package my.test.cpuloading;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

import android.util.Log;

public class BattInfoProc extends BasicFunc {
    private int mBattCurrentNow = 0;
    private int mBattVol = 0;
    private int mCapacity = 0;
    private int mVoltageNow = 0;
    private int mTemp = 0;
    private int mCurrentNow = 0;
    private int mW = 0;

    public final static String STR_BATT_BATT_CURRENT_NOW = "/sys/class/power_supply/battery/batt_current_now"; //500
    public final static String STR_BATT_BATT_VOL = "/sys/class/power_supply/battery/batt_vol"; //4163 = 4163 mV
    public final static String STR_BATT_CAPACITY = "/sys/class/power_supply/battery/capacity"; //99 = 99%
    public final static String STR_BATT_VOLTAGE_NOW = "/sys/class/power_supply/battery/voltage_now";  //4166 = 4166 mV
    public final static String STR_BATT_TEMP = "/sys/class/power_supply/battery/temp"; //312 = 31.2C
    public final static String STR_BATT_CURRENT_NOW = "/sys/class/power_supply/battery/current_now"; // 0 = 0 mA (Microamps)

    protected void dumpValues() {
        if(Loading.BATT_DEBUG)
            Log.v(Loading.TAG, "[--BattInfoProc--]");
        try {
            RandomAccessFile reader = new RandomAccessFile(Loading.STR_BATT_BATT_VOL, "r");
            String load = reader.readLine();
            mBattVol = Integer.parseInt(load);
            if(Loading.BATT_DEBUG)
                Log.v(Loading.TAG,"batt_vol:"+load+"("+mBattVol+")");
            reader.close();
        } catch (FileNotFoundException fx) {
            ;
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        try {
            RandomAccessFile reader = new RandomAccessFile(Loading.STR_BATT_CAPACITY, "r");
            String load = reader.readLine();
            mCapacity = Integer.parseInt(load);
            if(Loading.BATT_DEBUG)
                Log.v(Loading.TAG,"capacity:"+load+"("+mCapacity+")");
            reader.close();
        } catch (FileNotFoundException fx) {
            ;
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        try {
            RandomAccessFile reader = new RandomAccessFile(Loading.STR_BATT_VOLTAGE_NOW, "r");
            String load = reader.readLine();
            mVoltageNow = Integer.parseInt(load);
            if(Loading.BATT_DEBUG)
                Log.v(Loading.TAG,"voltage_now:"+load+"("+mVoltageNow+")");
            reader.close();
        } catch (FileNotFoundException fx) {
            ;
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        try {
            RandomAccessFile reader = new RandomAccessFile(Loading.STR_BATT_TEMP, "r");
            String load = reader.readLine();
            mTemp = Integer.parseInt(load);
            if(Loading.BATT_DEBUG)
                Log.v(Loading.TAG,"temp:"+load+"("+mTemp+")");
            reader.close();
        } catch (FileNotFoundException fx) {
            ;
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        try {
            RandomAccessFile reader = new RandomAccessFile(Loading.STR_BATT_CURRENT_NOW, "r");
            String load = reader.readLine();
            mCurrentNow = Integer.parseInt(load);
            if(Loading.BATT_DEBUG) Log.v(Loading.TAG,"current_now:"+load+"("+mCurrentNow+")");
            reader.close();
        } catch (FileNotFoundException fx) {
            ;
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        mW = mCurrentNow * mVoltageNow *1000;
    }

    private int getWatt() {
        return mW;
    }
    private int getCurrentNow() {
        return mCurrentNow;
    }
    private int getTemp() {
        return mTemp;
    }
    private int getVoltageNow() {
        return mVoltageNow;
    }
    private int getCapacity() {
        return mCapacity;
    }
    private int getBattVol() {
        return mBattVol;
    }
    private int getBattCurrentNow() {
        return mBattCurrentNow;
    }

    protected String getValues(int index){
        if(Loading.BATT_DEBUG)
            Log.v(Loading.TAG,BasicFunc.mClassName[index]+"/"+BasicFunc.mResourceName[index]+" getValues("+index+")");
        if (index==BATT_ALL_INDEX) {
            final int watt = getWatt();
            recordMaxMin(BATT_ALL_INDEX, watt);
            recordMean(BATT_ALL_INDEX, watt);
            return String.valueOf( watt);
        }
        return "--";
    }

}

package my.test.cpuloading;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import android.util.Log;

public abstract class BasicFunc {
	public static final int CPU_ALL_INDEX = 1;
	public static final int CPU_PID_INDEX = 2;
	public static final int MEM_ALL_INDEX = 3;
	public static final int MEM_PID_INDEX = 4;
	public static final int DISK_ALL_INDEX = 5;
	public static final int DISK_READ_INDEX = 6;
	public static final int DISK_WRITE_INDEX = 7;
	public static final int NET_ALL_INDEX = 8;
	public static final int NET_UID_INDEX = 9;
	public static final int BATT_ALL_INDEX = 10;
	public static final int AUDIO_ALL_INDEX = 11;
	
	private static final int mPrecision = 2;
	private LoadingService mContext;
	protected int mUID;
	protected String mPID;
	protected String mProcessName;
	private HashMap<Integer, Integer> mAmountInt = new HashMap<Integer, Integer>();
	private HashMap<Integer, Integer> mCount = new HashMap<Integer, Integer>();
	private HashMap<Integer, Integer> mMax = new HashMap<Integer, Integer>();
	private HashMap<Integer, Integer> mMin = new HashMap<Integer, Integer>();
	private HashMap<Integer, Integer> mMean = new HashMap<Integer, Integer>();
	private HashMap<Integer, Integer> mMedian = new HashMap<Integer, Integer>();
	private HashMap<Integer, ArrayList<Integer>> mValues = new HashMap<Integer, ArrayList<Integer>>();
	
	protected abstract void dumpValues();

	protected void insertValues(int index, ArrayList<String> output) {
		output.add( index, getValues(index));
	}

	protected void insertMean(int index, ArrayList<String> output) {
		output.add( index, getMean(index));
	}

	protected void insertMax(int index, ArrayList<String> output) {
		output.add( index, getMax(index));
	}

	protected void insertMin(int index, ArrayList<String> output) {
		output.add( index, getMin(index));
	}

	protected void insertAmount(int index, ArrayList<String> output) {
		output.add( index, getMin(index));
	}

	protected ArrayList<String> dataPasing(String load) {
		ArrayList<String> result = new ArrayList<String>();
		String[] toks = load.split(" ");
		int idx=0;
        for (int i=0;i<toks.length;i++)  {
        	String s = toks[i];
        	if(s.length()>0) {
        		//Log.v(Loading.TAG,"data: result["+idx+"]="+s);
        		result.add(idx, s);
        		idx++;
        	}
        }
		return result;
	}
	
	protected void recordMaxMin(int index, int value) {
		if (mMax.get(index)==null && mMin.get(index)==null) {
			if(Loading.DEBUG && Loading.AMOUNT_MEAN)
				Log.v(Loading.TAG,"index["+index+"], MAX("+value+") ,MIN("+value+")");
			mMax.put(index, value);
			mMin.put(index, value);
		}
		if (value > mMax.get(index)) {
			if(Loading.DEBUG && Loading.AMOUNT_MEAN)
				Log.v(Loading.TAG,"index["+index+"], MAX("+value+")");
			mMax.put(index,value);
		}
		if (value < mMin.get(index)) {
			if(Loading.DEBUG && Loading.AMOUNT_MEAN)
				Log.v(Loading.TAG,"index["+index+"], MIN("+value+")");
			mMin.put(index,value);
		}
	}

	protected void recordMean(int index, int value) {
		if (mCount.get(index)!=null && mCount.get(index)!=0) {
			if(Loading.DEBUG && Loading.AMOUNT_MEAN) {
				Log.v(Loading.TAG,"mAmountInt.put("+index+", "+mAmountInt.get(index)+"+"+value);
			}
			mAmountInt.put(index, mAmountInt.get(index)+value);
            mCount.put(index, mCount.get(index)+1);
            
            mValues.get(index).add(value);
		} else {
			if(Loading.DEBUG && Loading.AMOUNT_MEAN) {
				Log.v(Loading.TAG,"mAmountInt.put("+index+", "+value);
			}
	        mAmountInt.put(index, value);
	        mCount.put(index, 1);

	        ArrayList<Integer> values = new ArrayList<Integer>();
	        values.add(value);
	        mValues.put(index, values);
		}
	}

	protected String getValues(int index) {
		return String.valueOf(mAmountInt.get(index));
	}

	protected String getMax(int index) {
		return String.valueOf(mMax.get(index));
	}

	protected String getMin(int index) {
		return String.valueOf(mMin.get(index));
	}

	protected String getAmount(int index) {
		return String.valueOf(mAmountInt.get(index));
	}

	protected String getMean(int index) {
		int mean = Math.round(mAmountInt.get(index)/mCount.get(index));
		mMean.put(index, mean);
		if(Loading.DEBUG && Loading.AMOUNT_MEAN)
			Log.v(Loading.TAG,"index["+index+"], "+(Math.round(mAmountInt.get(index)))+"/"+mCount.get(index)+") = "+mean);
		return String.valueOf(mean);
	}
	protected String getMedian(int index) {
		float value;
		ArrayList<Integer> values = mValues.get(index);
		Collections.sort(values);
		int num = mCount.get(index);
		if (num%2==0) {
			value = (values.get(num/2)+values.get((num/2)+1))/2;
		} else {
			value = values.get((num/2)+1);
		}
		if(Loading.DEBUG && Loading.MEDIAN)
			Log.v(Loading.TAG,"index["+index+"], Median="+value);
		return String.valueOf(value);
	}

}
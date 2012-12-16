package my.test.cpuloading;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import android.util.Log;

public abstract class BasicFunc extends BasicDef {

    private static final int mPrecision = 2;
    private LoadingService mContext;
    protected int mUID=0;
    protected String mPID=null;
    protected boolean hasValue = false;
    protected String mProcessName;
    private HashMap<Integer, Integer> mAmountInt = new HashMap<Integer, Integer>();
    private HashMap<Integer, Double> mAmountFloat = new HashMap<Integer, Double>();
    private HashMap<Integer, Integer> mCount = new HashMap<Integer, Integer>();
    private HashMap<Integer, Integer> mMax = new HashMap<Integer, Integer>();
    private HashMap<Integer, Float> mMaxFloat = new HashMap<Integer, Float>();
    private HashMap<Integer, Integer> mMin = new HashMap<Integer, Integer>();
    private HashMap<Integer, Float> mMinFloat = new HashMap<Integer, Float>();
    private HashMap<Integer, Float> mMedian = new HashMap<Integer, Float>();
    private HashMap<Integer, Float> mMean = new HashMap<Integer, Float>();
    private HashMap<Integer, Float> mVar = new HashMap<Integer, Float>();
    private HashMap<Integer, ArrayList<Integer>> mValues = new HashMap<Integer, ArrayList<Integer>>();
    private HashMap<Integer, ArrayList<Float>> mValuesFloat = new HashMap<Integer, ArrayList<Float>>();
 
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
            if(Loading.AMOUNT_MEAN)
                Log.v(Loading.TAG,"index["+index+"], MAX("+value+") ,MIN("+value+")");
            mMax.put(index, value);
            mMin.put(index, value);
        }
        if (value > mMax.get(index)) {
            if(Loading.AMOUNT_MEAN)
                Log.v(Loading.TAG,"index["+index+"], MAX("+value+")");
            mMax.put(index,value);
        }
        if (value < mMin.get(index)) {
            if(Loading.AMOUNT_MEAN)
                Log.v(Loading.TAG,"index["+index+"], MIN("+value+")");
            mMin.put(index,value);
        }
    }

    protected void recordMaxMin(int index, float value) {
        if (mMaxFloat.get(index)==null && mMinFloat.get(index)==null) {
            if(Loading.AMOUNT_MEAN)
                Log.v(Loading.TAG,"index["+index+"], MAX("+value+") ,MIN("+value+")");
            mMaxFloat.put(index, value);
            mMinFloat.put(index, value);
        }
        if (value > mMaxFloat.get(index)) {
            if(Loading.AMOUNT_MEAN)
                Log.v(Loading.TAG,"index["+index+"], MAX("+value+")");
            mMaxFloat.put(index,value);
        }
        if (value < mMinFloat.get(index)) {
            if(Loading.AMOUNT_MEAN)
                Log.v(Loading.TAG,"index["+index+"], MIN("+value+")");
            mMinFloat.put(index,value);
        }
    }
    protected void recordMean(int index, int value) {
        if (mCount.get(index)!=null
                && mCount.get(index)!=0) {
            if(Loading.AMOUNT_MEAN) {
                Log.v(Loading.TAG,"mAmountInt.put("+index+", "+mAmountInt.get(index)+"+"+value);
            }
            mAmountInt.put(index, mAmountInt.get(index)+value);
            mCount.put(index, mCount.get(index)+1);
            mValues.get(index).add(value);
        } else {
            if(Loading.AMOUNT_MEAN) {
                Log.v(Loading.TAG,"mAmountInt.put("+index+", "+value);
            }
            mAmountInt.put(index, value);
            mCount.put(index, 1);
            ArrayList<Integer> values = new ArrayList<Integer>();
            values.add(value);
            mValues.put(index, values);
        }
    }
    protected void recordMean(int index, float value) {
        if (mCount.get(index)!=null
                && mCount.get(index)!=0) {
            if(Loading.AMOUNT_MEAN) {
                Log.v(Loading.TAG,"mAmountFloat.put("+index+", "+mAmountFloat.get(index)+"+"+value);
            }
            mAmountFloat.put(index, mAmountFloat.get(index)+value);
            mCount.put(index, mCount.get(index)+1);
            mValuesFloat.get(index).add(value);
        } else {
            if(Loading.AMOUNT_MEAN) {
                Log.v(Loading.TAG,"mAmountFloat.put("+index+", "+value);
            }
            mAmountFloat.put(index, (double) value);
            mCount.put(index, 1);
            ArrayList<Float> values = new ArrayList<Float>();
            values.add(value);
            mValuesFloat.put(index, values);
        }
    }
    protected String getValues(int index) {
        return "--";
    }

    protected String getMax(int index) {
        if (LoadingService.mFloatList.contains(index))
            return String.valueOf(mMaxFloat.get(index));
        else
            return String.valueOf(mMax.get(index));
    }

    protected String getMin(int index) {
    	if (LoadingService.mFloatList.contains(index))
            return String.valueOf(mMinFloat.get(index));
        else
            return String.valueOf(mMin.get(index));
    }

    protected String getAmount(int index) {
    	if (LoadingService.mFloatList.contains(index))
            return String.valueOf(mAmountFloat.get(index));
        else
            return String.valueOf(mAmountInt.get(index));
    }

    protected String getMean(int index) {
        if (mCount!=null &&
                mCount.get(index) !=null &&
                mCount.get(index)>0) {
        	if (LoadingService.mFloatList.contains(index)) {
                float mean = round(mAmountFloat.get(index)/(float)mCount.get(index),2);
                mMean.put(index, mean);
                if(Loading.AMOUNT_MEAN)
                    Log.v(Loading.TAG,"mAmountFloat["+index+"], "+
                            mAmountFloat.get(index)+"/"+(float)mCount.get(index)+") = "+mean);
                return String.valueOf(mean);
            } else {
                float mean = round((float)mAmountInt.get(index)/(float)mCount.get(index),2);
                mMean.put(index, mean);
                if(Loading.AMOUNT_MEAN)
                    Log.v(Loading.TAG,"mAmountInt["+index+"], "+
                            (float)mAmountInt.get(index)+"/"+(float)mCount.get(index)+") = "+mean);
                return String.valueOf(mean);
            }
        } else {
            return "";
        }
    }

    protected float round(float value,int dig) {
        NumberFormat nf = NumberFormat.getInstance();
        nf.setMaximumFractionDigits(dig);
        String result = nf.format(value).replace(",", "");
        return Float.valueOf(result);
    }

    private float round(double value,int dig) {
        NumberFormat nf = NumberFormat.getInstance();
        nf.setMaximumFractionDigits(dig);
        String result = nf.format(value).replace(",", "");
        return Float.valueOf(result);
    }

    protected String getMedian(int index) {
        if (mCount!=null &&
                mCount.get(index) !=null &&
                mCount.get(index)>0) {
            float value;
            if (LoadingService.mFloatList.contains(index)) {
                ArrayList<Float> values = mValuesFloat.get(index);
                Collections.sort(values);
                int num = mCount.get(index);
                String degStr = "mValuesFloat, num="+num+"; ";
                if(Loading.MEDIAN) {
                    for (int i=0; i<values.size();i++) {
                        degStr+= "["+(i+1)+"]:"+values.get(i)+",";
                    }
                    Log.v(Loading.TAG,"index["+index+"], degStr="+degStr);
                }
                if (num%2==0) {
                    int vidx = (num/2)-1;
                    value = (values.get(vidx)+values.get(vidx+1))/2;
                    if(Loading.MEDIAN)
                        Log.v(Loading.TAG,"mValuesFloat["+index+"], ("+values.get(vidx)+"+"+values.get(vidx+1)+")/2="+value);
                } else {
                    int vidx = (num/2)-1;
                    value = values.get(vidx+1);
                    if(Loading.MEDIAN)
                        Log.v(Loading.TAG,"mValuesFloat["+index+"], m="+(vidx+1)+"; value="+value);
                }
                if(Loading.MEDIAN)
                    Log.v(Loading.TAG,"mValuesFloat["+index+"], Median="+value);
                return String.valueOf(value);
            } else {
                ArrayList<Integer> values = mValues.get(index);
                Collections.sort(values);
                int num = mCount.get(index);
                String degStr = "mValues, num="+num+"; ";
                if(Loading.MEDIAN) {
                    for (int i=0; i<values.size();i++) {
                        degStr+= "["+(i+1)+"]:"+values.get(i)+",";
                    }
                    Log.v(Loading.TAG,"mValues["+index+"], degStr="+degStr);
                }
                if (num%2==0) {
                    int vidx = (num/2)-1;
                    value = ((float)values.get(vidx)+(float)values.get(vidx+1))/2;
                    if(Loading.MEDIAN)
                        Log.v(Loading.TAG,"mValues["+index+"], ("+values.get(vidx)+"+"+values.get(vidx+1)+")/2="+value);
                } else {
                    int vidx = (num/2)-1;
                    value = values.get(vidx+1);
                    if(Loading.MEDIAN)
                        Log.v(Loading.TAG,"mValues["+index+"], m="+(vidx+1)+"; value="+value);
                }
                if(Loading.MEDIAN)
                    Log.v(Loading.TAG,"mValues["+index+"], Median="+value);
                return String.valueOf(value);
            }
        } else {
            return "";
        }
    }

    protected String getVariance(int index) {
        float mean = mMean.get(index);
        double amount=0;
        if (mCount!=null &&
                mCount.get(index)!=null &&
                mCount.get(index)>0) {
        	if (LoadingService.mFloatList.contains(index)) {
                ArrayList<Float> values = mValuesFloat.get(index);
                for (int i=0; i<values.size();i++) {
                    float value=values.get(i);
                    amount+=(value-mean)*(value-mean);
                    if(Loading.VARIANCE)
                        Log.v(Loading.TAG,"mValuesFloat["+index+"], ("+value+"-"+mean+")^2="+(value-mean)*(value-mean));
                }
                float variance = round(amount/mCount.get(index),2);
                mVar.put(index, variance);
                if(Loading.VARIANCE)
                    Log.v(Loading.TAG,"index["+index+"], Variance: "+amount+"/"+mCount.get(index)+" ="+variance);
                return String.valueOf(variance);
            } else {
                ArrayList<Integer> values = mValues.get(index);
                for (int i=0; i<values.size();i++) {
                    int value=values.get(i);
                    amount+=(value-mean)*(value-mean);
                    if(Loading.VARIANCE)
                        Log.v(Loading.TAG,"mValues["+index+"], ("+value+"-"+mean+")^2="+(value-mean)*(value-mean));
                }
                float variance = round(amount/mCount.get(index),2);
                mVar.put(index, variance);
                if(Loading.VARIANCE)
                    Log.v(Loading.TAG,"index["+index+"], Variance: "+amount+"/"+mCount.get(index)+" ="+variance);
                return String.valueOf(variance);
            }
        } else {
            return "";
        }
    }
    protected String getSD(int index) {
        if (mCount!=null && mCount.get(index)>0) {
            float sd = round((float)Math.sqrt(mVar.get(index)),2);
            if(Loading.SD)
                Log.v(Loading.TAG,"index["+index+"], Standard Deviation: sqrt("+mVar.get(index)+") ="+sd);
            return String.valueOf(sd);
        } else {
            return "";
        }
    }

    protected void clearAllRecord(int index) {
        if (mCount!=null) {
            synchronized (this) {
                mCount.put(index, 0);
                mAmountInt.put(index, 0);
                mMax.put(index, 0);
                mMin.put(index, 0);
            }
        }
    }

}
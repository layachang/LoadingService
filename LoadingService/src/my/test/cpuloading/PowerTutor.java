package my.test.cpuloading;

import java.util.List;
import java.util.Vector;

import my.test.cpuloading.power.*;
import my.test.cpuloading.power.SystemInfo;

import android.content.Context;
import android.os.Build;
import android.util.Log;


public class PowerTutor {
	private Context mContext;
	private WriteFile2SD wfile;
	private static int mUID;
	private String mProcessName;
	private static long mRx;
	private static long mTx;
	private long mCurrentTime=0;
	private int mLogTime=0;
	private static final double POLY_WEIGHT = 0.02;
	private Object iterationLock = new Object();
	private long lastWrittenIteration;

	private Vector<PowerComponent> powerComponents;
	private Vector<PowerFunction> powerFunctions;
	private Vector<HistoryBuffer> histories;

	public static final int ALL_COMPONENTS = -1;
	
	public PowerTutor(LoadingService context, WriteFile2SD file,
			String procName, int uid) {
		powerComponents = new Vector<PowerComponent>();
		powerFunctions = new Vector<PowerFunction>();
		mContext = context;
		wfile = file; 
		mProcessName = procName;
		mUID = uid;
		Log.v(Loading.TAG, "PowerTutor/ mProcessName="+mProcessName+"; mUID="+mUID);
		generateComponents(context, powerComponents, powerFunctions);
		histories = new Vector<HistoryBuffer>();
	    for(int i = 0; i < powerComponents.size(); i++) {
	        histories.add(new HistoryBuffer(300));
	      }
	}

	private void generateComponents(Context context,
						            List<PowerComponent> components,
						            List<PowerFunction> functions) {
		/*
	    final PhoneConstants constants = getConstants(context);
	    final PhonePowerCalculator calculator = getCalculator(context);

	    //TODO: What about bluetooth?
	    //TODO: LED light on the Nexus

	    // Add display component. 
	    if(hasOled()) {
	      components.add(new OLED(context, constants));
	      functions.add(new PowerFunction() {
	        public double calculate(PowerData data) {
	          return calculator.getOledPower((OledData)data);
	        }});
	    } else {
	      components.add(new LCD(context));
	      functions.add(new PowerFunction() {
	        public double calculate(PowerData data) {
	          return calculator.getLcdPower((LcdData)data);
	        }});
	    }

	    // Add CPU component. 
	    components.add(new CPU(constants));
	    functions.add(new PowerFunction() {
	      public double calculate(PowerData data) {
	        return calculator.getCpuPower((CpuData)data);
	      }});

	    // Add Wifi component. 
	    String wifiInterface = 
	        SystemInfo.getInstance().getProperty("wifi.interface");
	    if(wifiInterface != null && wifiInterface.length() != 0) {
	      components.add(new Wifi(context, constants));
	      functions.add(new PowerFunction() {
	        public double calculate(PowerData data) {
	          return calculator.getWifiPower((WifiData)data);
	        }});
	    }

	    // Add 3G component. 
	    if(constants.threegInterface().length() != 0) {
	      components.add(new Threeg(context, constants));
	      functions.add(new PowerFunction() {
	        public double calculate(PowerData data) {
	          return calculator.getThreeGPower((ThreegData)data);
	        }});
	    }

	    // Add GPS component. 
	    components.add(new GPS(context, constants));
	    functions.add(new PowerFunction() {
	      public double calculate(PowerData data) {
	        return calculator.getGpsPower((GpsData)data);
	      }});

	    // Add Audio component. 
	    components.add(new Audio(context));
	    functions.add(new PowerFunction() {
	      public double calculate(PowerData data) {
	        return calculator.getAudioPower((AudioData)data);
	      }});

	    // Add Sensors component if avaialble. 
	    if(NotificationService.available()) {
	      components.add(new Sensors(context));
	      functions.add(new PowerFunction() {
	        public double calculate(PowerData data) {
	          return calculator.getSensorPower((SensorData)data);
	        }});
	    }
	    */
	  }

	  public static PhoneConstants getConstants(Context context) {
		  return null;
/*
		    switch(getPhoneType()) {
		      case PHONE_DREAM:
		        return new DreamConstants(context);
		      case PHONE_SAPPHIRE:
		        return new SapphireConstants(context);
		      case PHONE_PASSION:
		        return new PassionConstants(context);
		      default:
		        boolean oled = hasOled();
		        Log.w(TAG, "Phone type not recognized (" + Build.DEVICE + "), using " +
		              (oled ? "Passion" : "Dream") + " constants");
		        return oled ? new PassionConstants(context) :
		                      new DreamConstants(context);
		    }
		    */
		  }

	public double getCurrent() {
		double pow = calcPower();
		return 1000 * pow;
	}

	private double calcPower() {
		int count = 0;
		int[] history = getComponentHistory(5 * 60, -1, mUID, -1);
        double weightedAvgPower = 0;
        for(int i = history.length - 1; i >= 0; i--) {
          if(history[i] != 0) {
            count++;
            weightedAvgPower *= 1.0 - POLY_WEIGHT;
            weightedAvgPower += POLY_WEIGHT * history[i] / 1000.0;
          }
        }
        if(count == 0) return -1.0;
        return weightedAvgPower / (1.0 - Math.pow(1.0 - POLY_WEIGHT, count));
	}

    private int[] getComponentHistory(int count, int componentId, int uid, long iteration) {
	    if(iteration == -1) synchronized(iterationLock) {
		    iteration = lastWrittenIteration;
		}
        //int components = powerComponents.size();
        /*
        if(componentId == ALL_COMPONENTS) {
          int[] result = new int[count];
          for(int i = 0; i < components; i++) {
            int[] comp = histories.get(i).get(uid, iteration, count);
            for(int j = 0; j < count; j++) {
              result[j] += comp[j];
            }
          }
          return result;
        }
        if(componentId < 0 || components <= componentId) return null;
        */
      if(componentId < 0) return null;
      return histories.get(componentId).get(uid, iteration, count);
     
	}
}

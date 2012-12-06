package my.test.cpuloading;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import android.util.Log;

public class SysCpuLoad extends BasicFunc {

	public SysCpuLoad() {


	}

	protected void dumpValues() {
		getCpuUsage();
	}

	private int[] getCpuUsage() {

	    String tempString = executeTop();
	    if(Loading.DEBUG && Loading.CPU_DEBUG)
	    	Log.v(Loading.TAG, 	"executeTop="+tempString);
	    
	    //tempString = tempString.trim();
	    String[] myString = tempString.split(" = ");
	    if(Loading.DEBUG && Loading.CPU_DEBUG)
	    	Log.v(Loading.TAG, 	"total="+myString[1] );
	    
	    String[] myString2 = myString[0].split(" + ");
	    for (int i =0; i< myString2.length; i++) {
	    	
	    }
	    int[] cpuUsageAsInt = new int[myString.length];
	    for (int i = 0; i < myString.length; i++) {
	        myString[i] = myString[i].trim();
		    if(Loading.DEBUG && Loading.CPU_DEBUG)
		    	Log.v(Loading.TAG, 	"cpuUsageAsInt["+i+"] ="+cpuUsageAsInt[i] );
	        cpuUsageAsInt[i] = Integer.parseInt(myString[i]);
	    }
	    return cpuUsageAsInt;
	}
	
	private int[] getCpuUsageStatistic() {

	    String tempString = executeTop();
	    if(Loading.DEBUG && Loading.CPU_DEBUG)
	    	Log.v(Loading.TAG, 	"executeTop="+tempString);
	    
	    tempString = tempString.replaceAll(",", "");
	    tempString = tempString.replaceAll("User", "");
	    tempString = tempString.replaceAll("System", "");
	    tempString = tempString.replaceAll("IOW", "");
	    tempString = tempString.replaceAll("IRQ", "");
	    tempString = tempString.replaceAll("%", "");
	    for (int i = 0; i < 10; i++) {
	        tempString = tempString.replaceAll("  ", " ");
	    }
	    tempString = tempString.trim();
	    String[] myString = tempString.split(" ");
	    int[] cpuUsageAsInt = new int[myString.length];
	    for (int i = 0; i < myString.length; i++) {
	        myString[i] = myString[i].trim();
		    if(Loading.DEBUG && Loading.CPU_DEBUG)
		    	Log.v(Loading.TAG, 	"cpuUsageAsInt["+i+"] ="+cpuUsageAsInt[i] );
	        cpuUsageAsInt[i] = Integer.parseInt(myString[i]);
	    }
	    return cpuUsageAsInt;
	}
	
	private String executeTop() {
	    java.lang.Process p = null;
	    BufferedReader in = null;
	    String returnString = null;
	    try {
	        p = Runtime.getRuntime().exec("top -n 1");
	        in = new BufferedReader(new InputStreamReader(p.getInputStream()));
	        while (returnString == null || returnString.contentEquals("")) {
	            returnString = in.readLine();
	            returnString = in.readLine();
	    	    if(Loading.DEBUG && Loading.CPU_DEBUG)
	    	    	Log.v(Loading.TAG, "in="+returnString);
	        }
	    } catch (IOException e) {
	        Log.e(Loading.TAG, "error in getting first line of top");
	        e.printStackTrace();
	    } finally {
	        try {
	            in.close();
	            p.destroy();
	        } catch (IOException e) {
	            Log.e(Loading.TAG,
	                    "error in closing and destroying top process");
	            e.printStackTrace();
	        }
	    }
	    return returnString;
	}
}

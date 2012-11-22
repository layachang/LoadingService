package my.test.cpuloading;

import android.content.Context;
import android.util.Log;

public class ScreenTouch {
	private Context mContext;
	private WriteFile2SD wfile;
	private String mPID;
	private String mProcessName;
	private int mStatus = 0;
	private long mCurrentTime=0;
	private int mLogTime=0;
	
	public ScreenTouch(LoadingService context, WriteFile2SD file) {
		mContext = context;
		wfile = file; 
		//mProcessName = procName;
		Log.v(Loading.TAG, "ScreenTouch");
	}

	//public void onUserInteraction() {
    //    super.onUserInteraction();
	//}
}

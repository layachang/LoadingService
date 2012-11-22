package my.test.cpuloading;

import android.content.Context;
import android.media.MediaPlayer;
import android.media.audiofx.Equalizer;
import android.media.audiofx.Visualizer;
import android.media.audiofx.Visualizer.OnDataCaptureListener;
import android.util.Log;

public class VisualizerLinster {
	private Context mContext;
	private WriteFile2SD wfile;
	private String mPID;
	private String mProcessName;
	private int mStatus = 0;
	private long mCurrentTime=0;
	private int mLogTime=0;
	private MediaPlayer mMediaPlayer;  
	private Visualizer mVisualizer;  
	private Equalizer mEqualizer;
	private byte[] mBytes;
	private int mAmount=0;
	private int mCount=0;
	private int mMax=127;
	private int mCurrent=0;
	public VisualizerLinster(LoadingService context, WriteFile2SD file) {
		mContext = context;
		wfile = file; 
		//mProcessName = procName;
		Log.v(Loading.TAG, "VisualizerLinster");
		mVisualizer = new Visualizer(0);
		mVisualizer.setCaptureSize(Visualizer.getCaptureSizeRange()[1]);
		Log.v(Loading.TAG, "VisualizerLinster, "+Visualizer.getCaptureSizeRange()[1]);
		int result = mVisualizer.setDataCaptureListener( new OnDataCaptureListener(){

			public void onFftDataCapture(Visualizer arg0, byte[] bytes, int arg2) {
				//if(Loading.DEBUG && Loading.AUDIO_DEBUG) Log.v(Loading.TAG, "FftData: "+bytes.length);
				updateFftVisualizer(bytes);
				
			}

			public void onWaveFormDataCapture(Visualizer arg0, byte[] bytes,
					int arg2) {
				//if(Loading.DEBUG && Loading.AUDIO_DEBUG) Log.v(Loading.TAG, "WaveFormData: "+bytes.length);
				//upWavedateVisualizer(bytes);
				
			}
			
		}, Visualizer.getMaxCaptureRate(), true, true);
		mVisualizer.setEnabled(true);
		if(Loading.DEBUG && Loading.AUDIO_DEBUG) Log.v(Loading.TAG, "result: "+result);

	}

	protected void updateFftVisualizer(byte[] fft) {
		byte[] model = new byte[fft.length / 2 + 1];
		model[0] = (byte) Math.abs(fft[0]);
		if (mCurrent < model[0])
			mCurrent = model[0];
		mAmount +=model[0];
		mCount++;
	}
	/*
	protected void upWavedateVisualizer(byte[] fft) {
		byte[] model = new byte[fft.length / 2 + 1];
		model[0] = (byte) Math.abs(fft[0]);
		Log.v(Loading.TAG,"wave="+model[0]);
		
	}
	*/
	public void destory() {
		mVisualizer.setEnabled(false);  
	}
	
	public void printAll() {
		int cur_log_time=((LoadingService) mContext).getTime(mCurrentTime);

		while (cur_log_time-mLogTime>1) {
			mLogTime++;
			printVolumn(false);
		}
		mLogTime = cur_log_time;
		printVolumn(true);
	}

	private void printVolumn(boolean withValue) {
		float result = (mCurrent)*100/mMax;
		mCurrent = 0; //mAmount=mCount=0;
		String input = "6,Audio," + mLogTime + "," + Float.toString(result);
		if (withValue) {
			input = "6,Audio," + mLogTime + "," + String.valueOf(result);
		}
		if(Loading.DEBUG && Loading.AUDIO_DEBUG) Log.v(Loading.TAG, "input: "+input);
        wfile.write(input);
		
	}

	public void getVolumn(long ctime) {
		mCurrentTime = ctime;

	}

	public void printMean() {
		if(Loading.DEBUG && Loading.AUDIO_DEBUG) 
			Log.v(Loading.TAG, "mAmount= "+mAmount+"; mCount="+mCount);
		String input = "6,Audio, mean," + ((float)mAmount/(float)mCount);
		wfile.write(input);
		
	}
}

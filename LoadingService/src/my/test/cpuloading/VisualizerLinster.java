package my.test.cpuloading;

import android.media.MediaPlayer;
import android.media.audiofx.Equalizer;
import android.media.audiofx.Visualizer;
import android.media.audiofx.Visualizer.OnDataCaptureListener;
import android.util.Log;

public class VisualizerLinster extends BasicFunc {
	private Visualizer mVisualizer;
	private int mScaled=127;
	private int mCurrent=0;
	public VisualizerLinster() {
		//mProcessName = procName;
		Log.v(Loading.TAG, "VisualizerLinster");
		mVisualizer = new Visualizer(0);
		int result = mVisualizer.setDataCaptureListener( new OnDataCaptureListener(){
		public void onFftDataCapture(Visualizer arg0, byte[] bytes, int arg2) {
			updateFftVisualizer(bytes);
		}
		public void onWaveFormDataCapture(Visualizer arg0, byte[] bytes,
				int arg2) {
			//upWavedateVisualizer(bytes);
		}
	}, Visualizer.getMaxCaptureRate(), true, true);
		mVisualizer.setEnabled(true);
		if(Loading.DEBUG && Loading.AUDIO_DEBUG) Log.v(Loading.TAG, "result: "+result);
	}

	protected void updateFftVisualizer(byte[] fft) {
		byte[] model = new byte[fft.length / 2 + 1];
		model[0] = (byte) Math.abs(fft[0]);
		final int volumn = model[0];
		if (mCurrent < volumn)
			mCurrent = volumn;
		recordMaxMin(AUDIO_ALL_INDEX, volumn);
		recordMean(AUDIO_ALL_INDEX, volumn);
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

	private int getVolumn() {
		return Math.round((mCurrent)*100/mScaled);
	}

	@Override
	protected String getValues(int index) {
		if (index==AUDIO_ALL_INDEX) {
			return String.valueOf(getVolumn());
		}
		return null;
	}

	@Override
	protected void dumpValues() {
		// TODO Auto-generated method stub
		
	}
}

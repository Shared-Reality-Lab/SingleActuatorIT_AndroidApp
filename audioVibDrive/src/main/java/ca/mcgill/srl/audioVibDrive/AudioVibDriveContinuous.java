package ca.mcgill.srl.audioVibDrive;

import android.media.AudioTrack;
import android.util.Log;

public class AudioVibDriveContinuous extends AudioVibDrive implements Runnable {
	private boolean mIsRun = true;


	private final int mTimeFrame;
	private final int mFrameSize;


	protected int ampweak;
	protected int ampstrong;
	protected int eqweak[];
	protected int eqstrong[];


	private long mSampleTime = 0;

	private OnNextDriveListener mOnNextDriveListener = null;

	private short[] audioData;

	public AudioVibDriveContinuous(int timeFrame) {
		mTimeFrame = timeFrame;
		mFrameSize = timeFrame * VibAudioTrack.SAMPLE_RATE / 1000;
		mAudioTrack = new VibAudioTrack(AudioTrack.MODE_STREAM, mFrameSize);
		audioData = new short[mFrameSize];
		initBuf(mFrameSize);
	}

	public void vibVolumeChange(int volweak, int volstrong, int[] eqw, int[] eqs)	{
		ampweak = volweak;
		ampstrong = volstrong;
		eqweak = eqw;
		eqstrong = eqs;
	}


	//Custom function for generate Vibrations for Experiments.
	private void generateVibSignal(int freq, int amp) {
		double[] fb = {60, 160, 320};
		double[] fc = {80, 160, 360};

		//equalizing perceived intensity
		double[] weight_amp = {0.3, 0.3, 0.5, 0};
		double cAmp = convertAmp((amp/100.0) * weight_amp[freq]);


		final double cFreq1 = fb[freq];
		final double cFreq2 = fc[freq];
		//Log.e("VibGeneration", "Size="+mFrameSize + " Amp=" + cAmp + " Freq=" + cFreq1 + cFreq2);
		for (int i = 0; i < 0.6*mFrameSize; i++)
			mVibBuffer[i] = (short) (cAmp *
					(Math.sin(2.0 * Math.PI * (cFreq1 * (double) i / (double) VibAudioTrack.SAMPLE_RATE)) +
							Math.sin(2.0 * Math.PI * (cFreq2 * (double) i / (double) VibAudioTrack.SAMPLE_RATE))));

	}
	private void generateVibSignal(int number, int freq, int amp) {
		//(SAMPLE_RATE = 12000)
		int noteperbar = 8;
		int notesize = (int) (mFrameSize / noteperbar * 0.6);
		//Log.e ("framesize", Integer.toString(mFrameSize));
		//Log.e ("notesize", Integer.toString(notesize));
		double[] fb = {60, 160, 320, 0};
		double[] fc = {80, 160, 360, 0};

		//equalizing perceived intensity
		double[] weight_amp = {0.3, 0.3, 0.5, 0};
		double tamp;
		if(freq == 3) tamp = 0;
		else if (amp == 1)	{
			tamp = (double)(ampstrong/100.0) * (double)(eqstrong[freq]/50.0) * weight_amp[freq];
		}
		else	{
			tamp = (double)(ampweak/100.0) * (double)(eqweak[freq]/50.0)* weight_amp[freq];
		}
		double cAmp = tamp * weight_amp[freq];

		final double cFreq1 = fb[freq];
		final double cFreq2 = fc[freq];

		//making masker (numbers)
		short[] masker = new short[mFrameSize];
		//Log.e ("cAmp", Double.toString(cAmp) + " " + cFreq1);
		//Log.e ("number", Integer.toString(number));
		for (int i = 0; i < number; i++)	{
			for(int j = 0; j < notesize; j++)
			masker[i*(mFrameSize/noteperbar)+j] = 32767;
		}
		//vib coding
		for (int i = 0; i < mFrameSize; i++) {
			mVibBuffer[i] = (short) (masker[i] * (cAmp * (Math.sin(2.0 * Math.PI * (cFreq1 * (double) i / (double) VibAudioTrack.SAMPLE_RATE)) +
					Math.sin(2.0 * Math.PI * (cFreq2 * (double) i / (double) VibAudioTrack.SAMPLE_RATE)))));
		}
		//Log.e("mVib", Integer.toString(mVibBuffer[101]));
	}

	public void stop() {

		mIsRun = false;
	}
	//filling audio data into a single channel, length of "mFramesize"
	public void audioFill(double volume)	{
		for(int i = 0; i < mFrameSize; i++)
			mAudioBuffer[i] = (short) ((volume * audioData[i]));
	}
	public void run() {
		mAudioTrack.play();

		final long startTime = System.currentTimeMillis();
		long curTime;
		
		while (mIsRun) {
			VibInfo vibInfo = mOnNextDriveListener.onNextVibration();
			//Log.e("AudioVib", "Triggered: Mode="+vibInfo.mode);

			if (vibInfo != null) {
				audioFill(vibInfo.audiovolume/100.0);
				generateVibSignal(vibInfo.nID, vibInfo.fID, vibInfo.amp);
				mAudioTrack.write(mAudioBuffer, mVibBuffer, mFrameSize);
				//Log.e("vibInfo", "not null");
			}
			else {
				mAudioTrack.write(null, null, mFrameSize);
				Log.e("vibInfo", "null");

			}

			//Log.i("audioVibDrive", "curTime " + (System.currentTimeMillis() - startTime) + " sampleTime " + mSampleTime);    // !!!

			do {
				curTime = System.currentTimeMillis() - startTime;
			} while (mSampleTime - curTime > 5);
			
			mSampleTime += mTimeFrame;
		}
		
		mAudioTrack.flush();
		mAudioTrack.stop();
		
		mAudioTrack.release();
	}

	public void setOnNextDriveListener(OnNextDriveListener l) {
		mOnNextDriveListener = l;
	}
	public void setAudioData(short[] data)	{
		audioData = data;
	}
	public interface OnNextDriveListener {
		VibInfo onNextVibration();
	}
	
	public static class VibInfo {
		public int mode;
		public int audiovolume;
		public int fID, nID, amp;

		public VibInfo(int number, int freq, int amp, int av)	{
			this.fID = freq;
			this.nID = number;
			this.amp = amp;
			this.audiovolume = av;
		}
	}
}

package ca.mcgill.srl.audioVibDrive;

import android.media.AudioTrack;
import android.util.Log;

public class AudioVibDriveContinuous extends AudioVibDrive implements Runnable {
	private boolean mIsRun = true;


	private final int mTimeFrame;
	private final int mFrameSize;
	
	private float mVibPrevFreq = 0f;
	private double mVibPrevOffset = 0;

	private float mVibPrevFreq1 = 0f;
	private double mVibPrevOffset1 = 0;
	private float mVibPrevFreq2 = 0f;
	private double mVibPrevOffset2 = 0;


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
	public static float getFrac(float f) {
		return (f - (float)Math.floor(f));
	}
	public static double getFrac(double f) {
		return (f - Math.floor(f));
	}

	private void generateVibSignal(float freq, double amp) {
		final double offset = getFrac(mVibPrevFreq * (double)(mFrameSize) / (double) VibAudioTrack.SAMPLE_RATE + mVibPrevOffset);
		
		final double cAmp = convertAmp(getSafeAmp(amp));
		for (int i = 0; i < mFrameSize; i++)
			mVibBuffer[i] = (short)(cAmp * Math.sin(2.0 * Math.PI * (freq * (double)i / (double) VibAudioTrack.SAMPLE_RATE + offset)));
		
		mVibPrevFreq = freq;
		mVibPrevOffset = offset;
	}
	private void generateVibSignal(float freq1, double amp1, float freq2, double amp2) {
		final double offset1 = getFrac(mVibPrevFreq1 * (double)(mFrameSize) / (double) VibAudioTrack.SAMPLE_RATE + mVibPrevOffset1);
		final double offset2 = getFrac(mVibPrevFreq2 * (double)(mFrameSize) / (double) VibAudioTrack.SAMPLE_RATE + mVibPrevOffset2);
		double[] cAmp = new double[2];
		cAmp = getSafeAmp(amp1, amp2);
		final double cAmp1 = convertAmp(cAmp[1]);
		final double cAmp2 = convertAmp(cAmp[2]);
		for (int i = 0; i < mFrameSize; i++) {
			mVibBuffer[i] = (short) (cAmp1 * Math.sin(2.0 * Math.PI * (freq1 * (double) i / (double) VibAudioTrack.SAMPLE_RATE + offset1)) +
					cAmp2 * Math.sin(2.0 * Math.PI * (freq2 * (double) i / (double) VibAudioTrack.SAMPLE_RATE + offset2)));
		}
		mVibPrevFreq1 = freq1;
		mVibPrevOffset1 = offset1;
		mVibPrevFreq2 = freq2;
		mVibPrevOffset2 = offset2;
	}

	//Custom function for generate Vibrations for Experiments.
	private void generateVibSignal(int freq, int amp) {
		double[] fb = {50, 50, 110, 200, 320, 280};
		double[] fc = {70, 50, 110, 200, 320, 320};

		//equalizing perceived intensity
		double[] weight_amp = {0.11, 0.11, 0.12, 0.22, 0.37, 0.34};
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
		double[] p = {3.2, 2.4, 1.8, 1.2};
		//mFrameSize = (int)(mAudioTrack.getSampleRate() * p[period]);
		//mTimeFrame = (int)(p[period] * 1000);
		//initBuf(mFrameSize);
		//(SAMPLE_RATE = 12000)
		int noteperbar = 8;
		int notesize = (int) (mFrameSize / noteperbar * 0.6);
		double[] fb = {50, 50, 110, 200, 320, 280};
		double[] fc = {70, 50, 110, 200, 320, 320};

		//equalizing perceived intensity
		double[] weight_amp = {0.11, 0.11, 0.12, 0.22, 0.37, 0.34};
		double cAmp = convertAmp((amp/100.0) * weight_amp[freq]);

		final double cFreq1 = fb[freq];
		final double cFreq2 = fc[freq];

		//making masker (numbers)
		short[] masker = new short[mFrameSize];
		//Log.e ("framesize", Integer.toString(mFrameSize));
		for (int i = 0; i < number; i++)	{
			for(int j = 0; j < notesize; j++)
			masker[i*(mFrameSize/noteperbar)+j] = 1;
		}
		//vib coding
		for (int i = 0; i < mFrameSize; i++) {
			mVibBuffer[i] = (short) (masker[i] * cAmp *
					(Math.sin(2.0 * Math.PI * (cFreq1 * (double) i / (double) VibAudioTrack.SAMPLE_RATE)) +
							Math.sin(2.0 * Math.PI * (cFreq2 * (double) i / (double) VibAudioTrack.SAMPLE_RATE))));
		}
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
				if(vibInfo.mode == 0)	{
					generateVibSignal(vibInfo.fID, vibInfo.amp);
				}
				else if(vibInfo.mode == 1)	{
					generateVibSignal(vibInfo.nID, vibInfo.fID, vibInfo.amp);
				}
				else if(vibInfo.mode == 2)	{
					generateVibSignal(vibInfo.freq1, vibInfo.amp1);
				}
				else {
					generateVibSignal(vibInfo.freq1, vibInfo.amp1, vibInfo.freq2, vibInfo.amp2);
				}

				mAudioTrack.write(mAudioBuffer, mVibBuffer, mFrameSize);
			}
			else {
				mAudioTrack.write(null, null, mFrameSize);
				
				mVibPrevFreq = 0f;
				mVibPrevOffset = 0;
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
		public float freq1;
		public double amp1;
		public float freq2;
		public double amp2;
		public int audiovolume;
		public int fID, nID, amp;
		public VibInfo(int freq, int amp, int av)	{
			this.mode = 0;
			this.fID = freq;
			this.amp = amp;
			this.audiovolume = av;
		}
		public VibInfo(int number, int freq, int amp, int av)	{
			this.mode = 1;
			this.fID = freq;
			this.nID = number;
			this.amp = amp;
			this.audiovolume = av;
		}

		public VibInfo(float freq, double amp) {
			this.mode = 2;
			this.freq1 = freq;
			this.amp1 = amp;
		}
		public VibInfo(float freq, double amp, float freq2, double amp2) {
			this.mode = 3;
			this.freq1 = freq;
			this.amp1 = amp;
			this.freq2 = freq2;
			this.amp2 = amp2;
		}
	}
}

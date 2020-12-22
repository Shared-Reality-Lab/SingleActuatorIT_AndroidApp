package ca.mcgill.srl.audioVibDrive;

import android.media.AudioTrack;
import android.util.Log;

import java.nio.Buffer;

//Single vibration play
public class AudioVibDriveStatic extends AudioVibDrive {
	//public static final int COMMAND_DURATION_MAX = 5000;
	public int BUFFER_SIZE;
	protected int ampweak;
	protected int ampstrong;
	protected int eqweak[];
	protected int eqstrong[];

	
	public AudioVibDriveStatic(int length) {
		BUFFER_SIZE = length * VibAudioTrack.SAMPLE_RATE / 1000;
		initBuf(BUFFER_SIZE);
	}

	public void vibVolumeChange(int volweak, int volstrong, int[] eqw, int[] eqs)	{
		ampweak = volweak;
		ampstrong = volstrong;
		eqweak = eqw;
		eqstrong = eqs;
	}


	public void generateVibSignal(int number, int period, int freq, int amp) {
		double[] p = {3, 2, 1};
		//(SAMPLE_RATE = 12000)
		BUFFER_SIZE = (int)(p[period] * VibAudioTrack.SAMPLE_RATE);
		initBuf(BUFFER_SIZE);

		int noteperbar = 8;
		int notesize = (int) (BUFFER_SIZE / noteperbar * 0.6);
		double[] fb = {50, 100, 250, 0};
		double[] fc = {70, 100, 250, 0};

		//equalizing perceived intensity
		double[] weight_amp = {0.38, 0.32, 0.75, 0};
		double tamp;
		if (amp == 1)	{
			tamp = (double)(ampstrong/100.0) * (double)(eqstrong[freq]/100.0);
		}
		else	{
			tamp = (double)(ampweak/100.0) * (double)(eqweak[freq]/100.0);
		}
		double cAmp = tamp * weight_amp[freq];

		final double cFreq1 = fb[freq];
		final double cFreq2 = fc[freq];

		//making masker (numbers)
		short[] masker = new short[BUFFER_SIZE];
		Log.e ("framesize", Integer.toString(BUFFER_SIZE));
		for (int i = 0; i < number; i++)	{
			for(int j = 0; j < notesize; j++)
				masker[i*(BUFFER_SIZE/noteperbar)+j] = 32767;
		}
		//vib coding
		for (int i = 0; i < BUFFER_SIZE; i++) {
			mVibBuffer[i] = (short) (masker[i] * cAmp *
					(Math.sin(2.0 * Math.PI * (cFreq1 * (double) i / (double) VibAudioTrack.SAMPLE_RATE)) +
							Math.sin(2.0 * Math.PI * (cFreq2 * (double) i / (double) VibAudioTrack.SAMPLE_RATE)) ));
		}
	}
	
	public void vibrate() {
		if (mAudioTrack != null) {
			if (mAudioTrack.getPlayState() == AudioTrack.PLAYSTATE_PLAYING) {
				mAudioTrack.stop();
				mAudioTrack.flush();
			}
			
			mAudioTrack.release();
			mAudioTrack = null;
		}
		
		mAudioTrack = new VibAudioTrack(AudioTrack.MODE_STATIC, BUFFER_SIZE);
		
		mAudioTrack.write(null, mVibBuffer, BUFFER_SIZE);
		mAudioTrack.play();
	}

}

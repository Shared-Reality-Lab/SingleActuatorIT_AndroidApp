package ca.mcgill.srl.audioVibDrive;

import java.util.Arrays;

// Using stereo signal, drive audio and vibration actuator simultaneously
// Audio: left
// Vibration: right
abstract public class AudioVibDrive {
	protected VibAudioTrack mAudioTrack = null;

	protected short[] mAudioBuffer;
	protected short[] mVibBuffer;

	public static short convertAmp(double amp) {
		return (short)(amp * Short.MAX_VALUE);
	}

	public static int getSampleRate() {
		return VibAudioTrack.SAMPLE_RATE;
	}

	protected static double getSafeAmp(double amp) {
		if (amp < 0.0)
			return 0.0;
		else if (amp > 1.0)
			return 1.0;
		else
			return amp;
	}

	protected static double[] getSafeAmp(double amp1, double amp2) {
		double[] rAmp = new double[2];

		amp1 = getSafeAmp(amp1);
		amp2 = getSafeAmp(amp2);

		final double sumAmp = amp1 + amp2;
		if (sumAmp > 1.0) {
			final double ratio = 1.0 / sumAmp;
			rAmp[0] = amp1 * ratio;
			rAmp[1] = amp2 * ratio;
		}
		else {
			rAmp[0] = amp1;
			rAmp[1] = amp2;
		}

		return rAmp;
	}
	protected void initBuf(int bufferSize) {
		mAudioBuffer = new short[bufferSize];
		mVibBuffer = new short[bufferSize];
		Arrays.fill(mAudioBuffer, (short)0);
		Arrays.fill(mVibBuffer, (short)0);
	}
}

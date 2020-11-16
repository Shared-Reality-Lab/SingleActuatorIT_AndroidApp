package ca.mcgill.srl.audioVibDrive;

import java.util.Arrays;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;

class VibAudioTrack extends AudioTrack {
	private final short[] mBuffer;
	
	public static final int SAMPLE_RATE = 48000;
	
	public VibAudioTrack(int mode, int bufferSize) {
		super(AudioManager.STREAM_MUSIC, SAMPLE_RATE, AudioFormat.CHANNEL_OUT_STEREO, AudioFormat.ENCODING_PCM_16BIT,
				Math.max(AudioTrack.getMinBufferSize(SAMPLE_RATE, AudioFormat.CHANNEL_OUT_STEREO, AudioFormat.ENCODING_PCM_16BIT), bufferSize * 2), mode);
		
		mBuffer = new short[bufferSize * 2];
	}
	
	public int getFrameCount() {
		return getNativeFrameCount();
	}
	
	public void write(short[] left, short[] right, int length) {
		if (left == null && right == null)
			Arrays.fill(mBuffer, (short)0);
		else if (left == null) {
			for (int i = 0; i < length; i++) {
				final int t = i * 2;
				mBuffer[t] = 0;
				mBuffer[t+1] = right[i];
			}
		}
		else if (right == null) {
			for (int i = 0; i < length; i++) {
				final int t = i * 2;
				mBuffer[t] = left[i];
				mBuffer[t+1] = 0;
			}
		}
		else {
			for (int i = 0; i < length; i++) {
				final int t = i * 2;
				mBuffer[t] = left[i];
				mBuffer[t+1] = right[i];
			}
		}
		
		super.write(mBuffer, 0, length * 2);
	}
}
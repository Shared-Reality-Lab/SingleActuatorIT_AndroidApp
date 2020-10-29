package ca.mcgill.srl.audioVibDrive;

import android.media.AudioTrack;

//Single vibration play
public class AudioVibDriveStatic extends AudioVibDrive {
	public static final int COMMAND_DURATION_MAX = 5000;
	public static final int BUFFER_SIZE = COMMAND_DURATION_MAX * VibAudioTrack.SAMPLE_RATE / 1000;
	

	private static int durationToBufSize(int duration) {
		if (duration > COMMAND_DURATION_MAX)
			duration = COMMAND_DURATION_MAX;
		
		return (duration * VibAudioTrack.SAMPLE_RATE / 1000);
	}
	
	public AudioVibDriveStatic() {
		initBuf(BUFFER_SIZE);
	}
	
	
	public void vibrate(int freq, double amp, int duration) {
		//final int sampleSize = generateVibSignal(freq, amp, durationToBufSize(duration));
		
		//vibrate(sampleSize);
	}
	
	public void vibrate(int freq1, int freq2, double amp1, double amp2, int duration) {
		//final int sampleSize = generateVibSignal(freq1, freq2, amp1, amp2, durationToBufSize(duration));
		
		//vibrate(sampleSize);
	}
	
	private void vibrate(int sampleSize) {
		if (mAudioTrack != null) {
			if (mAudioTrack.getPlayState() == AudioTrack.PLAYSTATE_PLAYING) {
				mAudioTrack.stop();
				mAudioTrack.flush();
			}
			
			mAudioTrack.release();
			mAudioTrack = null;
		}
		
		mAudioTrack = new VibAudioTrack(AudioTrack.MODE_STATIC, BUFFER_SIZE);
		
		mAudioTrack.write(mAudioBuffer, mVibBuffer, sampleSize);
		mAudioTrack.play();
	}

}

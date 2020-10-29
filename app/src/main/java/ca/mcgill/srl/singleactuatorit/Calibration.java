package ca.mcgill.srl.singleactuatorit;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

import ca.mcgill.srl.audioVibDrive.AudioVibDriveContinuous;

public class Calibration extends AppCompatActivity {

    protected int[] ampweak = {33, 33, 33, 33, 33, 33};
    protected int[] ampstrong = {66, 66, 66, 66, 66, 66};
    protected int audiovolume = 50;
    protected int currentfr = 2;
    protected EditText userTxt;

    private Thread mVibThread = null;
    protected AudioVibDriveContinuous mVibDrive;
    //protected AudioVibDriveContinuous.OnNextDriveListener mNextVib;
    public static int TIME_FRAME = 800;
   // public static int SAMPLING_RATE = 48000;

    private void startThread()  {
        if (mVibThread == null) {
            mVibThread = new Thread(mVibDrive);
            mVibThread.start();
        }
        Log.i("Thread", "Started");
    }
    private void endThread()    {
        if (mVibThread != null) {
            mVibDrive.stop();
            try {
                mVibThread.join();
            } catch (Exception e) { }

            mVibThread = null;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calibration);

        Intent intent = getIntent();

        RadioGroup frgroup = findViewById(R.id.calib_frRadioGroup);
        final RadioGroup ampgroup = findViewById(R.id.calib_ampRadioGroup);

        final RadioButton frHighChordRadio = findViewById(R.id.calib_frHighChord);
        final RadioButton frVeryHighRadio = findViewById(R.id.calib_frVeryHigh);
        final RadioButton frHighRadio = findViewById(R.id.calib_frHigh);
        final RadioButton frMidRadio = findViewById(R.id.calib_frMid);
        final RadioButton frLowRadio = findViewById(R.id.calib_frLow);

        final RadioButton ampStrRadio = findViewById(R.id.calib_ampStrong);

        final SeekBar audioSlider = findViewById(R.id.calib_audioSeekbar);
        audioSlider.setProgress(audiovolume);
        userTxt = findViewById(R.id.userIdField);
        final SeekBar slider = findViewById(R.id.calibrationSlider);
        slider.setProgress(ampstrong[3]);
        Button confirmButton = findViewById(R.id.calib_confirmButton);

        if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        }

        short[] audiodata;
        final int[] userID = {intent.getExtras().getInt("id")};
        userTxt.setText(Integer.toString(userID[0]));
        ampweak = intent.getExtras().getIntArray("ampweak");
        ampstrong = intent.getExtras().getIntArray("ampstrong");
        audiodata = intent.getExtras().getShortArray("audiodata");

        mVibDrive = new AudioVibDriveContinuous(TIME_FRAME);
        mVibDrive.setAudioData(audiodata);
        mVibDrive.setOnNextDriveListener(new AudioVibDriveContinuous.OnNextDriveListener() {
            @Override
            public AudioVibDriveContinuous.VibInfo onNextVibration() {
                if (ampStrRadio.isChecked()) {
                    return new AudioVibDriveContinuous.VibInfo(currentfr, ampstrong[currentfr], audiovolume);
                } else {
                    return new AudioVibDriveContinuous.VibInfo(currentfr, ampweak[currentfr], audiovolume);
                }
            }
        });

        confirmButton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent resIntent = new Intent();
                //Toast.makeText(getApplicationContext(), userTxt.getText().toString(), Toast.LENGTH_SHORT).show();
                int uID = Integer.parseInt(userTxt.getText().toString());
                resIntent.putExtra("id", uID);
                resIntent.putExtra("ampweak", ampweak);
                resIntent.putExtra("ampstrong", ampstrong);
                resIntent.putExtra("audiovolume", audiovolume);

                setResult(RESULT_OK, resIntent);
                endThread();

                finish();
            }
        });

        frgroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (frHighChordRadio.isChecked()) {
                    currentfr = 5;
                } else if (frVeryHighRadio.isChecked()) {
                    currentfr = 4;
                } else if (frHighRadio.isChecked()) {
                    currentfr = 3;
                } else if (frMidRadio.isChecked()) {
                    currentfr = 2;
                } else if (frLowRadio.isChecked()) {
                    currentfr = 1;
                } else {
                    currentfr = 0;
                }
                if (ampStrRadio.isChecked()) {
                    slider.setProgress(ampstrong[currentfr]);
                } else {
                    slider.setProgress(ampweak[currentfr]);
                }

            }
        });
        ampgroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (frHighChordRadio.isChecked()) {
                    currentfr = 5;
                } else if (frVeryHighRadio.isChecked()) {
                    currentfr = 4;
                } else if (frHighRadio.isChecked()) {
                    currentfr = 3;
                } else if (frMidRadio.isChecked()) {
                    currentfr = 2;
                } else if (frLowRadio.isChecked()) {
                    currentfr = 1;
                } else {
                    currentfr = 0;
                }
                if (ampStrRadio.isChecked()) {
                    slider.setProgress(ampstrong[currentfr]);
                } else {
                    slider.setProgress(ampweak[currentfr]);
                }
            }
        });

        slider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser)
                    if (ampStrRadio.isChecked()) {
                        ampstrong[currentfr] = progress;
                    } else {
                        ampweak[currentfr] = progress;
                    }
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        audioSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    audiovolume = progress;
                }
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        startThread();

    }
}

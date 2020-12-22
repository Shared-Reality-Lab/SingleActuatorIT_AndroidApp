package ca.mcgill.srl.singleactuatorit;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.ToggleButton;

import ca.mcgill.srl.audioVibDrive.AudioVibDriveContinuous;

public class Calibration extends AppCompatActivity {

    int[] eqweak = {25, 30, 35};
    int[] eqstrong = {55, 60, 75};
    int ampweak = 40;
    int ampstrong = 70;
    int audiovolume = 50;
    short[] audiodata;
    protected int currentfr = 1;
    protected int currentnp = 4;
    protected int currentamp = 0;
    int tf = 2000;
    protected EditText userTxt;
    protected Logger mResultLogger;
    private Thread mVibThread = null;
    protected AudioVibDriveContinuous mVibDrive;
    public String rootPath;
    @Override
    public void onBackPressed() {
        //super.onBackPressed();
    }

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

        //save file location
        rootPath = Environment.getExternalStorageDirectory().getAbsolutePath();
        rootPath = rootPath + "/singleit/calib/";
        //Toast.makeText(getApplicationContext(), rootPath, Toast.LENGTH_SHORT).show();


        final ToggleButton Highbutton = findViewById(R.id.calib_hightoggle);
        final ToggleButton Midbutton = findViewById(R.id.calib_midtoggle);
        Midbutton.setChecked(true);
        final ToggleButton Lowbutton = findViewById(R.id.calib_lowtoggle);

        final SeekBar Highvol = findViewById(R.id.calib_highfreqvol);
        final SeekBar Midvol = findViewById(R.id.calib_midfreqvol);
        final SeekBar Lowvol = findViewById(R.id.calib_lowfreqvol);

        final RadioButton ampWeakRadio = findViewById(R.id.calib_radioweak);
        final RadioButton ampStrRadio = findViewById(R.id.calib_radiostrong);

        final RadioGroup pulsespeed = findViewById(R.id.pulseSpeedRadioGroup);

        final SeekBar Weakvol = findViewById(R.id.calib_weakvol);
        final SeekBar Strongvol = findViewById(R.id.calib_strongvol);

        final SeekBar audioSlider = findViewById(R.id.calib_audioSeekbar);
        audioSlider.setProgress(audiovolume);
        userTxt = findViewById(R.id.userIdField);
        Button confirmButton = findViewById(R.id.calib_confirmButton);

        final int[] userID = {intent.getExtras().getInt("id")};
        userTxt.setText(Integer.toString(userID[0]));
        ampweak = intent.getExtras().getInt("ampweak");
        ampstrong = intent.getExtras().getInt("ampstrong");
        eqweak = intent.getExtras().getIntArray("eqweak");
        eqstrong = intent.getExtras().getIntArray("eqstrong");
        audiodata = intent.getExtras().getShortArray("audiodata");
        audiovolume = intent.getExtras().getInt("audiovolume");


        //initial condition

        mVibDrive = new AudioVibDriveContinuous(tf);
        mVibDrive.setAudioData(audiodata);
        mVibDrive.vibVolumeChange(ampweak, ampstrong, eqweak, eqstrong);
        Log.e("volumes", ampweak + " " + ampstrong);
        mVibDrive.setOnNextDriveListener(new AudioVibDriveContinuous.OnNextDriveListener() {
            @Override
            public AudioVibDriveContinuous.VibInfo onNextVibration() {
                    return new AudioVibDriveContinuous.VibInfo(currentnp, currentfr, currentamp, audiovolume);
            }
        });
        startThread();
        pulsespeed.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch(checkedId)   {
                    case R.id.calib_radioFast:
                        tf = 1000;
                        endThread();
                        mVibDrive = new AudioVibDriveContinuous(tf);
                        mVibDrive.vibVolumeChange(ampweak, ampstrong, eqweak, eqstrong);
                        mVibDrive.setAudioData(audiodata);
                        mVibDrive.setOnNextDriveListener(new AudioVibDriveContinuous.OnNextDriveListener() {
                            @Override
                            public AudioVibDriveContinuous.VibInfo onNextVibration() {
                                return new AudioVibDriveContinuous.VibInfo(currentnp, currentfr, currentamp, audiovolume);
                            }
                        });
                        startThread();
                        break;
                    case R.id.calib_radioModerate:
                        tf = 2000;
                        endThread();
                        mVibDrive = new AudioVibDriveContinuous(tf);
                        mVibDrive.vibVolumeChange(ampweak, ampstrong, eqweak, eqstrong);
                        mVibDrive.setAudioData(audiodata);
                        mVibDrive.setOnNextDriveListener(new AudioVibDriveContinuous.OnNextDriveListener() {
                            @Override
                            public AudioVibDriveContinuous.VibInfo onNextVibration() {
                                return new AudioVibDriveContinuous.VibInfo(currentnp, currentfr, currentamp, audiovolume);
                            }
                        });
                        startThread();
                        break;
                    case R.id.calib_radioSlow:
                        tf = 3000;
                        endThread();
                        mVibDrive = new AudioVibDriveContinuous(tf);
                        mVibDrive.vibVolumeChange(ampweak, ampstrong, eqweak, eqstrong);
                        mVibDrive.setAudioData(audiodata);
                        mVibDrive.setOnNextDriveListener(new AudioVibDriveContinuous.OnNextDriveListener() {
                            @Override
                            public AudioVibDriveContinuous.VibInfo onNextVibration() {
                                return new AudioVibDriveContinuous.VibInfo(currentnp, currentfr, currentamp, audiovolume);
                            }
                        });
                        startThread();
                        break;
                    default:
                }
            }
        });

        confirmButton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                //file write
                int uID = Integer.parseInt(userTxt.getText().toString());
                if (uID != -1) {
                    rootPath = rootPath + "ID" + uID + ".txt";
                    //Toast.makeText(getApplicationContext(), rootPath, Toast.LENGTH_SHORT).show();
                    mResultLogger = new Logger(rootPath, getApplicationContext());
                    mResultLogger.WriteMessage(Integer.toString(uID), false);
                    mResultLogger.WriteMessage(Integer.toString(ampweak), false);
                    mResultLogger.WriteMessage(Integer.toString(ampstrong), false);
                    mResultLogger.WriteArray(eqweak, false, false);
                    mResultLogger.WriteArray(eqstrong, false, false);
                    mResultLogger.WriteMessage(Integer.toString(audiovolume), false);
                    mResultLogger.Close();


                    Intent resIntent = new Intent();
                    //Toast.makeText(getApplicationContext(), userTxt.getText().toString(), Toast.LENGTH_SHORT).show();

                    resIntent.putExtra("id", uID);
                    resIntent.putExtra("ampweak", ampweak);
                    resIntent.putExtra("ampstrong", ampstrong);
                    resIntent.putExtra("eqweak", eqweak);
                    resIntent.putExtra("eqstrong", eqstrong);
                    resIntent.putExtra("audiovolume", audiovolume);

                    setResult(RESULT_OK, resIntent);
                }

                endThread();
                finish();
            }
        });

        Highbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Highbutton.setChecked(true);
                Midbutton.setChecked(false);
                Lowbutton.setChecked(false);

                currentfr = 2;
                mVibDrive.setOnNextDriveListener(new AudioVibDriveContinuous.OnNextDriveListener() {
                    @Override
                    public AudioVibDriveContinuous.VibInfo onNextVibration() {
                        return new AudioVibDriveContinuous.VibInfo(currentnp, currentfr, currentamp, audiovolume);
                    }
                });
            }
        });
        Midbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Highbutton.setChecked(false);
                Midbutton.setChecked(true);
                Lowbutton.setChecked(false);
                currentfr = 1;
                mVibDrive.setOnNextDriveListener(new AudioVibDriveContinuous.OnNextDriveListener() {
                    @Override
                    public AudioVibDriveContinuous.VibInfo onNextVibration() {
                        return new AudioVibDriveContinuous.VibInfo(currentnp, currentfr, currentamp, audiovolume);
                    }
                });
            }
        });
        Lowbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Highbutton.setChecked(false);
                Midbutton.setChecked(false);
                Lowbutton.setChecked(true);
                currentfr = 0;
                mVibDrive.setOnNextDriveListener(new AudioVibDriveContinuous.OnNextDriveListener() {
                    @Override
                    public AudioVibDriveContinuous.VibInfo onNextVibration() {
                        return new AudioVibDriveContinuous.VibInfo(currentnp, currentfr, currentamp, audiovolume);
                    }
                });
            }
        });
        ampStrRadio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Highvol.setProgress(eqstrong[2]);
                Midvol.setProgress(eqstrong[1]);
                Lowvol.setProgress(eqstrong[0]);
                currentamp = 1;
                mVibDrive.setOnNextDriveListener(new AudioVibDriveContinuous.OnNextDriveListener() {
                    @Override
                    public AudioVibDriveContinuous.VibInfo onNextVibration() {
                        return new AudioVibDriveContinuous.VibInfo(currentnp, currentfr, currentamp, audiovolume);
                    }
                });
            }
        });
        ampWeakRadio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Highvol.setProgress(eqweak[2]);
                Midvol.setProgress(eqweak[1]);
                Lowvol.setProgress(eqweak[0]);
                currentamp = 0;
                mVibDrive.setOnNextDriveListener(new AudioVibDriveContinuous.OnNextDriveListener() {
                    @Override
                    public AudioVibDriveContinuous.VibInfo onNextVibration() {
                        return new AudioVibDriveContinuous.VibInfo(currentnp, currentfr, currentamp, audiovolume);
                    }
                });
            }
        });
        Highvol.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    if (ampStrRadio.isChecked()) {
                        eqstrong[2] = progress;
                    } else {
                        eqweak[2] = progress;
                    }
                    mVibDrive.vibVolumeChange(ampweak, ampstrong, eqweak, eqstrong);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        Midvol.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser){
                    if (ampStrRadio.isChecked()) {
                        eqstrong[1] = progress;
                    } else {
                        eqweak[1] = progress;
                    }
                    mVibDrive.vibVolumeChange(ampweak, ampstrong, eqweak, eqstrong);

                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        Lowvol.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser)   {
                    if (ampStrRadio.isChecked()) {
                        eqstrong[0] = progress;
                    } else {
                        eqweak[0] = progress;
                    }
                    mVibDrive.vibVolumeChange(ampweak, ampstrong, eqweak, eqstrong);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        Strongvol.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser)   {
                    ampstrong = progress;
                    mVibDrive.vibVolumeChange(ampweak, ampstrong, eqweak, eqstrong);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        Weakvol.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser)   {
                    ampweak = progress;
                    mVibDrive.vibVolumeChange(ampweak, ampstrong, eqweak, eqstrong);
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



    }
}


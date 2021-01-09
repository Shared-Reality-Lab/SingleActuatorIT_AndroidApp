package ca.mcgill.srl.singleactuatorit;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import ca.mcgill.srl.audioVibDrive.AudioVibDriveContinuous;

import static java.lang.Thread.sleep;

public class Familization extends AppCompatActivity {
    protected int ampweak;
    protected int ampstrong;
    protected int[] eqweak;
    protected int[] eqstrong;
    protected int audiovolume;
    protected int np = 3, pr = 1, fr = 1, amp = 1;
    protected int tf = 2000;
    protected short[] audiodata;
    private Thread mVibThread = null;
    protected AudioVibDriveContinuous mVibDrive;

    int userID;
    TextView userTxt;

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
    /*Handler handler = new Handler(new Handler.Callback(){
        @Override
        public boolean handleMessage(Message msg) {
            if(msg.what == 1) {
                final String str = "UID="+Integer.toString(userID);
                userTxt.setText(str);
            }
            return true;
        }
    });*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_familization);

        Intent intent = getIntent();

        final RadioGroup npgroup = findViewById(R.id.famil_npRadioGroup);
        final RadioGroup prgroup = findViewById(R.id.famil_prRadioGroup);
        final RadioGroup frgroup = findViewById(R.id.famil_frRadioGroup);
        final RadioGroup ampgroup = findViewById(R.id.famil_ampRadioGroup);


        final ToggleButton random_hide = findViewById(R.id.famil_random_hide);
        Button doneButton = findViewById(R.id.doneButton);

        final ConstraintLayout mLayout = findViewById(R.id.constraintLayout);

        ampweak = intent.getExtras().getInt("ampweak");
        ampstrong = intent.getExtras().getInt("ampstrong");
        eqweak = intent.getExtras().getIntArray("eqweak");
        eqstrong = intent.getExtras().getIntArray("eqstrong");
        audiodata = intent.getExtras().getShortArray("audiodata");
        audiovolume = intent.getExtras().getInt("audiovolume");


        //initial condition
        final int SAMPLING_RATE = 12000;
        final int LONGEST_TIME_FRAME = 4000;
        audiodata = new short[(SAMPLING_RATE * LONGEST_TIME_FRAME / 1000)];
        String audioFileName = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC).getPath() + "/pinknoise_12k.wav";
        File file = new File(audioFileName);
        //Log.e("audioFill", "=" + file.length());
        byte[] wavHeader = new byte[44];
        byte[] musicBytes = new byte[2];

        try {
            FileInputStream in = new FileInputStream(audioFileName);
            in.read(wavHeader);
            for(int i = 0; i < SAMPLING_RATE * LONGEST_TIME_FRAME / 1000 - 1; i++) {
                in.read(musicBytes);
                audiodata[i] = (short) ((musicBytes[0] & 0xFF) << 8 | (musicBytes[1] & 0xFF));
            }

            //Log.e("ra", "read");
            in.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            //Log.e ("file", e.toString());
        } catch (IOException e) {
            e.printStackTrace();
            //Log.e ("file", e.toString());
        }
        mVibDrive = new AudioVibDriveContinuous(tf);
        mVibDrive.setAudioData(audiodata);
        mVibDrive.vibVolumeChange(ampweak, ampstrong, eqweak, eqstrong);
        mVibDrive.setOnNextDriveListener(new AudioVibDriveContinuous.OnNextDriveListener() {
            @Override
            public AudioVibDriveContinuous.VibInfo onNextVibration() {
                return new AudioVibDriveContinuous.VibInfo(np, fr, amp, audiovolume);
            }
        });

        random_hide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (random_hide.isChecked())    {
                    int t = (int)(Math.random() * 100) % 5;
                    switch (t) {
                        case 4:
                            np = 7;
                            npgroup.check(R.id.famil_np7);
                            break;
                        case 3:
                            np = 5;
                            npgroup.check(R.id.famil_np5);
                            break;
                        case 2:
                            np = 3;
                            npgroup.check(R.id.famil_np3);
                            break;
                        case 1:
                            np = 2;
                            npgroup.check(R.id.famil_np2);
                            break;
                        case 0:
                            np = 1;
                            npgroup.check(R.id.famil_np1);
                            break;
                    }
                    pr = (int)(Math.random() * 99) % 3;
                    switch (pr) {
                        case 2:
                            prgroup.check(R.id.famil_prFast);
                            break;
                        case 1:
                            prgroup.check(R.id.famil_prModerate);
                            break;
                        case 0:
                            prgroup.check(R.id.famil_prSlow);
                            break;
                    }
                    fr = (int)(Math.random() * 99) % 3;
                    switch (fr) {
                        case 2:
                            frgroup.check(R.id.famil_frHigh);
                            break;
                        case 1:
                            frgroup.check(R.id.famil_frMid);
                            break;
                        case 0:
                            frgroup.check(R.id.famil_frLow);
                            break;
                    }
                    amp = (int)(Math.random() * 10) % 2;
                    switch (amp) {
                        case 1:
                            ampgroup.check(R.id.famil_ampStrong);
                            break;
                        case 0:
                            ampgroup.check(R.id.famil_ampWeak);
                            break;
                    }
                    npgroup.setVisibility(View.INVISIBLE);
                    prgroup.setVisibility(View.INVISIBLE);
                    frgroup.setVisibility(View.INVISIBLE);
                    ampgroup.setVisibility(View.INVISIBLE);
                    Log.e("random", np + " " + pr + " " + fr + " " + amp);
                    endThread();
                    mVibDrive = new AudioVibDriveContinuous(tf);
                    mVibDrive.setAudioData(audiodata);
                    mVibDrive.vibVolumeChange(ampweak, ampstrong, eqweak, eqstrong);
                    mVibDrive.setOnNextDriveListener(new AudioVibDriveContinuous.OnNextDriveListener() {
                        @Override
                        public AudioVibDriveContinuous.VibInfo onNextVibration() {
                            return new AudioVibDriveContinuous.VibInfo(np, fr, amp, audiovolume);
                        }
                    });
                    startThread();

                }
                else    {
                    npgroup.setVisibility(View.VISIBLE);
                    prgroup.setVisibility(View.VISIBLE);
                    frgroup.setVisibility(View.VISIBLE);
                    ampgroup.setVisibility(View.VISIBLE);
                }
            }
        });

        npgroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch(checkedId) {
                    case R.id.famil_np7:
                        np = 7;
                        break;
                    case R.id.famil_np5:
                        np = 5;
                        break;
                    case R.id.famil_np3:
                        np = 3;
                        break;
                    case R.id.famil_np2:
                        np = 2;
                        break;
                    case R.id.famil_np1:
                        np = 1;
                        break;
                }
                mVibDrive.setOnNextDriveListener(new AudioVibDriveContinuous.OnNextDriveListener() {
                    @Override
                    public AudioVibDriveContinuous.VibInfo onNextVibration() {
                       return new AudioVibDriveContinuous.VibInfo(np, fr, amp, audiovolume);
                    }
                });
                //Toast.makeText(getApplicationContext(), Integer.toString(np)+ pr + fr + amp, Toast.LENGTH_SHORT).show();
            }
        });
        prgroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch(checkedId) {
                    case R.id.famil_prFast:
                        pr = 2;
                        tf = 1000;
                        break;
                    case R.id.famil_prModerate:
                        pr = 1;
                        tf = 2000;
                        break;
                    case R.id.famil_prSlow:
                        pr = 0;
                        tf = 4000;
                        break;
                }
                endThread();
                mVibDrive = new AudioVibDriveContinuous(tf);
                mVibDrive.setAudioData(audiodata);
                mVibDrive.vibVolumeChange(ampweak, ampstrong, eqweak, eqstrong);
                mVibDrive.setOnNextDriveListener(new AudioVibDriveContinuous.OnNextDriveListener() {
                    @Override
                    public AudioVibDriveContinuous.VibInfo onNextVibration() {
                        return new AudioVibDriveContinuous.VibInfo(np, fr, amp, audiovolume);
                    }
                });
                startThread();
                //Toast.makeText(getApplicationContext(), Integer.toString(np)+ pr + fr + amp, Toast.LENGTH_SHORT).show();
            }
        });
        frgroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch(checkedId) {
                    case R.id.famil_frHigh:
                        fr = 2;
                        break;
                    case R.id.famil_frMid:
                        fr = 1;
                        break;
                    case R.id.famil_frLow:
                        fr = 0;
                        break;
                }
                mVibDrive.setOnNextDriveListener(new AudioVibDriveContinuous.OnNextDriveListener() {
                    @Override
                    public AudioVibDriveContinuous.VibInfo onNextVibration() {
                        return new AudioVibDriveContinuous.VibInfo(np, fr, amp, audiovolume);
                    }
                });
                //Toast.makeText(getApplicationContext(), Integer.toString(np)+ pr + fr + amp, Toast.LENGTH_SHORT).show();
            }
        });
        ampgroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch(checkedId) {
                    case R.id.famil_ampStrong:
                        amp = 1;
                        break;
                    case R.id.famil_ampWeak:
                        amp = 0;
                        break;
                }
                mVibDrive.setOnNextDriveListener(new AudioVibDriveContinuous.OnNextDriveListener() {
                    @Override
                    public AudioVibDriveContinuous.VibInfo onNextVibration() {
                        return new AudioVibDriveContinuous.VibInfo(np, fr, amp, audiovolume);
                    }
                });
                //Toast.makeText(getApplicationContext(), Integer.toString(np)+ pr + fr + amp, Toast.LENGTH_SHORT).show();
            }
        });
        doneButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent resIntent = new Intent();
                //Toast.makeText(getApplicationContext(), userTxt.getText().toString(), Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK, resIntent);
                endThread();
                finish();
            }
        });

        startThread();
    }
}
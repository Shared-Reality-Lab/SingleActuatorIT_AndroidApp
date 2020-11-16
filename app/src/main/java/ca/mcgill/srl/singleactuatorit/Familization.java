package ca.mcgill.srl.singleactuatorit;

import androidx.appcompat.app.AppCompatActivity;
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
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import ca.mcgill.srl.audioVibDrive.AudioVibDriveContinuous;

import static java.lang.Thread.sleep;

public class Familization extends AppCompatActivity {
    protected int[] ampweak;
    protected int[] ampstrong;
    protected int audiovolume;
    protected int np = 4, pr = 2, fr = 2, amp = 1;
    protected int tf = 1800;
    protected short[] audiodata;
    private Thread mVibThread = null;
    protected AudioVibDriveContinuous mVibDrive;

    int userID;
    TextView userTxt;

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


        Button doneButton = findViewById(R.id.doneButton);
        userID = intent.getExtras().getInt("id");
        ampweak = intent.getExtras().getIntArray("ampweak");
        ampstrong = intent.getExtras().getIntArray("ampstrong");
        audiovolume = intent.getExtras().getInt("audiovolume");
        audiodata = intent.getExtras().getShortArray("audiodata");
        Log.e ("volumes", Integer.toString(audiovolume));

        userTxt = findViewById(R.id.famil_txtIDView);
        final String str = "UserID="+ userID;
        userTxt.setText(str);

        mVibDrive = new AudioVibDriveContinuous(tf);
        mVibDrive.setAudioData(audiodata);
        mVibDrive.setOnNextDriveListener(new AudioVibDriveContinuous.OnNextDriveListener() {
            @Override
            public AudioVibDriveContinuous.VibInfo onNextVibration() {
                return new AudioVibDriveContinuous.VibInfo(4, 2, ampstrong[2], audiovolume);
            }
        });
        npgroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch(checkedId) {
                    case R.id.famil_np7:
                        np = 7;
                        break;
                    case R.id.famil_np6:
                        np = 6;
                        break;
                    case R.id.famil_np5:
                        np = 5;
                        break;
                    case R.id.famil_np4:
                        np = 4;
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
                        if (amp == 1) return new AudioVibDriveContinuous.VibInfo(np, fr, ampstrong[fr], audiovolume);
                        else return new AudioVibDriveContinuous.VibInfo(np, fr, ampweak[fr], audiovolume);
                    }
                });
                Toast.makeText(getApplicationContext(), Integer.toString(np)+ pr + fr + amp, Toast.LENGTH_SHORT).show();
            }
        });
        prgroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch(checkedId) {
                    case R.id.famil_prFast:
                        pr = 3;
                        tf = 1200;
                        break;
                    case R.id.famil_prModerate:
                        pr = 2;
                        tf = 1800;
                        break;
                    case R.id.famil_prSlow:
                        pr = 1;
                        tf = 2400;
                        break;
                    case R.id.famil_prVerySlow:
                        pr = 0;
                        tf = 3200;
                        break;
                }
                endThread();
                mVibDrive = new AudioVibDriveContinuous(tf);
                mVibDrive.setAudioData(audiodata);
                startThread();
                mVibDrive.setOnNextDriveListener(new AudioVibDriveContinuous.OnNextDriveListener() {
                    @Override
                    public AudioVibDriveContinuous.VibInfo onNextVibration() {
                        if (amp == 1) return new AudioVibDriveContinuous.VibInfo(np, fr, ampstrong[fr], audiovolume);
                        else return new AudioVibDriveContinuous.VibInfo(np, fr, ampweak[fr], audiovolume);
                    }
                });
                Toast.makeText(getApplicationContext(), Integer.toString(np)+ pr + fr + amp, Toast.LENGTH_SHORT).show();
            }
        });
        frgroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch(checkedId) {
                    case R.id.famil_frHighChord:
                        fr = 5;
                        break;
                    case R.id.famil_frVeryHigh:
                        fr = 4;
                        break;
                    case R.id.famil_frHigh:
                        fr = 3;
                        break;
                    case R.id.famil_frMid:
                        fr = 2;
                        break;
                    case R.id.famil_frLow:
                        fr = 1;
                        break;
                    case R.id.famil_frLowChord:
                        fr = 0;
                        break;
                }
                mVibDrive.setOnNextDriveListener(new AudioVibDriveContinuous.OnNextDriveListener() {
                    @Override
                    public AudioVibDriveContinuous.VibInfo onNextVibration() {
                        if (amp == 1) return new AudioVibDriveContinuous.VibInfo(np, fr, ampstrong[fr], audiovolume);
                        else return new AudioVibDriveContinuous.VibInfo(np, fr, ampweak[fr], audiovolume);
                    }
                });
                Toast.makeText(getApplicationContext(), Integer.toString(np)+ pr + fr + amp, Toast.LENGTH_SHORT).show();
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
                        if (amp == 1) return new AudioVibDriveContinuous.VibInfo(np, fr, ampstrong[fr], audiovolume);
                        else return new AudioVibDriveContinuous.VibInfo(np, fr, ampweak[fr], audiovolume);
                    }
                });
                Toast.makeText(getApplicationContext(), Integer.toString(np)+ pr + fr + amp, Toast.LENGTH_SHORT).show();
            }
        });
        doneButton.setOnClickListener(new Button.OnClickListener() {
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
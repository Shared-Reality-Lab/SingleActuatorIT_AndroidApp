package ca.mcgill.srl.singleactuatorit;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import ca.mcgill.srl.audioVibDrive.AudioVibDriveContinuous;

public class Experiment2 extends AppCompatActivity {

    protected int[] ampweak;
    protected int[] ampstrong;
    protected int audiovolume;
    protected int np,pr,fr,amp, tf = 1800;
    protected short[] audioData;

    protected int numstimuli;
    protected int trial = 0;
    protected int[][] stimuli;
    protected int[][] results;
    protected String logfilename;
    protected File file;
    protected boolean pause;


    private Thread mVibThread = null;
    protected AudioVibDriveContinuous mVibDrive;
    protected AudioVibDriveContinuous.OnNextDriveListener mNextVib;
    public static int SAMPLING_RATE = 48000;

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
        setContentView(R.layout.activity_experiment1);
        Intent intent = getIntent();

        final TextView userTxt = findViewById(R.id.exp2_txtIDView);
        final TextView trialTxt = findViewById(R.id.exp2_txtTrialView);
        Button nextButton = findViewById(R.id.exp2_NextButton);
        Button previousButton = findViewById(R.id.exp2_btPrev);
        Button pauseButton = findViewById(R.id.exp2_btPause);
        RadioGroup npgroup = findViewById(R.id.exp2_npRadioGroup);
        RadioGroup prgroup = findViewById(R.id.exp2_prRadioGroup);
        RadioGroup frgroup = findViewById(R.id.exp2_frRadioGroup);
        RadioGroup ampgroup = findViewById(R.id.exp2_ampRadioGroup);

        int userID = intent.getExtras().getInt("id");
        userTxt.setText("User ID: " + Integer.toString(userID));
        ampweak = intent.getExtras().getIntArray("ampweak");
        ampstrong = intent.getExtras().getIntArray("ampstrong");
        audiovolume = intent.getExtras().getInt("audiovolume");
        audioData = intent.getExtras().getShortArray("audiodata");


        stimuli = new int[numstimuli][4];
        results = new int[numstimuli][4];

        stimuliCreate();
        trial = 0;
        //init the variables for default
        //vibration thread init and start
        //1st trial's one.
        switch(stimuli[trial][1])   {
            case 0:
                tf = 3200;
                break;
            case 1:
                tf = 2400;
                break;
            case 2:
                tf = 1800;
                break;
            case 3:
                tf = 1200;
                break;
        }
        mVibDrive = new AudioVibDriveContinuous(tf);
        mVibDrive.setOnNextDriveListener(new AudioVibDriveContinuous.OnNextDriveListener()
        {
            @Override
            public AudioVibDriveContinuous.VibInfo onNextVibration() {
                if(stimuli[trial][3] == 1)
                    return new AudioVibDriveContinuous.VibInfo(stimuli[trial][0], stimuli[trial][2], ampstrong[stimuli[trial][2]], audiovolume);
                else
                    return new AudioVibDriveContinuous.VibInfo(stimuli[trial][0], stimuli[trial][2], ampweak[stimuli[trial][2]], audiovolume);
            }
        });
        startThread();

        npgroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.exp2_np7:
                        np = 7;
                        break;
                    case R.id.exp2_np6:
                        np = 6;
                        break;
                    case R.id.exp2_np5:
                        np = 5;
                        break;
                    case R.id.exp2_np4:
                        np = 4;
                        break;
                    case R.id.exp2_np3:
                        np = 3;
                        break;
                    case R.id.exp2_np2:
                        np = 2;
                        break;
                    case R.id.exp2_np1:
                        np = 1;
                        break;
                }
                //Toast.makeText(getApplicationContext(), Integer.toString(np) + Integer.toString(pr) + Integer.toString(fr) + Integer.toString(amp), Toast.LENGTH_SHORT).show();
            }
        });
        prgroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.exp2_prFast:
                        pr = 4;
                        break;
                    case R.id.exp2_prModerate:
                        pr = 3;
                        break;
                    case R.id.exp2_prSlow:
                        pr = 2;
                        break;
                    case R.id.exp2_prVerySlow:
                        pr = 1;
                        break;
                }
                //Toast.makeText(getApplicationContext(), Integer.toString(np) + Integer.toString(pr) + Integer.toString(fr) + Integer.toString(amp), Toast.LENGTH_SHORT).show();
            }
        });
        frgroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.exp2_frHighChord:
                        fr = 6;
                        break;
                    case R.id.exp2_frVeryHigh:
                        fr = 5;
                        break;
                    case R.id.exp2_frHigh:
                        fr = 4;
                        break;
                    case R.id.exp2_frMid:
                        fr = 3;
                        break;
                    case R.id.exp2_frLow:
                        fr = 2;
                        break;
                    case R.id.exp2_frLowChord:
                        fr = 1;
                        break;
                }
                //Toast.makeText(getApplicationContext(), Integer.toString(np) + Integer.toString(pr) + Integer.toString(fr) + Integer.toString(amp), Toast.LENGTH_SHORT).show();
            }
        });
        ampgroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.exp2_ampStrong:
                        amp = 2;
                        break;
                    case R.id.exp2_ampWeak:
                        amp = 1;
                        break;
                }
                //Toast.makeText(getApplicationContext(), Integer.toString(np) + Integer.toString(pr) + Integer.toString(fr) + Integer.toString(amp), Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void stimuliCreate()    {
        //pick random
        int numtraining = 56;
        int numoflevels[] = {7, 4, 6, 2}; //np, pr, fr, amp
        for (int i = 0; i < numtraining; i++) {
            stimuli[i][0] = (int) (Math.random() * numoflevels[0]) + 1;
            stimuli[i][1] = (int) (Math.random() * numoflevels[1]);
            stimuli[i][2] = (int) (Math.random() * numoflevels[2]);
            stimuli[i][3] = (int) (Math.random() * numoflevels[3]);
        }
         //randomization
        for(int j = 0; j < numtraining; j++)    {
            int r = (int) (Math.random() * numtraining);
            if (r == j) {
                continue;
            }
            int temp[] = new int[4];
            temp = stimuli[r];
            stimuli[r] = stimuli[j];
            stimuli[j] = temp;
            //rcount = rcount + 1;
        }
        //main stimuli
        int count = numtraining;
        for (int i =0; i<numoflevels[0]; i++) {
            for (int j = 0; j < (numoflevels[1]); j++) {
                for (int k = 0; k < (numoflevels[2]); k++) {
                    for (int l = 0; l < numoflevels[3]; l++) {
                        stimuli[count][0] = i+1;
                        stimuli[count][1] = j;
                        stimuli[count][2] = k;
                        stimuli[count][3] = l;
                        count = count + 1;
                    }
                }
            }
        }
        for(int j = numtraining; j < count; j++)    {
            int r = (int) (Math.random() * (count-numtraining));
            if (r == j) {
                continue;
            }
            int temp[] = new int[4];
            temp = stimuli[r];
            stimuli[r] = stimuli[j];
            stimuli[j] = temp;
            //rcount = rcount + 1;
        }
    }

}
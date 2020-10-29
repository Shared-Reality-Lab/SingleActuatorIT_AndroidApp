package ca.mcgill.srl.singleactuatorit;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import ca.mcgill.srl.audioVibDrive.AudioVibDriveContinuous;

import static java.lang.Math.floor;
import static java.lang.Math.random;

public class Experiment1 extends AppCompatActivity {

    protected int[] ampweak;
    protected int[] ampstrong;
    protected int audiovolume;
    protected int np,pr,fr,amp, tf = 1800;

    private Thread mVibThread = null;
    protected AudioVibDriveContinuous mVibDrive;
    protected AudioVibDriveContinuous.OnNextDriveListener mNextVib;
    public static int SAMPLING_RATE = 48000;

    //1 : numerosity, 2: period, 3: freq, 4: amp
    protected int[][] experimentalorder = {{0,1,2,3}, {1,0,3,2}, {2,3,0,1}, {3,2,1,0}};
    protected int[] numoftraining = {14,8,12,4};
    protected int[] numoflevels = {7,4,6,2};
    protected int[] phasesum;
    protected int[] breaktimes = {24,0,18,0};
    protected int numstimuli;
    protected int trial = 0;
    protected int[][] stimuli;
    protected int[][] results;
    protected String logfilename;
    protected File file;
    protected boolean pause;
    protected int phase;
    int latin;
    protected short[] audioData;

    RadioGroup npgroup, prgroup, frgroup, ampgroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_experiment1);
        Intent intent = getIntent();

        final TextView userTxt = findViewById(R.id.exp1_txtIDView);
        final TextView trialTxt = findViewById(R.id.exp1_txtTrialView);
        Button nextButton = findViewById(R.id.exp1_NextButton);
        Button previousButton = findViewById(R.id.exp1_btPrev);
        Button pauseButton = findViewById(R.id.exp1_btPause);
        npgroup = findViewById(R.id.exp1_npRadioGroup);
        prgroup = findViewById(R.id.exp1_prRadioGroup);
        frgroup = findViewById(R.id.exp1_frRadioGroup);
        ampgroup = findViewById(R.id.exp1_ampRadioGroup);

        int userID = intent.getExtras().getInt("id");
        userTxt.setText("User ID: " + Integer.toString(userID));
        ampweak = intent.getExtras().getIntArray("ampweak");
        ampstrong = intent.getExtras().getIntArray("ampstrong");
        audiovolume = intent.getExtras().getInt("audiovolume");
        audioData = intent.getExtras().getShortArray("audiodata");

        //stimuli cond. init.
        numstimuli = numoftraining[0] + (numoflevels[0]*numoflevels[0]) + numoftraining[1] + (numoflevels[1]*numoflevels[1]) +
                numoftraining[2] + (numoflevels[2]*numoflevels[2]) + numoftraining[3] + (numoflevels[3]*numoflevels[3]);
        Log.e ("number", Integer.toString(numstimuli));
        stimuli = new int[numstimuli][4];
        results = new int[numstimuli][3];
        stimuliCreate(userID);
        phase = 0;
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

        //finding current mode (what to answer)
        int mode = experimentalorder[latin][phase];
        hideRadioGroups(mode);

        npgroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch(checkedId) {
                    case R.id.exp1_np7:
                        np = 7;
                        break;
                    case R.id.exp1_np6:
                        np = 6;
                        break;
                    case R.id.exp1_np5:
                        np = 5;
                        break;
                    case R.id.exp1_np4:
                        np = 4;
                        break;
                    case R.id.exp1_np3:
                        np = 3;
                        break;
                    case R.id.exp1_np2:
                        np = 2;
                        break;
                    case R.id.exp1_np1:
                        np = 1;
                        break;
                }
            }
        });
        prgroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch(checkedId) {
                    case R.id.exp1_prFast:
                        pr = 3;
                        tf = 1200;
                        break;
                    case R.id.exp1_prModerate:
                        pr = 2;
                        tf = 1800;
                        break;
                    case R.id.exp1_prSlow:
                        pr = 1;
                        tf = 2400;
                        break;
                    case R.id.exp1_prVerySlow:
                        pr = 0;
                        tf = 3200;
                        break;
                }
            }
        });
        frgroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch(checkedId) {
                    case R.id.exp1_frHighChord:
                        fr = 5;
                        break;
                    case R.id.exp1_frVeryHigh:
                        fr = 4;
                        break;
                    case R.id.exp1_frHigh:
                        fr = 3;
                        break;
                    case R.id.exp1_frMid:
                        fr = 2;
                        break;
                    case R.id.exp1_frLow:
                        fr = 1;
                        break;
                    case R.id.exp1_frLowChord:
                        fr = 0;
                        break;
                }
            }
        });
        ampgroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch(checkedId) {
                    case R.id.exp1_ampStrong:
                        amp = 1;
                        break;
                    case R.id.exp1_ampWeak:
                        amp = 0;
                        break;
                }
            }
        });

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //if it is a training session, output feedback
                //results[trial] = new int[]{np, pr, fr, amp};
                int answer = 0;
                int t = experimentalorder[latin][phase];
                switch (t) {
                    case 0:
                        answer = np;
                        break;
                    case 1:
                        answer = pr;
                        break;
                    case 2:
                        answer = fr;
                        break;
                    case 3:
                        answer = amp;
                        break;
                    default:
                        throw new IllegalStateException("Unexpected value: " + t);
                }

                if (isTrainingTrial(trial, phase)) {
                    int correctanswer = stimuli[trial][t];
                    if (answer == correctanswer) {
                        MessageBox("Feedback", "Correct!");
                    }
                    else {
                        String ansstr;
                        ansstr = getCorrectFeedbackanswer(t);
                        MessageBox("Feedback", "The correct answer is " + ansstr);
                    }
                }
                results[trial][0] = experimentalorder[latin][phase]; //type
                results[trial][1] = stimuli[trial][phase]; // correct answer
                results[trial][2] = answer; // user's answer
                Log.e("result", "" + results[trial][0] + results[trial][1] + results[trial][2]);
                trial = trial + 1;
                trialTxt.setText("Trial: " + Integer.toString(trial));
                //finish
                if(trial == numstimuli)    {
                    MessageBox("Finish", "Experiment 1 done. Thank you and see you tomorrow!");
                    endThread();
                    //File summary output
                    finish();
                }
                //phase up
                if(trial == phasesum[phase+1]) {
                    MessageBox("Session done", "Session done. Please take a 2-minute rest.");
                    phase++;
                    hideRadioGroups(phase);
                }

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
                endThread();
                mVibDrive = new AudioVibDriveContinuous(tf);
                mVibDrive.setAudioData(audioData);
                startThread();
                mVibDrive.setOnNextDriveListener(new AudioVibDriveContinuous.OnNextDriveListener() {
                    public AudioVibDriveContinuous.VibInfo onNextVibration() {
                        if(stimuli[trial][3] == 1)
                            return new AudioVibDriveContinuous.VibInfo(stimuli[trial][0], stimuli[trial][2], ampstrong[stimuli[trial][2]], audiovolume);
                        else
                            return new AudioVibDriveContinuous.VibInfo(stimuli[trial][0], stimuli[trial][2], ampweak[stimuli[trial][2]], audiovolume);
                    }
                });
                //re-init vars for the next trial.
                Log.e("Answer", ": "+stimuli[trial][0] + " " + stimuli[trial][1] + " " + stimuli[trial][2] + " " + stimuli[trial][3]);
                //Toast.makeText(getApplicationContext(), Integer.toString(trial), Toast.LENGTH_SHORT).show();
                //+Integer.toString(np)+Integer.toString(pr)+Integer.toString(fr)+Integer.toString(amp)
            }
        });

        previousButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (trial > 1)  {
                    trial = trial - 1;
                    //setting back the selections
                }
                else {
                    MessageBox("Error", "This is the first trial!");
                    return;
                } 
                //update vibration
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
                endThread();
                mVibDrive = new AudioVibDriveContinuous(tf);
                mVibDrive.setAudioData(audioData);
                startThread();
                mVibDrive.setOnNextDriveListener(new AudioVibDriveContinuous.OnNextDriveListener() {
                    public AudioVibDriveContinuous.VibInfo onNextVibration() {
                        if(stimuli[trial][3] == 1)
                            return new AudioVibDriveContinuous.VibInfo(stimuli[trial][0], stimuli[trial][2], ampstrong[stimuli[trial][2]], audiovolume);
                        else
                            return new AudioVibDriveContinuous.VibInfo(stimuli[trial][0], stimuli[trial][2], ampweak[stimuli[trial][2]], audiovolume);
                    }
                });
                Toast.makeText(getApplicationContext(), Integer.toString(np)+Integer.toString(pr)+Integer.toString(fr)+Integer.toString(amp), Toast.LENGTH_SHORT).show();
            }
        });

        pauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pause = !pause;
                if(pause == true)  {
                    //stop vib thread
                    //stop timer
                    endThread();
                    Toast.makeText(getApplicationContext(), "Paused vib", Toast.LENGTH_SHORT).show();
                }
                else    {
                    //resume vib thread
                    //restart timer
                    mVibDrive = new AudioVibDriveContinuous(tf);
                    mVibDrive.setAudioData(audioData);
                    startThread();
                    mVibDrive.setOnNextDriveListener(new AudioVibDriveContinuous.OnNextDriveListener() {
                        public AudioVibDriveContinuous.VibInfo onNextVibration() {
                            if(stimuli[trial][3] == 1)
                                return new AudioVibDriveContinuous.VibInfo(stimuli[trial][0], stimuli[trial][2], ampstrong[stimuli[trial][2]], audiovolume);
                            else
                                return new AudioVibDriveContinuous.VibInfo(stimuli[trial][0], stimuli[trial][2], ampweak[stimuli[trial][2]], audiovolume);
                        }
                    });
                    Toast.makeText(getApplicationContext(), "Restarted vib", Toast.LENGTH_SHORT).show();
                }
            }
        });


    }

    @SuppressLint("ResourceType")
    private String getCorrectFeedbackanswer(int t)   {
        switch (t) {
            case 0:
                switch (stimuli[trial][0]) {
                    case 1:
                        return "1";
                    case 2:
                        return "2";
                    case 3:
                        return "3";
                    case 4:
                        return "4";
                    case 5:
                        return "5";
                    case 6:
                        return "6";
                    case 7:
                        return "7";
                }
                break;
            case 1:
                switch (stimuli[trial][1]) {
                    case 0:
                        return getString(R.string.veryslow);
                    case 1:
                        return getString(R.string.slow);
                    case 2:
                        return getString(R.string.moderate);
                    case 3:
                        return getString(R.string.fast);
                }
                break;
            case 2:
                switch (stimuli[trial][2]) {
                    case 0:
                        return getString(R.string.lowchord);
                    case 1:
                        return getString(R.string.low);
                    case 2:
                        return getString(R.string.mid);
                    case 3:
                        return getString(R.string.high);
                    case 4:
                        return getString(R.string.veryhigh);
                    case 5:
                        return getString(R.string.highchord);
                }
                break;
            case 3:
                switch (stimuli[trial][3]) {
                    case 0:
                        return getString(R.string.weak);
                    case 1:
                        return getString(R.string.strong);
                }
                break;
        }
        return null;
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
    private void MessageBox(String title, String str) {
        AlertDialog.Builder dlgAlert = new AlertDialog.Builder(this);
        dlgAlert.setMessage(str);
        dlgAlert.setTitle(title);
        dlgAlert.setPositiveButton("Ok",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        //dismiss the dialog
                    }
                });

        dlgAlert.create().show();
    }
    // 0 to numtrials[1st], numtrials+numlevels^2[1st]+numtrials[2nd] ...
    private boolean isTrainingTrial(int trial, int phase) {
        int rangebottom = phasesum[phase], rangetop = phasesum[phase]+numoftraining[experimentalorder[latin][phase]];
        if (trial >= rangebottom && trial < rangetop) {
            return true;
        }
        else {
            return false;
        }
    }
    private void stimuliCreate(int userID)    {
        //pick random
        int count = 0;
        latin = userID % 4;
        phasesum = new int[5];
        for (int i = 0; i < 4; i++) {
            int t = experimentalorder[latin][i];
            for(int j = 0; j < (numoftraining[t]); j++) {
                switch (t) {
                    case 0:
                        stimuli[count][0] = j % numoflevels[0] + 1;
                        stimuli[count][1] = (int) (Math.random() * numoflevels[1]);
                        stimuli[count][2] = (int) (Math.random() * numoflevels[2]);
                        stimuli[count][3] = (int) (Math.random() * numoflevels[3]);
                        count = count + 1;
                        break;
                    case 1:
                        stimuli[count][0] = (int) (Math.random() * numoflevels[0]) + 1;
                        stimuli[count][1] = j % numoflevels[1];
                        stimuli[count][2] = (int) (Math.random() * numoflevels[2]);
                        stimuli[count][3] = (int) (Math.random() * numoflevels[3]);
                        count = count + 1;
                        break;
                    case 2:
                        stimuli[count][0] = (int) (Math.random() * numoflevels[0]) + 1;
                        stimuli[count][1] = (int) (Math.random() * numoflevels[1]);
                        stimuli[count][2] = j % numoflevels[2];
                        stimuli[count][3] = (int) (Math.random() * numoflevels[3]);
                        count = count + 1;
                        break;
                    case 3:
                        stimuli[count][0] = (int) (Math.random() * numoflevels[0]) + 1;
                        stimuli[count][1] = (int) (Math.random() * numoflevels[1]);
                        stimuli[count][2] = (int) (Math.random() * numoflevels[2]);
                        stimuli[count][3] = j % numoflevels[3];
                        count = count + 1;
                        break;
                }
            }
            //randomization
            /*int rcount = count - numoftraining[t];
            for(int j = 0; j < numoftraining[t]; j++)    {
                int r = (int) (random() * numoftraining[t]);
                if (r == j) {
                    //rcount = rcount + 1;
                    continue;
                }
                int temp[] = new int[4];
                temp = stimuli[rcount+r];
                stimuli[rcount+r] = stimuli[rcount+j];
                stimuli[rcount+j] = temp;
                //rcount = rcount + 1;
            }*/
            //main stimuli
            for(int j = 0; j < (numoflevels[t]*numoflevels[t]); j++)    {
                switch (t) {
                    case 0:
                        stimuli[count][0] = j % numoflevels[0] + 1;
                        stimuli[count][1] = (int) (Math.random() * numoflevels[1]);
                        stimuli[count][2] = (int) (Math.random() * numoflevels[2]);
                        stimuli[count][3] = (int) (Math.random() * numoflevels[3]);
                        count = count + 1;
                        break;
                    case 1:
                        stimuli[count][0] = (int) (Math.random() * numoflevels[0]) + 1;
                        stimuli[count][1] = j % numoflevels[1];
                        stimuli[count][2] = (int) (Math.random() * numoflevels[2]);
                        stimuli[count][3] = (int) (Math.random() * numoflevels[3]);
                        count = count + 1;
                        break;
                    case 2:
                        stimuli[count][0] = (int) (Math.random() * numoflevels[0]) + 1;
                        stimuli[count][1] = (int) (Math.random() * numoflevels[1]);
                        stimuli[count][2] = j % numoflevels[2];
                        stimuli[count][3] = (int) (Math.random() * numoflevels[3]);
                        count = count + 1;
                        break;
                    case 3:
                        stimuli[count][0] = (int) (Math.random() * numoflevels[0]) + 1;
                        stimuli[count][1] = (int) (Math.random() * numoflevels[1]);
                        stimuli[count][2] = (int) (Math.random() * numoflevels[2]);
                        stimuli[count][3] = j % numoflevels[3];
                        count = count + 1;
                        break;
                }
            }
            /*rcount = count - numoflevels[t]*numoflevels[t];
            for(int j = 0; j < (numoflevels[t]*numoflevels[t]); j++)    {
                int r = (int) (random() * (numoflevels[t]*numoflevels[t]));
                if (r == j) {
                    //rcount = rcount + 1;
                    continue;
                }
                int temp[] = new int[4];
                temp = stimuli[rcount+r];
                stimuli[rcount+r] = stimuli[rcount+j];
                stimuli[rcount+j] = temp;
                //rcount = rcount + 1;
            }*/
            phasesum[i+1] = count;
        }
    }
    private void hideRadioGroups(int mode)  {
        switch(mode)    {
            case 0: // number
                npgroup.setVisibility(View.VISIBLE);
                prgroup.setVisibility(View.INVISIBLE);
                frgroup.setVisibility(View.INVISIBLE);
                ampgroup.setVisibility(View.INVISIBLE);
                return;
            case 1: // period
                npgroup.setVisibility(View.INVISIBLE);
                prgroup.setVisibility(View.VISIBLE);
                frgroup.setVisibility(View.INVISIBLE);
                ampgroup.setVisibility(View.INVISIBLE);
                return;
            case 2: // frequency
                npgroup.setVisibility(View.INVISIBLE);
                prgroup.setVisibility(View.INVISIBLE);
                frgroup.setVisibility(View.VISIBLE);
                ampgroup.setVisibility(View.INVISIBLE);
                return;
            case 3: // amp
                npgroup.setVisibility(View.INVISIBLE);
                prgroup.setVisibility(View.INVISIBLE);
                frgroup.setVisibility(View.INVISIBLE);
                ampgroup.setVisibility(View.VISIBLE);
                return;
        }
    }

}
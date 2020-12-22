package ca.mcgill.srl.singleactuatorit;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.icu.text.SimpleDateFormat;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Date;

import ca.mcgill.srl.audioVibDrive.AudioVibDriveContinuous;
import ca.mcgill.srl.audioVibDrive.AudioVibDriveStatic;

import static java.lang.Math.random;

public class Experiment1_2 extends AppCompatActivity {

    protected int[] eqweak;
    protected int[] eqstrong;
    protected int ampweak, ampstrong;
    protected int audiovolume;
    protected int np,pr,fr,amp, tf;

    private Thread mNoiseThread = null;
    protected AudioVibDriveContinuous mNoiseDrive;
    //protected AudioVibDriveContinuous.OnNextDriveListener mNextVib;
    protected AudioVibDriveStatic mVibDrive;
    public static int SAMPLING_RATE = 12000;

    //1 : numerosity, 2: period, 3: freq, 4: amp
    protected int[][] experimentalorder = {{0,1,2,3}, {1,0,3,2}, {2,3,0,1}, {3,2,1,0}};
    protected int[] numoftraining = {14,6,6,4};
    protected int[] numoflevels = {7,3,3,2};
    protected int[] phasesum;
    protected int[] breaktimes = {38,0,0,0};//including num of training
    protected int[] mandatoryBreak = {0, 0, 0, 0};
    protected int numstimuli;
    protected int trial = 0;
    protected int[][] stimuli;
    protected int[][] results;
    protected boolean pause;
    protected int phase;
    int latin;
    protected short[] audioData;
    protected boolean istouched;

    RadioGroup npgroup, prgroup, frgroup, ampgroup;
    Logger mLogger;
    Logger mResultLogger;

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_id_experiment);
        Intent intent = getIntent();

        final TextView userTxt = findViewById(R.id.exp2_txtIDView);
        final TextView trialTxt = findViewById(R.id.exp2_txtTrialView);
        final Button nextButton = findViewById(R.id.exp2_NextButton);
        final Button pauseButton = findViewById(R.id.exp2_pauseButton);
        final Button playButton = findViewById(R.id.exp2_playvib);
        npgroup = findViewById(R.id.exp2_npRadioGroup);
        prgroup = findViewById(R.id.exp2_prRadioGroup);
        frgroup = findViewById(R.id.exp2_frRadioGroup);
        ampgroup = findViewById(R.id.exp2_ampRadioGroup);

        int userID = intent.getExtras().getInt("id");
        userTxt.setText("User ID: " + userID);
        ampweak = intent.getExtras().getInt("ampweak");
        ampstrong = intent.getExtras().getInt("ampstrong");
        eqweak = intent.getExtras().getIntArray("eqweak");
        eqstrong = intent.getExtras().getIntArray("eqstrong");
        audiovolume = intent.getExtras().getInt("audiovolume");
        audioData = intent.getExtras().getShortArray("audiodata");

        //logger setting
        SimpleDateFormat sdf = new SimpleDateFormat("yy-MM-dd_HH:mm:ss.SSS");
        Date time = new Date();
        String time1 = sdf.format(time);
        String logPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/singleit/log/" + userID +"_Log_Exp1_2_" + time1 + ".txt";
        mLogger = new Logger(logPath, getApplicationContext());
        String resultPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/singleit/result/" + userID +"_Result_Exp1_2_" + time1 + ".txt";
        mResultLogger = new Logger(resultPath, getApplicationContext());

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
        tf = 3000;
        mNoiseDrive = new AudioVibDriveContinuous(tf);
        mNoiseDrive.setAudioData(audioData);
        mNoiseDrive.vibVolumeChange(ampweak, ampstrong, eqweak, eqstrong);
        mNoiseDrive.setOnNextDriveListener(new AudioVibDriveContinuous.OnNextDriveListener()
        {
            @Override
            public AudioVibDriveContinuous.VibInfo onNextVibration() {
                return new AudioVibDriveContinuous.VibInfo(3, 3, 1, audiovolume);
            }
        });
        startThread();

        mVibDrive = new AudioVibDriveStatic(tf);
        mVibDrive.vibVolumeChange(ampweak, ampstrong, eqweak, eqstrong);
        mVibDrive.generateVibSignal(stimuli[trial][0], stimuli[trial][1], stimuli[trial][2], stimuli[trial][3]);


        //finding current mode (what to answer)
        int mode = experimentalorder[latin][phase];
        Log.e("Mode", ""+mode);
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
                if(istouched == false)  {
                    istouched = true;
                    nextButton.setEnabled(true);
                }
                mLogger.WriteMessage("Number\t"+ np, true);
            }
        });
        prgroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch(checkedId) {
                    case R.id.exp1_prFast:
                        pr = 2;
                        break;
                    case R.id.exp1_prModerate:
                        pr = 1;
                        break;
                    case R.id.exp1_prSlow:
                        pr = 0;
                        break;
                }
                if(istouched == false)  {
                    istouched = true;
                    nextButton.setEnabled(true);
                }
                mLogger.WriteMessage("Period\t"+ pr, true);
            }
        });
        frgroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch(checkedId) {
                    case R.id.exp1_frHigh:
                        fr = 2;
                        break;
                    case R.id.exp1_frMid:
                        fr = 1;
                        break;
                    case R.id.exp1_frLow:
                        fr = 0;
                        break;
                }
                if(istouched == false)  {
                    istouched = true;
                    nextButton.setEnabled(true);
                }
                mLogger.WriteMessage("Frequency\t"+ fr, true);
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
                if(istouched == false)  {
                    istouched = true;
                    nextButton.setEnabled(true);
                }
                mLogger.WriteMessage("Amplitude\t"+ amp, true);
            }
        });

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //if it is a training session, output feedback
                //results[trial] = new int[]{np, pr, fr, amp};
                istouched = false;
                nextButton.setEnabled(false);
                playButton.setEnabled(true);
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

                //current output;
                results[trial][0] = experimentalorder[latin][phase]; //type
                results[trial][1] = stimuli[trial][t]; // correct answer
                results[trial][2] = answer; // user's answer
                Log.e("result", "" + results[trial][0] + results[trial][1] + results[trial][2]);
                String LoggerString = "Trial: " + trial + " Stimuli: " + stimuli[trial][phase] + " Answer :" + answer + "\n";
                mLogger.WriteMessage(LoggerString, true);


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
                trial = trial + 1;
                trialTxt.setText("Trial: " + trial);

                if(trial < numstimuli) {
                    mVibDrive.generateVibSignal(stimuli[trial][0], stimuli[trial][1], stimuli[trial][2], stimuli[trial][3]);
                    //re-init vars for the next trial.
                    Log.e("Answer", ""+stimuli[trial][0] + stimuli[trial][1] +  stimuli[trial][2] +  stimuli[trial][3]);
                    Log.e("trial", Integer.toString(trial));
                }
                else{
                    //finish
                    //prevent error
                    trial = trial - 1;
                    endThread();
                    Toast.makeText(getApplicationContext(),"Experiment 1 done. Thank you and see you tomorrow!", Toast.LENGTH_LONG);
                    //File summary output
                    for(int i = 0; i <numstimuli; i++) {
                        mResultLogger.WriteArray(results[i], false, true);
                    }

                    //resintent = new Intent();
                    setResult(RESULT_OK);
                    finish();
                    return;
                }
                //conditions
                //phase up
                if(trial == phasesum[phase+1]) {
                    MessageBox("Session done", "Session done. Please take a 2-minute rest.");
                    phase++;
                    hideRadioGroups(experimentalorder[latin][phase]);
                }
                //mandatory break
                else if(trial == mandatoryBreak[phase])  {
                    MessageBox("Mandatory break", "Please take a 2-minute rest.");
                }


                //Toast.makeText(getApplicationContext(), Integer.toString(trial), Toast.LENGTH_SHORT).show();
                //+Integer.toString(np)+Integer.toString(pr)+Integer.toString(fr)+Integer.toString(amp)
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
                    mNoiseDrive = new AudioVibDriveContinuous(tf);
                    mNoiseDrive.vibVolumeChange(ampweak, ampstrong, eqweak, eqstrong);
                    mNoiseDrive.setAudioData(audioData);
                    startThread();
                    mNoiseDrive.setOnNextDriveListener(new AudioVibDriveContinuous.OnNextDriveListener() {
                        public AudioVibDriveContinuous.VibInfo onNextVibration() {
                            return new AudioVibDriveContinuous.VibInfo(3, 3, 3, audiovolume);
                        }
                    });
                    Toast.makeText(getApplicationContext(), "Restarted Noise", Toast.LENGTH_SHORT).show();
                }
            }
        });
        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mVibDrive.vibrate();
                playButton.setEnabled(false);
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
                        return "3s";
                    case 1:
                        return "2s";
                    case 2:
                        return "1s";
                }
                break;
            case 2:
                switch (stimuli[trial][2]) {
                    case 0:
                        return getString(R.string.low);
                    case 1:
                        return getString(R.string.mid);
                    case 2:
                        return getString(R.string.high);
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
        if (mNoiseThread == null) {
            mNoiseThread = new Thread(mNoiseDrive);
            mNoiseThread.start();
        }
        Log.i("Thread", "Started");
    }
    private void endThread()    {
        if (mNoiseThread != null) {
            mNoiseDrive.stop();
            try {
                mNoiseThread.join();
            } catch (Exception e) { }

            mNoiseThread = null;
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
        return trial >= rangebottom && trial < rangetop;
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
            int rcount = count - numoftraining[t];
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
            }
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
            rcount = count - numoflevels[t]*numoflevels[t];
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
            }
            if(breaktimes[t] != 0) mandatoryBreak[i] = phasesum[i] + breaktimes[t];
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
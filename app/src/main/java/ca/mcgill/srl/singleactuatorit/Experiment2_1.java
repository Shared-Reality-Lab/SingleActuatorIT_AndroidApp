package ca.mcgill.srl.singleactuatorit;

import androidx.appcompat.app.AppCompatActivity;

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

import java.io.File;
import java.util.Date;

import ca.mcgill.srl.audioVibDrive.AudioVibDriveContinuous;

public class Experiment2_1 extends AppCompatActivity {

    protected int[] eqweak;
    protected int[] eqstrong;
    protected int ampweak, ampstrong;
    protected int audiovolume;
    protected int np=3,pr=1,fr=1,amp=1, tf=4000;
    protected short[] audioData;

    protected int numstimuli = 30 + 180;
    protected int trial = 0;
    protected int[][] stimuli;
    protected int[][] results;
    protected String logfilename;
    protected File file;
    protected boolean pause;
    Logger mLogger;
    Logger mResultLogger;
    TextView userTxt;
    TextView trialTxt;

    private Thread mVibThread = null;
    protected AudioVibDriveContinuous mVibDrive;
    protected AudioVibDriveContinuous.OnNextDriveListener mNextVib;
    public static int SAMPLING_RATE = 12000;

    protected boolean istouched;

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
        setContentView(R.layout.activity_cont_experiment);
        Intent intent = getIntent();

        userTxt = findViewById(R.id.exp1_txtIDView);
        trialTxt = findViewById(R.id.exp1_txtTrialView);
        final Button nextButton = findViewById(R.id.exp1_NextButton);
        Button pauseButton = findViewById(R.id.exp1_btPause);
        Button previousButton = findViewById(R.id.exp1_btPrev);
        RadioGroup npgroup = findViewById(R.id.exp1_npRadioGroup);
        RadioGroup prgroup = findViewById(R.id.exp1_prRadioGroup);
        RadioGroup frgroup = findViewById(R.id.exp1_frRadioGroup);
        RadioGroup ampgroup = findViewById(R.id.exp1_ampRadioGroup);

        int userID = intent.getExtras().getInt("id");
        userTxt.setText("User ID: " + userID);
        ampweak = intent.getExtras().getInt("ampweak");
        ampstrong = intent.getExtras().getInt("ampstrong");
        eqweak = intent.getExtras().getIntArray("eqweak");
        eqstrong = intent.getExtras().getIntArray("eqstrong");
        audiovolume = intent.getExtras().getInt("audiovolume");
        audioData = intent.getExtras().getShortArray("audiodata");

        SimpleDateFormat sdf = new SimpleDateFormat("yy-MM-dd_HH:mm:ss.SSS");
        Date time = new Date();
        String time1 = sdf.format(time);
        String logPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/singleit/log/" + userID +"_Log_Exp2_1_" + time1 + ".txt";
        mLogger = new Logger(logPath, getApplicationContext());
        String resultPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/singleit/result/" + userID +"_Result_Exp2_1_" + time1 + ".txt";
        mResultLogger = new Logger(resultPath, getApplicationContext());

        stimuli = new int[numstimuli][4];
        results = new int[numstimuli][4];

        stimuliCreate();
        trial = 0;
        //init the variables for default
        //vibration thread init and start
        //1st trial's one.
        switch(stimuli[trial][1])   {
            case 0:
                tf = 4000;
                break;
            case 1:
                tf = 2000;
                break;
            case 2:
                tf = 1000;
                break;
        }
        mVibDrive = new AudioVibDriveContinuous(tf);
        mVibDrive.setAudioData(audioData);
        mVibDrive.vibVolumeChange(ampweak, ampstrong, eqweak, eqstrong);
        mVibDrive.setOnNextDriveListener(new AudioVibDriveContinuous.OnNextDriveListener()
        {
            @Override
            public AudioVibDriveContinuous.VibInfo onNextVibration() {
            return new AudioVibDriveContinuous.VibInfo(stimuli[trial][0], stimuli[trial][2], stimuli[trial][3], audiovolume);
            }
        });
        istouched = false;
        startThread();

        npgroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.exp1_np7:
                        np = 7;
                        break;
                    case R.id.exp1_np5:
                        np = 5;
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
                mLogger.WriteMessage("Number\t"+ np, true);
                if(istouched == false)  {
                    istouched = true;
                    nextButton.setEnabled(true);
                }
                //Toast.makeText(getApplicationContext(), Integer.toString(np) + Integer.toString(pr) + Integer.toString(fr) + Integer.toString(amp), Toast.LENGTH_SHORT).show();
            }
        });
        prgroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
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
                mLogger.WriteMessage("Tempo\t"+ pr, true);
                //Toast.makeText(getApplicationContext(), Integer.toString(np) + Integer.toString(pr) + Integer.toString(fr) + Integer.toString(amp), Toast.LENGTH_SHORT).show();
            }
        });
        frgroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
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
                //Toast.makeText(getApplicationContext(), Integer.toString(np) + Integer.toString(pr) + Integer.toString(fr) + Integer.toString(amp), Toast.LENGTH_SHORT).show();
            }
        });
        ampgroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
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
                //Toast.makeText(getApplicationContext(), Integer.toString(np) + Integer.toString(pr) + Integer.toString(fr) + Integer.toString(amp), Toast.LENGTH_SHORT).show();
            }
        });

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                istouched = false;
                nextButton.setEnabled(false);

                results[trial][0] = np;
                results[trial][1] = pr;
                results[trial][2] = fr;
                results[trial][3] = amp;
                Log.e("result", np + "" +pr+fr+amp);
                int[] resultarr = new int[9];
                resultarr[0] = trial;
                resultarr[1] = stimuli[trial][0]; resultarr[2] = stimuli[trial][1]; resultarr[3] = stimuli[trial][2]; resultarr[4] = stimuli[trial][3];
                resultarr[5] = results[trial][0]; resultarr[6] = results[trial][1]; resultarr[7] = results[trial][2]; resultarr[8] = results[trial][3];
                String LoggerString = "Trial: " + trial;
                mLogger.WriteMessage(LoggerString, true);
                mLogger.WriteArray(resultarr, false, true);
                endThread();

                if (trial < 30) {
                    String str;
                    if((resultarr[1] == resultarr[5]) && (resultarr[2] == resultarr[6]) && (resultarr[3] == resultarr[7]) && (resultarr[4] == resultarr[8])) {
                        str = "Correct";
                    }
                    else {
                        str = getCorrectFeedbackanswer();
                    }
                    MessageBox("Feedback", str);
                }
                //session finish: 2 min break;

                trial = trial + 1;

                trialTxt.setText("Trial: " + trial);
                if(trial < numstimuli) {
                    switch(stimuli[trial][1])   {
                        case 0:
                            tf = 4000;
                            break;
                        case 1:
                            tf = 2000;
                            break;
                        case 2:
                            tf = 1000;
                            break;
                    }
                    mVibDrive = new AudioVibDriveContinuous(tf);
                    mVibDrive.setAudioData(audioData);
                    mVibDrive.vibVolumeChange(ampweak, ampstrong, eqweak, eqstrong);
                    mVibDrive.setOnNextDriveListener(new AudioVibDriveContinuous.OnNextDriveListener()
                    {
                        @Override
                        public AudioVibDriveContinuous.VibInfo onNextVibration() {
                        return new AudioVibDriveContinuous.VibInfo(stimuli[trial][0], stimuli[trial][2], stimuli[trial][3], audiovolume);
                        }
                    });
                    startThread();
                }
                //training: feedback

                //finish
                if (trial == numstimuli)    {
                    //prevent error
                    trial = trial - 1;

                    for(int i = 0; i <numstimuli; i++) {
                        int t[] = new int[8];
                        t[0] = stimuli[i][0]; t[1] = stimuli[i][1]; t[2] = stimuli[i][2]; t[3] = stimuli[i][3];
                        t[4] = results[i][0]; t[5] = results[i][1]; t[6] = results[i][2]; t[7] = results[i][3];
                        mResultLogger.WriteArray(t, false, true);
                    }
                    setResult(RESULT_OK);
                    finish();
                    return;
                }
                if (trial % 30 == 0) {
                    MessageBox("Session break", "Please make a 2-min rest.");
                }



            }
        });
        previousButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (trial > 1) {
                    trial = trial - 1;
                    trialTxt.setText("Trial: " + trial);
                    //setting back the selections
                } else {
                    MessageBox("Error", "This is the first trial!");
                    return;
                }
                //update vibration
                switch (stimuli[trial][1]) {
                    case 0:
                        tf = 4000;
                        break;
                    case 1:
                        tf = 2000;
                        break;
                    case 2:
                        tf = 1000;
                        break;
                }

                endThread();
                mVibDrive = new AudioVibDriveContinuous(tf);
                mVibDrive.setAudioData(audioData);
                mVibDrive.vibVolumeChange(ampweak, ampstrong, eqweak, eqstrong);
                startThread();
                mVibDrive.setOnNextDriveListener(new AudioVibDriveContinuous.OnNextDriveListener() {
                    public AudioVibDriveContinuous.VibInfo onNextVibration() {
                        return new AudioVibDriveContinuous.VibInfo(stimuli[trial][0], stimuli[trial][2], stimuli[trial][3], audiovolume);
                    }
                });
                String LoggerString = "Trial: " + trial + " Stimuli: " + stimuli[trial].toString() + "Back";
                mLogger.WriteMessage(LoggerString, true);
                //Toast.makeText(getApplicationContext(),Integer.toString(np)+ pr + fr + amp,Toast.LENGTH_SHORT).show();
            }
        });
        pauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pause = !pause;
                if(pause)  {
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
                    mVibDrive.vibVolumeChange(ampweak,ampstrong,eqweak,eqstrong);
                    startThread();
                    mVibDrive.setOnNextDriveListener(new AudioVibDriveContinuous.OnNextDriveListener() {
                        public AudioVibDriveContinuous.VibInfo onNextVibration() {
                            return new AudioVibDriveContinuous.VibInfo(stimuli[trial][0], stimuli[trial][2], stimuli[trial][3], audiovolume);
                        }
                    });
                    Toast.makeText(getApplicationContext(), "Restarted vib", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    private void stimuliCreate()    {
        //pick random
        int numtraining = 30;
        int[] numoflevels = {5, 3, 3, 2}; //np, pr, fr, amp
        int[] pulses = {1,2,3,5,7};
        for (int i = 0; i < numtraining; i++) {
            stimuli[i][0] =  pulses[(int) (Math.random() * numoflevels[0])];
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
            int[] temp = new int[4];
            temp = stimuli[r];
            stimuli[r] = stimuli[j];
            stimuli[j] = temp;
            //rcount = rcount + 1;
        }
        //main stimuli
        int count = numtraining;
        for (int r =0; r < 2; r++)  {
            for (int i =0; i<numoflevels[0]; i++) {
                for (int j = 0; j < (numoflevels[1]); j++) {
                    for (int k = 0; k < (numoflevels[2]); k++) {
                        for (int l = 0; l < numoflevels[3]; l++) {
                            stimuli[count][0] = pulses[i];
                            stimuli[count][1] = j;
                            stimuli[count][2] = k;
                            stimuli[count][3] = l;
                            count = count + 1;
                        }
                    }
                }
            }
        }
        for(int j = numtraining; j < count; j++)    {
            int r = (int) (Math.random() * (count-numtraining) + numtraining);
            if (r == j) {
                continue;
            }
            int[] temp = new int[4];
            temp = stimuli[r];
            stimuli[r] = stimuli[j];
            stimuli[j] = temp;
            //rcount = rcount + 1;
        }
        numstimuli = count;
    }
    private String getCorrectFeedbackanswer()   {
        String value = "";
        switch (stimuli[trial][0]) {
            case 1:
                value = "Number: 1 ";
                break;
            case 2:
                value = "Number: 2 ";
                break;
            case 3:
                value = "Number: 3 ";
                break;
            case 5:
                value = "Number: 5 ";
                break;
            case 7:
                value = "Number: 7 ";
                break;
        }
        switch (stimuli[trial][1]) {
            case 0:
                value = value + "\nTempo: " + getString(R.string.slow);
                break;
            case 1:
                value = value + "\nTempo: " + getString(R.string.moderate);
                break;
            case 2:
                value = value + "\nTempo: " + getString(R.string.fast);
                break;
        }
        switch (stimuli[trial][2]) {
            case 0:
                value = value + "\nFrequency: " + getString(R.string.low);
                break;
            case 1:
                value = value + "\nFrequency: " + getString(R.string.mid);
                break;
            case 2:
                value = value + "\nFrequency: " + getString(R.string.high);
                break;
        }
        switch (stimuli[trial][3]) {
            case 0:
                value = value + "\nAmplitude: " + getString(R.string.weak);
                break;
            case 1:
                value = value + "\nAmplitude: " + getString(R.string.strong);
                break;
        }
        return value;
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
}
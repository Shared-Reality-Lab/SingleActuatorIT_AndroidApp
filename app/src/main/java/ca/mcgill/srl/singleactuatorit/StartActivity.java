package ca.mcgill.srl.singleactuatorit;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class StartActivity extends AppCompatActivity {

    protected int userID = -1;
    protected int request_calib_Code = 1001;
    protected int request_famil_Code = 1002;
    protected int request_exp1_1_Code = 1003;
    protected int request_fileload_Code = 1004;
    protected int request_exp2_1_Code = 1005;
    protected int request_exp1_2_Code = 1006;
    protected int request_exp2_2_Code = 1007;

    int[] eqweak = {25, 30, 35};
    int[] eqstrong = {55, 60, 75};
    int ampweak = 40;
    int ampstrong = 70;
    protected int audiovolume = 50;
    protected short[] audiodata;
    protected Button exp1Button;
    protected Button exp2Button;
    //protected Button exp1_2Button;
    //protected Button exp2_2Button;
    protected Button calibrationButton;
    protected Button familButton;
    protected Button loadButton;

    final int SAMPLING_RATE = 12000;
    final int LONGEST_TIME_FRAME = 4000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        }
        if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }
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

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        TextView uidView = findViewById(R.id.txtIDView);
        exp1Button = findViewById(R.id.exp1Button);
        exp1Button.setEnabled(false);
        exp1Button.setOnClickListener(new Button.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), Experiment2_1.class);
                intent.putExtra("id", userID);
                intent.putExtra("ampweak", ampweak);
                intent.putExtra("ampstrong", ampstrong);
                intent.putExtra("eqweak", eqweak);
                intent.putExtra("eqstrong", eqstrong);
                intent.putExtra("audiovolume", audiovolume);
                startActivityForResult(intent, request_exp2_1_Code);
            }
        });

        exp2Button = findViewById(R.id.exp2Button);
        exp2Button.setEnabled(false);
        exp2Button.setOnClickListener(new Button.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), Experiment2_2.class);
                intent.putExtra("id", userID);
                intent.putExtra("ampweak", ampweak);
                intent.putExtra("ampstrong", ampstrong);
                intent.putExtra("eqweak", eqweak);
                intent.putExtra("eqstrong", eqstrong);
                intent.putExtra("audiovolume", audiovolume);
                startActivityForResult(intent, request_exp2_2_Code);
            }
        });
        /*
        exp1_2Button = findViewById(R.id.exp12Button);
        //exp1_2Button.setEnabled(false);
        exp1_2Button.setOnClickListener(new Button.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), Experiment1_2.class);
                intent.putExtra("id", userID);
                intent.putExtra("ampweak", ampweak);
                intent.putExtra("ampstrong", ampstrong);
                intent.putExtra("eqweak", eqweak);
                intent.putExtra("eqstrong", eqstrong);
                intent.putExtra("audiovolume", audiovolume);
                intent.putExtra("audiodata", audiodata);
                startActivityForResult(intent, request_exp1_2_Code);
            }
        });
        */

        /*
        exp2_2Button = findViewById(R.id.exp22Button);
        //exp2_2Button.setEnabled(false);
        exp2_2Button.setOnClickListener(new Button.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), Experiment2_2.class);
                intent.putExtra("id", userID);
                intent.putExtra("ampweak", ampweak);
                intent.putExtra("ampstrong", ampstrong);
                intent.putExtra("eqweak", eqweak);
                intent.putExtra("eqstrong", eqstrong);
                intent.putExtra("audiovolume", audiovolume);
                intent.putExtra("audiodata", audiodata);
                startActivityForResult(intent, request_exp2_2_Code);
            }
        });
        */
        calibrationButton = findViewById(R.id.start_CalibButton);
        calibrationButton.setEnabled(true);
        calibrationButton.setOnClickListener(new Button.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), Calibration.class);
                intent.putExtra("id", userID);
                intent.putExtra("ampweak", ampweak);
                intent.putExtra("ampstrong", ampstrong);
                intent.putExtra("eqweak", eqweak);
                intent.putExtra("eqstrong", eqstrong);
                intent.putExtra("audiovolume", audiovolume);
                startActivityForResult(intent, request_calib_Code);
            }
        });

        familButton = findViewById(R.id.familButton);
        familButton.setEnabled(false);
        familButton.setOnClickListener(new Button.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), Familization.class);
                intent.putExtra("id", userID);
                intent.putExtra("ampweak", ampweak);
                intent.putExtra("ampstrong", ampstrong);
                intent.putExtra("eqweak", eqweak);
                intent.putExtra("eqstrong", eqstrong);
                intent.putExtra("audiovolume", audiovolume);
                startActivityForResult(intent, request_famil_Code);
            }
        });

        loadButton = findViewById(R.id.Loadbutton);
        loadButton.setEnabled(true);
        loadButton.setOnClickListener(new Button.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), LoadFile.class);
                startActivityForResult(intent, request_fileload_Code);
            }
        });



    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if(resultCode == RESULT_OK) {
            if (requestCode == request_calib_Code) {
                userID = intent.getExtras().getInt("id");
                ampweak = intent.getExtras().getInt("ampweak");
                ampstrong = intent.getExtras().getInt("ampstrong");
                eqweak = intent.getExtras().getIntArray("eqweak");
                eqstrong = intent.getExtras().getIntArray("eqstrong");
                audiovolume = intent.getExtras().getInt("audiovolume");
                TextView uidView = findViewById(R.id.txtIDView);
                uidView.setText("user ID: " + userID);
                if (userID != -1) {
                    familButton.setEnabled(true);
                }
            } else if (requestCode == request_famil_Code) {
                exp1Button.setEnabled(true);
            } else if (requestCode == request_exp1_2_Code) {
                //exp2Button.setEnabled(true);
                MessageBox("Experiment", "Finished 1st day. Thank you.");

            }
            else if (requestCode == request_exp1_1_Code) {
                MessageBox("Experiment", "Click Experiment1-2 button to continue. Thank you.");
                //exp1_2Button.setEnabled(true);
            }
            else if(requestCode == request_fileload_Code) {
                userID = intent.getExtras().getInt("id");
                ampweak = intent.getExtras().getInt("ampweak");
                ampstrong = intent.getExtras().getInt("ampstrong");
                eqweak = intent.getExtras().getIntArray("eqweak");
                eqstrong = intent.getExtras().getIntArray("eqstrong");
                audiovolume = intent.getExtras().getInt("audiovolume");
                exp2Button.setEnabled(true);
                familButton.setEnabled(true);
                TextView uidView = findViewById(R.id.txtIDView);
                uidView.setText("user ID: " + userID);
                MessageBox("Experiment", "Click Familiarization to review your vibration sets for the experiment. Thank you.");
            }else if (requestCode == request_exp2_1_Code) {
                //exp2_2Button.setEnabled(true);
                MessageBox("Experiment", "Finished 1st day. Thank you.");
            }
            else if (requestCode == request_exp2_2_Code) {
                //exp2Button.setEnabled(true);
                MessageBox("Experiment", "It's all set. Thank you for your participation.");
            }

        }
        super.onActivityResult(requestCode, resultCode, intent);
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

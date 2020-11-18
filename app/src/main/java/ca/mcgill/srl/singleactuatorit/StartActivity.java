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
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class StartActivity extends AppCompatActivity {

    protected int userID = -1;
    protected int request_calib_Code = 1001;
    protected int request_famil_Code = 1002;
    protected int request_exp1_Code = 1003;
    protected int request_fileload_Code = 1004;

    protected int[] ampweak = {33, 33, 33, 33, 33, 33};
    protected int[] ampstrong = {66, 66, 66, 66, 66, 66};
    protected int audiovolume = 50;
    protected short[] audiodata;
    protected Button exp1Button;
    protected Button exp2Button;
    protected Button calibrationButton;
    protected Button familButton;
    protected Button loadButton;

    final int SAMPLING_RATE = 12000;
    final int LONGEST_TIME_FRAME = 3200;

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
                Intent intent = new Intent(getApplicationContext(), Experiment1.class);
                intent.putExtra("id", userID);
                intent.putExtra("ampweak", ampweak);
                intent.putExtra("ampstrong", ampstrong);
                intent.putExtra("audiovolume", audiovolume);
                intent.putExtra("audiodata", audiodata);
                startActivityForResult(intent, request_exp1_Code);
            }
        });

        exp2Button = findViewById(R.id.exp2Button);
        exp2Button.setEnabled(false);
        exp2Button.setOnClickListener(new Button.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), Experiment2.class);
                intent.putExtra("id", userID);
                intent.putExtra("ampweak", ampweak);
                intent.putExtra("ampstrong", ampstrong);
                intent.putExtra("audiovolume", audiovolume);
                intent.putExtra("audiodata", audiodata);
                startActivity(intent);
            }
        });

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
                intent.putExtra("audiovolume", audiovolume);
                intent.putExtra("audiodata", audiodata);
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
                intent.putExtra("audiovolume", audiovolume);
                intent.putExtra("audiodata", audiodata);
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
                ampweak = intent.getExtras().getIntArray("ampweak");
                ampstrong = intent.getExtras().getIntArray("ampstrong");
                audiovolume = intent.getExtras().getInt("audiovolume");
                TextView uidView = findViewById(R.id.txtIDView);
                uidView.setText("user ID: " + userID);
                if (userID != -1) {
                    familButton.setEnabled(true);
                }
            } else if (requestCode == request_famil_Code) {
                exp1Button.setEnabled(true);
            } else if (requestCode == request_exp1_Code) {
                //exp2Button.setEnabled(true);
                Toast.makeText(this.getApplicationContext(),"Finished 1st day", Toast.LENGTH_LONG);

            } else if(requestCode == request_fileload_Code) {
                userID = intent.getExtras().getInt("id");
                ampweak = intent.getExtras().getIntArray("ampweak");
                ampstrong = intent.getExtras().getIntArray("ampstrong");
                audiovolume = intent.getExtras().getInt("audiovolume");
                exp2Button.setEnabled(true);
                TextView uidView = findViewById(R.id.txtIDView);
                uidView.setText("user ID: " + userID);
            }

        }
        super.onActivityResult(requestCode, resultCode, intent);
    }
}

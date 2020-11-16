package ca.mcgill.srl.singleactuatorit;

import android.content.Context;
import android.icu.text.SimpleDateFormat;
import android.util.Log;
import android.widget.Toast;

import java.io.*;
import java.util.Date;

public class Logger {
    File file;
    FileOutputStream foStream;
    Context context;
    Logger(String filename, Context c) {
        try {
            foStream = new FileOutputStream(filename);
            Toast.makeText(c, "File Created", Toast.LENGTH_SHORT).show();
            context = c;
            //Log.e("Logger", "File created: " + file.getName());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void WriteMessage(String string, boolean timestamp) {
        try {
            if(timestamp)    {
                SimpleDateFormat sdf = new SimpleDateFormat("yy-MM-dd HH:mm:ss.SSS");
                Date time = new Date();
                String time1 = sdf.format(time) + "\t";
                byte[] strDate = time1.getBytes();
                foStream.write(strDate);
            }
            String output = string + "\n";
            byte[] strToBytes = output.getBytes();
            foStream.write(strToBytes);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void Close() {
        try {
            foStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void WriteArray(int[] Array, boolean timestamp, boolean bracket) {
        String text;
        if(bracket) {
            text = "[" + Array[0];
            for (int i = 1; i < Array.length; i++) {
                text += "," + Array[i];
            }
            text += "]";
        }
        else    {
            text = "" + Array[0];
            for (int i = 1; i < Array.length; i++) {
                text += "," + Array[i];
            }
        }
        byte[] ArrToBytes = text.getBytes();
        try {
            if(timestamp)    {
                SimpleDateFormat sdf = new SimpleDateFormat("yy-MM-dd HH:mm:ss.SSS");
                Date time = new Date();
                String time1 = sdf.format(time) + "\t";
                byte[] strDate = time1.getBytes();
                foStream.write(strDate);
            }
            foStream.write(ArrToBytes);
            foStream.write('\n');
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

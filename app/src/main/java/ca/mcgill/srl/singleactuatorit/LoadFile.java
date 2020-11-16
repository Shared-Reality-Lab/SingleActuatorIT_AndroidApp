package ca.mcgill.srl.singleactuatorit;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class LoadFile extends AppCompatActivity {

    //Get the text file
    //File file = new File(sdcard,"file.txt");


    private TextView textView;
    private ListView listView;
    private ArrayAdapter<String> listAdapter;
    private ArrayList<String> items;
    private Button btnConfirm;
    private Button btnCancel;
    private String rootPath = "";
    private String nextPath = "";
    private String prevPath = "";
    private String currentPath = "";
    private TextView messageView;

    Intent resIntent;
    String filename;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loadfile);
        textView = findViewById(R.id.filetextView);
        listView = findViewById(R.id.filelistView);
        btnConfirm = findViewById(R.id.filebtnConfirm);
        btnCancel = findViewById(R.id.filebtnCancel);

        items = new ArrayList<>();
        listAdapter = new ArrayAdapter<String>(LoadFile.this,
                android.R.layout.simple_list_item_1, items);

        rootPath = Environment.getExternalStorageDirectory().getAbsolutePath();
        rootPath = rootPath + "/singleit/calib/";
        boolean result = Init(rootPath);

        if ( result == false )
            return;

        listView.setAdapter(listAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //Log.d("File_selection", position + " : " + items.get(position));
                //currentPath = textView.getText().toString();
                String path = items.get(position);
                Log.d("File_selection", path);
                currentPath = rootPath + path;
                textView.setText(path);
                if (path.equals("..")) {
                    prevPath(path);
                } else {
                    nextPath(path);
                }
            }
        });

        btnCancel.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(RESULT_CANCELED, resIntent);
                finish();
                return;
            }

        });

        btnConfirm.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                //load the file
                int[] weakArray = new int[6];
                int[] strongArray = new int[6];
                int ruID = 0;
                int audiovol = 0;
                File file = new File(currentPath);
                FileReader fr = null;
                BufferedReader bufrd = null;
                String ret = "";
                try {
                    fr = new FileReader(file);
                    bufrd = new BufferedReader(fr);
                    String receiveString = "";
                    //uID read
                    receiveString = bufrd.readLine();
                    Log.e("File", receiveString);
                    ruID = Integer.parseInt(receiveString);
                    receiveString = bufrd.readLine();
                    //stringBuilder.append(receiveString);
                    String[] raw = receiveString.split(",");
                    Log.e ("File", Integer.toString(raw.length));
                    for (int i = 0; i < raw.length; i++) {
                        weakArray[i] = Integer.parseInt(raw[i]);
                    }
                    Log.e("File", receiveString);
                    receiveString = bufrd.readLine();
                    //stringBuilder.append(receiveString);
                    raw = receiveString.split(",");
                    for (int i = 0; i < raw.length; i++) {
                        strongArray[i] = Integer.parseInt(raw[i]);
                    }
                    Log.e("File", receiveString);
                    receiveString = bufrd.readLine();
                    audiovol = Integer.parseInt(receiveString);
                    Log.e("File", receiveString);

                    bufrd.close();
                    fr.close();
                    //ret = stringBuilder.toString();
                    }
                catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }


                //converting into array
                resIntent = new Intent();
                resIntent.putExtra("id", ruID);
                resIntent.putExtra("ampweak", weakArray);
                resIntent.putExtra("ampstrong", strongArray);
                resIntent.putExtra("audiovolume", audiovol);

                setResult(RESULT_OK, resIntent);
                finish();
            }

        });

    }

    public boolean Init(String rootPath)    {
        File fileRoot = new File(rootPath);
        if(fileRoot.isDirectory() == false)        {
            //Toast.makeText(LoadFile.this, "Not Directory" , Toast.LENGTH_SHORT).show();
            return false;
        }
        textView.setText(rootPath);

        String[] fileList = fileRoot.list();
        if ( fileList == null )        {
            Toast.makeText(LoadFile.this, "Could not find List" , Toast.LENGTH_SHORT).show();
            return false;
        }

        items.clear();

        items.add(".."); // previous folder
        for ( int i = 0; i < fileList.length; i++ )        {
            items.add(fileList[i]);
        }
        listAdapter.notifyDataSetChanged();
        return true;
    }

    public void nextPath(String str)    {
        prevPath = currentPath;

        // add '/'
        nextPath = currentPath + "/" + str;
        File file = new File(nextPath);
        if ( file.isDirectory() == false )        {
            //Toast.makeText(LoadFile.this, "Not Directory" , Toast.LENGTH_SHORT).show();
            return;
        }

        String[] fileList = file.list();
        items.clear();
        items.add("..");

        for ( int i = 0; i < fileList.length; i++ )        {
            items.add(fileList[i]);
        }

        textView.setText(nextPath);
        listAdapter.notifyDataSetChanged();

    }

    public void prevPath(String str)    {
        nextPath = currentPath;
        prevPath = currentPath;

        int lastSlashPosition = prevPath.lastIndexOf("/");

        prevPath = prevPath.substring(0, lastSlashPosition);
        File file = new File(prevPath);

        if ( file.isDirectory() == false)        {
            //Toast.makeText(LoadFile.this, "Not a Directory" , Toast.LENGTH_SHORT).show();
            return;
        }

        String[] fileList = file.list();
        items.clear();
        items.add("..");

        for( int i = 0; i < fileList.length; i++ )        {
            items.add(fileList[i]);
        }

        textView.setText(prevPath);
        listAdapter.notifyDataSetChanged();
    }

}

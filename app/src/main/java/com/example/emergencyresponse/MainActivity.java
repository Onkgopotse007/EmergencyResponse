package com.example.emergencyresponse;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.ComponentName;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.AudioManager;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    Button volumeDown;
    Timer timer = new Timer();
    int i = 0;
    BootDeviceReceiver bootDeviceReceiver = null;
    float timeBetweenPresses = 0;
    float totalTimeBetweenPresses = 0;
    long totalTime = 0L;
    long endTime = 0L;
    AutoCompleteTextView dropdownListView;
    TextInputLayout dropDownListLayout;


    String delNumber;
    DatabaseHelper myDB;
    int phoneIndex = 1;
    Button saveButton, deleteButton;
    TextView names, cellNumber;
    TableLayout contactTable;
    int noRows;
    Spinner contactDelete;
    String currentItem;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //dropDownListLayout = (TextInputLayout) findViewById(R.id.dropLayout);
       // dropdownListView = findViewById(R.id.selections);
        saveButton = findViewById(R.id.saveButton);
        deleteButton = findViewById(R.id.deleteButton);
        names = findViewById(R.id.editName);
        cellNumber = findViewById(R.id.editTextPhone);
        contactTable = findViewById(R.id.contactTable);
        contactDelete=findViewById(R.id.selectContact);
        String[] nameCollection = new String[6];
        String[] options1= new String[6];
        ArrayList<String> options = new ArrayList<String>(8);
        options.add("Select Contact");

        myDB = new DatabaseHelper(this);
        IntentFilter intentFilter = new IntentFilter();

        intentFilter.addAction("android.intent.action.ACTION_LOCKED_BOOT_COMPLETED");
        intentFilter.addAction("android.intent.action.ACTION_BOOT_COMPLETED");
        startService(new Intent(this, AutoStartService.class));
        // Register the broadcast receiver with the intent filter object
        registerReceiver(bootDeviceReceiver, intentFilter);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED&&ActivityCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.SEND_SMS}, 1);

        }
        Cursor res = myDB.getData();
        noRows=res.getCount();
        if (res.getCount() == 0) {
            Boolean insertFirst = myDB.initialInsert();
            Toast.makeText(MainActivity.this, "No Data exists", Toast.LENGTH_SHORT).show();
            if (insertFirst) {
                Toast.makeText(MainActivity.this, "police contact added", Toast.LENGTH_SHORT).show();
            }
            restartApp();
        }
        if (res.getCount() > 0) {
            while (res.moveToNext()) {
                nameCollection[phoneIndex] = res.getString(0);
                options.add(res.getString(1));
                options1[phoneIndex]=res.getString(1);
                phoneIndex++;
            }

        }

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, com.google.android.material.R.layout.support_simple_spinner_dropdown_item, options);
        arrayAdapter.setDropDownViewResource(androidx.appcompat.R.layout.support_simple_spinner_dropdown_item);
        contactDelete.setAdapter(arrayAdapter);
        contactDelete.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                delNumber=contactDelete.getSelectedItem().toString();

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });



        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!names.getText().toString().isEmpty() && !cellNumber.getText().toString().isEmpty()&&res.getCount()<6) {
                    String nameText = names.getText().toString();
                    String cellText = cellNumber.getText().toString();
                    Boolean insertContact = myDB.insertContact(nameText, cellText);
                    if (insertContact == true) {
                        names.setText("");
                        cellNumber.setText("");
                        restartApp();
                        phoneIndex = 0;
                    } else {
                        Toast.makeText(MainActivity.this, "Failed to insert contact info", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(MainActivity.this, "Fields are empty or you have 5 emergency contacts already", Toast.LENGTH_SHORT).show();
                }
            }
        });
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!delNumber.equals("74636354")&&!delNumber.equalsIgnoreCase("Select Contact")) {
                    myDB.deleteContact(delNumber);
                    restartApp();
                    phoneIndex = 0;
                } else {
                    Toast.makeText(MainActivity.this, "cannot delete Police number", Toast.LENGTH_SHORT).show();
                }
            }
        });





        TextView[] firstColumnInfo = new TextView[nameCollection.length];

        TextView[] secondColumnInfo = new TextView[options1.length];
        TableRow[] fillRows = new TableRow[options1.length];
        for (i = 0; i < nameCollection.length; i++) {
            fillRows[i] = new TableRow(this);
            fillRows[i].setId(i + 1);
            fillRows[i].setLayoutParams(new TableLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                    )
            );
            firstColumnInfo[i] = new TextView(this);
            firstColumnInfo[i].setId(i + 111);
            firstColumnInfo[i].setText(nameCollection[i]);
            firstColumnInfo[i].setPadding(40, 5, 5, 5);
            fillRows[i].addView(firstColumnInfo[i]);

            secondColumnInfo[i] = new TextView(this);
            secondColumnInfo[i].setId(i + 111);
            secondColumnInfo[i].setText(options1[i]);
            secondColumnInfo[i].setPadding(300, 5, 5, 5);
            fillRows[i].addView(secondColumnInfo[i]);
            contactTable.addView(fillRows[i], new TableLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            ));
        }


    }

    public void restartApp() {
        Intent i = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(i);
        finish();
    }

    /*@Override
    public boolean onKeyDown(int keyCode, KeyEvent event){
       if (keyCode != KeyEvent.KEYCODE_VOLUME_DOWN){
           timer.stop();
           i=0;
           totalTime=0;
       }if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN && timer.stopWatchRunning){
           timer.stop();
           i++;
           endTime =timer.getElapsedMilliseconds();
           totalTime += endTime;
            System.out.println("Number of presses= "+i);
            System.out.println("Time between presses= "+totalTime);
       }if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN && !timer.stopWatchRunning){
           timer.start();
           i++;
       }
        return super.onKeyDown(keyCode, event);
    }
    @Override
    protected void  onStart(){
        super.onStart();
        IntentFilter filter = new IntentFilter(Intent.ACTION_MEDIA_BUTTON);
        registerReceiver(sendAlert, filter);
    }
    @Override
    protected void onStop(){
        super.onStop();
        unregisterReceiver(sendAlert);
    }
    @Override
    protected void onDestroy(){
        super.onDestroy();
        //unregister screenonoff receiver
        if(sendAlert!=null){
            unregisterReceiver(sendAlert);
            Log.d(SendAlert.SCREEN_TOGGLE_TAG,"onDestroy: screenOnOff Receiver is unregistered");
        }
    }
    @Override
    protected void onPause(){

        //Screen about to turn off
        if(SendAlert.screenOn){
            timer.start();
            i++;
            System.out.println("number of presses "+i);
        }
        if(!SendAlert.screenOn){
            timer.start();
            i++;
            System.out.println("number of presses "+i);
        }

            timer.stop();
            timeBetweenPresses= timer.getElapsedMilliseconds();
            totalTimeBetweenPresses += timeBetweenPresses;
            System.out.println("Time between all presses "+totalTimeBetweenPresses);
        super.onPause();
    }
    @Override
    protected void onResume(){
        timer.stop();
        timeBetweenPresses= timer.getElapsedMilliseconds();
        totalTimeBetweenPresses += timeBetweenPresses;
        System.out.println("Time between all presses "+totalTimeBetweenPresses);
        //Screen turning on
        if(!SendAlert.screenOn){
            timer.start();
            i++;
            System.out.println("number of presses "+i);
        }
        else {

        }
        super.onResume();
    }

    */
}
package com.example.emergencyresponse;



import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class StopAlert extends AppCompatActivity {
    Button stopper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stop_alert);
        stopper = findViewById(R.id.stopButton);
        stopper.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startService(new Intent(StopAlert.this, AutoStartService.class));
                System.out.println("Pressed stop");
            }
        });
    }
}
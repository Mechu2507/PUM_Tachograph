package com.example.tirowka;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.tirowka.gps.GPSTracking;
import com.example.tirowka.timer.Timer;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Button gpsButton = findViewById(R.id.gps);
        final Button timerButton = findViewById(R.id.timer);

        gpsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, GPSTracking.class);
                startActivity(intent);
            }
        });

        timerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, Timer.class);
                startActivity(intent);
            }
        });

    }
}
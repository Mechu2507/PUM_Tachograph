package com.example.tirowka.timer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.provider.Settings;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.tirowka.R;

import java.util.Locale;

public class Timer extends AppCompatActivity {

    private EditText et_input;
    private TextView tv_countdown;
    private Button btn_start_pause, btn_reset, btn_set, btn_start, btn_pause, btn_stop;
    static TextView tv_distance, tv_time;

    private CountDownTimer countDownTimer;

    private boolean timerRunning;
    static boolean status;

    static long startTime, finishTime;
    private long startTimeInMillis;
    private long timeLeftInMillis;
    private long endTime;

    static ProgressDialog progressDialog;
    static int p = 0;

    LocationService myService;
    LocationManager locationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timer);

        et_input = findViewById(R.id.et_input);
        tv_countdown = findViewById(R.id.tv_countdown);
        btn_start_pause = findViewById(R.id.btn_start_pause);
        btn_reset = findViewById(R.id.btn_reset);
        btn_set = findViewById(R.id.btn_set);
        tv_distance = findViewById(R.id.tv_distance);
        tv_time = findViewById(R.id.tv_time);
        btn_start = findViewById(R.id.btn_start);
        btn_pause = findViewById(R.id.btn_pause);
        btn_stop = findViewById(R.id.btn_stop);

        btn_set.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String input = et_input.getText().toString();
                if (input.length() == 0){
                    Toast.makeText(Timer.this, "Field can't be empty", Toast.LENGTH_SHORT).show();
                    return;
                }

                long millisInput = Long.parseLong(input) * 60000;
                if (millisInput == 0){
                    Toast.makeText(Timer.this, "Please enter right number", Toast.LENGTH_SHORT).show();
                    return;
                }

                setTime(millisInput);
                et_input.setText("");
            }
        });

        btn_start_pause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (timerRunning){
                    pauseTimer();
                } else {
                    startTimer();
                }
            }
        });

        btn_reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resetTimer();
            }
        });


        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED ){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                }, 1000);
            }
        }

        btn_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkGPS();
                locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
                if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
                    return;
                if (status == false)
                    bindService();
                progressDialog = new ProgressDialog(Timer.this);
                progressDialog.setIndeterminate(true);
                progressDialog.setCancelable(false);
                progressDialog.setMessage("GettingLocation...");
                progressDialog.show();

                btn_start.setVisibility(View.GONE);
                btn_pause.setVisibility(View.VISIBLE);
                btn_stop.setVisibility(View.VISIBLE);
            }
        });

        btn_pause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (btn_pause.getText().toString().equalsIgnoreCase("pause")){
                    btn_pause.setText("Resume");
                    p = 1;
                } else if (btn_pause.getText().toString().equalsIgnoreCase("resume")){
                    checkGPS();
                    locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
                    if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
                        return;
                    btn_pause.setText("Pause");
                    p = 0;
                }
            }
        });

        btn_stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (status == true)
                    unbindService();
                btn_start.setVisibility(View.VISIBLE);
                btn_pause.setText("Pause");
                btn_pause.setVisibility(View.GONE);
                btn_stop.setVisibility(View.GONE);
            }
        });
    }

    private void setTime(long milliseconds){
        startTimeInMillis = milliseconds;
        resetTimer();
        closeKeyboard();
    }

    private void startTimer(){

        endTime = System.currentTimeMillis() + timeLeftInMillis;

        countDownTimer = new CountDownTimer(timeLeftInMillis, 1000){

            @Override
            public void onTick(long l) {
                timeLeftInMillis = l;
                updateCountDownText();
            }

            @Override
            public void onFinish() {
                timerRunning = false;
                updateButtons();
            }
        }.start();

        timerRunning = true;
        updateButtons();
    }

    private void pauseTimer(){
        countDownTimer.cancel();
        timerRunning = false;
        updateButtons();
    }

    private void resetTimer(){
        timeLeftInMillis = startTimeInMillis;
        updateCountDownText();
        updateButtons();
    }

    private void updateCountDownText(){
        int hours = (int) (timeLeftInMillis / 1000) / 3600;
        int minutes = (int) ((timeLeftInMillis / 1000) % 3600) / 60;
        int seconds = (int) (timeLeftInMillis / 1000) % 60;

        String timeLeftFormatted;
        if (hours > 0){
            timeLeftFormatted = String.format(Locale.getDefault(),
                    "%d:%02d:%02d", hours, minutes, seconds);
        } else {
            timeLeftFormatted = String.format(Locale.getDefault(),"%02d:%02d", minutes, seconds);
        }

        tv_countdown.setText(timeLeftFormatted);
    }

    private void updateButtons(){
        if (timerRunning){
            et_input.setVisibility(View.INVISIBLE);
            btn_set.setVisibility(View.INVISIBLE);
            btn_reset.setVisibility(View.INVISIBLE);
            btn_start_pause.setText("Pause");
        } else {
            et_input.setVisibility(View.VISIBLE);
            btn_set.setVisibility(View.VISIBLE);
            btn_start_pause.setText("Start");
            if (timeLeftInMillis < 1000) {
                btn_start_pause.setVisibility(View.INVISIBLE);
            } else {
                btn_start_pause.setVisibility(View.VISIBLE);
            }

            if (timeLeftInMillis < startTimeInMillis){
                btn_reset.setVisibility(View.VISIBLE);
            } else {
                btn_reset.setVisibility(View.INVISIBLE);
            }
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong("millisLeft", timeLeftInMillis);
        outState.putBoolean("timerRunning", timerRunning);
        outState.putLong("endTime", endTime);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        timeLeftInMillis = savedInstanceState.getLong("millisLeft");
        timerRunning = savedInstanceState.getBoolean("timerRunning");
        updateCountDownText();
        updateButtons();

        if (timerRunning){
            endTime = savedInstanceState.getLong("endTime");
            timeLeftInMillis = endTime - System.currentTimeMillis();
            startTimer();
        }
    }

    private void closeKeyboard(){
        View view = this.getCurrentFocus();
        if (view != null){
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        editor.putLong("startTimeInMillis", startTimeInMillis);
        editor.putLong("millisLeFt", timeLeftInMillis);
        editor.putBoolean("timerRunning", timerRunning);
        editor.putLong("endTime", endTime);

        editor.apply();

        if (countDownTimer != null){
            countDownTimer.cancel();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE);

        startTimeInMillis = prefs.getLong("startTimeInMillis", 600000);
        timeLeftInMillis = prefs.getLong("millisLeft", startTimeInMillis);
        timerRunning = prefs.getBoolean("timerRunning", false);

        updateCountDownText();
        updateButtons();

        if (timerRunning){
            endTime = prefs.getLong("endTime", 0);
            timeLeftInMillis = endTime - System.currentTimeMillis();

            if (timeLeftInMillis < 0){
                timeLeftInMillis = 0;
                timerRunning = false;
                updateCountDownText();
                updateButtons();
            } else {
                startTimer();
            }
        }
    }
//
//    _______________________________________
//    Speedometer
//
    private ServiceConnection sc = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            LocationService.LocalBinder binder = (LocationService.LocalBinder)iBinder;
            myService = binder.getService();
            status = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            status = false;
        }
    };

    @Override
    protected void onDestroy() {
        if (status == true)
            unbindService();
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        if (status == false)
            super.onBackPressed();
        else
            moveTaskToBack(true);
    }

    private void unbindService() {
        if(status == false)
            return;
        Intent i = new Intent(getApplicationContext(), LocationService.class);
        unbindService(sc);
        status = false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1000: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    Toast.makeText(this, "GRANTED", Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(this, "DENIED", Toast.LENGTH_SHORT).show();
            }
            return;
        }
    }

    private void checkGPS() {
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
            showGPSDisabledAlert();
    }

    private void showGPSDisabledAlert() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage("Enable GPS to use application")
                .setCancelable(false)
                .setPositiveButton("Enable GPS", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivity(intent);
                    }
                });
        alertDialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });
        AlertDialog alert = alertDialogBuilder.create();
        alert.show();
    }

    private void bindService() {
        if (status == true)
            return;
        Intent i = new Intent(getApplicationContext(), LocationService.class);
        bindService(i, sc, BIND_AUTO_CREATE);
        status = true;
        startTime = System.currentTimeMillis();
    }
}
package com.example.tirowka.timer;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.tirowka.R;

import java.util.Locale;

public class Timer extends AppCompatActivity {
    private static final long START_TIME_IN_MILLIS = 600000;

    private TextView tv_countdown;
    private Button btn_start_pause, btn_reset;

    private CountDownTimer countDownTimer;

    private boolean timerRunning;

    private long timeLeftInMillis = START_TIME_IN_MILLIS;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timer);

        tv_countdown = findViewById(R.id.tv_countdown);
        btn_start_pause = findViewById(R.id.btn_start_pause);
        btn_reset = findViewById(R.id.btn_reset);

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

        updateCountDownText();
    }

    private void startTimer(){
        countDownTimer = new CountDownTimer(timeLeftInMillis, 1000){

            @Override
            public void onTick(long l) {
                timeLeftInMillis = l;
                updateCountDownText();
            }

            @Override
            public void onFinish() {
                timerRunning = false;
                btn_start_pause.setText("Start");
                btn_start_pause.setVisibility(View.INVISIBLE);
                btn_reset.setVisibility(View.VISIBLE);
            }
        }.start();

        timerRunning = true;
        btn_start_pause.setText("pause");
        btn_reset.setVisibility(View.INVISIBLE);
    }

    private void pauseTimer(){
        countDownTimer.cancel();
        timerRunning = false;
        btn_start_pause.setText("start");
        btn_reset.setVisibility(View.VISIBLE);
    }

    private void resetTimer(){
        timeLeftInMillis = START_TIME_IN_MILLIS;
        updateCountDownText();
        btn_reset.setVisibility(View.INVISIBLE);
        btn_start_pause.setVisibility(View.VISIBLE);
    }

    private void updateCountDownText(){
        int minutes = (int) (timeLeftInMillis / 1000) / 60;
        int seconds = (int) (timeLeftInMillis / 1000) % 60;

        String timeLeftFormatted = String.format(Locale.getDefault(),"%02d:%02d", minutes, seconds);

        tv_countdown.setText(timeLeftFormatted);
    }
}
package com.shageldi.intercom;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

public class PttScan extends AppCompatActivity {
    ProgressBar ProgressBar;
    TextView intro;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ptt_scan);

        ProgressBar=findViewById(R.id.ProgressBar);
        intro=findViewById(R.id.intro);

        findViewById(R.id.yza).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN || keyCode==KeyEvent.KEYCODE_VOLUME_UP){
            Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
// Vibrate for 500 milliseconds
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                v.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                //deprecated in API 26
                v.vibrate(500);
            }
           ProgressBar.setVisibility(View.INVISIBLE);
           intro.setText("Ses düwmesi hasaba alyndy");
            SharedPreferences.Editor editor = getSharedPreferences("ptt_volume", MODE_PRIVATE).edit();
            editor.putString("ptt_volume", "1");
            editor.apply();
        }
        if(keyCode==KeyEvent.KEYCODE_HEADSETHOOK) {
            Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
// Vibrate for 500 milliseconds
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                v.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                //deprecated in API 26
                v.vibrate(500);
            }
            ProgressBar.setVisibility(View.INVISIBLE);
            intro.setText("Aýtdyr / Duruz düwmesi hasaba alyndy");
            SharedPreferences.Editor editor = getSharedPreferences("play_pause", MODE_PRIVATE).edit();
            editor.putString("play_pause", "1");
            editor.apply();

        }

        return super.onKeyDown(keyCode, event);
    }
}
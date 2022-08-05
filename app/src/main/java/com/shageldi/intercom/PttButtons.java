package com.shageldi.intercom;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class PttButtons extends AppCompatActivity {
    ImageButton stop,stop2;
    RelativeLayout two,one;
    String play_pause_str="1",volume_str="1";
    FloatingActionButton fab;
    TextView supported_buttons;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ptt_buttons);
        stop=findViewById(R.id.stop);
        stop2=findViewById(R.id.stop2);
        one=findViewById(R.id.one);
        two=findViewById(R.id.two);
        supported_buttons=findViewById(R.id.supported_buttons);

        supported_buttons.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(PttButtons.this,SupportedButtons.class));
            }
        });

        fab=findViewById(R.id.fab);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(PttButtons.this,PttScan.class));
            }
        });

        SharedPreferences share = getSharedPreferences("play_pause", Activity.MODE_PRIVATE);
        play_pause_str = share.getString("play_pause", "");

        SharedPreferences share2 = getSharedPreferences("ptt_volume", Activity.MODE_PRIVATE);
        volume_str = share2.getString("ptt_volume", "");

        if(play_pause_str.equals("0")){
            one.setVisibility(View.GONE);
        } else{
            one.setVisibility(View.VISIBLE);
        }

        if(volume_str.equals("0")){
            two.setVisibility(View.GONE);
        } else{
            two.setVisibility(View.VISIBLE);
        }

        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = getSharedPreferences("play_pause", MODE_PRIVATE).edit();
                editor.putString("play_pause", "0");
                editor.apply();
                one.setVisibility(View.GONE);
            }
        });

        stop2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = getSharedPreferences("ptt_volume", MODE_PRIVATE).edit();
                editor.putString("ptt_volume", "0");
                editor.apply();
                two.setVisibility(View.GONE);
            }
        });

        findViewById(R.id.yza).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences share = getSharedPreferences("play_pause", Activity.MODE_PRIVATE);
        play_pause_str = share.getString("play_pause", "");

        SharedPreferences share2 = getSharedPreferences("ptt_volume", Activity.MODE_PRIVATE);
        volume_str = share2.getString("ptt_volume", "");

        if(play_pause_str.equals("0")){
            one.setVisibility(View.GONE);
        } else{
            one.setVisibility(View.VISIBLE);
        }

        if(volume_str.equals("0")){
            two.setVisibility(View.GONE);
        } else{
            two.setVisibility(View.VISIBLE);
        }
    }
}
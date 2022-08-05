package com.shageldi.intercom;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.audiofx.AcousticEchoCanceler;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;




import static android.provider.Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS;

public class Settings extends AppCompatActivity {
    String username="Android Studio",oldUsername="Android Studio";
    TextView user;
    Context context=this;
    SeekBar quality,volume;
    LinearLayout acousticechocanceler;
    TextView recordaudiofromtv,playastv;
    String recordaudiofromstr="",acousticEchoCancelerstr="",qualitystr="0",playasstr="",volumestr="",autostartstr="";
    Switch acousticEchoCancelersw,autostart;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        SharedPreferences share = getSharedPreferences("Settings", Activity.MODE_PRIVATE);
        username = share.getString("username", "");
        oldUsername = share.getString("username", "");

        SharedPreferences share1 = getSharedPreferences("recordaudiofrom", Activity.MODE_PRIVATE);
        recordaudiofromstr = share1.getString("recordaudiofrom", "");

        SharedPreferences share2 = getSharedPreferences("acousticEchoCanceler", Activity.MODE_PRIVATE);
        acousticEchoCancelerstr = share2.getString("acousticEchoCanceler", "");

        SharedPreferences share3 = getSharedPreferences("playas", Activity.MODE_PRIVATE);
        playasstr = share3.getString("playas", "");

        SharedPreferences share6 = getSharedPreferences("autostart", Activity.MODE_PRIVATE);
        autostartstr = share6.getString("autostart", "");






        user=findViewById(R.id.user);
        quality=findViewById(R.id.quality);
        volume=findViewById(R.id.volume);
        recordaudiofromtv=findViewById(R.id.recordaudiofromtv);
        acousticechocanceler=findViewById(R.id.acousticechocanceler);
        acousticEchoCancelersw=findViewById(R.id.acousticEchoCancelersw);
        playastv=findViewById(R.id.playastv);
        autostart=findViewById(R.id.autostart);
        user.setText(username);



        SharedPreferences share4 = getSharedPreferences("volume", Activity.MODE_PRIVATE);
        volumestr = share4.getString("volume", "");
        if(!volumestr.isEmpty()){
            volume.setProgress(Integer.parseInt(volumestr));
        } else{
            volume.setProgress(0);
        }


        SharedPreferences share5 = getSharedPreferences("quality", Activity.MODE_PRIVATE);
        qualitystr = share5.getString("quality", "");
        if(!qualitystr.isEmpty()){
            quality.setProgress(Integer.parseInt(qualitystr));
        } else{
            quality.setProgress(0);
        }



        if(recordaudiofromstr.equals("1") || recordaudiofromstr.isEmpty()){

            recordaudiofromtv.setText("Aragatnaşyk çeşmesi (Esasy)");
        } else if(recordaudiofromstr.equals("2")){
            recordaudiofromtv.setText("Mikrofon");
            acousticechocanceler.setVisibility(View.VISIBLE);
        }

        if(playasstr.equals("1") || playasstr.isEmpty()){

            playastv.setText("Jaňdaky ses");
        } else if(playasstr.equals("2")){
            playastv.setText("Saz sesi");
        }

        if(acousticEchoCancelerstr.equals("1") || acousticEchoCancelerstr.isEmpty()){
              acousticEchoCancelersw.setChecked(false);
        } else if(recordaudiofromstr.equals("2")){
            acousticEchoCancelersw.setChecked(true);
        }

        findViewById(R.id.email).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_SEND);
                i.setType("message/rfc822");
                i.putExtra(Intent.EXTRA_EMAIL  , new String[]{"sh.alyyew2019@gmail.com"});
                i.putExtra(Intent.EXTRA_SUBJECT, "subject of email");
                i.putExtra(Intent.EXTRA_TEXT   , "body of email");
                try {
                    startActivity(Intent.createChooser(i, "Send mail..."));
                } catch (android.content.ActivityNotFoundException ex) {
                    Toast.makeText(Settings.this, "There are no email clients installed.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        if(autostartstr.equals("1") || autostartstr.isEmpty()){
            autostart.setChecked(false);
        } else if(autostartstr.equals("2")){
            autostart.setChecked(true);
        }

        autostart.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    SharedPreferences.Editor editor = getSharedPreferences("autostart", MODE_PRIVATE).edit();
                    editor.putString("autostart", "2");
                    editor.apply();
                    //    Toast.makeText(context, ""+isChecked, Toast.LENGTH_SHORT).show();
                } else{
                    SharedPreferences.Editor editor = getSharedPreferences("autostart", MODE_PRIVATE).edit();
                    editor.putString("autostart", "1");
                    editor.apply();
                }
            }
        });


        acousticEchoCancelersw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    SharedPreferences.Editor editor = getSharedPreferences("acousticEchoCanceler", MODE_PRIVATE).edit();
                    editor.putString("acousticEchoCanceler", "2");
                    editor.apply();
                //    Toast.makeText(context, ""+isChecked, Toast.LENGTH_SHORT).show();
                } else{
                    SharedPreferences.Editor editor = getSharedPreferences("acousticEchoCanceler", MODE_PRIVATE).edit();
                    editor.putString("acousticEchoCanceler", "1");
                    editor.apply();
                }
            }
        });

        findViewById(R.id.ptt).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Settings.this,PttButtons.class));
            }
        });

        quality.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                SharedPreferences.Editor editor = getSharedPreferences("quality", MODE_PRIVATE).edit();
                editor.putString("quality", progress+"");
                editor.apply();

               // Toast.makeText(context, ""+progress, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        final MediaPlayer mediaPlayer = MediaPlayer.create(Settings.this,R.raw.start_talk);


        volume.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                SharedPreferences.Editor editor = getSharedPreferences("volume", MODE_PRIVATE).edit();
                editor.putString("volume", progress+"");
                editor.apply();

                AudioManager audioManager = (AudioManager)getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
                 audioManager.adjustVolume(progress, AudioManager.FLAG_PLAY_SOUND);
                mediaPlayer.setVolume(progress,progress);
                mediaPlayer.start();
                // audioManager.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
//                audioManager.adjustStreamVolume(
//                        AudioManager.STREAM_VOICE_CALL, AudioManager.ADJUST_MUTE, /* flags= */ 0);
                audioManager.setSpeakerphoneOn(true);

                //Toast.makeText(context, "", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        findViewById(R.id.username).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(Settings.this);
                alertDialog.setTitle("Ulanyjy ady");

                View input= LayoutInflater.from(Settings.this).inflate(R.layout.edit_text,null,false);
                final EditText editText=input.findViewById(R.id.edit);
                alertDialog.setView(input);
                alertDialog.setIcon(R.drawable.ic_outline_person_24);
                editText.setText(username);

                alertDialog.setPositiveButton("Howwa",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {

                                if(!editText.getText().toString().trim().isEmpty()){

                                    username = editText.getText().toString();
                                    SharedPreferences.Editor editor = getSharedPreferences("Settings", MODE_PRIVATE).edit();
                                    editor.putString("username", username);
                                    editor.apply();
                                    user.setText(username);

                                    Intent serviceIntent = new Intent(Settings.this, CallService.class);
                                    serviceIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    stopService(serviceIntent);

                                    startService(serviceIntent);



//                                    Intent serviceIntent=new Intent(Settings.this,ExampleService.class);
//                                    stopService(serviceIntent);
//                                    String string="OK";
//                                    //  Intent serviceIntent=new Intent(context,ExampleService.class);
//                                    serviceIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                                    serviceIntent.putExtra("inputExtra",string);
//                                    context.startService(serviceIntent);



//                                    contactManager.bye(username);
//                                    contactManager.stopBroadcasting();
//                                    contactManager.stopListening();




                                }

                            }
                        });

                alertDialog.setNegativeButton("Ýok",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });

                alertDialog.show();
            }
        });

        findViewById(R.id.recordaudiofrom).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final AlertDialog.Builder alertDialog = new AlertDialog.Builder(Settings.this);
                alertDialog.setTitle("Ses ýagysynyň çeşmesi...");
                SharedPreferences share = getSharedPreferences("recordaudiofrom", Activity.MODE_PRIVATE);
                recordaudiofromstr = share.getString("recordaudiofrom", "");
                View input= LayoutInflater.from(Settings.this).inflate(R.layout.radio_button,null,false);
                final RadioButton radioButton=new RadioButton(Settings.this);
                if(recordaudiofromstr.equals("1") || recordaudiofromstr.isEmpty()){
                    radioButton.setChecked(true);
                    recordaudiofromtv.setText("Aragatnaşyk çeşmesi (Esasy)");
                }

                radioButton.setText("Aragatnaşyk çeşmesi (Esasy)");
                radioButton.setTextSize(16f);



                final RadioButton radioButton2=new RadioButton(Settings.this);
                if(recordaudiofromstr.equals("2")){
                    radioButton2.setChecked(true);
                    recordaudiofromtv.setText("Mikrofon");
                }


                radioButton2.setText("Mikrofon");
                radioButton2.setTextSize(16f);



                radioButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        radioButton.setChecked(true);
                        radioButton2.setChecked(false);


                    }
                });

                radioButton2.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        radioButton.setChecked(false);
                        radioButton2.setChecked(true);


                    }
                });


                LinearLayout linearLayout=input.findViewById(R.id.linear);
                linearLayout.addView(radioButton);
                linearLayout.addView(radioButton2);

                alertDialog.setView(input);





                alertDialog.setNegativeButton("Inkär etmek",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });

                alertDialog.setPositiveButton("Ýatda saklatmak", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                         dialog.dismiss();
                         if(radioButton.isChecked()){
                             SharedPreferences.Editor editor = getSharedPreferences("recordaudiofrom", MODE_PRIVATE).edit();
                             editor.putString("recordaudiofrom", "1");
                             editor.apply();
                             acousticechocanceler.setVisibility(View.GONE);
                             SharedPreferences.Editor editor2 = getSharedPreferences("acousticEchoCanceler", MODE_PRIVATE).edit();
                             editor2.putString("acousticEchoCanceler", "2");
                             editor2.apply();
                             recordaudiofromtv.setText("Aragatnaşyk çeşmesi (Esasy)");

                         }
                        if(radioButton2.isChecked()){
                            SharedPreferences.Editor editor = getSharedPreferences("recordaudiofrom", MODE_PRIVATE).edit();
                            editor.putString("recordaudiofrom", "2");
                            editor.apply();
                            acousticechocanceler.setVisibility(View.VISIBLE);
                            recordaudiofromtv.setText("Mikrofon");
                        }

                        Intent serviceIntent = new Intent(Settings.this, CallService.class);
                        serviceIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        stopService(serviceIntent);

                        startService(serviceIntent);

                    }
                });

                alertDialog.show();
            }
        });

        findViewById(R.id.ignore_battery).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    Intent intent = new Intent();
                    String packageName = getPackageName();
                    PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
                    if (!pm.isIgnoringBatteryOptimizations(packageName)) {
                        intent.setAction(ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                        intent.setData(Uri.parse("package:" + packageName));
                        startActivity(intent);
                    }
                }
            }
        });

        findViewById(R.id.playas).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(Settings.this);
                alertDialog.setTitle("Ses nähilli aýtdyrylmasy...");
                SharedPreferences share3 = getSharedPreferences("playas", Activity.MODE_PRIVATE);
                playasstr = share3.getString("playas", "");
                View input= LayoutInflater.from(Settings.this).inflate(R.layout.radio_button,null,false);
                final RadioButton radioButton=new RadioButton(Settings.this);
                if(playasstr.equals("1") || playasstr.isEmpty()){
                    radioButton.setChecked(true);
                    playastv.setText("Jaňdaky ses");
                }
                radioButton.setText("Jaňdaky ses");
                radioButton.setTextSize(16f);



                final RadioButton radioButton2=new RadioButton(Settings.this);


                if(playasstr.equals("2")){
                    radioButton2.setChecked(true);
                    playastv.setText("Saz sesi");
                }
                radioButton2.setText("Saz sesi");
                radioButton2.setTextSize(16f);



                radioButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        radioButton.setChecked(true);
                        radioButton2.setChecked(false);

                    }
                });

                radioButton2.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        radioButton.setChecked(false);
                        radioButton2.setChecked(true);

                    }
                });


                LinearLayout linearLayout=input.findViewById(R.id.linear);
                linearLayout.addView(radioButton);
                linearLayout.addView(radioButton2);

                alertDialog.setView(input);




                alertDialog.setNegativeButton("Inkär etmek",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });

                alertDialog.setPositiveButton("Ýatda saklatmak", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        if(radioButton.isChecked()){
                            SharedPreferences.Editor editor = getSharedPreferences("playas", MODE_PRIVATE).edit();
                            editor.putString("playas", "1");
                            editor.apply();
                            playastv.setText("Jaňdaky ses");




                        }
                        if(radioButton2.isChecked()){
                            SharedPreferences.Editor editor = getSharedPreferences("playas", MODE_PRIVATE).edit();
                            editor.putString("playas", "2");
                            editor.apply();
                            playastv.setText("Saz sesi");
                        }

                        Intent serviceIntent = new Intent(Settings.this, CallService.class);
                        serviceIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        stopService(serviceIntent);

                        startService(serviceIntent);

                    }
                });


                alertDialog.show();
            }
        });




        findViewById(R.id.back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                 onBackPressed();

            }
        });
    }

    @Override
    public void onBackPressed() {
      //  if(oldUsername.equals(username)) {
            super.onBackPressed();
        //    Animatoo.animateSlideRight(context);
//        } else{
//            finish();
//            startActivity(new Intent(Settings.this,MainActivity.class));
//            Animatoo.animateSlideRight(context);
//        }

    }
}
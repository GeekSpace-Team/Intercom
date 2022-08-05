package com.shageldi.intercom;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.google.android.material.navigation.NavigationView;

import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static android.provider.Settings.ACTION_WIFI_SETTINGS;

public class HomePage extends AppCompatActivity implements SensorEventListener {
    private native void startEngine();
    private native void tap(boolean b);
    private native void setFrequency(float frequency);
    private native void playStream(byte[] audio);
   ListenUsers listenUsers;
   RecyclerView recyclerView;
    private static final int Interval=10000;
    LinearLayoutManager layoutManager;
    Timer  timer2;
    Handler  handler2;
    //LiveUsersDB liveUsersDB;
    boolean doubleBackToExitPressedOnce = false;
    ImageView compass;

    int read=0;

    // MainActivity
    TextView username, textView;

    int usercount = -1;
    Button ptt;
    private AppBarConfiguration mAppBarConfiguration;
    private AudioRecord ar = null;
    private int minSize;
    ImageView navBtn;
    Timer timer;
    Handler handler;
    ProgressBar progressBar;
    private boolean isTimerRunning = false;
    FloatingActionMenu materialDesignFAM;
    FloatingActionButton floatingActionButton1, floatingActionButton2;

    boolean isPressed = false;

    private boolean isListenMic = false;

    Switch voice_detection, speaker, soundeffetct;
    String myIp = "";
    LinearLayout rec;
    LinearLayout top;
    private String displayName = "";
    String voice_detection_str = "0", soundeffetct_str = "0", speaker_str = "0", volume_ptt = "", play_pause_ptt = "", recordaudiofromstr = "";
    String limitoftime = "";
    MediaPlayer mediaPlayer;
    Context context = this;
    private static HomePage INSTANCE;


    AudioTrack audioTrack;

    private static int SAMPLE_RATE = 8000; // Hertz
    private static final int SAMPLE_INTERVAL = 20; // Milliseconds
    private static final int SAMPLE_SIZE = 2; // Bytes
    private int BUF_SIZE_AUDIO = SAMPLE_INTERVAL * SAMPLE_INTERVAL * SAMPLE_SIZE * 2;

    int bytes_read = 0;
    int bytes_sent = 0;
    byte[] buf = new byte[BUF_SIZE_AUDIO];

    int periodInFrames = 8000 / 10;
    int bufferSize = periodInFrames * 1 * 16 / 8;
    short[] audioData = new short [bufferSize / 2];

    short[] lin = new short[1024];
    int num = 0;

//    static {
//        System.loadLibrary("native-lib");
//    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
//            Toast.makeText(context, ""+event.getAction(), Toast.LENGTH_SHORT).show();
//            if(event.getAction()==MotionEvent.ACTION_DOWN) {
//                tap(true);
//
//            } else if(event.getAction()==MotionEvent.ACTION_UP){
//                tap(false);
//            }
        return super.onTouchEvent(event);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        INSTANCE=this;
        setContentView(R.layout.activity_home_page);
       // listenUsers=new ListenUsers(this,getBroadcast(getIpAddressNew()));


       // startEngine();


        SensorManager sensorManager=(SensorManager) getSystemService(SENSOR_SERVICE);
        Sensor rotationSensor=sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        sensorManager.registerListener(this,rotationSensor,SensorManager.SENSOR_DELAY_FASTEST);


        checkDate();
        //registSettings();






//        Toast.makeText(HomePage.this,getBroadcast(getIpAddressNew()).toString(),Toast.LENGTH_SHORT).show();












        recyclerView=findViewById(R.id.rec);
        compass=findViewById(R.id.compass);
        layoutManager=new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        SharedPreferences share122 = getSharedPreferences("recordaudiofrom", Activity.MODE_PRIVATE);
        recordaudiofromstr = share122.getString("recordaudiofrom", "");

        SharedPreferences.Editor editor = getSharedPreferences("UpdateContacts", MODE_PRIVATE).edit();
        editor.putString("UpdateContacts", "1");
        editor.apply();

        SharedPreferences share1 = getSharedPreferences("voicedetection", Activity.MODE_PRIVATE);
        voice_detection_str = share1.getString("voicedetection", "");

        SharedPreferences share2 = getSharedPreferences("soundeffetct", Activity.MODE_PRIVATE);
        soundeffetct_str = share2.getString("soundeffetct", "");

        SharedPreferences share3 = getSharedPreferences("speaker", Activity.MODE_PRIVATE);
        speaker_str = share3.getString("speaker", "");

        SharedPreferences share = getSharedPreferences("Settings", Activity.MODE_PRIVATE);
        displayName = share.getString("username", "");

        SharedPreferences preferences = getSharedPreferences("ptt_volume", Activity.MODE_PRIVATE);
        volume_ptt = preferences.getString("ptt_volume", "");

        SharedPreferences preferences2 = getSharedPreferences("play_pause", Activity.MODE_PRIVATE);
        play_pause_ptt = preferences.getString("play_pause", "");

        if (displayName.isEmpty()) {

            String str = android.os.Build.MODEL;
            SharedPreferences.Editor editor16 = getSharedPreferences("Settings", MODE_PRIVATE).edit();
            editor16.putString("username", str);
            editor16.apply();
            displayName = str;
        }
        progressBar = findViewById(R.id.ProgressBar);
        ptt = findViewById(R.id.ppt);
        navBtn = findViewById(R.id.navBtn);
        top = findViewById(R.id.top);

        if (voice_detection_str.equals("1")) {
            ptt.setVisibility(View.GONE);
        } else if (voice_detection_str.equals("0")) {
            ptt.setVisibility(View.VISIBLE);
        }


        timer = new Timer();
        handler = new Handler(Looper.getMainLooper());


        materialDesignFAM = (FloatingActionMenu) findViewById(R.id.material_design_android_floating_action_menu);
        floatingActionButton1 = (FloatingActionButton) findViewById(R.id.material_design_floating_action_menu_item1);
        floatingActionButton2 = (FloatingActionButton) findViewById(R.id.material_design_floating_action_menu_item2);
        floatingActionButton1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startActivity(new Intent(ACTION_WIFI_SETTINGS));

            }
        });
        floatingActionButton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Intent intent = new Intent(Intent.ACTION_MAIN, null);
                intent.addCategory(Intent.CATEGORY_LAUNCHER);
                final ComponentName cn = new ComponentName("com.android.settings", "com.android.settings.TetherSettings");
                intent.setComponent(cn);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });


        final DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);

        voice_detection = navigationView.getHeaderView(0).findViewById(R.id.voice_detection);
        soundeffetct = navigationView.getHeaderView(0).findViewById(R.id.soundeffetct);
        speaker = navigationView.getHeaderView(0).findViewById(R.id.speaker);

        mediaPlayer = MediaPlayer.create(HomePage.this, R.raw.start_talk);
        mediaPlayer.setVolume(100, 100);
        findViewById(R.id.ppt).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                compass.setImageResource(R.drawable.compasgreen);
                mediaPlayer.start();
            }
        });

        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                compass.setImageResource(R.drawable.compas);
            }
        });

        ptt.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                //Toast.makeText(context, ""+event.getAction(), Toast.LENGTH_SHORT).show();
                switch (event.getAction()) {

                    case MotionEvent.ACTION_DOWN:
                     //   tap(true);
                        // PRESSED
                        if (!isPressed) {
                            if (soundeffetct_str.equals("1")) {
                                compass.setImageResource(R.drawable.compasgreen);
                                mediaPlayer.start();
                            }
                            isPressed = true;
                        }
                        if (isTimerRunning) {
                            timer.cancel();
                        }
                        SharedPreferences.Editor editor = getSharedPreferences("ptt_enable", MODE_PRIVATE).edit();
                        editor.putString("ptt_enable", "1");
                        editor.apply();

                       // startLevelVoice();

                        isListenMic = true;
                        return true; // if you want to handle the touch event
                    case MotionEvent.ACTION_UP:
                     //   tap(false);
                        // RELEASED
                        try {
                            isListenMic = false;
                            isTimerRunning = false;
//                            timer.cancel();
//                            isPressed = false;
//                            if (ar != null) {
//                                ar.stop();
//                                ar.release();
//                            }
                        } catch (IllegalStateException ex){
                            ex.printStackTrace();
                        } catch (Exception ex){
                            ex.printStackTrace();
                        }
                        SharedPreferences.Editor editor2 = getSharedPreferences("ptt_enable", MODE_PRIVATE).edit();
                        editor2.putString("ptt_enable", "0");
                        editor2.apply();
                        progressBar.setProgress(5);
                        Intent serviceIntent = new Intent(HomePage.this, CallService.class);
                        serviceIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        stopService(serviceIntent);

                        startService(serviceIntent);
                        return true; // if you want to handle the touch event

//                    case MotionEvent.:
//                        compass.setImageResource(R.drawable.compasgreen);
//                        Toast.makeText(context, "press", Toast.LENGTH_SHORT).show();
//                        updateContactList();
//                        mediaPlayer.start();
//                        return true;
                }
                return false;
            }
        });

        if (voice_detection_str.equals("1")) {
            voice_detection.setChecked(true);
            isListenMic = true;
        } else if (voice_detection_str.equals("0")) {
            isListenMic = false;
            progressBar.setProgress(5);
            voice_detection.setChecked(false);
        }

        if (soundeffetct_str.equals("1")) {
            soundeffetct.setChecked(true);
        } else if (soundeffetct_str.equals("0")) {
            soundeffetct.setChecked(false);
        }

        if (speaker_str.equals("1")) {
            speaker.setChecked(true);
        } else if (speaker_str.equals("0")) {
            speaker.setChecked(false);
        }


        speaker.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    SharedPreferences.Editor editor = getSharedPreferences("speaker", MODE_PRIVATE).edit();
                    editor.putString("speaker", "1");
                    editor.apply();
                } else {
                    SharedPreferences.Editor editor = getSharedPreferences("speaker", MODE_PRIVATE).edit();
                    editor.putString("speaker", "0");
                    editor.apply();
                }
            }
        });

        soundeffetct.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    SharedPreferences.Editor editor = getSharedPreferences("soundeffetct", MODE_PRIVATE).edit();
                    editor.putString("soundeffetct", "1");
                    editor.apply();
                    soundeffetct_str = "1";

                } else {
                    SharedPreferences.Editor editor = getSharedPreferences("soundeffetct", MODE_PRIVATE).edit();
                    editor.putString("soundeffetct", "0");
                    editor.apply();
                    soundeffetct_str = "";
                }
            }
        });

        voice_detection.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    isListenMic = true;
                    SharedPreferences.Editor editor = getSharedPreferences("voicedetection", MODE_PRIVATE).edit();
                    editor.putString("voicedetection", "1");
                    voice_detection_str = "1";
                    editor.apply();
                    ptt.setVisibility(View.GONE);
                    if (isTimerRunning) {
                        timer.cancel();
                    }
                   // startLevelVoice();
                } else {
                    isListenMic = false;
                    SharedPreferences.Editor editor = getSharedPreferences("voicedetection", MODE_PRIVATE).edit();
                    editor.putString("voicedetection", "0");
                    editor.apply();
                    voice_detection_str = "0";
                    ptt.setVisibility(View.VISIBLE);
                    progressBar.setProgress(5);
//                    timer.cancel();
                    isTimerRunning = false;
//                    if (ar != null) {
//                        ar.stop();
//                        ar.release();
//                    }

                    Intent serviceIntent = new Intent(HomePage.this, CallService.class);
                    serviceIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    stopService(serviceIntent);

                    startService(serviceIntent);
                }
            }
        });

        navigationView.getHeaderView(0).findViewById(R.id.settings).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(HomePage.this, Settings.class));
            }
        });

        navigationView.getHeaderView(0).findViewById(R.id.exit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent serviceIntent = new Intent(HomePage.this, CallService.class);
                stopService(serviceIntent);

                CallService callService=new CallService();
                callService.StopListener();



                finish();
            }
        });

        username = navigationView.getHeaderView(0).findViewById(R.id.username);
        textView = navigationView.getHeaderView(0).findViewById(R.id.textView);
        username.setText(displayName);
        textView.setText("Işjeňlik möhleti " + limitoftime + " çenli");


        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO},
                    1);
        } else {
            if (voice_detection_str.equals("1")) {
               // startLevelVoice();
            }

            Intent serviceIntent = new Intent(this, CallService.class);
            serviceIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startService(serviceIntent);
        }

        navBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawer.openDrawer(GravityCompat.START);
            }
        });




       // listenUsers();
//        ArrayList<Ulanyjylar> userList=new ArrayList<>();
//        try {
//            userList.add(new Ulanyjylar("Shageldi",InetAddress.getByName("10.102.10.20")));
//            userList.add(new Ulanyjylar("Merdan",InetAddress.getByName("10.102.10.20")));
//            userList.add(new Ulanyjylar("Pen",InetAddress.getByName("10.102.10.20")));
//            updateUsers(HomePage.this,userList);
//        } catch (UnknownHostException e) {
//            e.printStackTrace();
//        }
//        Thread usersThread=new Thread(new Runnable() {
//            @Override
//            public void run() {
//                while (true) {
//
//                    try {
//                        updateUsers(HomePage.this, listenUsers.getUsers());
//                        Thread.sleep(Interval);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    } catch (NullPointerException ex){
//                        ex.printStackTrace();
//                    }
//                }
//            }
//        });
//        usersThread.start();



    }


    public static HomePage get(){
        return INSTANCE;
    }

    public RecyclerView getRecyclerView(){
        return recyclerView;
    }
    public ImageView getCompass(){
        return compass;
    }
    public ProgressBar getProgressBar(){
        return progressBar;
    }
    public void updateUsers(Context context){
//        ArrayList<Ulanyjylar> users=new ArrayList<>();
//        users.clear();
//        liveUsersDB=new LiveUsersDB(context);
//        Cursor cursor=liveUsersDB.getAll();
//        if(cursor.getCount()==0){
//            compass.setVisibility(View.VISIBLE);
//            Log.d("EMPTY","Hic hilli ulanyjy yok");
//            UsersAdapter adapter = new UsersAdapter(users, context);
//            recyclerView = ((Activity) context).findViewById(R.id.rec);
//            recyclerView.setAdapter(adapter);
//        } else {
//            compass.setVisibility(View.GONE);
//            while (cursor.moveToNext()){
//                try {
//                    users.add(new Ulanyjylar(cursor.getString(1),InetAddress.getByName(cursor.getString(2))));
//                } catch (UnknownHostException e) {
//                    e.printStackTrace();
//                }
//            }
//            Log.d("Users count: ", users.size() + "");
//            UsersAdapter adapter = new UsersAdapter(users, context);
//            recyclerView = ((Activity) context).findViewById(R.id.rec);
//            recyclerView.setAdapter(adapter);
//        }
//
//        cursor.close();
//        liveUsersDB.close();

    }

    public InetAddress getIpAddressNew() {
        InetAddress inetAddress = null;
        InetAddress myAddr = null;

        try {
            for (Enumeration<NetworkInterface> networkInterface = NetworkInterface
                    .getNetworkInterfaces(); networkInterface.hasMoreElements();) {

                NetworkInterface singleInterface = networkInterface.nextElement();

                for (Enumeration<InetAddress> IpAddresses = singleInterface.getInetAddresses(); IpAddresses
                        .hasMoreElements();) {
                    inetAddress = IpAddresses.nextElement();

                    if (!inetAddress.isLoopbackAddress() && (singleInterface.getDisplayName()
                            .contains("wlan0") ||
                            singleInterface.getDisplayName().contains("eth0") ||
                            singleInterface.getDisplayName().contains("ap0"))) {

                        myAddr = inetAddress;
                    }
                }
            }

        } catch (SocketException ex) {
            Log.e("ERROR", ex.toString());
        }catch (NullPointerException ex){
            ex.printStackTrace();
        }
        return myAddr;
    }

    public InetAddress getBroadcast(InetAddress inetAddr) {

        if(inetAddr!=null) {
            NetworkInterface temp;
            InetAddress iAddr = null;
            try {
                temp = NetworkInterface.getByInetAddress(inetAddr);
                List<InterfaceAddress> addresses = temp.getInterfaceAddresses();

                for (InterfaceAddress inetAddress : addresses)

                    iAddr = inetAddress.getBroadcast();
                Log.d("MSG", "iAddr=" + iAddr);
                return iAddr;

            } catch (SocketException e) {

                e.printStackTrace();
                Log.d("ERROR", "getBroadcast" + e.getMessage());
            } catch (NullPointerException ex){
                ex.printStackTrace();
            }
        }
        return null;
    }


    public void listenUsers() {
        timer2 = new Timer();
        handler2 = new Handler(Looper.getMainLooper());
        timer2.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {


                handler2.post(new Runnable() {
                    @Override
                    public void run() {
                        SharedPreferences share6 = getSharedPreferences("UpdateContacts", Activity.MODE_PRIVATE);
                        String UpdateContacts = share6.getString("UpdateContacts", "");

                        if(UpdateContacts.equals("1")){
                            Log.d("UpadetContacts","Updating");
                            updateUsers(HomePage.this);
                            SharedPreferences.Editor editor = getSharedPreferences("UpdateContacts", MODE_PRIVATE).edit();
                            editor.putString("UpdateContacts", "0");
                            editor.apply();
                        }
                    }
                });


            }
        }, 0, 1000);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(timer2!=null) {
            timer2.cancel();
        }

        if (isTimerRunning) {
            timer.cancel();
            isTimerRunning = false;
        }
//        if (ar != null && ar.getState()==AudioRecord.STATE_INITIALIZED) {
//            ar.stop();
//            ar.release();
//        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(timer2!=null) {
            timer2.cancel();
        }

        if (isTimerRunning) {
//            timer.cancel();
//            isTimerRunning = false;
//            if (ar != null) {
//                ar.stop();
//            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(timer2!=null) {
            timer2.cancel();
        }
        //listenUsers();

        SharedPreferences share = getSharedPreferences("Settings", Activity.MODE_PRIVATE);
        displayName = share.getString("username", "");
        if(username!=null) {
            username.setText(displayName);
        }
        if (!isTimerRunning) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {


            } else {
                if (voice_detection_str.equals("1")) {
                  //  startLevelVoice();
                    SharedPreferences share122 = getSharedPreferences("recordaudiofrom", Activity.MODE_PRIVATE);
                    recordaudiofromstr = share122.getString("recordaudiofrom", "");
                }
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isTimerRunning) {
//            timer.cancel();
            isTimerRunning = false;
//            if (ar != null) {
//                ar.stop();
//            }
        }

        SharedPreferences.Editor editor12 = getSharedPreferences("ptt_enable", MODE_PRIVATE).edit();
        editor12.putString("ptt_enable", "0");
        editor12.apply();
    }

    @Override
    public void onBackPressed() {

        if (doubleBackToExitPressedOnce) {
            Intent serviceIntent = new Intent(this, CallService.class);
            stopService(serviceIntent);

            CallService callService=new CallService();
            callService.StopListener();
            finish();
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "Çykmak üçin ýene bir gezek basyň", Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce = false;
            }
        }, 2000);
    }

    public double getAmplitude() {
        short[] buffer = new short[minSize];
        ar.read(buffer, 0, minSize);
        int max = 0;
        for (short s : buffer) {
            if (Math.abs(s) > max) {
                max = Math.abs(s);
            }
        }
        return max;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 &&
                        grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (voice_detection_str.equals("1")) {

                       // startLevelVoice();
                    }
                    Intent serviceIntent = new Intent(this, CallService.class);
                    serviceIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startService(serviceIntent);
                    //startLevelVoice();
                    // Permission is granted. Continue the action or workflow
                    // in your app.
                } else {
                    // Explain to the user that the feature is unavailable because
                    // the features requires a permission that the user has denied.
                    // At the same time, respect the user's decision. Don't link to
                    // system settings in an effort to convince the user to change
                    // their decision.

                    Toast.makeText(this, "Mikrofona baglanyp bolmady!!!", Toast.LENGTH_SHORT).show();
                }
                return;
        }
        // Other 'case' lines to check for other
        // permissions this app might request.
    }

    public AudioRecord.OnRecordPositionUpdateListener mRecordListener = new AudioRecord.OnRecordPositionUpdateListener() {
        @Override
        public void onMarkerReached(AudioRecord audioRecord) {

        }

        public void onPeriodicNotification(AudioRecord recorder) {
            num = recorder.read(lin, 0, 1024);
            audioTrack.write(lin, 0, num);
        }
    };

    private byte[] short2byte(short[] data) {
        int dataSize = data.length;
        byte[] bytes = new byte[dataSize * 2];

        for (int i = 0; i < dataSize; i++) {
            bytes[i * 2] = (byte) (data[i] & 0x00FF);
            bytes[(i * 2) + 1] = (byte) (data[i] >> 8);
            data[i] = 0;
        }
        return bytes;
    }

    public void startLevelVoice() {

//        audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, 8000, AudioFormat.CHANNEL_OUT_MONO,
//                AudioFormat.ENCODING_PCM_16BIT, BUF_SIZE_AUDIO, AudioTrack.MODE_STREAM);
//
//        int periodInFrames = 8000 / 10;

        try {
            if (recordaudiofromstr.equals("1")) {
                minSize = AudioRecord.getMinBufferSize(8000, AudioFormat.CHANNEL_IN_STEREO, AudioFormat.ENCODING_PCM_16BIT);
                ar = new AudioRecord(MediaRecorder.AudioSource.VOICE_COMMUNICATION, 8000, AudioFormat.CHANNEL_IN_STEREO, AudioFormat.ENCODING_PCM_16BIT, minSize);
            } else {
                minSize = AudioRecord.getMinBufferSize(8000, AudioFormat.CHANNEL_IN_STEREO, AudioFormat.ENCODING_PCM_16BIT);
                ar = new AudioRecord(MediaRecorder.AudioSource.MIC, 8000, AudioFormat.CHANNEL_IN_STEREO, AudioFormat.ENCODING_PCM_16BIT, minSize);
            }
            if (ar.getState() == AudioRecord.STATE_UNINITIALIZED) {
                minSize = AudioRecord.getMinBufferSize(8000, AudioFormat.CHANNEL_IN_STEREO, AudioFormat.ENCODING_PCM_16BIT);
                ar = new AudioRecord(MediaRecorder.AudioSource.VOICE_COMMUNICATION, 8000, AudioFormat.CHANNEL_IN_STEREO, AudioFormat.ENCODING_PCM_16BIT, minSize);
            }

//            ar.setRecordPositionUpdateListener(mRecordListener);
//            ar.setPositionNotificationPeriod(periodInFrames);


            ar.startRecording();

//            audioTrack.play();



            isTimerRunning = true;
            timer = new Timer();
            handler = new Handler(Looper.getMainLooper());
            timer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {


                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (isListenMic) {
                                int perce = ((int) getAmplitude() * 100) / 2000;
                                progressBar.setProgress(perce);
                            }
                            //  updateContactList();


                            // Toast.makeText(MainActivity.this, perce+"", Toast.LENGTH_SHORT).show();
                        }
                    });


                }
            }, 0, 100);
        } catch (IllegalArgumentException ex){
            ex.printStackTrace();
        } catch (Exception ex){
            ex.printStackTrace();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        SharedPreferences share122 = getSharedPreferences("recordaudiofrom", Activity.MODE_PRIVATE);
        recordaudiofromstr = share122.getString("recordaudiofrom", "");

        SharedPreferences share1 = getSharedPreferences("voicedetection", Activity.MODE_PRIVATE);
        voice_detection_str = share1.getString("voicedetection", "");

        SharedPreferences preferences = getSharedPreferences("ptt_volume", Activity.MODE_PRIVATE);
        volume_ptt = preferences.getString("ptt_volume", "");

        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN || keyCode == KeyEvent.KEYCODE_VOLUME_UP) {

            if (voice_detection_str.equals("0") && volume_ptt.equals("1")) {
                //Toast.makeText(context, ""+keyCode, Toast.LENGTH_SHORT).show();
                if (soundeffetct_str.equals("1")) {
                    compass.setImageResource(R.drawable.compasgreen);
                    mediaPlayer.start();
                }
                if (!isListenMic) {

                   // startLevelVoice();
                    isListenMic = true;

                    SharedPreferences.Editor editor = getSharedPreferences("ptt_enable", MODE_PRIVATE).edit();
                    editor.putString("ptt_enable", "1");
                    editor.apply();

                } else {
                    timer.cancel();
                    isTimerRunning = false;
                    isListenMic = false;
                    progressBar.setProgress(5);
                    SharedPreferences.Editor editor = getSharedPreferences("ptt_enable", MODE_PRIVATE).edit();
                    editor.putString("ptt_enable", "0");
                    editor.apply();

                    Intent serviceIntent = new Intent(HomePage.this, CallService.class);
                    serviceIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    stopService(serviceIntent);
                    startService(serviceIntent);

                }
            }
        }
        if (keyCode == KeyEvent.KEYCODE_HEADSETHOOK) {

            if (voice_detection_str.equals("0") && volume_ptt.equals("1")) {
                //  Toast.makeText(context, "Pressed"+keyCode, Toast.LENGTH_SHORT).show();
                if (soundeffetct_str.equals("1")) {
                    compass.setImageResource(R.drawable.compasgreen);
                    mediaPlayer.start();
                }
                if (!isTimerRunning) {
                   // startLevelVoice();
                    isListenMic = true;
                    SharedPreferences.Editor editor = getSharedPreferences("ptt_enable", MODE_PRIVATE).edit();
                    editor.putString("ptt_enable", "1");
                    editor.apply();
                } else {
                    timer.cancel();
                    isTimerRunning = false;
                    isListenMic = false;
                    progressBar.setProgress(5);
                    SharedPreferences.Editor editor = getSharedPreferences("ptt_enable", MODE_PRIVATE).edit();
                    editor.putString("ptt_enable", "0");
                    editor.apply();
                    Intent serviceIntent = new Intent(HomePage.this, CallService.class);
                    serviceIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    stopService(serviceIntent);

                    startService(serviceIntent);
                }
            }
        }

        return super.onKeyDown(keyCode, event);
    }


    public void checkDate(){
        Checker obj = new Checker();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/M/yyyy hh:mm:ss");

        SharedPreferences share122 = getSharedPreferences("recordaudiofrom", Activity.MODE_PRIVATE);
        recordaudiofromstr = share122.getString("recordaudiofrom", "");


        SharedPreferences share12 = getSharedPreferences("LastDate", Activity.MODE_PRIVATE);
        String LastDate = share12.getString("LastDate", "");

        DateFormat dateFormat = new SimpleDateFormat("dd/M/yyyy hh:mm:ss");

        try {
            Date date = new Date();
            String datestr = dateFormat.format(date);
            Date date1 = simpleDateFormat.parse(datestr);
            Date date2 = simpleDateFormat.parse(LastDate);

            long days = obj.printDifference(date1, date2);
            long hours = obj.hoursDiff(date1, date2);
            long minutes = obj.minutesDiff(date1, date2);
            //Toast.makeText(context, ""+days+":"+hours, Toast.LENGTH_SHORT).show();
           // Log.d("Sene",datestr+"    "+LastDate);
//
//            System.out.println(days+"");
//            System.out.println(hours+"");
//            System.out.println(minutes+"");

            if (days > 0) {
               // if (hours > 0) {
                    Intent serviceIntent = new Intent(HomePage.this, CallService.class);
                    stopService(serviceIntent);
                    Alert alert = new Alert(HomePage.this, "no", "Programmanyň ulanmak möhleti soňlandy", "Programmanyň ulanmak möhleti bilen baglanşykly ýalňyşlyk ýüze çykdy. Telefonyň senesini we wagtyny barlaň we gaýtadan synanşyň");
                    alert.ShowDialog();

                    return;
               // }
            } else {
                SharedPreferences.Editor prr = getSharedPreferences("LastDate", MODE_PRIVATE).edit();
                prr.putString("LastDate", datestr);
                prr.apply();
            }

        } catch (ParseException e) {
            e.printStackTrace();
        }


        SharedPreferences pr = getSharedPreferences("limitoftime", Activity.MODE_PRIVATE);
        limitoftime = pr.getString("limitoftime", "");


        try {
            Date date = new Date();
            String datestr = dateFormat.format(date);
            Date date1 = simpleDateFormat.parse(datestr);
            Date date2 = simpleDateFormat.parse(limitoftime);

            long days = obj.printDifference(date1, date2);
            long hours = obj.hoursDiff(date1, date2);


             // Toast.makeText(context, ""+days+":"+hours, Toast.LENGTH_SHORT).show();

            if (days <= 0) {
                if (hours <= 0) {
                    AlertDialog.Builder alert = new AlertDialog.Builder(context);
                    alert.setTitle("Ulanmak möhleti soňlandy!!!");
                    alert.setMessage("Programmany täzeden aktiwleşdiriň.");
                    alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();

                            Intent serviceIntent = new Intent(HomePage.this, CallService.class);
                            serviceIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            stopService(serviceIntent);

                            SharedPreferences.Editor prr = getSharedPreferences("LastDate", MODE_PRIVATE).edit();
                            prr.putString("LastDate", "");
                            prr.apply();
                            startActivity(new Intent(HomePage.this, Register.class));
                            finish();


                        }
                    });
                    alert.setCancelable(false);
                    alert.show();
                    return;
                }
            }

        } catch (ParseException e) {
            e.printStackTrace();
        }

    }


    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        //setFrequency(20+(sensorEvent.values[0]*80));
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }



    public void registSettings() {
        SharedPreferences.Editor editor = getSharedPreferences("autostart", MODE_PRIVATE).edit();
        editor.putString("autostart", "1");
        editor.apply();

        SharedPreferences.Editor editor2 = getSharedPreferences("acousticEchoCanceler", MODE_PRIVATE).edit();
        editor2.putString("acousticEchoCanceler", "1");
        editor2.apply();

        SharedPreferences.Editor editor3 = getSharedPreferences("quality", MODE_PRIVATE).edit();
        editor3.putString("quality", "50");
        editor3.apply();

        SharedPreferences.Editor editor4 = getSharedPreferences("volume", MODE_PRIVATE).edit();
        editor4.putString("volume", "50");
        editor4.apply();

        SharedPreferences.Editor editor5 = getSharedPreferences("recordaudiofrom", MODE_PRIVATE).edit();
        editor5.putString("recordaudiofrom", "1");
        editor5.apply();

        SharedPreferences.Editor editor6 = getSharedPreferences("playas", MODE_PRIVATE).edit();
        editor6.putString("playas", "2");
        editor6.apply();

        SharedPreferences.Editor editor7 = getSharedPreferences("ptt_volume", MODE_PRIVATE).edit();
        editor7.putString("ptt_volume", "1");
        editor7.apply();

        SharedPreferences.Editor editor8 = getSharedPreferences("play_pause", MODE_PRIVATE).edit();
        editor8.putString("play_pause", "1");
        editor8.apply();

        SharedPreferences.Editor editor9 = getSharedPreferences("voicedetection", MODE_PRIVATE).edit();
        editor9.putString("voicedetection", "0");
        editor9.apply();

        SharedPreferences.Editor editor10 = getSharedPreferences("soundeffetct", MODE_PRIVATE).edit();
        editor10.putString("soundeffetct", "0");
        editor10.apply();

        SharedPreferences.Editor editor11 = getSharedPreferences("speaker", MODE_PRIVATE).edit();
        editor11.putString("speaker", "0");
        editor11.apply();

        SharedPreferences.Editor editor12 = getSharedPreferences("ptt_enable", MODE_PRIVATE).edit();
        editor12.putString("ptt_enable", "1");
        editor12.apply();

        SharedPreferences.Editor editor67 = getSharedPreferences("voicedetection", MODE_PRIVATE).edit();
        editor67.putString("voicedetection", "0");
        editor67.apply();
    }
}
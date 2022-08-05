package com.shageldi.intercom;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.ConnectivityManager;
import android.net.DhcpInfo;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.ContactsContract;
import android.support.v4.media.session.MediaSessionCompat;
import android.text.format.Formatter;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SoundEffectConstants;
import android.view.View;
import android.view.Menu;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.navigation.NavigationView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static android.provider.Settings.ACTION_WIFI_SETTINGS;
import static com.shageldi.intercom.ContactManager.BROADCAST_PORT;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private AudioRecord ar = null;
    private int minSize;
    ImageView navBtn;
    Timer timer, timer2;
    Handler handler, handler2;
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
    ImageView compass;

    Context context = this;

    ArrayList<Users> arrayList = new ArrayList<>();

    TextView username, textView;

    int usercount = -1;
    Button ptt;


    static final String LOG_TAG = "UDPchat";
    private static final int LISTENER_PORT = 50003;
    private static final int BUF_SIZE = 1024;
    private ContactManager contactManager;
    private String displayName = "";
    private boolean STARTED = false;
    private boolean IN_CALL = false;
    private boolean LISTEN = false;

    public final static String EXTRA_CONTACT = "hw.dt83.udpchat.CONTACT";
    public final static String EXTRA_IP = "hw.dt83.udpchat.IP";
    public final static String EXTRA_DISPLAYNAME = "hw.dt83.udpchat.DISPLAYNAME";

    String voice_detection_str = "0", soundeffetct_str = "0", speaker_str = "0", volume_ptt = "", play_pause_ptt = "", recordaudiofromstr = "";
    String limitoftime = "";
    MediaPlayer mediaPlayer;

    public static final String BROADCAST = "com.shageldi.intercom.android.action.broadcast";


    //Make Call

    boolean onemoretime = true;

    private static final String LOG_TAG_MAKE_CALL = "MakeCall";
    private static final int BROADCAST_PORT_MAKE_CALL = 50002;
    private static final int BUF_SIZE_MAKE_CALL = 1024;
    private String contactName_MAKE_CALL;
    private String contactIp_MAKE_CALL;
    private boolean LISTEN_MAKE_CALL = true;
    private boolean IN_CALL_MAKE_CALL = false;
    private AudioCall call;

    ArrayList<String> inCallIps = new ArrayList<>();

    String isINCALL = "", inCALLIP = "", oldInCALL = "1";

    boolean doubleBackToExitPressedOnce = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


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
//            Toast.makeText(context, ""+days+":"+hours, Toast.LENGTH_SHORT).show();
//
//            System.out.println(days+"");
//            System.out.println(hours+"");
//            System.out.println(minutes+"");

            if (days >= 0) {
                if (hours > 0) {
                    Alert alert = new Alert(MainActivity.this, "no", "Programmanyň ulanmak möhleti soňlandy", "Programmanyň ulanmak möhleti bilen baglanşykly ýalňyşlyk ýüze çykdy. Telefonyň senesini we wagtyny barlaň we gaýtadan synanşyň");
                    alert.ShowDialog();
                    return;
                }
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


            //  Toast.makeText(context, ""+date2.toString(), Toast.LENGTH_SHORT).show();

            if (days <= 0) {
                if (hours <= 0) {
                    AlertDialog.Builder alert = new AlertDialog.Builder(context);
                    alert.setTitle("Ulanmak möhleti soňlandy!!!");
                    alert.setMessage("Programmany täzeden aktiwleşdiriň.");
                    alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            finish();
                            startActivity(new Intent(MainActivity.this, Register.class));
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
            SharedPreferences.Editor editor = getSharedPreferences("Settings", MODE_PRIVATE).edit();
            editor.putString("username", str);
            editor.apply();
            displayName = str;
        }

        // WifiManager wifiManager=(WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        //Toast.makeText(context, ""+getBroadcastIp(), Toast.LENGTH_SHORT).show();

        //   Toast.makeText(context, ""+getBroadcast(getIpAddressNew())+" : "+getBroadcastIp(), Toast.LENGTH_SHORT).show();


        // Log.i(LOG_TAG, "UDPChat started");

        // Toast.makeText(context, ""+getBroadcastIp().getHostAddress(), Toast.LENGTH_SHORT).show();


        SharedPreferences share6 = getSharedPreferences("INCALL", Activity.MODE_PRIVATE);
        oldInCALL = share6.getString("INCALL", "");

        progressBar = findViewById(R.id.ProgressBar);

//        setVolumeControlStream(AudioManager.STREAM_MUSIC);
//
//
//        final IntentFilter intentFilter = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
//        final BecomingNoisyReceiver myNoisyAudioStreamReceiver = new BecomingNoisyReceiver();
//
//        MediaSessionCompat.Callback callback = new
//                MediaSessionCompat.Callback() {
//                    @Override
//                    public void onPlay() {
//                        registerReceiver(myNoisyAudioStreamReceiver, intentFilter);
//                    }
//
//                    @Override
//                    public void onStop() {
//                        unregisterReceiver(myNoisyAudioStreamReceiver);
//                    }
//                };


        ptt = findViewById(R.id.ppt);
        navBtn = findViewById(R.id.navBtn);
        rec = findViewById(R.id.rec);
        top = findViewById(R.id.top);
        compass = findViewById(R.id.compass);


        if (voice_detection_str.equals("1")) {
            ptt.setVisibility(View.GONE);
        } else if (voice_detection_str.equals("0")) {
            ptt.setVisibility(View.VISIBLE);
        }


        timer = new Timer();
        handler = new Handler(Looper.getMainLooper());

        listenUsers();

        // if(getIpAddressNew()!=null) {
        contactManager = new ContactManager(displayName, getBroadcast(getIpAddressNew()));
        //  }
        // startCallListener();

        String string = "OK";
        Intent serviceIntent = new Intent(context, ExampleService.class);
        serviceIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        serviceIntent.putExtra("inputExtra", string);
        context.startService(serviceIntent);

        updateContactList();

        Log.i(LOG_TAG, "Start button pressed");
        STARTED = true;

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
//        Toolbar toolbar = findViewById(R.id.toolbar);
//       setSupportActionBar(toolbar);
//        FloatingActionButton fab = findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });
        final DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);

        voice_detection = navigationView.getHeaderView(0).findViewById(R.id.voice_detection);
        soundeffetct = navigationView.getHeaderView(0).findViewById(R.id.soundeffetct);
        speaker = navigationView.getHeaderView(0).findViewById(R.id.speaker);

        mediaPlayer = MediaPlayer.create(MainActivity.this, R.raw.start_talk);
        mediaPlayer.setVolume(100, 100);
        findViewById(R.id.ppt).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //

                // int resID=getResources().getIdentifier("start_talk.wav", "raw", getPackageName());
                compass.setImageResource(R.drawable.compasgreen);

                updateContactList();
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
                        // PRESSED
                        if (!isPressed) {
                            if (soundeffetct_str.equals("1")) {
                                compass.setImageResource(R.drawable.compasgreen);

                                updateContactList();
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

                        startLevelVoice();

                        isListenMic = true;
                        return true; // if you want to handle the touch event
                    case MotionEvent.ACTION_UP:
                        // RELEASED
                        isListenMic = false;
                        isTimerRunning = false;
                        timer.cancel();
                        isPressed = false;
                        if(ar!=null){
                            ar.stop();
                            ar.release();

                        }
                        SharedPreferences.Editor editor2 = getSharedPreferences("ptt_enable", MODE_PRIVATE).edit();
                        editor2.putString("ptt_enable", "0");
                        editor2.apply();
                        progressBar.setProgress(5);
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

//        IntentFilter intentFilter = new IntentFilter(BROADCAST);
//        registerReceiver( CallBroadcastReceiver.class , intentFilter);



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
                    startLevelVoice();
                } else {
                    isListenMic = false;
                    SharedPreferences.Editor editor = getSharedPreferences("voicedetection", MODE_PRIVATE).edit();
                    editor.putString("voicedetection", "0");
                    editor.apply();
                    voice_detection_str = "0";
                    ptt.setVisibility(View.VISIBLE);
                    progressBar.setProgress(5);
                    timer.cancel();
                    isTimerRunning = false;
                    if (ar != null) {
                        ar.stop();
                        ar.release();
                    }
                }
            }
        });

        navigationView.getHeaderView(0).findViewById(R.id.settings).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, Settings.class));
            }
        });

        navigationView.getHeaderView(0).findViewById(R.id.exit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent serviceIntent = new Intent(MainActivity.this, ExampleService.class);
                stopService(serviceIntent);

                SharedPreferences.Editor editor = getSharedPreferences("INCALL", MODE_PRIVATE).edit();
                editor.putString("INCALL", "0");
                editor.apply();

                SharedPreferences.Editor editor2 = getSharedPreferences("INCALLIP", MODE_PRIVATE).edit();
                editor2.putString("INCALLIP", "");
                editor2.apply();

                SharedPreferences.Editor editor3 = getSharedPreferences("INCALLNAME", MODE_PRIVATE).edit();
                editor3.putString("INCALLNAME", "");
                editor3.apply();

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
                startLevelVoice();
            }

        }


        //  findViewById(R.id.ppt).playSoundEffect(SoundEffectConstants.NAVIGATION_UP);


        navBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawer.openDrawer(GravityCompat.START);
            }
        });


        drawer.setDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {

            }

            @Override
            public void onDrawerOpened(@NonNull View drawerView) {
                //navBtn.setImageResource(R.drawable.ic_baseline_close_24);
            }

            @Override
            public void onDrawerClosed(@NonNull View drawerView) {
//                navBtn.startAnimation(AnimationUtils.loadAnimation(MainActivity.this,R.anim.rotate_left));
//                navBtn.setImageResource(R.drawable.ic_baseline_menu_24);
            }

            @Override
            public void onDrawerStateChanged(int newState) {

            }
        });


//  mobile hotspot state check
//        final WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
//        final int apState;
//        try {
//            apState = (Integer) wifiManager.getClass().getMethod("getWifiApState").invoke(wifiManager);
//
//            Toast.makeText(this, apState+"", Toast.LENGTH_SHORT).show();
//            if (apState == 13) {
//                // Ap Enabled
//            }
//        } catch (IllegalAccessException e) {
//            e.printStackTrace();
//        } catch (InvocationTargetException e) {
//            e.printStackTrace();
//        } catch (NoSuchMethodException e) {
//            e.printStackTrace();
//        }
//
//
//        public static int AP_STATE_DISABLING = 10;
//        public static int AP_STATE_DISABLED = 11;
//        public static int AP_STATE_ENABLING = 12;
//        public static int AP_STATE_ENABLED = 13;
//        public static int AP_STATE_FAILED = 14;


        //   intent of wifi settings
        // startActivity(new Intent(ACTION_WIFI_SETTINGS));


        //intent of hotspot settings
//        final Intent intent = new Intent(Intent.ACTION_MAIN, null);
//        intent.addCategory(Intent.CATEGORY_LAUNCHER);
//        final ComponentName cn = new ComponentName("com.android.settings", "com.android.settings.TetherSettings");
//        intent.setComponent(cn);
//        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        startActivity( intent);


        // Toast.makeText(context, ""+getIpAddressNew().toString().substring(1), Toast.LENGTH_SHORT).show();

        ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        if (mWifi.isConnected()) {
            // Do whatever


        }
        //   Toast.makeText(this, ""+mWifi.isConnected(), Toast.LENGTH_SHORT).show();
//        while (true){
//            progressBar.setProgress((int)getAmplitude());
//        }

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
//        mAppBarConfiguration = new AppBarConfiguration.Builder()
//                .setDrawerLayout(drawer)
//                .build();
//        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
//        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
//        NavigationUI.setupWithNavController(navigationView, navController);
    }

    @Override
    protected void onStart() {
        super.onStart();

    }

    @Override
    protected void onStop() {
        super.onStop();
        // Log.i(LOG_TAG, "App stopped!");
        //  stopCallListener();
//        if(!IN_CALL) {
//
//            finish();
//        }
        if (isTimerRunning) {
            timer.cancel();
            isTimerRunning = false;
        }
        if (ar != null && ar.getState()==AudioRecord.STATE_INITIALIZED) {
            ar.stop();
            ar.release();
        }
    }

    public String getCurrentIp() {
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        return Formatter.formatIpAddress(wifiManager.getConnectionInfo().getIpAddress());
    }

    private InetAddress getBroadcastIp() {
        // Function to return the broadcast address, based on the IP address of the device
        try {

            WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            int ipAddress = wifiInfo.getIpAddress();
            //Toast.makeText(context, ""+ipAddress, Toast.LENGTH_SHORT).show();
            String addressString = toBroadcastIp(ipAddress);
            // Toast.makeText(context, "one: "+addressString, Toast.LENGTH_SHORT).show();
            InetAddress broadcastAddress = InetAddress.getByName(addressString);

            //  Toast.makeText(context, "broadcastAddress: "+broadcastAddress, Toast.LENGTH_SHORT).show();
            //  myIp=broadcastAddress.getHostAddress();
            //  Toast.makeText(context, myIp, Toast.LENGTH_SHORT).show();
            return broadcastAddress;
        } catch (UnknownHostException e) {

            Log.e(LOG_TAG, "UnknownHostException in getBroadcastIP: " + e);
            return null;
        }

    }

    private String toBroadcastIp(int ip) {
        // Returns converts an IP address in int format to a formatted string
        return (ip & 0xFF) + "." +
                ((ip >> 8) & 0xFF) + "." +
                ((ip >> 16) & 0xFF) + "." +
                "255";
    }


    public InetAddress getIpAddressNew() {
        InetAddress inetAddress = null;
        InetAddress myAddr = null;

        try {
            for (Enumeration<NetworkInterface> networkInterface = NetworkInterface
                    .getNetworkInterfaces(); networkInterface.hasMoreElements(); ) {

                NetworkInterface singleInterface = networkInterface.nextElement();

                for (Enumeration<InetAddress> IpAddresses = singleInterface.getInetAddresses(); IpAddresses
                        .hasMoreElements(); ) {
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
        }
        return myAddr;
    }

    public InetAddress getBroadcast(InetAddress inetAddr) {

        if (inetAddr != null) {
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
            }
        }
        return null;
    }


    private InetAddress getBroadcastAddress() throws IOException {
        WifiManager wifi = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        DhcpInfo dhcp = wifi.getDhcpInfo();
        // handle null somehow

        int broadcast = (dhcp.ipAddress & dhcp.netmask) | ~dhcp.netmask;
        Toast.makeText(context, "" + broadcast, Toast.LENGTH_SHORT).show();
        byte[] quads = new byte[4];
        for (int k = 0; k < 4; k++)
            quads[k] = (byte) (broadcast >> (k * 8));
        return InetAddress.getByAddress(quads);
    }


    private InetAddress getIpAddress() {
        InetAddress ip = null;
        try {
            Enumeration<NetworkInterface> enumNetworkInterfaces = NetworkInterface
                    .getNetworkInterfaces();
            while (enumNetworkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = enumNetworkInterfaces
                        .nextElement();
                Enumeration<InetAddress> enumInetAddress = networkInterface
                        .getInetAddresses();
                while (enumInetAddress.hasMoreElements()) {
                    InetAddress inetAddress = enumInetAddress.nextElement();

                    if (inetAddress.isSiteLocalAddress()) {
                        //Toast.makeText(context, "Inet: "+inetAddress, Toast.LENGTH_SHORT).show();
//                        ip += "SiteLocalAddress: "
//                                + inetAddress.getHostAddress() + "\n";
                        //    String addressString = toBroadcastIp(inetAddress);
                        //    Toast.makeText(context, "one: "+addressString, Toast.LENGTH_SHORT).show();
                        ip = inetAddress;
                    }
                }
            }

        } catch (SocketException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            ip = null;
            // ip += "Something Wrong! " + e.toString() + "\n";
        }
        return ip;
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
                        startLevelVoice();
                    }
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

    public void listenUsers() {
        timer2 = new Timer();
        handler2 = new Handler(Looper.getMainLooper());
        timer2.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {


                handler2.post(new Runnable() {
                    @Override
                    public void run() {

                        SharedPreferences share6 = getSharedPreferences("INCALL", Activity.MODE_PRIVATE);
                        isINCALL = share6.getString("INCALL", "");

                        SharedPreferences share8 = getSharedPreferences("INCALLIP", Activity.MODE_PRIVATE);
                        inCALLIP = share8.getString("INCALLIP", "");

                        if (!oldInCALL.equals(isINCALL)) {
                            if (isINCALL.equals("1")) {


                                usercount = 0;


                            } else {


                                usercount = 0;
                                IN_CALL = false;
                                IN_CALL_MAKE_CALL = false;


                            }
                        }

                        updateContactList();


                        // Toast.makeText(MainActivity.this, perce+"", Toast.LENGTH_SHORT).show();
                    }
                });


            }
        }, 0, 1000);
    }


    public void startLevelVoice() {


        if (recordaudiofromstr.equals("1")) {
            minSize = AudioRecord.getMinBufferSize(8000, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
            ar = new AudioRecord(MediaRecorder.AudioSource.VOICE_COMMUNICATION, 8000, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, minSize);
        } else {
            minSize = AudioRecord.getMinBufferSize(8000, AudioFormat.CHANNEL_CONFIGURATION_MONO, AudioFormat.ENCODING_PCM_16BIT);
            ar = new AudioRecord(MediaRecorder.AudioSource.MIC, 8000, AudioFormat.CHANNEL_CONFIGURATION_MONO, AudioFormat.ENCODING_PCM_16BIT, minSize);
        }
        ar.startRecording();

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
                            int perce = ((int) getAmplitude() * 100) / 20000;
                            progressBar.setProgress(perce);
                        }
                        //  updateContactList();


                        // Toast.makeText(MainActivity.this, perce+"", Toast.LENGTH_SHORT).show();
                    }
                });


            }
        }, 0, 100);
    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences share = getSharedPreferences("Settings", Activity.MODE_PRIVATE);
        displayName = share.getString("username", "");
        if(username!=null) {
            username.setText(displayName);
        }
        if (!isTimerRunning) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {


            } else {
                if (voice_detection_str.equals("1")) {
                    startLevelVoice();
                    SharedPreferences share122 = getSharedPreferences("recordaudiofrom", Activity.MODE_PRIVATE);
                    recordaudiofromstr = share122.getString("recordaudiofrom", "");
                }
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
//        if(STARTED) {
//
//            if(getIpAddressNew()!=null) {
//                contactManager.bye(displayName);
//                contactManager.stopBroadcasting();
//                contactManager.stopListening();
//            }
//            //STARTED = false;
//        }
//        stopCallListener();
//        Log.i(LOG_TAG, "App paused!");
        if (isTimerRunning) {
            timer.cancel();
            isTimerRunning = false;
            if (ar != null) {
                ar.stop();
            }
        }

        SharedPreferences.Editor editor12 = getSharedPreferences("ptt_enable", MODE_PRIVATE).edit();
        editor12.putString("ptt_enable", "0");
        editor12.apply();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isTimerRunning) {
            timer.cancel();
            isTimerRunning = false;
            if (ar != null) {
                ar.stop();
            }
        }
    }

    private void updateContactList() {
        if (contactManager.getContacts().size() > 0) {
            // Create a copy of the HashMap used by the ContactManager
            HashMap<String, InetAddress> contacts = contactManager.getContacts();

            SharedPreferences share6 = getSharedPreferences("INCALL", Activity.MODE_PRIVATE);
            oldInCALL = share6.getString("INCALL", "");
            if (usercount != contacts.size()) {
                //  onemoretime=true;
                arrayList.clear();
                for (String name : contacts.keySet()) {

//            RadioButton radioButton = new RadioButton(getBaseContext());
//            radioButton.setText(name);
//            radioButton.setTextColor(Color.BLACK);

                    InetAddress ip = contactManager.getContacts().get(name);

                    //  Toast.makeText(context, ""+ip.getHostAddress()+" : "+getIpAddressNew().getHostAddress(), Toast.LENGTH_SHORT).show();
                    if (!ip.getHostAddress().equals(getIpAddressNew().toString().substring(1))) {
                        arrayList.add(new Users(name, ip.getHostAddress()));
                    }
                    //  radioGroup.addView(radioButton);
                }
                usercount = contacts.size();
                if (arrayList.size() == 0) {
                    findViewById(R.id.include).setVisibility(View.VISIBLE);
                    top.setVisibility(View.GONE);
                } else {
                    findViewById(R.id.include).setVisibility(View.GONE);
                    top.setVisibility(View.VISIBLE);
                    rec.removeAllViews();

                    for (int i = 0; i < arrayList.size(); i++) {
                        View itemView = LayoutInflater.from(this).inflate(R.layout.users_design, null, false);
                        final TextView name, ip;
                        final ImageButton call, endcall;
                        name = itemView.findViewById(R.id.name);
                        ip = itemView.findViewById(R.id.ip);
                        call = itemView.findViewById(R.id.call);
                        endcall = null;
                        final Users user = arrayList.get(i);
                        name.setText(user.getName());
                        ip.setText(user.getIp());

                        if (user.getIp().equals(inCALLIP)) {
                            call.setVisibility(View.GONE);
                            endcall.setVisibility(View.VISIBLE);

//                            contactIp_MAKE_CALL = inCALLIP;
//                            contactName_MAKE_CALL = user.getName();
                            IN_CALL = true;
                            IN_CALL_MAKE_CALL = true;

                            endcall.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    entCall();
                                    endcall.setVisibility(View.GONE);
                                    call.setVisibility(View.VISIBLE);
                                }
                            });

                        } else {
                            endcall.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    endCall();
                                    endcall.setVisibility(View.GONE);
                                    call.setVisibility(View.VISIBLE);
                                }
                            });
                        }


                        call.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {


                                String contact = name.getText().toString();
                                InetAddress ip = contactManager.getContacts().get(contact);
                                String address2 = ip.toString();
                                address2 = address2.substring(1, address2.length());

                                if (!IN_CALL) {
                                    IN_CALL = true;

                                    call.setVisibility(View.GONE);
                                    endcall.setVisibility(View.VISIBLE);


                                    // Send this information to the MakeCallActivity and start that activity
//                                Intent intent = new Intent(MainActivity.this, MakeCallActivity.class);
//                                intent.putExtra(EXTRA_CONTACT, contact);
                                    String address = ip.toString();
                                    address = address.substring(1, address.length());
//                                intent.putExtra(EXTRA_IP, address);
//                                intent.putExtra(EXTRA_DISPLAYNAME, displayName);
//                                startActivity(intent);

                                    contactIp_MAKE_CALL = address;
                                    contactName_MAKE_CALL = displayName;

                                    inCallIps.add(contactIp_MAKE_CALL);
                                    makeCall();
                                } else {
                                    Snackbar.make(call, "Siz başga ulanyjy bilen baglanşykda!!!", Snackbar.LENGTH_LONG).setAction("OK", null).show();
                                }
                            }
                        });

                        rec.addView(itemView);
                    }
                }
            }
            // Create a radio button for each contact in the HashMap
//        RadioGroup radioGroup = (RadioGroup) findViewById(R.id.contactList);
            // radioGroup.removeAllViews();


        }


        // radioGroup.clearCheck();
    }

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {

            Intent serviceIntent = new Intent(this, ExampleService.class);
            stopService(serviceIntent);

            SharedPreferences.Editor editor = getSharedPreferences("INCALL", MODE_PRIVATE).edit();
            editor.putString("INCALL", "0");
            editor.apply();

            SharedPreferences.Editor editor2 = getSharedPreferences("INCALLIP", MODE_PRIVATE).edit();
            editor2.putString("INCALLIP", "");
            editor2.apply();

            SharedPreferences.Editor editor3 = getSharedPreferences("INCALLNAME", MODE_PRIVATE).edit();
            editor3.putString("INCALLNAME", "");
            editor3.apply();

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

    private void startCallListener() {
        // Creates the listener thread
        LISTEN = true;
        Thread listener = new Thread(new Runnable() {

            @Override
            public void run() {

                try {
                    // Set up the socket and packet to receive
                    Log.i(LOG_TAG, "Incoming call listener started");
                    DatagramSocket socket = new DatagramSocket(LISTENER_PORT);
                    socket.setSoTimeout(1000);
                    byte[] buffer = new byte[BUF_SIZE];
                    DatagramPacket packet = new DatagramPacket(buffer, BUF_SIZE);
                    while (LISTEN) {
                        // Listen for incoming call requests
                        try {
                            Log.i(LOG_TAG, "Listening for incoming calls");
                            socket.receive(packet);
                            String data = new String(buffer, 0, packet.getLength());
                            Log.i(LOG_TAG, "Packet received from " + packet.getAddress() + " with contents: " + data);
                            String action = data.substring(0, 4);
                            if (action.equals("CAL:")) {
                                // Received a call request. Start the ReceiveCallActivity
                                String address = packet.getAddress().toString();
                                String name = data.substring(4, packet.getLength());

                                Intent intent = new Intent(MainActivity.this, ReceiveCallActivity.class);
                                intent.putExtra(EXTRA_CONTACT, name);
                                intent.putExtra(EXTRA_IP, address.substring(1, address.length()));
                                IN_CALL = true;
                                //LISTEN = false;
                                //stopCallListener();
                                startActivity(intent);

                                //updateContactList();
                            } else {
                                // Received an invalid request
                                Log.w(LOG_TAG, packet.getAddress() + " sent invalid message: " + data);
                            }
                        } catch (Exception e) {
                        }
                    }
                    Log.i(LOG_TAG, "Call Listener ending");
                    socket.disconnect();
                    socket.close();
                } catch (SocketException e) {

                    Log.e(LOG_TAG, "SocketException in listener " + e);
                }
            }
        });
        listener.start();
    }

    private void stopCallListener() {
        // Ends the listener thread
        LISTEN = false;
    }

    private void entCall() {


        SharedPreferences.Editor editor12 = getSharedPreferences("ptt_enable", MODE_PRIVATE).edit();
        editor12.putString("ptt_enable", "0");
        editor12.apply();

        stopListener();
        if(IN_CALL) {
            call=new AudioCall(getBroadcast(getIpAddressNew()),context);
            call.endCall();
        }
        IN_CALL = false;
        IN_CALL_MAKE_CALL = false;
        sendEndMessage("END:");
    }

    @Override
    public void onRestart() {

        super.onRestart();
//        Log.i(LOG_TAG, "App restarted!");
//        IN_CALL = false;
//        STARTED = true;
//      //  if(getIpAddressNew()!=null) {
//            contactManager = new ContactManager(displayName, getBroadcast(getIpAddressNew()));
//            startCallListener();
//      //  }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        SharedPreferences share122 = getSharedPreferences("recordaudiofrom", Activity.MODE_PRIVATE);
        recordaudiofromstr = share122.getString("recordaudiofrom", "");
        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN || keyCode == KeyEvent.KEYCODE_VOLUME_UP) {

            if (voice_detection_str.equals("0") && volume_ptt.equals("1")) {
                //Toast.makeText(context, ""+keyCode, Toast.LENGTH_SHORT).show();
                if (soundeffetct_str.equals("1")) {
                    compass.setImageResource(R.drawable.compasgreen);


                    updateContactList();
                    mediaPlayer.start();
                }
                if (!isTimerRunning) {
                    startLevelVoice();
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
                }
            }
        }
        if (keyCode == KeyEvent.KEYCODE_HEADSETHOOK) {

            if (voice_detection_str.equals("0") && volume_ptt.equals("1")) {
                //  Toast.makeText(context, "Pressed"+keyCode, Toast.LENGTH_SHORT).show();
                if (soundeffetct_str.equals("1")) {
                    compass.setImageResource(R.drawable.compasgreen);

                    updateContactList();
                    mediaPlayer.start();
                }
                if (!isTimerRunning) {
                    startLevelVoice();
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
                }
            }
        }

        return super.onKeyDown(keyCode, event);
    }


    private void makeCall() {
        // Send a request to start a call
        Log.i(LOG_TAG_MAKE_CALL, "MakeCallActivity started!");

        Intent intent = getIntent();
        //  displayName = intent.getStringExtra(MainActivity.EXTRA_DISPLAYNAME);
        // contactName_MAKE_CALL = intent.getStringExtra(MainActivity.EXTRA_CONTACT);
        //contactIp_MAKE_CALL = intent.getStringExtra(MainActivity.EXTRA_IP);


//        TextView textView = (TextView) findViewById(R.id.textViewCalling);
//        textView.setText("Calling: " + contactName_MAKE_CALL);

        startListener();
        // makeCall();
        sendMessage("CAL:" + displayName, 50003);

    }

    private void endCall() {
        // Ends the chat sessions

        stopListener();
        if(IN_CALL) {

            call.endCall();
        }

        usercount=0;

        SharedPreferences.Editor editor = getSharedPreferences("INCALL", MODE_PRIVATE).edit();
        editor.putString("INCALL", "0");
        editor.apply();

        SharedPreferences.Editor editor2 = getSharedPreferences("INCALLIP", MODE_PRIVATE).edit();
        editor2.putString("INCALLIP", "");
        editor2.apply();

        SharedPreferences.Editor editor3 = getSharedPreferences("INCALLNAME", MODE_PRIVATE).edit();
        editor3.putString("INCALLNAME", "");
        editor3.apply();

        SharedPreferences.Editor editor12 = getSharedPreferences("ptt_enable", MODE_PRIVATE).edit();
        editor12.putString("ptt_enable", "0");
        editor12.apply();

        IN_CALL=false;
        IN_CALL_MAKE_CALL=false;


        sendMessage("END:", BROADCAST_PORT_MAKE_CALL);
        //  finish();
    }

    private void startListener() {
        // Create listener thread
        LISTEN_MAKE_CALL = true;
        Thread listenThread = new Thread(new Runnable() {

            @Override
            public void run() {

                try {

                    Log.i(LOG_TAG_MAKE_CALL, "Listener started!");
                    DatagramSocket socket = new DatagramSocket(BROADCAST_PORT_MAKE_CALL);
                    socket.setSoTimeout(15000);
                    byte[] buffer = new byte[BUF_SIZE];
                    DatagramPacket packet = new DatagramPacket(buffer, BUF_SIZE_MAKE_CALL);
                    while (LISTEN_MAKE_CALL) {

                        try {

                            Log.i(LOG_TAG_MAKE_CALL, "Listening for packets");
                            socket.receive(packet);
                            String data = new String(buffer, 0, packet.getLength());
                            Log.i(LOG_TAG_MAKE_CALL, "Packet received from " + packet.getAddress() + " with contents: " + data);
                            String action = data.substring(0, 4);
                            if (action.equals("ACC:")) {
                                // Accept notification received. Start call
                                call = new AudioCall(packet.getAddress(), MainActivity.this);
                                call.startCall();
                                IN_CALL_MAKE_CALL = true;
                            } else if (action.equals("REJ:")) {
                                // Reject notification received. End call
                                endCall();
                            } else if (action.equals("END:")) {
                                // End call notification received. End call
                                endCall();
                            } else {
                                // Invalid notification received
                                Log.w(LOG_TAG_MAKE_CALL, packet.getAddress() + " sent invalid message: " + data);
                            }
                        } catch (SocketTimeoutException e) {
                            if (!IN_CALL_MAKE_CALL) {

                                Log.i(LOG_TAG_MAKE_CALL, "No reply from contact. Ending call");
                                endCall();
                                return;
                            }
                        } catch (IOException e) {

                        }
                    }
                    Log.i(LOG_TAG_MAKE_CALL, "Listener ending");
                    socket.disconnect();
                    socket.close();
                    return;
                } catch (SocketException e) {

                    Log.e(LOG_TAG_MAKE_CALL, "SocketException in Listener");
                    endCall();
                }
            }
        });
        listenThread.start();
    }

    private void stopListener() {
        // Ends the listener thread
        LISTEN_MAKE_CALL = false;
    }

    private void sendMessage(final String message, final int port) {
        // Creates a thread used for sending notifications
        Thread replyThread = new Thread(new Runnable() {

            @Override
            public void run() {

                try {

                    InetAddress address = InetAddress.getByName(contactIp_MAKE_CALL);
                    byte[] data = message.getBytes();
                    DatagramSocket socket = new DatagramSocket();
                    DatagramPacket packet = new DatagramPacket(data, data.length, address, port);
                    socket.send(packet);
                    Log.i(LOG_TAG_MAKE_CALL, "Sent message( " + message + " ) to " + contactIp_MAKE_CALL);
                    socket.disconnect();
                    socket.close();
                } catch (UnknownHostException e) {

                    Log.e(LOG_TAG, "Failure. UnknownHostException in sendMessage: " + contactIp_MAKE_CALL);
                } catch (SocketException e) {

                    Log.e(LOG_TAG, "Failure. SocketException in sendMessage: " + e);
                } catch (IOException e) {

                    Log.e(LOG_TAG, "Failure. IOException in sendMessage: " + e);
                }
            }
        });
        replyThread.start();
    }


    private void sendEndMessage(final String message) {
        // Creates a thread for sending notifications
        Thread replyThread = new Thread(new Runnable() {

            @Override
            public void run() {

                try {
                    SharedPreferences share8 = getSharedPreferences("INCALLIP", Activity.MODE_PRIVATE);
                    inCALLIP = share8.getString("INCALLIP", "");
                    SharedPreferences share9 = getSharedPreferences("INCALLNAME", Activity.MODE_PRIVATE);
                    String INCALLNAME = share9.getString("INCALLNAME", "");
                    stopListener();
                    IN_CALL = false;
                    InetAddress address = InetAddress.getByName(inCALLIP);
                    if (IN_CALL_MAKE_CALL) {
                        // InetAddress ip = contactManager.getContacts().get(INCALLNAME);
//                        call = new AudioCall(address, MainActivity.this);
//                        call.endCall();
                    }

                    byte[] data = message.getBytes();
                    DatagramSocket socket = new DatagramSocket();
                    DatagramPacket packet = new DatagramPacket(data, data.length, address, BROADCAST_PORT_MAKE_CALL);
                    socket.send(packet);
                    Log.i(LOG_TAG, "Sent message( " + message + " ) to " + inCALLIP);
                    socket.disconnect();
                    socket.close();
                } catch (UnknownHostException e) {

                    Log.e(LOG_TAG, "Failure. UnknownHostException in sendMessage: " + inCALLIP);
                } catch (SocketException e) {

                    Log.e(LOG_TAG, "Failure. SocketException in sendMessage: " + e);
                } catch (IOException e) {

                    Log.e(LOG_TAG, "Failure. IOException in sendMessage: " + e);
                }
            }
        });
        replyThread.start();
    }


}
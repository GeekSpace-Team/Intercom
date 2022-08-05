package com.shageldi.intercom;

import static com.shageldi.intercom.App.CHANNEL_ID;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.media.audiofx.LoudnessEnhancer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Handler;
import android.text.format.Formatter;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.math.BigInteger;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.InterfaceAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.Timer;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.SourceDataLine;

public class CallService extends AccessibilityService {
    private static final String LOG_TAG = "HomePage";
    private static final int LISTENER_PORT = 40005;
    private static final int BROADCAST_INTERVAL = 10000; // Milliseconds
    private static final int BUF_SIZE = 1024;
    private boolean BROADCAST = true;
    private boolean LISTEN = false;
    ArrayList<Ulanyjylar> users = new ArrayList<>();
    ArrayList<String> ips = new ArrayList<>();
    //LiveUsersDB usersDB;
    ListenUsers listenUsers;
    String oldName = "";
    InetAddress oldAddress = null;
    private IntentFilter mIntentFilter;
    public static final String BroadcastStringForAction = "checkinternet";
    MediaPlayer mediaPlayer;


    int bytes_read = 0;
    int bytes_sent = 0;

    DatagramSocket serverSocket = null;

    // Audio Call
    private static int SAMPLE_RATE = 16000; // Hertz
    private static final int SAMPLE_INTERVAL = 20; // Milliseconds
    private static final int SAMPLE_SIZE = 2; // Bytes
    private int BUF_SIZE_AUDIO = 512;//SAMPLE_INTERVAL * SAMPLE_INTERVAL * SAMPLE_SIZE*2; //Bytes
    private InetAddress address; // Address to call
    private int port = 50078; // Port the packets are addressed to
    private boolean mic = false; // Enable mic?
    private boolean speakers = false; // Enable speakers?
    private static final int SESSION_ID = 2;
    AudioTrack track = null;

    String recordaudiofrom, acousticEchoCanceler, playas, quality, volume, progressbar = "";
    String voice_detection_str = "0", soundeffetct_str = "0", speaker_str = "0", volume_ptt = "", play_pause_ptt = "", recordaudiofromstr = "";
    Context context = this;
    boolean isTrackStoped = false;

    boolean isOnline = false;

    InetAddress broadIp = null;
    String myIp = "";

    int framesPerBufferInt = 0;

    Timer timer;
    Handler handler;

    DatagramSocket socket = null;

    LoudnessEnhancer enhancer = null;


    private static final String USB_NEAR_IFACE_ADDR = "192.168.42.129";
    private static final int USB_PREFIX_LENGTH = 24;

// USB is  192.168.42.1 and 255.255.255.0
// Wifi is 192.168.43.1 and 255.255.255.0
// BT is limited to max default of 5 connections. 192.168.44.1 to 192.168.48.1
// with 255.255.255.0

    private String[] mDhcpRange;
    private static final String[] DHCP_DEFAULT_RANGE = {
            "192.168.42.2", "192.168.42.254", "192.168.43.2", "192.168.43.254",
            "192.168.44.2", "192.168.44.254", "192.168.45.2", "192.168.45.254",
            "192.168.46.2", "192.168.46.254", "192.168.47.2", "192.168.47.254",
            "192.168.48.2", "192.168.48.254",
    };


    AudioInputStream audioInputStream;
    static AudioInputStream ais;
    static javax.sound.sampled.AudioFormat format;
    static boolean status = true;

    static DataLine.Info dataLineInfo;
    static SourceDataLine sourceDataLine;

    ByteArrayOutputStream baiss = new ByteArrayOutputStream();

    WifiManager.MulticastLock multicastLock;
    private ByteBuffer mSamples;
    private int mNumSamples;

    int bufferSize = 512;//SAMPLE_INTERVAL * SAMPLE_INTERVAL * SAMPLE_SIZE*2;

    @Override
    public void onCreate() {
        super.onCreate();

        boolean hasLowLatencyFeature =
                getPackageManager().hasSystemFeature(PackageManager.FEATURE_AUDIO_LOW_LATENCY);

        boolean hasProFeature =
                getPackageManager().hasSystemFeature(PackageManager.FEATURE_AUDIO_PRO);

        AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        String sampleRateStr = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR1) {
            sampleRateStr = am.getProperty(AudioManager.PROPERTY_OUTPUT_SAMPLE_RATE);
        }
//        SAMPLE_RATE = Integer.parseInt(sampleRateStr);
//        if (SAMPLE_RATE == 0) SAMPLE_RATE = 44100; // Use a default value if property not found


        String framesPerBuffer = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR1) {
            framesPerBuffer = am.getProperty(AudioManager.PROPERTY_OUTPUT_FRAMES_PER_BUFFER);
        }
        framesPerBufferInt = Integer.parseInt(framesPerBuffer);
        if (framesPerBufferInt == 0) framesPerBufferInt = 256; // Use default


        // Toast.makeText(context, "" + hasLowLatencyFeature + "\n" + hasProFeature + "\n" + sampleRateStr + "\n" + framesPerBufferInt, Toast.LENGTH_SHORT).show();

//        try {
//            Toast.makeText(context, ""+InetAddress.getLocalHost().getHostAddress(), Toast.LENGTH_SHORT).show();
//        } catch (UnknownHostException e) {
//            e.printStackTrace();
//        } catch (RuntimeException ex){
//            ex.printStackTrace();
//        }

        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
//        usersDB=new LiveUsersDB(CallService.this);
//        usersDB.truncate();

//          startCall();

        try {
            for(Enumeration<NetworkInterface> list = NetworkInterface.getNetworkInterfaces(); list.hasMoreElements();)
            {
                NetworkInterface i = list.nextElement();
                Log.e("network_interfaces", "display name " + i.getDisplayName());
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        SharedPreferences share122 = getSharedPreferences("recordaudiofrom", Activity.MODE_PRIVATE);
        recordaudiofromstr = share122.getString("recordaudiofrom", "1");

        SharedPreferences share1 = getSharedPreferences("voicedetection", Activity.MODE_PRIVATE);
        voice_detection_str = share1.getString("voicedetection", "");

        SharedPreferences share2 = getSharedPreferences("soundeffetct", Activity.MODE_PRIVATE);
        soundeffetct_str = share2.getString("soundeffetct", "");

        SharedPreferences share3 = getSharedPreferences("speaker", Activity.MODE_PRIVATE);
        speaker_str = share3.getString("speaker", "");

        SharedPreferences preferences = getSharedPreferences("ptt_volume", Activity.MODE_PRIVATE);
        volume_ptt = preferences.getString("ptt_volume", "");

        SharedPreferences preferences2 = getSharedPreferences("play_pause", Activity.MODE_PRIVATE);
        play_pause_ptt = preferences.getString("play_pause", "");

        mediaPlayer = MediaPlayer.create(CallService.this, R.raw.call);
        mediaPlayer.setVolume(100, 100);
        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(BroadcastStringForAction);
        Intent serviceIntent = new Intent(this, InternetService.class);
        startService(serviceIntent);


        try {
            if (isOnline(getApplicationContext())) {

//                AudioManager audioManager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
////get the current volume set
//                int deviceCallVol = audioManager.getStreamVolume(AudioManager.STREAM_VOICE_CALL);
////set volume to maximum
//                audioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL, audioManager.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL), 0);
//                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC), 0);

                startCall();
                isOnline = true;
                myIp = getCurrentIp();
                broadIp = getBroadcast(InetAddress.getByName(myIp));


                //Toast.makeText(context, ""+broadIp.getHostAddress(), Toast.LENGTH_SHORT).show();
                if (broadIp == null) {
                    broadIp = getBroadcast(getIpAddressNew());
                    myIp = getIpAddressNew().getHostAddress();
                }
                //  Log.d("BroadIP", broadIp.getHostAddress());

                //Toast.makeText(context, ""+intToInetAddress(wifiManager.getDhcpInfo().serverAddress).getHostAddress(), Toast.LENGTH_SHORT).show();

                //  Log.d("MyIp", myIp);


                //  Log.d(LOG_TAG, "Online State");
                SharedPreferences share = getSharedPreferences("Settings", Activity.MODE_PRIVATE);
                String str = share.getString("username", "");
                if (str.isEmpty()) {
                    str = android.os.Build.MODEL;
                }
                //  Log.d("Username", str);
                listenUsers = new ListenUsers(CallService.this, broadIp);
                broadcastName(str, broadIp);
//                oldName = str;
//                oldAddress = broadIp;


            } else {
                //    Log.d(LOG_TAG, "Offline state");
            }
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (NullPointerException ex) {
            ex.printStackTrace();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        registerReceiver(MyReceiver, mIntentFilter);

        startCallListener();


        //listenNetwork();
        String input = intent.getStringExtra("inputExtra");
        Intent notificationIntent = new Intent(CallService.this, Login.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(CallService.this,
                0, notificationIntent, 0);
        Notification notification = new NotificationCompat.Builder(CallService.this, CHANNEL_ID)
                .setContentTitle(CallService.this.getResources().getString(R.string.app_name))
                .setContentText("")
                .setSmallIcon(R.drawable.ic_baseline_notifications_24)
                .setNotificationSilent()
                .build();
        notification.flags = Notification.FLAG_INSISTENT | Notification.FLAG_AUTO_CANCEL;
        startForeground(1, notification);
        NotificationManager notificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancelAll();
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {


            if (enhancer != null) {
                enhancer.release();
            }


            StopListener();
            endCall();
            bye(broadIp);
            unregisterReceiver(MyReceiver);


        } catch (IllegalArgumentException ex) {
            ex.printStackTrace();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent accessibilityEvent) {

    }

    @Override
    public void onInterrupt() {

    }

    public InetAddress intToInetAddress(int hostAddress) {
        byte[] addressBytes = {(byte) (0xff & hostAddress),
                (byte) (0xff & (hostAddress >> 8)),
                (byte) (0xff & (hostAddress >> 16)),
                (byte) (0xff & (hostAddress >> 24))};

        try {
            return InetAddress.getByAddress(addressBytes);
        } catch (UnknownHostException e) {
            throw new AssertionError();
        }
    }
//
//    @Nullable
//    @Override
//    public IBinder onBind(Intent intent) {
//        return null;
//    }


    public void broadcastName(final String name, final InetAddress broadcastIP) {
        // Broadcasts the name of the device at a regular interval
        //   Log.i(LOG_TAG, "Broadcasting started!");
//        if(contacts.containsValue(broadcastIP)){
//            Log.d("Error","Address already in use");
//            return;
//        }

        Thread broadcastThread = new Thread(new Runnable() {
            @Override
            public void run() {

                try {


                    String request = "ADD:" + name;
                    byte[] message = request.getBytes();
                    DatagramSocket socket = new DatagramSocket();
                    socket.setBroadcast(true);
                    DatagramPacket packet = new DatagramPacket(message, message.length, broadcastIP, LISTENER_PORT);
                    while (BROADCAST) {
                        socket.send(packet);
                        // Log.i(LOG_TAG, "Broadcast packet sent: " + packet.getAddress().toString());
                        Thread.sleep(BROADCAST_INTERVAL);
                    }
                    if (socket.isConnected()) {
                        //    Log.i(LOG_TAG, "Broadcaster ending!");
                        socket.disconnect();
                        socket.close();
                    }

                } catch (SocketException e) {

//                    Log.e(LOG_TAG, "SocketExceltion in broadcast: " + e);
//                    Log.i(LOG_TAG, "Broadcaster ending!");
                    return;
                } catch (IOException e) {

//                    Log.e(LOG_TAG, "IOException in broadcast: " + e);
//                    Log.i(LOG_TAG, "Broadcaster ending!");
                    return;
                } catch (InterruptedException e) {
//                    Log.e(LOG_TAG, "InterruptedException in broadcast: " + e);
//                    Log.i(LOG_TAG, "Broadcaster ending!");
                    return;
                } catch (NullPointerException ex) {
                    ex.printStackTrace();
                }
            }
        });
        broadcastThread.start();
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
        } catch (NullPointerException ex) {
            ex.printStackTrace();
        }
        return myAddr;
    }

    private String getHotspotIPAddress() {

        int ipAddress = ((WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE)).getDhcpInfo().serverAddress;

        if (ByteOrder.nativeOrder().equals(ByteOrder.LITTLE_ENDIAN)) {
            ipAddress = Integer.reverseBytes(ipAddress);
        }

        byte[] ipByteArray = BigInteger.valueOf(ipAddress).toByteArray();

        String ipAddressString;
        try {
            ipAddressString = InetAddress.getByAddress(ipByteArray).getHostAddress();
        } catch (UnknownHostException ex) {
            ipAddressString = "";
        }

        return ipAddressString;

    }

    public InetAddress getBroadcast(InetAddress inetAddr) {

        if (inetAddr != null) {
            NetworkInterface temp;
            InetAddress iAddr = null;
            try {
                temp = NetworkInterface.getByInetAddress(inetAddr);
                if (temp != null) {
                    List<InterfaceAddress> addresses = temp.getInterfaceAddresses();

                    for (InterfaceAddress inetAddress : addresses)

                        iAddr = inetAddress.getBroadcast();
                    //Log.d("MSG", "iAddr=" + iAddr);
                    return iAddr;
                } else {
                    return null;
                }

            } catch (SocketException e) {

                e.printStackTrace();
                // Log.d("ERROR", "getBroadcast" + e.getMessage());
            } catch (NullPointerException ex) {
                ex.printStackTrace();
            }
        }
        return null;
    }

    public void startCallListener() {
        // Creates the listener thread
        try {
            // if (broadIp != null) {
            LISTEN = true;
            users.clear();
            ips.clear();
            Thread listener = new Thread(new Runnable() {

                @Override
                public void run() {

                    try {

                        // Set up the socket and packet to receive
                        //  Log.i(LOG_TAG, "Ahli ulanyjylary dinlenip baslanyar");
                        DatagramSocket socket = new DatagramSocket(null);
//                            socket.setSoTimeout(1000);
                        socket.setReuseAddress(true);
                        socket.setBroadcast(true);
                        socket.bind(new InetSocketAddress(LISTENER_PORT));
                        byte[] buffer = new byte[BUF_SIZE];
                        DatagramPacket packet = new DatagramPacket(buffer, BUF_SIZE);
                        while (LISTEN) {
                            // Listen for incoming call requests
                            try {

                                //  Log.i(LOG_TAG, "Ulanyjylar Dinlenyar");
                                socket.receive(packet);
                                String data = new String(buffer, 0, packet.getLength());
                                // Log.i(LOG_TAG, "Packet received from " + packet.getAddress() + " with contents: " + data);
                                String action = data.substring(0, 4);
                                if (action.equals("ADD:")) {
                                    // Received a call request. Start the ReceiveCallActivity
                                    String address = packet.getAddress().toString();
                                    String name = data.substring(4, packet.getLength());

                                    if (ips.contains(packet.getAddress().getHostAddress()) || packet.getAddress().getHostAddress().equals(myIp)) {
                                        //  Log.d("Users", "Ulanyjy eyyam ulgama goshulan ya-da myIp-a den");
                                    } else {
                                        ips.add(packet.getAddress().getHostAddress());
                                        users.add(new Ulanyjylar(name, packet.getAddress()));
                                        //    Log.d("Added User", packet.getAddress().getHostAddress());
                                        updateUsers();
                                    }
//                                        Cursor cursor = usersDB.getSelect(packet.getAddress().getHostAddress());
//
//                                        if (cursor.getCount() != 0) {
//                                            Log.d(LOG_TAG, "Ulanyjy eyyam Hasaba alynan");
//                                            while (cursor.moveToNext()) {
//                                                if (!cursor.getString(1).equals(name)) {
//                                                    Log.d(LOG_TAG, "Uytedildi");
//                                                    // listenUsers.AddContact(name, packet.getAddress());
//                                                    usersDB.updateData(name, packet.getAddress().getHostAddress());
//                                                    SharedPreferences.Editor editor = getSharedPreferences("UpdateContacts", MODE_PRIVATE).edit();
//                                                    editor.putString("UpdateContacts", "1");
//                                                    editor.apply();
//                                                } else {
//                                                    Log.d(LOG_TAG, "Uytgemage gerek yok");
//                                                }
//                                            }
//
//                                            cursor.close();
//                                            usersDB.close();
//                                        } else {
//                                            //users.add(new Ulanyjylar(name,packet.getAddress()));
//                                            //ips.add(packet.getAddress());
//                                            Log.d("Name", name + "," + address);
//                                            SharedPreferences.Editor editor = getSharedPreferences("UpdateContacts", MODE_PRIVATE).edit();
//                                            editor.putString("UpdateContacts", "1");
//                                            editor.apply();
//                                            // listenUsers.AddContact(name,packet.getAddress());
//                                            boolean isInsert = usersDB.insertData(name, packet.getAddress().getHostAddress());
//                                            Log.d("IsInsert", isInsert + "");
//
//                                        }


                                } else if (action.equals("BYE:")) {
                                    // Received an invalid request
                                    String address = packet.getAddress().toString();
                                    String name = data.substring(4, packet.getLength());
                                    //   Log.d("BYE", "Bye received");
                                    for (int i = 0; i < ips.size(); i++) {
                                        if (ips.get(i).equals(packet.getAddress().getHostAddress())) {
                                            users.remove(i);
                                            ips.remove(i);
                                        }
                                    }
                                    updateUsers();
//                                        Cursor cursor = usersDB.getSelect(packet.getAddress().getHostAddress());
//                                        if (cursor.getCount() != 0) {
//                                            int isDelete = usersDB.deleteData(packet.getAddress().getHostAddress());
//                                            Log.d(LOG_TAG, "Deleting" + isDelete);
//                                            SharedPreferences.Editor editor = getSharedPreferences("UpdateContacts", MODE_PRIVATE).edit();
//                                            editor.putString("UpdateContacts", "1");
//                                            editor.apply();
//                                        }

                                } else if (action.equals("NOT:")) {
                                    // Received an invalid request
                                    String address = packet.getAddress().toString();
                                    String getip = data.substring(4, packet.getLength());
//                                    Log.d("NOT", "Notification received:" + getip);
//                                    Log.d("EQUALS", myIp + " : " + getip);
                                    if (myIp.equals(getip)) {
                                        mediaPlayer.start();
                                    }

                                } else {
                                    //  Log.d(LOG_TAG, "40005-nji porta nadogry hat geldi");
                                }
                            } catch (Exception e) {
                                // Log.e("Call error", e.getMessage());
                                e.printStackTrace();
                            }
                        }
                        //   Log.i(LOG_TAG, "Call Listener ending");
                        socket.disconnect();
                        socket.close();
                    } catch (SocketException e) {

                        //  Log.e(LOG_TAG, "SocketException in listener " + e);
                    }
                }
            });
            listener.start();
            //    }
        } catch (NullPointerException ex) {
            // Log.e("Call error", ex.getMessage());
            ex.printStackTrace();
        } catch (Exception ex) {
            //  Log.e("Call error", ex.getMessage());
            ex.printStackTrace();
        }
    }

    public void updateUsers() {
        if (users.size() == 0) {

            //  Log.d("EMPTY", "Hic hilli ulanyjy yok");

            final UsersAdapter adapter = new UsersAdapter(users, context);
            final LinearLayoutManager layoutManager = new LinearLayoutManager(context);
            HomePage.get().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    HomePage.get().getCompass().setVisibility(View.VISIBLE);
                    HomePage.get().getRecyclerView().setLayoutManager(layoutManager);
                    adapter.notifyDataSetChanged();
                    HomePage.get().getRecyclerView().setAdapter(adapter);
                }
            });

        } else {
            final UsersAdapter adapter = new UsersAdapter(users, context);
            final LinearLayoutManager layoutManager = new LinearLayoutManager(context);
            HomePage.get().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    HomePage.get().getCompass().setVisibility(View.GONE);
                    HomePage.get().getRecyclerView().setLayoutManager(layoutManager);
                    HomePage.get().getRecyclerView().setAdapter(adapter);
                }
            });

        }
    }

    public void listenNetwork() {
        Thread usersThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        String str = android.os.Build.MODEL;


                        if (getBroadcast(getIpAddressNew()) != null) {
                            boolean connected = false;
                            ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                            if (connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                                    connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED) {
                                //we are connected to a network
                                connected = true;
                            } else
                                connected = false;

                            broadcastName(str, getBroadcast(getIpAddressNew()));
                        } else {

                        }
                        Thread.sleep(BROADCAST_INTERVAL);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (NullPointerException ex) {
                        ex.printStackTrace();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });

        usersThread.start();
    }


    public void bye(final InetAddress ip) {
        // Sends a Bye notification to other devices
        Thread byeThread = new Thread(new Runnable() {

            @Override
            public void run() {

                try {
                    if (ip != null) {
                        //   Log.i(LOG_TAG, "Attempting to broadcast BYE notification!");
                        String notification = "BYE:" + ip.getHostAddress();
                        byte[] message = notification.getBytes();
                        DatagramSocket socket = new DatagramSocket();
                        socket.setBroadcast(true);
                        DatagramPacket packet = new DatagramPacket(message, message.length, ip, LISTENER_PORT);
                        socket.send(packet);
                        //   Log.i(LOG_TAG, "Broadcast BYE notification!");
                        socket.disconnect();
                        socket.close();
                        return;
                    }
                } catch (SocketException e) {

                    //   Log.e(LOG_TAG, "SocketException during BYE notification: " + e);
                } catch (IOException e) {

                    // Log.e(LOG_TAG, "IOException during BYE notification: " + e);
                } catch (NullPointerException ex) {
                    ex.printStackTrace();
                }
            }
        });
        byeThread.start();
    }

    public BroadcastReceiver MyReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(BroadcastStringForAction)) {
                if (intent.getStringExtra("online_status").equals("true")) {
                    // for online state
                    //  Log.d(LOG_TAG, "Online");
                    try {
                        if (!isOnline) {
                            myIp = getCurrentIp();
                            broadIp = getBroadcast(InetAddress.getByName(myIp));


                            //Toast.makeText(context, ""+broadIp.getHostAddress(), Toast.LENGTH_SHORT).show();
                            if (broadIp == null) {
                                broadIp = getBroadcast(getIpAddressNew());
                                myIp = getIpAddressNew().getHostAddress();
                            }
                            endCall();
                            StopListener();

                            //startCall();
                            // startCallListener();
                            isOnline = true;

                            //  Log.d("Online", "1");
                            Intent serviceIntent = new Intent(CallService.this, CallService.class);
                            serviceIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            stopService(serviceIntent);

                            startService(serviceIntent);

                        }
                        if (broadIp != null) {
                            //Log.d("Online", "1");
                            SharedPreferences share = getSharedPreferences("Settings", Activity.MODE_PRIVATE);
                            String str = share.getString("username", "");
                            if (str.isEmpty()) {
                                str = android.os.Build.MODEL;
                            }
                            broadcastName(str, broadIp);
                            //oldAddress = getBroadcast(getIpAddressNew());

                        }
                    } catch (NullPointerException ex) {
                        ex.printStackTrace();
                    } catch (OutOfMemoryError ex) {
                        ex.printStackTrace();
                    } catch (UnknownHostException e) {
                        e.printStackTrace();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                } else {
                    // for offline state
                    isOnline = false;
                    // usersDB.truncate();
//                    SharedPreferences.Editor editor = getSharedPreferences("UpdateContacts", MODE_PRIVATE).edit();
//                    editor.putString("UpdateContacts", "1");
//                    editor.apply();
//                    endCall();
                    //  bye(getBroadcast(getIpAddressNew()));
                    //    Log.d(LOG_TAG, "Offline");
                }
            }
        }
    };


    public boolean isOnline(Context c) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        ConnectivityManager cm = (ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();

        final WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        final int apState = (Integer) wifiManager.getClass().getMethod("getWifiApState").invoke(wifiManager);

        if ((ni != null && ni.isConnectedOrConnecting()) || apState == 13)
            return true;
        else if (apState == 0) {
            //   Log.d("STATE", "Disabling");
            return false;
        } else
            return false;
    }

    public void StopListener() {
        LISTEN = false;
        BROADCAST = false;
    }

    public void startCall() {
//		AudioManager manager=(AudioManager) context.getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
//		manager.setMode(AudioManager.MODE_IN_COMMUNICATION);
//		manager.setSpeakerphoneOn(false);
//		manager.setStreamVolume(AudioManager.STREAM_MUSIC, 9, 0);

        // Log.d("Call Started", "Call Started");

        SharedPreferences share1 = context.getSharedPreferences("recordaudiofrom", Activity.MODE_PRIVATE);
        recordaudiofrom = share1.getString("recordaudiofrom", "1");

        SharedPreferences share2 = context.getSharedPreferences("acousticEchoCanceler", Activity.MODE_PRIVATE);
        acousticEchoCanceler = share2.getString("acousticEchoCanceler", "");

        SharedPreferences share3 = context.getSharedPreferences("playas", Activity.MODE_PRIVATE);
        playas = share3.getString("playas", "");

        SharedPreferences share4 = context.getSharedPreferences("quality", Activity.MODE_PRIVATE);
        quality = share4.getString("quality", "");

        SharedPreferences share5 = context.getSharedPreferences("volume", Activity.MODE_PRIVATE);
        volume = share5.getString("volume", "");

        SharedPreferences share16 = context.getSharedPreferences("progressbar", Activity.MODE_PRIVATE);
        progressbar = share16.getString("progressbar", "");

//		if(Integer.valueOf(quality)>=0 && Integer.valueOf(quality)<=20){
//			SAMPLE_RATE=4000;
//		}
//
//		if(Integer.valueOf(quality)>20 && Integer.valueOf(quality)<=40){
//			SAMPLE_RATE=8000;
//		}
//
//		if(Integer.valueOf(quality)>40 && Integer.valueOf(quality)<=80){
//			SAMPLE_RATE=16000;
//		}
//
//		if(Integer.valueOf(quality)>80 && Integer.valueOf(quality)<=100){
//			SAMPLE_RATE=32000;
//		}

// Acquire multicast lock
        WifiManager wifi = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        multicastLock = wifi.createMulticastLock("multicastLock");
        multicastLock.setReferenceCounted(true);
        multicastLock.acquire();
        //listenMic();
        startMic();
        startSpeakers();
        // Speak();

    }

    public void endCall() {

        //  Log.i(LOG_TAG, "Ending call!");
        muteSpeakers();
        muteMic();
// Once your finish using it, release multicast lock
        if (multicastLock != null) {
            multicastLock.release();
            multicastLock = null;
        }

    }

    public void muteMic() {
        mic = false;
    }

    public void muteSpeakers() {
        speakers = false;
    }


    public void startMic() {
        // Creates the thread for capturing and transmitting audio
        mic = true;

        Thread thread = new Thread(new Runnable() {

            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void run() {
//                int bufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE,
//                        AudioFormat.CHANNEL_IN_MONO,
//                        AudioFormat.ENCODING_PCM_16BIT);
//
//                if (bufferSize == AudioRecord.ERROR || bufferSize == AudioRecord.ERROR_BAD_VALUE) {
//                    bufferSize = SAMPLE_RATE * 2;
//                }

//                short[] audioBuffer = new short[bufferSize / 2];
                android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
                AudioRecord audioRecorder = initiliazeAudioRecord();
                // Create an instance of the AudioRecord class
                //Log.i(LOG_TAG, "Send thread started. Thread id: " + Thread.currentThread().getId());


//				AcousticEchoCanceler acousticEchoCanceler=AcousticEchoCanceler.create(audioRecorder.getAudioSessionId());
//				acousticEchoCanceler.setEnabled(true);


                //Toast.makeText(context, ""+ptt_enable, Toast.LENGTH_SHORT).show();


                int bytes_read = 0;
                int bytes_sent = 0;
                byte[] buf = new byte[bufferSize];
                long shortsRead = 0;
                try {
                    audioRecorder.startRecording();
                    // Create a socket and start recording
                    MulticastSocket socket = new MulticastSocket();

                    InetAddress group = InetAddress.getByName("224.2.0.0");
//                    InetAddress group = InetAddress.getByName("224.0.0.1");
                   // NetworkInterface nif2 = NetworkInterface.getByName("wlan0");

                    //socket.setNetworkInterface(nif2);

                    // DatagramSocket socket = new DatagramSocket();
                    socket.setSendBufferSize(1024 * 1024);
                  //  socket.setNetworkInterface(nif2);
//                    socket.joinGroup(group);
                    //setInterface(socket,false);
                    //socket.setSendBufferSize(bufferSize);
                    //byte[] key=Encryption.generateKey("password");
                    while (mic) {
                        // Capture audio from the mic and transmit it
                        SharedPreferences share6 = context.getSharedPreferences("ptt_enable", Activity.MODE_PRIVATE);
                        String ptt_enable = share6.getString("ptt_enable", "");

                        SharedPreferences share61 = context.getSharedPreferences("voicedetection", Activity.MODE_PRIVATE);
                        String voicedetection = share61.getString("voicedetection", "");

                        //  Log.d("Mic","running");


                        if (voicedetection.equals("0") && ptt_enable.equals("1")) {
//                            Log.i(LOG_TAG, "Packet destination: " + broadIp);

//                            if(audioRecorder==null){
//                                audioRecorder=initiliazeAudioRecord();
//                            } else{
//                                if(audioRecorder.getState()==AudioRecord.STATE_UNINITIALIZED) {
//                                    audioRecorder=initiliazeAudioRecord();
//                                }
//                            }
                            //audioRecorder.startRecording();

//                            int numberOfShort = audioRecorder.read(audioBuffer, 0, audioBuffer.length);
//                            shortsRead += numberOfShort;


                            bytes_read = audioRecorder.read(buf, 0, buf.length);

                            DatagramPacket packet = new DatagramPacket(buf, bufferSize, group, port);
                            // packet.setLength(2 * 1024 * 1024);
                            socket.send(packet);
                            bytes_sent += bytes_read;


                             Log.d("Sent buffersize",socket.getSendBufferSize()+"");


                            //Log.d("Sended length",bytes_read+"");


                            //	System.out.println("Send data: "+buf);
                            //System.out.println("Encryption Send data: "+AESencrp.encrypt(buf));
                            //	Log.i(LOG_TAG, "Total bytes sent: " + bytes_sent);
                            Thread.sleep(0, 0);
                        } else if (voicedetection.equals("1")) {
//                            Log.i(LOG_TAG, "Packet destination: " + broadIp);

//                            if(audioRecorder==null){
//                                audioRecorder=initiliazeAudioRecord();
//                            } else{
//                                if(audioRecorder.getState()==AudioRecord.STATE_UNINITIALIZED) {
//                                    audioRecorder=initiliazeAudioRecord();
//                                }
//                            }

                            bytes_read = audioRecorder.read(buf, 0, buf.length);

                            DatagramPacket packet = new DatagramPacket(buf, bufferSize, group, port);
                            socket.send(packet);
                            bytes_sent += bytes_read;


                              Log.d("Sent buffersize",socket.getSendBufferSize()+"");

                            //  Log.d("Sended length",bytes_read+"");

                            //	System.out.println("Send data: "+buf);
                            //System.out.println("Encryption Send data: "+AESencrp.encrypt(buf));
                            //	Log.i(LOG_TAG, "Total bytes sent: " + bytes_sent);
                            Thread.sleep(0, 0);
                        }
//                        else{
//                            if(audioRecorder!=null) {
//                                if (audioRecorder.getState() == AudioRecord.STATE_INITIALIZED) {
//                                    audioRecorder=null;
//                                    // muteMic();
//                                }
//                            }
//                        }
                    }
                    // Stop recording and release resources
                    //  Log.d("AUDIORECORD", "Stopping audio recorder");
                    audioRecorder.stop();
                    audioRecorder.release();
                    socket.leaveGroup(group);
                    socket.disconnect();
                    socket.close();
                    mic = false;
                    return;
                } catch (SocketException e) {

                    //   Log.e(LOG_TAG, "SocketExceptionRecord: " + e.toString());
                    mic = false;
                } catch (UnknownHostException e) {

                    // Log.e(LOG_TAG, "UnknownHostExceptionRecord: " + e.toString());
                    mic = false;
                } catch (IOException e) {

                    // Log.e(LOG_TAG, "IOExceptionRecord: " + e.toString());
                    mic = false;
                } catch (Exception e) {
                    e.printStackTrace();
                }


            }
        });
        thread.start();
    }

    private AudioRecord initiliazeAudioRecord() {
//        if(audioRecorder!=null){
//            if(audioRecorder.getState()==AudioRecord.STATE_INITIALIZED){
//                //audioRecorder.stop();
//                audioRecorder.release();
//            }
//        }
        try {
            AudioRecord audioRecorder = null;

//            int bufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE,
//                    AudioFormat.CHANNEL_IN_MONO,
//                    AudioFormat.ENCODING_PCM_16BIT);

//            if (bufferSize == AudioRecord.ERROR || bufferSize == AudioRecord.ERROR_BAD_VALUE) {
//                bufferSize = SAMPLE_RATE * 2;
//            }

            Log.d("AudioRecordBuffersize", bufferSize + "");


            if (recordaudiofrom.equals("1") || recordaudiofrom.isEmpty()) {
                audioRecorder = new AudioRecord(MediaRecorder.AudioSource.VOICE_COMMUNICATION, SAMPLE_RATE,
                        AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT,
                        bufferSize);
            } else if (recordaudiofrom.equals("2")) {
                audioRecorder = new AudioRecord(MediaRecorder.AudioSource.MIC, SAMPLE_RATE,
                        AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT,
                        bufferSize);
            } else {
                audioRecorder = new AudioRecord(MediaRecorder.AudioSource.VOICE_COMMUNICATION, SAMPLE_RATE,
                        AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT,
                        bufferSize);
            }

            //  BUF_SIZE_AUDIO=AudioRecord.getMinBufferSize(SAMPLE_RATE, AudioFormat.CHANNEL_IN_STEREO, AudioFormat.ENCODING_PCM_16BIT);


            // MediaRecorder mediaRecorder=new MediaRecorder();


            try {
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN && recordaudiofrom.equals("2")) {
//                    if (AutomaticGainControl.isAvailable()) {
//                        AutomaticGainControl agc = AutomaticGainControl.create(audioRecorder.getAudioSessionId());
//                        //agc.g
//                        //Log.d("AudioRecord", "AGC is " + (agc.getEnabled()?"enabled":"disabled"));
//                        agc.setEnabled(true);
//                        //Log.d("AudioRecord", "AGC is " + (agc.getEnabled()?"enabled":"disabled" +" after trying to enable"));
//                    } else {
//                        //Log.d("AudioRecord", "AGC is unavailable");
//                    }
//                }
////
////
////
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN && recordaudiofrom.equals("2")) {
//                    if (NoiseSuppressor.isAvailable()) {
//                        NoiseSuppressor ns = NoiseSuppressor.create(audioRecorder.getAudioSessionId());
//                        //Log.d("AudioRecord", "NS is " + (ns.getEnabled()?"enabled":"disabled"));
//                        ns.setEnabled(true);
//                        //Log.d("AudioRecord", "NS is " + (ns.getEnabled()?"enabled":"disabled" +" after trying to disable"));
//                    } else {
//                        //Log.d("AudioRecord", "NS is unavailable");
//                    }
//                }


//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
//                    if (AcousticEchoCanceler.isAvailable() && recordaudiofrom.equals("2") && acousticEchoCanceler.equals("2")) {
//
//                        AcousticEchoCanceler aec = AcousticEchoCanceler.create(audioRecorder.getAudioSessionId());
//                        //Log.d("AudioRecord", "AEC is " + (aec.getEnabled()?"enabled":"disabled"));
//                        if (aec != null) {
//                            aec.setEnabled(true);
//                        }
//                        //Log.d("AudioRecord", "AEC is " + (aec.getEnabled()?"enabled":"disabled" +" after trying to disable"));
//
//                    } else {
//                        //Log.d("AudioRecord", "aec is unavailable");
//                    }
//                }

//                if (audioRecorder.getState() == AudioRecord.STATE_UNINITIALIZED) {
//                    audioRecorder = new AudioRecord(MediaRecorder.AudioSource.VOICE_COMMUNICATION, SAMPLE_RATE,
//                            AudioFormat.CHANNEL_IN_DEFAULT, AudioFormat.ENCODING_PCM_16BIT,
//                            AudioRecord.getMinBufferSize(SAMPLE_RATE, AudioFormat.CHANNEL_IN_DEFAULT, AudioFormat.ENCODING_PCM_16BIT)*2);
//                }
                return audioRecorder;
            } catch (Exception ex) {
                ex.printStackTrace();
                return audioRecorder;
            }
        } catch (IllegalArgumentException ex) {
            ex.printStackTrace();
            return null;
        } catch (NullPointerException ex) {
            ex.printStackTrace();
            return null;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }


    public void startSpeakers() {
        // Creates the thread for receiving and playing back audio
        if (!speakers) {

            speakers = true;
            Thread receiveThread = new Thread(new Runnable() {

                @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                @Override
                public void run() {

                    track = null;

                    int gain = 100;

                    int trackBuff = AudioTrack.getMinBufferSize(SAMPLE_RATE, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT);

                    if (playas.equals("1") || playas.isEmpty()) {

                        track = new AudioTrack(
                                new AudioAttributes.Builder()
                                        .setUsage(AudioAttributes.USAGE_VOICE_COMMUNICATION)
                                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                                        .build(),
                                new AudioFormat.Builder()
                                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                                        .setSampleRate(SAMPLE_RATE)
                                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                                        .build(), bufferSize, AudioTrack.MODE_STREAM, 1);

                        gain = 3000;

                        enhancer = new LoudnessEnhancer(track.getAudioSessionId());

//                        if (NoiseSuppressor.isAvailable()) {
//                            NoiseSuppressor.create(track.getAudioSessionId());
//                        }
//                        if (AcousticEchoCanceler.isAvailable()) {
//                            AcousticEchoCanceler.create(track.getAudioSessionId());
//                        }

                        enhancer.setTargetGain(gain);

                        enhancer.setEnabled(true);

                    } else if (playas.equals("2")) {


                        track = new AudioTrack(
                                new AudioAttributes.Builder()
                                        .setUsage(AudioAttributes.USAGE_MEDIA)
                                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                                        .build(),
                                new AudioFormat.Builder()
                                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                                        .setSampleRate(SAMPLE_RATE)
                                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                                        .build(),
                                bufferSize,
                                AudioTrack.MODE_STREAM, 1);
                        gain = 1000;

                        enhancer = new LoudnessEnhancer(track.getAudioSessionId());


//                        if (NoiseSuppressor.isAvailable()) {
//                            NoiseSuppressor.create(track.getAudioSessionId());
//                        }
//                        if (AcousticEchoCanceler.isAvailable()) {
//                            AcousticEchoCanceler.create(track.getAudioSessionId());
//                        }
                        enhancer.setTargetGain(gain);

                        enhancer.setEnabled(true);


                    }


                    if (track != null) {
//                        track.setPlaybackRate(SAMPLE_RATE);
                        track.setStereoVolume(AudioTrack.getMaxVolume(), AudioTrack.getMaxVolume());
                        track.setVolume(AudioTrack.getMaxVolume());

//                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//                            track.setBufferSizeInFrames(bufferSize);
//                        }
                        if (track.getState() == AudioTrack.STATE_INITIALIZED) {
                            track.play();
                        }
                    }
                    isTrackStoped = false;
                    try {
                        InetAddress group = InetAddress.getByName("224.2.0.0");
//                        InetAddress group = InetAddress.getByName("224.0.0.1");
                        // Define a socket to receive the audio
                        MulticastSocket socket = new MulticastSocket(port);
//                        socket.setTTL((byte) 1);
                        //NetworkInterface nif2 = NetworkInterface.getByName("wlan0");

                       // socket.setNetworkInterface(nif2);
                        // socket.setSoTimeout(1000);
                        //DatagramSocket socket = new DatagramSocket(null);
                        // socket.setReuseAddress(true);
                        //  socket.setLoopbackMode(true);
                        // socket.setBroadcast(true);
                        // socket.setReceiveBufferSize(1024 * 1024);
                        //  socket.setReceiveBufferSize(bufferSize);
                        // socket.bind(new InetSocketAddress(port));
                      //  socket.setNetworkInterface(nif2);
                        socket.joinGroup(group);

                       // setInterface(socket,false);
                        byte[] buf = new byte[bufferSize];

                        BlockingQueue queue = new ArrayBlockingQueue(bufferSize);

                        ByteBuffer buffer = ByteBuffer.allocateDirect(bufferSize);

                        while (speakers) {

                            DatagramPacket
                                    packet = new DatagramPacket(buf, bufferSize);
                            socket.receive(packet);
                            System.out.println("Decrypted Received STREAM: " + packet.getAddress().getHostAddress()+" / "+myIp);
                            if (!packet.getAddress().getHostAddress().equals(myIp)) {


//                                queue.put(packet.getData());
//                                byte[] output = trim(packet.getData());
//                                Log.d("Stream",output.length+"");

//                                short[] sbuffer = new short[bufferSize];
//                                for(int i = 0; i < buf.length; i++)
//                                {
//
//                                    int asInt = 0;
//                                    asInt = ((buf[i] & 0xFF) << 0)
//                                            | ((buf[i+1] & 0xFF) << 8)
//                                            | ((buf[i+2] & 0xFF) << 16)
//                                            | ((buf[i+3] & 0xFF) << 24);
//                                    float asFloat = 0;
//                                    asFloat = Float.intBitsToFloat(asInt);
//                                    int k=0;
//                                    try{k = i/4;}catch(Exception e){}Y
//                                    sbuffer[k] = (short)(asFloat * Short.MAX_VALUE);
//
//                                    i=i+3;
//                                }
//                                track.write(sbuffer, 0, sbuffer.length);


                                //byte[] buffer = new byte[8192];

//                            FloatBuffer floatBuffer = ByteBuffer.wrap(packet.getData()).asFloatBuffer();
//                            float[] audioFloats = new float[floatBuffer.capacity()];
//                            floatBuffer.get(audioFloats);
//                            for (int i = 0; i < audioFloats.length; i++)
//                            { audioFloats[i] = audioFloats[i] / 0x8000000; }
//                            track.write(audioFloats, 0, audioFloats.length, AudioTrack.WRITE_NON_BLOCKING);
                                // long beforeTime = SystemClock.currentThreadTimeMillis(), afterTime = 0;


                                  Log.d("Sender",packet.getAddress().getHostAddress());


                                track.write(packet.getData(), 0, bufferSize);


//                                track.flush();
//
//
//                                buffer.flip();
                                // buffer.clear();


                                //  Log.d("Recieved BufferSize",socket.getReceiveBufferSize()+"");
                                // track.flush();
                                //Log.d("BufferSizeInFrames",track.getBufferSizeInFrames()+"");
                                //  track.write(packet.getData(), 0, bufferSize);

                                // Log.d("Writed",wr+"");

                                // Log.d("BufferSize",socket.getReceiveBufferSize()+"");

                                //  track.release();

                            }


                        }
                        if (enhancer != null) {
                            enhancer.release();
                        }
                        enhancer = null;
                        track.stop();
                        track.flush();
                        track.release();
                        socket.leaveGroup(group);
                        socket.disconnect();
                        socket.close();


                        isTrackStoped = true;
                        track = null;


                        speakers = false;
                        return;
                    } catch (SocketException e) {

                        //Log.e(LOG_TAG, "SocketExceptionTRack: " + e.toString());
                        speakers = false;
                    } catch (IOException e) {

                        // Log.e(LOG_TAG, "IOExceptionTrack: " + e.toString());
                        speakers = false;
                    } catch (IllegalStateException ex) {
                        ex.printStackTrace();
                    } catch (IllegalArgumentException ex) {
                        ex.printStackTrace();
                    } catch (Exception e) {
                        e.printStackTrace();
                        speakers = false;

                    }
                }
            });
            receiveThread.start();
        }
    }

    public void initiliazeTrack() {

    }

    static byte[] trim(byte[] bytes) {
        int i = bytes.length - 1;
        while (i >= 0 && bytes[i] == 0) {
            --i;
        }

        return Arrays.copyOf(bytes, i + 1);
    }

    private byte[] short2byte(byte[] data) {
        int dataSize = data.length;
        byte[] bytes = new byte[dataSize * 2];

        for (int i = 0; i < dataSize; i++) {
            bytes[i * 2] = (byte) (data[i] & 0x00FF);
            bytes[(i * 2) + 1] = (byte) (data[i] >> 8);
            data[i] = 0;
        }
        return bytes;
    }


    private javax.sound.sampled.AudioFormat getAudioFormat() {
        float sampleRate = 8000.0F;
        int sampleSizeInBits = 16;
        int channels = 1;
        boolean signed = true;
        boolean bigEndian = false;
        return new javax.sound.sampled.AudioFormat(sampleRate, sampleSizeInBits, channels, signed,
                bigEndian);
    }

    private AudioTrack generateTone(double freqHz, int durationMs, int type) {
        int count = (int) (SAMPLE_RATE * 2.0 * (durationMs / 1000.0)) & ~1;
        short[] samples = new short[count];
        for (int i = 0; i < count; i += 2) {
            short sample = (short) (Math.sin(2 * Math.PI * i / (SAMPLE_RATE / freqHz)) * 0x7FFF);
            samples[i + 0] = sample;
            samples[i + 1] = sample;
        }


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            track = new AudioTrack(
                    new AudioAttributes.Builder()
                            .setUsage(type)
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .build(),
                    new AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(SAMPLE_RATE)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_STEREO)
                            .build(), BUF_SIZE_AUDIO, AudioTrack.MODE_STREAM, 1);
        } else {
            track = new AudioTrack(AudioManager.STREAM_MUSIC, SAMPLE_RATE,
                    AudioFormat.CHANNEL_OUT_STEREO, AudioFormat.ENCODING_PCM_16BIT,
                    count * (Short.SIZE / 8), AudioTrack.MODE_STREAM, 1);
        }
        track.write(samples, 0, count);
        return track;
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        //Toast.makeText(context,"Service Started",Toast.LENGTH_SHORT).show();
        AccessibilityServiceInfo info = new AccessibilityServiceInfo();
        //  info.packageNames=new String[]{"com.android.mms"};
        info.eventTypes = AccessibilityEvent.TYPES_ALL_MASK;
        info.notificationTimeout = 100;
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_SPOKEN;
        info.flags = AccessibilityServiceInfo.FLAG_REQUEST_FILTER_KEY_EVENTS;
        setServiceInfo(info);
    }

    @Override
    protected boolean onKeyEvent(KeyEvent event) {
        // Toast.makeText(context,""+event.getAction(),Toast.LENGTH_SHORT).show();
        return handleKeyEvent(event);
    }

    private boolean handleKeyEvent(KeyEvent event) {
        int action = event.getAction();
        int keyCode = event.getKeyCode();
        //Log.d("KEYDOWN", "" + keyCode);
        // Toast.makeText(context, ""+keyCode, Toast.LENGTH_SHORT).show();
        if (action == KeyEvent.ACTION_DOWN) {
            SharedPreferences share122 = getSharedPreferences("recordaudiofrom", Activity.MODE_PRIVATE);
            recordaudiofromstr = share122.getString("recordaudiofrom", "");
            SharedPreferences share6 = getSharedPreferences("ptt_enable", Activity.MODE_PRIVATE);
            String ptt_enable = share6.getString("ptt_enable", "");
            if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN || keyCode == KeyEvent.KEYCODE_VOLUME_UP) {

                if (voice_detection_str.equals("0") && volume_ptt.equals("1")) {
                    //Toast.makeText(context, ""+keyCode, Toast.LENGTH_SHORT).show();
                    if (soundeffetct_str.equals("1")) {
                        mediaPlayer.start();
                    }
                    if (ptt_enable.equals("0")) {

                        SharedPreferences.Editor editor = getSharedPreferences("ptt_enable", MODE_PRIVATE).edit();
                        editor.putString("ptt_enable", "1");
                        editor.apply();
                    } else {

                        SharedPreferences.Editor editor = getSharedPreferences("ptt_enable", MODE_PRIVATE).edit();
                        editor.putString("ptt_enable", "0");
                        editor.apply();
                    }
                }
                return true;
            }
            if (keyCode == KeyEvent.KEYCODE_HEADSETHOOK) {

                if (voice_detection_str.equals("0") && volume_ptt.equals("1")) {
                    //Toast.makeText(context, ""+keyCode, Toast.LENGTH_SHORT).show();
                    if (soundeffetct_str.equals("1")) {
                        mediaPlayer.start();
                    }
                    if (ptt_enable.equals("0")) {

                        SharedPreferences.Editor editor = getSharedPreferences("ptt_enable", MODE_PRIVATE).edit();
                        editor.putString("ptt_enable", "1");
                        editor.apply();
                    } else {

                        SharedPreferences.Editor editor = getSharedPreferences("ptt_enable", MODE_PRIVATE).edit();
                        editor.putString("ptt_enable", "0");
                        editor.apply();
                    }
                }
                return true;
            }
        }
        return false;
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

            // Log.e(LOG_TAG, "UnknownHostException in getBroadcastIP: " + e);
            return null;
        } catch (NullPointerException ex) {
            ex.printStackTrace();
            return null;
        } catch (Exception ex) {
            ex.printStackTrace();
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

    public double getAmplitude(AudioRecord ar, int minSize) {
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


    public void Speak() {
        serverSocket = null;
        try {
            speakers = true;
            serverSocket = new DatagramSocket(null);
            serverSocket.setReuseAddress(true);
            serverSocket.setBroadcast(true);
            serverSocket.bind(new InetSocketAddress(port));
            byte[] receiveData = new byte[1280];

            Log.d("SPEAKER", "Starting");
            // ( 1280 for 16 000Hz and 3584 for 44 100Hz (use AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat) to get the correct size)

            format = new javax.sound.sampled.AudioFormat(SAMPLE_RATE, 16, 1, true, false);
            Log.d("SPEAKER", "Starting...");


            // A thread solve the problem of chunky audio
            new Thread(new Runnable() {
                @Override
                public void run() {

                    while (speakers) {


                        try {
                            Log.d("SPEAKER1", "running");
                            DatagramPacket receivePacket = new DatagramPacket(receiveData,
                                    receiveData.length);
                            serverSocket.receive(receivePacket);
                            ByteArrayInputStream baiss = new ByteArrayInputStream(
                                    receivePacket.getData());

                            ais = new AudioInputStream(baiss, format, receivePacket.getLength());

                            toSpeaker(receivePacket.getData());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }


                    }

                }
            }).start();


            if (ais != null) {
                ais.close();
                ais.reset();
            }


            serverSocket.disconnect();
            serverSocket.close();
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }


    public static void toSpeaker(byte soundbytes[]) {
        try {

            sourceDataLine.write(soundbytes, 0, soundbytes.length);
        } catch (Exception e) {
            System.out.println("Not working in speakers...");
            e.printStackTrace();
        }
    }



    public static void setInterface (MulticastSocket multicastSocket, boolean preferIpv6) throws IOException{
        boolean interfaceSet = false;
        Enumeration interfaces = NetworkInterface.getNetworkInterfaces();
        while (interfaces.hasMoreElements()) {
            NetworkInterface i = (NetworkInterface) interfaces.nextElement();
            Enumeration addresses = i.getInetAddresses();
            while (addresses.hasMoreElements()) {
                InetAddress address = (InetAddress) addresses.nextElement();
                if (preferIpv6 && address instanceof Inet6Address) {
                    multicastSocket.setInterface(address);
                    interfaceSet = true;
                    break;
                } else if (!preferIpv6 && address instanceof Inet4Address) {
                    multicastSocket.setInterface(address);
                    interfaceSet = true;
                    break;
                }
            }
            if (interfaceSet) {
                break;
            }
        }
    }


}

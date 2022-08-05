package com.shageldi.intercom;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.RadioGroup;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;

import static com.shageldi.intercom.App.CHANNEL_ID;

public class ExampleService extends Service {


    static final String LOG_TAG = "UDPchat";
    private static final int LISTENER_PORT = 50003;
    private static final int BUF_SIZE = 1024;
    private ContactManager contactManager;
    private String displayName;
    private boolean STARTED = false;
    private boolean IN_CALL = false;
    private boolean LISTEN = false;

    public final static String EXTRA_CONTACT = "hw.dt83.udpchat.CONTACT";
    public final static String EXTRA_IP = "hw.dt83.udpchat.IP";
    public final static String EXTRA_DISPLAYNAME = "hw.dt83.udpchat.DISPLAYNAME";



    // receive call


    private static final String LOG_TAG_RECEIVE_CALL = "ReceiveCall";
    private static final int BROADCAST_PORT_RECEIVE_CALL = 50002;
    private static final int BUF_SIZE_RECEIVE_CALL = 1024;
    private String contactIp_RECEIVE_CALL;
    private String contactName_RECEIVE_CALL;
    private boolean LISTEN_RECEIVE_CALL = true;
    private boolean IN_CALL_RECEIVE_CALL = false;
    private AudioCall call;
  //  LinearLayout end_RECEIVE_CALL,calll_RECEIVE_CALL;
    MediaPlayer mediaPlayer;

    Timer timer;
    Handler handler;
    int san=1;
    @Override
    public void onCreate() {
        super.onCreate();

    }

    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {
        timer=new Timer();
        mediaPlayer = MediaPlayer.create(ExampleService.this,R.raw.call);
        mediaPlayer.setVolume(100,100);
        SharedPreferences share = getSharedPreferences("Settings", Activity.MODE_PRIVATE);
        displayName = share.getString("username", "");

        if(displayName.isEmpty()){

            String str = android.os.Build.MODEL;
            SharedPreferences.Editor editor = getSharedPreferences("Settings", MODE_PRIVATE).edit();
            editor.putString("username", str);
            editor.apply();
            displayName=str;
        }
        contactManager = new ContactManager(displayName, getBroadcast(getIpAddressNew()));
        startCallListener();
        handler=new Handler(Looper.getMainLooper());

//        timer.scheduleAtFixedRate(new TimerTask() {
//            @Override
//            public void run() {
//
//                handler.post(new Runnable() {
//                    @Override
//                    public void run() {

//                        SharedPreferences sh = getSharedPreferences("push", Activity.MODE_PRIVATE);
//                        String p = sh.getString("n", "");
//                        if(p.equals("1")) {
                            String input=intent.getStringExtra("inputExtra");
                            Intent notificationIntent=new Intent(ExampleService.this,MainActivity.class);
                            PendingIntent pendingIntent=PendingIntent.getActivity(ExampleService.this,
                                    0,notificationIntent,0);
                            Notification notification=new NotificationCompat.Builder(ExampleService.this,CHANNEL_ID)
                                    .setContentTitle(ExampleService.this.getResources().getString(R.string.returntoapp))
                                    .setContentText("GeekSpace")
                                    .setSmallIcon(R.mipmap.ic_launcher)
                                    .setContentIntent(pendingIntent)
                                    .setNotificationSilent()
                                    .build();

        notification.flags = Notification.FLAG_INSISTENT | Notification.FLAG_AUTO_CANCEL;

        startCallListener();

        startForeground(san,notification);

        NotificationManager notificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.cancelAll();






//       NotificationManager mNotificationManager =
//                (NotificationManager) ExampleService.this.getSystemService(Context.NOTIFICATION_SERVICE);
//
//       mNotificationManager.cancel(san);

                      //  }

//                    }
//                });
//
//
//            }
//        },0,1000);


        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        endCall();
        stopCallListener();
       // if(getIpAddressNew()!=null) {
                contactManager.bye(displayName);
                contactManager.stopBroadcasting();
                contactManager.stopListening("Service");
        //    }


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

        stopSelf();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
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
            }
        }
        return null;
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
                    while(LISTEN) {
                        // Listen for incoming call requests
                        try {
                            Log.i(LOG_TAG, "Listening for incoming calls");
                            socket.receive(packet);
                            String data = new String(buffer, 0, packet.getLength());
                            Log.i(LOG_TAG, "Packet received from "+ packet.getAddress() +" with contents: " + data);
                            String action = data.substring(0, 4);
                            if(action.equals("CAL:")) {
                                // Received a call request. Start the ReceiveCallActivity
                                String address = packet.getAddress().toString();
                                String name = data.substring(4, packet.getLength());

                                Intent intent = new Intent(ExampleService.this, ReceiveCallActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                intent.putExtra(EXTRA_CONTACT, name);
                                intent.putExtra(EXTRA_IP, address.substring(1, address.length()));
                                IN_CALL = true;
                                //LISTEN = false;
                                //stopCallListener();
                                //startActivity(intent);

                                mediaPlayer.start();



                                contactName_RECEIVE_CALL = name;
                                contactIp_RECEIVE_CALL = address.substring(1, address.length());
                                startListener();

                                sendMessage("ACC:");
                                InetAddress address2 = InetAddress.getByName(address.substring(1, address.length()));
                                Log.i(LOG_TAG_RECEIVE_CALL, "Calling " + address.toString());
                                IN_CALL_RECEIVE_CALL = true;
                                call = new AudioCall(address2,ExampleService.this);
                                call.startCall();

                               // String str = android.os.Build.MODEL;
                                SharedPreferences.Editor editor = getSharedPreferences("INCALL", MODE_PRIVATE).edit();
                                editor.putString("INCALL", "1");
                                editor.apply();

                                SharedPreferences.Editor editor2 = getSharedPreferences("INCALLIP", MODE_PRIVATE).edit();
                                editor2.putString("INCALLIP", contactIp_RECEIVE_CALL);
                                editor2.apply();

                                SharedPreferences.Editor editor3 = getSharedPreferences("INCALLNAME", MODE_PRIVATE).edit();
                                editor3.putString("INCALLNAME", contactName_RECEIVE_CALL);
                                editor3.apply();

                                //updateContactList();
                            }
                            else {
                                // Received an invalid request
                                Log.w(LOG_TAG, packet.getAddress() + " sent invalid message: " + data);
                            }
                        }
                        catch(Exception e) {}
                    }
                    Log.i(LOG_TAG, "Call Listener ending");
                    socket.disconnect();
                    socket.close();
                }
                catch(SocketException e) {

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


    private void endCall() {
        // End the call and send a notification
        stopListener();
        if(IN_CALL_RECEIVE_CALL) {

            call.endCall();
        }

        SharedPreferences.Editor editor = getSharedPreferences("INCALL", MODE_PRIVATE).edit();
        editor.putString("INCALL", "0");
        editor.apply();

        SharedPreferences.Editor editor2 = getSharedPreferences("INCALLIP", MODE_PRIVATE).edit();
        editor2.putString("INCALLIP", "");
        editor2.apply();

        SharedPreferences.Editor editor3 = getSharedPreferences("INCALLNAME", MODE_PRIVATE).edit();
        editor3.putString("INCALLNAME", "");
        editor3.apply();


        sendMessage("END:");
       // finish();
    }

    private void startListener() {
        // Creates the listener thread
        LISTEN_RECEIVE_CALL = true;
        Thread listenThread = new Thread(new Runnable() {

            @Override
            public void run() {

                try {

                    Log.i(LOG_TAG_RECEIVE_CALL, "Listener started!");
                    DatagramSocket socket = new DatagramSocket(BROADCAST_PORT_RECEIVE_CALL);
                    socket.setSoTimeout(1500);
                    byte[] buffer = new byte[BUF_SIZE_RECEIVE_CALL];
                    DatagramPacket packet = new DatagramPacket(buffer, BUF_SIZE_RECEIVE_CALL);
                    while(LISTEN_RECEIVE_CALL) {

                        try {

                            Log.i(LOG_TAG_RECEIVE_CALL, "Listening for packets");
                            socket.receive(packet);
                            String data = new String(buffer, 0, packet.getLength());
                            Log.i(LOG_TAG_RECEIVE_CALL, "Packet received from "+ packet.getAddress() +" with contents: " + data);
                            String action = data.substring(0, 4);
                            if(action.equals("END:")) {
                                // End call notification received. End call
                                endCall();
                            }
                            else {
                                // Invalid notification received.
                                Log.w(LOG_TAG_RECEIVE_CALL, packet.getAddress() + " sent invalid message: " + data);
                            }
                        }
                        catch(IOException e) {

                            Log.e(LOG_TAG_RECEIVE_CALL, "IOException in Listener " + e);
                        }
                    }
                    Log.i(LOG_TAG_RECEIVE_CALL, "Listener ending");
                    socket.disconnect();
                    socket.close();
                    return;
                }
                catch(SocketException e) {

                    Log.e(LOG_TAG_RECEIVE_CALL, "SocketException in Listener " + e);
                    if(IN_CALL_RECEIVE_CALL) {
                        endCall();
                    }
                }
            }
        });
        listenThread.start();
    }

    private void stopListener() {
        // Ends the listener thread
        LISTEN_RECEIVE_CALL = false;
    }

    private void sendMessage(final String message) {
        // Creates a thread for sending notifications
        Thread replyThread = new Thread(new Runnable() {

            @Override
            public void run() {

                try {

                    InetAddress address = InetAddress.getByName(contactIp_RECEIVE_CALL);
                    byte[] data = message.getBytes();
                    DatagramSocket socket = new DatagramSocket();
                    DatagramPacket packet = new DatagramPacket(data, data.length, address, BROADCAST_PORT_RECEIVE_CALL);
                    socket.send(packet);
                    Log.i(LOG_TAG_RECEIVE_CALL, "Sent message( " + message + " ) to " + contactIp_RECEIVE_CALL);
                    socket.disconnect();
                    socket.close();
                }
                catch(UnknownHostException e) {

                    Log.e(LOG_TAG_RECEIVE_CALL, "Failure. UnknownHostException in sendMessage: " + contactIp_RECEIVE_CALL);
                }
                catch(SocketException e) {

                    Log.e(LOG_TAG_RECEIVE_CALL, "Failure. SocketException in sendMessage: " + e);
                }
                catch(IOException e) {

                    Log.e(LOG_TAG_RECEIVE_CALL, "Failure. IOException in sendMessage: " + e);
                }
            }
        });
        replyThread.start();
    }


}

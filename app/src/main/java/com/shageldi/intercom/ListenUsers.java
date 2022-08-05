package com.shageldi.intercom;

import android.content.Context;
import android.util.Log;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;

public class ListenUsers {
    private static final String LOG_TAG="HomePage";
    private static final int LISTENER_PORT = 40005;
    private static final int BROADCAST_INTERVAL = 10000; // Milliseconds
    private static final int BUF_SIZE = 1024;
    private boolean BROADCAST = true;
    private boolean LISTEN = false;
    private ArrayList<Ulanyjylar> users;
    private ArrayList<InetAddress> ips=new ArrayList<>();
    Context context;
    private InetAddress myAddress;

    public ListenUsers(Context context, InetAddress myAddress) {
        users=new ArrayList<>();
        this.context = context;
        this.myAddress = myAddress;
    }

    public void AddContact(String name,InetAddress address){
        boolean isHas=false;
        for(int i=0;i<users.size();i++){
            Ulanyjylar ulanyjylar=users.get(i);
            if(ulanyjylar.getAddress().equals(address)){
                Log.d(LOG_TAG,"Ulanyjy uytgedildi");
                users.set(i,new Ulanyjylar(name,address));
                isHas=true;

                break;
            }
        }

        if(!isHas){
            users.add(new Ulanyjylar(name,address));

        }
    }

    public void RemoveContact(String name,InetAddress address){
        boolean isHas=false;
        for(int i=0;i<users.size();i++){
            Ulanyjylar ulanyjylar=users.get(i);
            if(ulanyjylar.getAddress().equals(address)){
                Log.d(LOG_TAG,"Ulanyjy pozulyar");
                users.remove(i);
                isHas=true;
                break;
            }
        }

        if(!isHas){
            Log.d(LOG_TAG,"Beyle ulanyjy yok");
        }
    }

    public ArrayList<Ulanyjylar> getUsers(){
        return users;
    }




}

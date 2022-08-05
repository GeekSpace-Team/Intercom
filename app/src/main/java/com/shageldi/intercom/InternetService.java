package com.shageldi.intercom;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;

import androidx.annotation.Nullable;

import java.lang.reflect.InvocationTargetException;

public class InternetService extends Service {

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implement");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        handler.post(periodicUpdate);
        return START_STICKY;
    }

    public boolean isOnline(Context c) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        ConnectivityManager cm=(ConnectivityManager)c.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni=cm.getActiveNetworkInfo();

        final WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        final int apState = (Integer) wifiManager.getClass().getMethod("getWifiApState").invoke(wifiManager);

        if(ni!=null && ni.isConnectedOrConnecting() || apState==13)
            return true;
        else if(apState==0 || wifiManager.getWifiState()==0) {
            Log.d("STATE","Disabling");
            return false;
        }
        else
            return false;
    }

    Handler handler=new Handler();

    private Runnable periodicUpdate=new Runnable() {
        @Override
        public void run() {
            handler.postDelayed(periodicUpdate,1*1000- SystemClock.elapsedRealtime()%1000);

            Intent broadcast=new Intent();
            broadcast.setAction(CallService.BroadcastStringForAction);
            try {
                broadcast.putExtra("online_status",""+isOnline(InternetService.this));
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
            sendBroadcast(broadcast);
        }
    };
}

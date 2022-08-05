package com.shageldi.intercom;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.widget.Toast;

public class StartActivityOnBootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
       // Toast.makeText(context,"On Boot Completed",Toast.LENGTH_SHORT).show();
        if(Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())){
//            Intent i=new Intent(context,MainActivity.class);
//            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//            context.startActivity(i);

//            Intent ii = new Intent("com.shageldi.intercom.CallService");
//            ii.setClass(context, CallService.class);
            SharedPreferences share6 = context.getSharedPreferences("autostart", Activity.MODE_PRIVATE);
            String autostartstr = share6.getString("autostart", "");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if(autostartstr.equals("2")) {
                    context.startForegroundService(new Intent(context, CallService.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                }
            } else {
                if(autostartstr.equals("2")) {
                    context.startService(new Intent(context, CallService.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                }
            }
//
//
//                String string="OK";
//                Intent serviceIntent=new Intent(context,ExampleService.class);
//                serviceIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                serviceIntent.putExtra("inputExtra",string);
//                context.startService(serviceIntent);

        }
    }
}

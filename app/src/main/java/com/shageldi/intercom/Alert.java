package com.shageldi.intercom;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class Alert {
    Dialog dialog;
    Context context;
    String yes,t1,t2;

    public Alert(Context context, String yes, String t1, String t2) {
        this.context = context;
        this.yes = yes;
        this.t1 = t1;
        this.t2 = t2;
    }

    void ShowDialog() {
        Button back;
        TextView tv1,tv2;
        ImageView status;


        dialog=new Dialog(context);
        dialog.setContentView(R.layout.custom_alert);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        Window window=dialog.getWindow();
        window.setGravity(Gravity.CENTER);
        window.getAttributes().windowAnimations=R.style.DialogAnimation;

        back=dialog.findViewById(R.id.back);
        tv1=dialog.findViewById(R.id.t1);
        tv2=dialog.findViewById(R.id.t2);
        status=dialog.findViewById(R.id.status);


        tv1.setText(t1);
        tv2.setText(t2);
        if(yes.equals("yes")){
            status.setImageResource(R.drawable.smile);
            tv1.setTextColor(Color.GREEN);
            back.setBackgroundResource(R.drawable.green);

        } else if(yes.equals("no")){
            status.setImageResource(R.drawable.sad_emoji);
            tv1.setTextColor(Color.RED);
            back.setBackgroundResource(R.drawable.r_s);
        }



        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                ((Activity) context).finish();
            }
        });

        dialog.setCancelable(false);
        window.setLayout(ActionBar.LayoutParams.MATCH_PARENT,ActionBar.LayoutParams.WRAP_CONTENT);
        dialog.show();

    }
}

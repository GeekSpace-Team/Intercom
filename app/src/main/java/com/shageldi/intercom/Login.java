package com.shageldi.intercom;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

public class Login extends AppCompatActivity {

    TextView tv;
    Integer san1=null,san2=null;
    String alamat="";
    Button minus,plus,equals,devide,multiply,clear;
    ImageButton clear_one;
    String password="";
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        tv=findViewById(R.id.tv);
        minus=findViewById(R.id.minus);
        plus=findViewById(R.id.goshmak);
        equals=findViewById(R.id.equals);
        devide=findViewById(R.id.bolmek);
        multiply=findViewById(R.id.kopeltmek);
        clear=findViewById(R.id.clear);
        clear_one=findViewById(R.id.clear_one);

        SharedPreferences share = getSharedPreferences("ok", Activity.MODE_PRIVATE);
        password = share.getString("ok", "");






       // Toast.makeText(Login.this, ""+getDate(now.getSeconds()), Toast.LENGTH_SHORT).show();













        clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tv.setText("");
                san1=null;
                san2=null;
                alamat="";
            }
        });

        clear_one.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(tv.getText().toString().length()!=0){
                    int len=tv.getText().toString().length()-1;
                    tv.setText(tv.getText().toString().substring(0,len));
                }
            }
        });

        equals.setOnClickListener(new DoubleClickListener() {
            @Override
            public void onDoubleClick() {
                if(tv.getText().toString().length()>=4){





                    SharedPreferences share12 = getSharedPreferences("LastDate", Activity.MODE_PRIVATE);
                    String LastDate = share12.getString("LastDate", "");
                    if(password.isEmpty()){
                        SharedPreferences.Editor editor4 = getSharedPreferences("ok", MODE_PRIVATE).edit();
                        editor4.putString("ok", tv.getText().toString());
                        editor4.apply();
                        finish();
                        startActivity(new Intent(Login.this, Register.class));
                    } else if(password.equals(tv.getText().toString())){
                        if(LastDate.isEmpty()) {
                            finish();
                            startActivity(new Intent(Login.this, Register.class));
                        } else{
                            finish();
                            startActivity(new Intent(Login.this, HomePage.class));
                        }
                    }

                }
            }
        });




    }

    public void hasapla(){
        Integer result=null;
        if(alamat.equals("+")){
            result=san1+san2;
        }
        if(alamat.equals("-")){
            result=san1-san2;
        }
        if(alamat.equals("*")){
            result=san1*san2;
        }
        if(alamat.equals("/")){
            result=san1/san2;
        }

        tv.setText(""+result);
        san1=null;
        san2=null;
        alamat="";



    }



    public void buttons(View view){
        Button button=(Button) view;
        if(button.getText().toString().equals(".")){
            for(int i=0;i<tv.getText().toString().length();i++){
                String item=tv.getText().toString().charAt(i)+"";
                if(item.equals(".")){

                    return;
                }
            }
        }

            tv.append(button.getText().toString());


    }

    public void alamats(View view){
        Button button=(Button) view;
        alamat=button.getText().toString();
        if(san1==null){
            san1=Integer.parseInt(tv.getText().toString());
            tv.setText("");
        } else{
            san2=Integer.parseInt(tv.getText().toString());
            hasapla();

        }
    }


    public abstract class DoubleClickListener implements View.OnClickListener {

        // The time in which the second tap should be done in order to qualify as
        // a double click
        private static final long DEFAULT_QUALIFICATION_SPAN = 200;
        private long doubleClickQualificationSpanInMillis;
        private long timestampLastClick;

        public DoubleClickListener() {
            doubleClickQualificationSpanInMillis = DEFAULT_QUALIFICATION_SPAN;
            timestampLastClick = 0;
        }

        public DoubleClickListener(long doubleClickQualificationSpanInMillis) {
            this.doubleClickQualificationSpanInMillis = doubleClickQualificationSpanInMillis;
            timestampLastClick = 0;
        }

        @Override
        public void onClick(View v) {
           // Toast.makeText(Login.this, "One", Toast.LENGTH_SHORT).show();

            if((SystemClock.elapsedRealtime() - timestampLastClick) < doubleClickQualificationSpanInMillis) {
                onDoubleClick();
            } else{
                if(san1==null || alamat.isEmpty()){

                } else{
                    san2=Integer.parseInt(tv.getText().toString());

                    hasapla();
                }

            }
            timestampLastClick = SystemClock.elapsedRealtime();
        }

        public abstract void onDoubleClick();

    }
}
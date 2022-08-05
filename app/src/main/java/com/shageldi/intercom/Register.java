package com.shageldi.intercom;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

public class Register extends AppCompatActivity {
    EditText username, password;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences share12 = getSharedPreferences("LastDate", Activity.MODE_PRIVATE);
        String LastDate = share12.getString("LastDate", "");

        if (!LastDate.isEmpty()) {
            finish();
            startActivity(new Intent(Register.this, HomePage.class));
        }

        setContentView(R.layout.regist);
        username = findViewById(R.id.username);
        password = findViewById(R.id.password);

//        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
//                .setPersistenceEnabled(false)
//                .build();
//
//        db.setFirestoreSettings(settings);

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Maglumatlar barlanýar");
        progressDialog.setMessage("Biraz garaşyň...");
        progressDialog.setCancelable(false);


        findViewById(R.id.regist).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (username.getText().toString().trim().isEmpty() || password.getText().toString().trim().isEmpty()) {
                    Toast.makeText(Register.this, "Maglumatlary doly girizin!!!", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!isOnline()) {
                    Toast.makeText(Register.this, "Internet baglanşygyny gözden geçiriň we gaýtadan synanşyň!!!", Toast.LENGTH_SHORT).show();
                    return;
                }
                progressDialog.show();


                db.collection("userstable0907").whereEqualTo("password0965", password.getText().toString()).whereEqualTo("username0976", username.getText().toString()).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            if (task.getResult().size() == 0) {
                                Toast.makeText(Register.this, "Ulanyjy adyňyz ýa-da açar sözüňiz nädogry!!!", Toast.LENGTH_SHORT).show();
                            }
                            for (QueryDocumentSnapshot snapshot : task.getResult()) {
                                if (snapshot.get("isEnable").toString().equals("1")) {
                                    DateFormat dateFormat = new SimpleDateFormat("dd/M/yyyy hh:mm:ss");
                                    Checker obj = new Checker();
                                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/M/yyyy hh:mm:ss");

                                    try {
                                        Date date = new Date();
                                        String datestr = dateFormat.format(date);
                                        Date date1 = simpleDateFormat.parse(datestr);
//                                        Date date2 = simpleDateFormat.parse(snapshot.get("timelimit").toString());

                                        //Timestamp now = com.google.firebase.Timestamp.now();


                                        // Toast.makeText(Register.this, ""+now, Toast.LENGTH_SHORT).show();

//                                        long days = obj.printDifference(date1, date2);

                                        int count_s = Integer.parseInt(snapshot.get("login_count").toString());
                                        count_s = count_s + 1;
                                        Map<String, Object> up = new HashMap<>();
                                        up.put("login_count", count_s + "");
                                        db.collection("userstable0907").document(snapshot.getId()).update(up);
                                        Timestamp now = com.google.firebase.Timestamp.now();
                                        SharedPreferences.Editor editor = getSharedPreferences("progressbar", MODE_PRIVATE).edit();
                                        editor.putString("progressbar", snapshot.get("EncryptionAndDecryptionKey").toString());
                                        editor.apply();

                                        GetAfter getAfter = new GetAfter();
                                        Date exDate = getAfter.getBetween(simpleDateFormat.parse(getDate(now.getSeconds())), Integer.parseInt(snapshot.get("timelimit").toString()));

                                        SharedPreferences.Editor editor2 = getSharedPreferences("limitoftime", MODE_PRIVATE).edit();
                                        editor2.putString("limitoftime", dateFormat.format(exDate));
                                        editor2.apply();

                                        registSettings();

                                        Toast.makeText(Register.this, exDate.toString(), Toast.LENGTH_SHORT).show();


                                        //  Date date3=simpleDateFormat()

//

                                        SharedPreferences.Editor prr = getSharedPreferences("LastDate", MODE_PRIVATE).edit();
                                        prr.putString("LastDate", getDate(now.getSeconds()));
                                        prr.apply();


                                        finish();
                                        startActivity(new Intent(Register.this, HomePage.class));


                                    } catch (ParseException e) {
                                        e.printStackTrace();
                                    }
                                } else{
                                    Toast.makeText(Register.this, "Bu hasap admin tarapyndan tassyklanmadyk!", Toast.LENGTH_SHORT).show();
                                }

                                //Toast.makeText(Register.this, snapshot.get("EncryptionAndDecryptionKey").toString(), Toast.LENGTH_SHORT).show();
                            }
                            progressDialog.dismiss();
                        }
                    }
                });

            }
        });
    }

    private String getDate(long time) {
        Date date = new Date(time * 1000L); // *1000 is to convert seconds to milliseconds
        SimpleDateFormat sdf = new SimpleDateFormat("dd/M/yyyy hh:mm:ss"); // the format of your date
        sdf.setTimeZone(TimeZone.getTimeZone("GMT+5"));
        return sdf.format(date);
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

    public boolean isOnline() {

        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnectedOrConnecting()) {
            return true;
        } else {
            return false;
        }
    }

    public void gotoCreateAccount(View view) {
        startActivity(new Intent(Register.this, CreateAccount.class));
    }
}
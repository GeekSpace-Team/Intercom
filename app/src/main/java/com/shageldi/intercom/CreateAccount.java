package com.shageldi.intercom;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class CreateAccount extends AppCompatActivity {
    EditText username,password,gun;
    Button button;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    ProgressDialog progressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);
        username=findViewById(R.id.username);
        password=findViewById(R.id.password);
        gun=findViewById(R.id.gun);
        button=findViewById(R.id.button);

        progressDialog=new ProgressDialog(this);
        progressDialog.setTitle("Maglumatlar goşulýar");
        progressDialog.setMessage("Biraz garaşyň...");
        progressDialog.setCancelable(false);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    if (username.getText().toString().trim().isEmpty()) {
                        Toast.makeText(CreateAccount.this, "Ulanyjy adyny doly giriziň!", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (password.getText().toString().trim().isEmpty()) {
                        Toast.makeText(CreateAccount.this, "Açar sözüni doly giriziň!", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (gun.getText().toString().trim().isEmpty()) {
                        Toast.makeText(CreateAccount.this, "Gününi doly giriziň!", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (Integer.parseInt(gun.getText().toString()) < 0) {
                        Toast.makeText(CreateAccount.this, "Güni kabul edilmedi!", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    progressDialog.show();
                    RandomString gen=null;
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                        gen = new RandomString(16, ThreadLocalRandom.current());
                       // Toast.makeText(CreateAccount.this,gen.nextString(),Toast.LENGTH_SHORT).show();
                    }
                    Map<String, Object> user=new HashMap<>();
                    user.put("EncryptionAndDecryptionKey",gen.nextString());
                    user.put("isEnable","0");
                    user.put("login_count","0");
                    user.put("password0965",password.getText().toString());
                    user.put("timelimit",gun.getText().toString());
                    user.put("username0976",username.getText().toString());

                    db.collection("userstable0907").add(user).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentReference> task) {
                            if(task.isSuccessful()){
                                Toast.makeText(CreateAccount.this, "Hasaba alyndy", Toast.LENGTH_SHORT).show();
                                progressDialog.dismiss();
                                onBackPressed();
                            } else{
                                Toast.makeText(CreateAccount.this,"Ýalňyşlyk ýüze çykdy!",Toast.LENGTH_SHORT).show();
                                progressDialog.dismiss();
                            }
                        }
                    });


                } catch (Exception ex){
                    ex.printStackTrace();
                }

            }
        });


    }

    public void yza(View view){
        onBackPressed();
    }


}
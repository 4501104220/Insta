package com.dev.insta;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity {
    EditText password,email;
    Button login;
    TextView text_signup;
    FirebaseAuth mauth;
    DatabaseReference reference;
    ProgressDialog pd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        password = findViewById(R.id.password);
        email = findViewById(R.id.email);
        login = findViewById(R.id.login);
        text_signup = findViewById(R.id.txt_singnp);
        mauth = FirebaseAuth.getInstance();

        text_signup.setOnClickListener(view -> startActivity(new Intent(LoginActivity.this, RegisterActivity.class)));


        login.setOnClickListener(view -> {

            pd = new ProgressDialog(LoginActivity.this);
            pd.setMessage("Please wait...");
            pd.show();
          //  String str_username= username.getText().toString();
          //  String str_fullname= fullname.getText().toString();
            String str_email= email.getText().toString();
            String str_password= password.getText().toString();

            if(TextUtils.isEmpty(str_email) || TextUtils.isEmpty(str_password)){

                Toast.makeText(LoginActivity.this,"All fields are required", Toast.LENGTH_SHORT).show();

            }else if(str_password.length()<6) {
                Toast.makeText(LoginActivity.this,"Password must have 6 characters", Toast.LENGTH_SHORT).show();

            }else {

                mauth.signInWithEmailAndPassword(str_email,str_password).addOnCompleteListener(LoginActivity.this, task -> {

                    if (task.isSuccessful()){

                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Users")
                                .child(mauth.getCurrentUser().getUid());

                        reference.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                pd.dismiss();

                                Intent intent = new Intent(LoginActivity.this,MainActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                                finish();

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                                pd.dismiss();
                            }
                        });
                    }else {
                        Toast.makeText(LoginActivity.this,"Authentication failed!", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
}

package com.dev.insta;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class StartActivity extends AppCompatActivity {

    Button login,register;
    FirebaseUser firebaseUser;

    @Override
    protected void onStart() {
        super.onStart();
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        if (firebaseUser != null){

            startActivity(new Intent(com.dev.insta.StartActivity.this,MainActivity.class));
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        login = findViewById(R.id.login_btn);
        register = findViewById(R.id.Register);

        login.setOnClickListener(view -> startActivity(new Intent(StartActivity.this,LoginActivity.class)));

        register.setOnClickListener(view -> startActivity(new Intent(StartActivity.this, RegisterActivity.class)));

    }
}

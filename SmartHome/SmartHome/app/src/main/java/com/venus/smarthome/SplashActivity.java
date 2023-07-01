package com.venus.smarthome;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_DURATION = 1200;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        new Handler().postDelayed(() -> {
            Class<?> destinationActivity = (user != null) ? MainActivity.class : LoginActivity.class;
            startActivity(new Intent(SplashActivity.this, destinationActivity));
            finish();
        }, SPLASH_DURATION);
    }
}

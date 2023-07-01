package com.venus.smarthome;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.venus.smarthome.Fragments.HomeFragment;
import com.venus.smarthome.Fragments.SettingFragment;
import com.venus.smarthome.Fragments.VoiceFragment;

public class MainActivity extends AppCompatActivity {
    private BottomNavigationView bottomNavigationView;
    private HomeFragment homeFragment = new HomeFragment();
    private VoiceFragment voiceFragment = new VoiceFragment();
    private SettingFragment settingFragment = new SettingFragment();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        getSupportFragmentManager().beginTransaction().replace(R.id.main_container, homeFragment).commit();

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.home) {
                getSupportFragmentManager().beginTransaction().replace(R.id.main_container, homeFragment).commit();
                return true;
            } else if (id == R.id.voice) {
                getSupportFragmentManager().beginTransaction().replace(R.id.main_container, voiceFragment).commit();
                return true;
            } else if (id == R.id.setting) {
                getSupportFragmentManager().beginTransaction().replace(R.id.main_container, settingFragment).commit();
                return true;
            }
            return false;
        });
    }
}

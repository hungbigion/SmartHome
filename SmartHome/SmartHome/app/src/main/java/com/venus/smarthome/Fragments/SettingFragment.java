package com.venus.smarthome.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.venus.smarthome.ConfigActivity;
import com.venus.smarthome.LoginActivity;
import com.venus.smarthome.R;

import java.util.Objects;

public class SettingFragment extends Fragment {

    private FirebaseAuth auth;
    private DatabaseReference dbRef, refUser, refDevice;
    private String uid;
    private MaterialCardView btnChangeEmail, btnChangePassword, btnConfig, btnAbout;
    private Button btnLogout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_setting, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initializeViews(view);
        setupClickListeners();
        setupFirebase();
    }

    private void initializeViews(View view) {
        btnChangeEmail = view.findViewById(R.id.btnChangeEmail);
        btnChangePassword = view.findViewById(R.id.btnChangePassword);
        btnConfig = view.findViewById(R.id.btnConfig);
        btnAbout = view.findViewById(R.id.btnAbout);
        btnLogout = view.findViewById(R.id.btnLogout);
    }

    private void setupClickListeners() {
        btnChangeEmail.setOnClickListener(v -> replaceFragment(new ChangeEmailFragment()));

        btnChangePassword.setOnClickListener(v -> replaceFragment(new ChangePasswordFragment()));

        btnConfig.setOnClickListener(v -> startActivity(new Intent(requireActivity(), ConfigActivity.class)));

        btnAbout.setOnClickListener(v -> replaceFragment(new AboutFragment()));

        btnLogout.setOnClickListener(v -> {
            auth.signOut();
            startActivity(new Intent(requireActivity(), LoginActivity.class));
            requireActivity().finish();
        });
    }

    private void replaceFragment(Fragment fragment) {
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.main_container, fragment)
                .commit();
    }

    private void setupFirebase() {
        auth = FirebaseAuth.getInstance();
        uid = Objects.requireNonNull(auth.getCurrentUser()).getUid();
        dbRef = FirebaseDatabase.getInstance().getReference("users");
        refUser = dbRef.child(uid);
        refDevice = refUser.child("devices");
    }
}

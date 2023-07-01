package com.venus.smarthome.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.venus.smarthome.R;

public class AboutFragment extends Fragment {

    View v;
    ImageView imgButtonBack;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_about, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        imgButtonBack = view.findViewById(R.id.imgButtonBack);

        imgButtonBack.setOnClickListener(v -> getActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.main_container,new SettingFragment()).commit());
    }
}
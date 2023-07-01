package com.venus.smarthome.Fragments;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.venus.smarthome.Modal.Device;
import com.venus.smarthome.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class EditDeviceFragment extends Fragment {
    private List<Device> deviceList;
    private FirebaseAuth auth;
    private DatabaseReference userRef, deviceRef;
    private String uid;
    private ImageView imgButtonBack;
    private EditText edtNameDevice;
    private RadioButton radioRelay1, radioRelay2, radioRelay3, radioServo, radioToggle, radioAdjust;
    private Button btnEditDevice;
    private Device currentDevice;

    public static EditDeviceFragment newInstance(Device device) {
        EditDeviceFragment fragment = new EditDeviceFragment();
        Bundle args = new Bundle();
        args.putParcelable("device", device);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_edit_device, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        deviceList = new ArrayList<>();

        auth = FirebaseAuth.getInstance();
        uid = Objects.requireNonNull(auth.getCurrentUser()).getUid();
        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        userRef = rootRef.child("users").child(uid);
        deviceRef = userRef.child("devices");
        imgButtonBack = view.findViewById(R.id.imgButtonBack);
        edtNameDevice = view.findViewById(R.id.edtNameDevice);
        radioRelay1 = view.findViewById(R.id.radioRelay1);
        radioRelay2 = view.findViewById(R.id.radioRelay2);
        radioRelay3 = view.findViewById(R.id.radioRelay3);
        radioServo = view.findViewById(R.id.radioServo);
        radioToggle = view.findViewById(R.id.radioToggle);
        radioAdjust = view.findViewById(R.id.radioAdjust);
        btnEditDevice = view.findViewById(R.id.btnEditDevice);

        if (getArguments() != null) {
            currentDevice = getArguments().getParcelable("device");
            if (currentDevice != null) {
                populateDeviceData();
            }
        }

        deviceRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                deviceList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Device device = dataSnapshot.getValue(Device.class);
                    if (device != null) {
                        deviceList.add(device);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        imgButtonBack.setOnClickListener(v -> {
            getActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.main_container, new HomeFragment()).commit();
        });

        btnEditDevice.setOnClickListener(v -> {
            if (currentDevice == null) {
                return;
            }

            String name = edtNameDevice.getText().toString().trim();
            String id = currentDevice.getId();
            String type = currentDevice.getType();
            int speed = currentDevice.getSpeed();
            int status = currentDevice.getStatus();

            if (TextUtils.isEmpty(name)) {
                Toast.makeText(getContext(), "Nhập tên thiết bị!", Toast.LENGTH_SHORT).show();
                return;
            }

            if (radioServo.isChecked() && radioAdjust.isChecked()) {
                Toast.makeText(getContext(), "Bạn phải chọn Loại thiết bị Bật Tắt khi chọn chân thiết bị Servo!", Toast.LENGTH_SHORT).show();
                return;
            }

            if (radioRelay1.isChecked()) {
                id = "relay1";
            } else if (radioRelay2.isChecked()) {
                id = "relay2";
            } else if (radioRelay3.isChecked()) {
                id = "relay3";
            } else if (radioServo.isChecked()) {
                id = "servo";
            }

            for (Device device : deviceList) {
                if (device.getId().equals(id) && !device.getId().equals(currentDevice.getId())) {
                    Toast.makeText(getContext(), "Chân thiết bị đã được sử dụng!", Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            if (radioToggle.isChecked()) {
                type = "toggle";
            } else if (radioAdjust.isChecked()) {
                type = "adjust";
            }

            Device updatedDevice = new Device(id, name, type, speed, status);
            deviceRef.child(id).setValue(updatedDevice).addOnSuccessListener(unused -> {
                Toast.makeText(getContext(), "Cập nhật thiết bị thành công!", Toast.LENGTH_SHORT).show();
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.main_container, new HomeFragment()).commit();
            });
        });
    }

    private void populateDeviceData() {
        edtNameDevice.setText(currentDevice.getName());

        switch (currentDevice.getId()) {
            case "relay1":
                radioRelay1.setChecked(true);
                break;
            case "relay2":
                radioRelay2.setChecked(true);
                break;
            case "relay3":
                radioRelay3.setChecked(true);
                break;
            case "servo":
                radioServo.setChecked(true);
                break;
        }

        switch (currentDevice.getType()) {
            case "toggle":
                radioToggle.setChecked(true);
                break;
            case "adjust":
                radioAdjust.setChecked(true);
                break;
        }
    }
}

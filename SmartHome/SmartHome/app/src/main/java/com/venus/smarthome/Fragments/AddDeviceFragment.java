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

public class AddDeviceFragment extends Fragment {
    private List<Device> deviceList;
    private FirebaseAuth auth;
    private DatabaseReference userRef, deviceRef;
    private String uid;
    private ImageView imgButtonBack;
    private EditText edtNameDevice;
    private RadioButton radioRelay1, radioRelay2, radioRelay3, radioServo, radioToggle, radioAdjust;
    private Button btnAddDevice;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_add_device, container, false);
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
        btnAddDevice = view.findViewById(R.id.btnAddDevice);

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

        btnAddDevice.setOnClickListener(v -> {
            String id = null, type = null;
            String name = edtNameDevice.getText().toString().trim();
            int speed = 0, status = 0;

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
                if (device.getId().equals(id)) {
                    Toast.makeText(getContext(), "Chân thiết bị đã được sử dụng!", Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            if (radioToggle.isChecked()) {
                type = "toggle";
            } else if (radioAdjust.isChecked()) {
                type = "adjust";
            }

            Device device = new Device(id, name, type, speed, status);
            deviceRef.child(id).setValue(device).addOnSuccessListener(unused -> {
                Toast.makeText(getContext(), "Thêm thiết bị thành công!", Toast.LENGTH_SHORT).show();
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.main_container, new HomeFragment()).commit();
            });
        });
    }
}

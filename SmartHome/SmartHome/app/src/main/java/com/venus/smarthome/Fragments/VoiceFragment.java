package com.venus.smarthome.Fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
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
import java.util.Locale;
import java.util.Objects;

public class VoiceFragment extends Fragment {
    private FirebaseAuth auth;
    private DatabaseReference dbRef, refUser, refDevice;
    private String uid;
    private List<Device> list;
    private TextView text;
    private ImageView imgVoice;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_voice, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        auth = FirebaseAuth.getInstance();
        uid = Objects.requireNonNull(auth.getCurrentUser()).getUid();
        dbRef = FirebaseDatabase.getInstance().getReference("users");
        refUser = dbRef.child(uid);
        refDevice = refUser.child("devices");

        list = new ArrayList<>();

        text = view.findViewById(R.id.text);
        imgVoice = view.findViewById(R.id.imgVoice);

        ActivityResultLauncher<Intent> startActivityForResult = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == Activity.RESULT_OK) {
                Intent data = result.getData();
                if (data != null) {
                    ArrayList<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    String dataVoice = Objects.requireNonNull(results).get(0).toLowerCase();
                    text.setText(dataVoice);
                    processVoiceCommand(dataVoice);
                }
            }
        });

        refDevice.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        Device device = dataSnapshot.getValue(Device.class);
                        list.add(device);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        imgVoice.setOnClickListener(v -> {
            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
            intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Hãy thử nói \"Tắt bóng đèn\"");

            try {
                startActivityForResult.launch(intent);
            } catch (Exception e) {
                Toast.makeText(getContext(), "" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void processVoiceCommand(String command) {
        if (list.size() != 0) {
            String state;
            boolean foundDevice = false;

            if (command.contains("bật")) {
                state = "bật";
            } else if (command.contains("tắt")) {
                state = "tắt";
            } else if (command.contains("mở")) {
                state = "mở";
            } else if (command.contains("đóng")) {
                state = "đóng";
            } else {
                Toast.makeText(requireContext(), "Không hiểu bạn nói gì", Toast.LENGTH_LONG).show();
                return;
            }

            if (command.contains("tất cả")) {
                for (Device device : list) {
                    String key = device.getId();
                    if (state.equals("bật") || state.equals("mở"))
                        refDevice.child(key).child("status").setValue(1);
                    else if (state.equals("tắt") || state.equals("đóng"))
                        refDevice.child(key).child("status").setValue(0);
                }
                Toast.makeText(requireContext(), "Đã " + state + " tất cả thiết bị!", Toast.LENGTH_LONG).show();
                return;
            }

            for (Device device : list) {
                String name = device.getName().toLowerCase();
                String key = device.getId();

                if (command.contains(name)) {
                    if (state.equals("bật") || state.equals("mở"))
                        refDevice.child(key).child("status").setValue(1);
                    else if (state.equals("tắt") || state.equals("đóng"))
                        refDevice.child(key).child("status").setValue(0);
                    Toast.makeText(requireContext(), "Đã " + state + " " + name + " thành công!", Toast.LENGTH_LONG).show();
                    foundDevice = true;
                }
            }

            if (!foundDevice)
                Toast.makeText(requireContext(), "Không tìm thấy thiết bị!", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(requireContext(), "Không có thiết bị nào!", Toast.LENGTH_LONG).show();
        }
    }
}

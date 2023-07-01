package com.venus.smarthome.Fragments;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.venus.smarthome.R;
import com.venus.smarthome.Utils.LoadingDialog;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ChangeEmailFragment extends Fragment {

    private FirebaseAuth auth;
    private DatabaseReference dbRef, refUser;
    private LoadingDialog loadingDialog;
    private ImageView imgButtonBack;
    private EditText edtEmail, edtPassword;
    private Button btnConfirm;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_change_email, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        auth = FirebaseAuth.getInstance();
        dbRef = FirebaseDatabase.getInstance().getReference("users");
        refUser = dbRef.child(auth.getCurrentUser().getUid());
        loadingDialog = new LoadingDialog(getContext());
        imgButtonBack = view.findViewById(R.id.imgButtonBack);
        edtEmail = view.findViewById(R.id.edtEmail);
        edtPassword = view.findViewById(R.id.edtPassword);
        btnConfirm = view.findViewById(R.id.btnConfirm);

        imgButtonBack.setOnClickListener(v -> getParentFragmentManager().beginTransaction()
                .replace(R.id.main_container, new SettingFragment()).commit());

        btnConfirm.setOnClickListener(v -> {
            String email = edtEmail.getText().toString().trim();
            String password = edtPassword.getText().toString().trim();

            if (TextUtils.isEmpty(email)) {
                Toast.makeText(getContext(), "Nhập email!", Toast.LENGTH_SHORT).show();
                return;
            }

            if (TextUtils.isEmpty(password)) {
                Toast.makeText(getContext(), "Nhập mật khẩu!", Toast.LENGTH_SHORT).show();
                return;
            }

            loadingDialog.show();

            AuthCredential credential = EmailAuthProvider.getCredential(auth.getCurrentUser().getEmail(), password);

            auth.getCurrentUser().reauthenticate(credential).addOnCompleteListener(reauthTask -> {
                if (reauthTask.isSuccessful()) {
                    auth.getCurrentUser().updateEmail(email).addOnCompleteListener(updateTask -> {
                        if (updateTask.isSuccessful()) {
                            Map<String, Object> map = new HashMap<>();
                            map.put("email", email);
                            refUser.updateChildren(map);
                            Toast.makeText(requireContext(), "Email đã được cập nhật", Toast.LENGTH_SHORT).show();
                            loadingDialog.dismiss();
                        } else {
                            loadingDialog.dismiss();
                            Toast.makeText(requireContext(), "Email đã tồn tại!", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    loadingDialog.dismiss();
                    Toast.makeText(requireContext(), "Mật khẩu không chính xác!", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }
}

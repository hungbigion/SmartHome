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

public class ChangePasswordFragment extends Fragment {

    private FirebaseAuth auth;
    private DatabaseReference dbRef, refUser;
    private LoadingDialog loadingDialog;
    private ImageView imgButtonBack;
    private EditText edtPasswordOld, edtPasswordNew;
    private Button btnConfirm;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_change_password, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        auth = FirebaseAuth.getInstance();
        dbRef = FirebaseDatabase.getInstance().getReference("users");
        refUser = dbRef.child(auth.getCurrentUser().getUid());
        loadingDialog = new LoadingDialog(getContext());
        imgButtonBack = view.findViewById(R.id.imgButtonBack);
        edtPasswordOld = view.findViewById(R.id.edtPasswordOld);
        edtPasswordNew = view.findViewById(R.id.edtPasswordNew);
        btnConfirm = view.findViewById(R.id.btnConfirm);

        imgButtonBack.setOnClickListener(v -> getParentFragmentManager().beginTransaction()
                .replace(R.id.main_container, new SettingFragment()).commit());

        btnConfirm.setOnClickListener(v -> {
            String passwordOld = edtPasswordOld.getText().toString().trim();
            String passwordNew = edtPasswordNew.getText().toString().trim();

            if (TextUtils.isEmpty(passwordOld)) {
                Toast.makeText(getContext(), "Nhập mật khẩu cũ!", Toast.LENGTH_SHORT).show();
                return;
            }

            if (TextUtils.isEmpty(passwordNew)) {
                Toast.makeText(getContext(), "Nhập mật khẩu mới!", Toast.LENGTH_SHORT).show();
                return;
            }

            if (passwordNew.length() < 6) {
                Toast.makeText(getContext(), "Mật khẩu mới quá ngắn, tối thiểu 6 ký tự", Toast.LENGTH_SHORT).show();
                return;
            }

            loadingDialog.show();

            AuthCredential credential = EmailAuthProvider.getCredential(auth.getCurrentUser().getEmail(), passwordOld);

            auth.getCurrentUser().reauthenticate(credential).addOnCompleteListener(reauthTask -> {
                if (reauthTask.isSuccessful()) {
                    auth.getCurrentUser().updatePassword(passwordNew).addOnCompleteListener(updateTask -> {
                        if (updateTask.isSuccessful()) {
                            Map<String, Object> map = new HashMap<>();
                            map.put("password", passwordNew);
                            refUser.updateChildren(map);
                            Toast.makeText(requireContext(), "Mật khẩu đã được cập nhật", Toast.LENGTH_SHORT).show();
                            loadingDialog.dismiss();
                        } else {
                            loadingDialog.dismiss();
                            Toast.makeText(requireContext(), "Lỗi : " + updateTask.getException(), Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    loadingDialog.dismiss();
                    Toast.makeText(requireContext(), "Mật khẩu cũ không chính xác!", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }
}

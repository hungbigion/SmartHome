package com.venus.smarthome;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.venus.smarthome.Modal.User;
import com.venus.smarthome.Utils.LoadingDialog;

import java.util.Objects;

public class SignupActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private DatabaseReference dbRef;
    private LoadingDialog loadingDialog;
    private EditText edtName, edtEmail, edtPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        auth = FirebaseAuth.getInstance();
        dbRef = FirebaseDatabase.getInstance().getReference("users");
        loadingDialog = new LoadingDialog(this);
        edtName = findViewById(R.id.edtName);
        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        ImageView imgButtonRegister = findViewById(R.id.imgButtonRegister);
        TextView txtLogin = findViewById(R.id.txtLogin);
        TextView txtForgetPassword = findViewById(R.id.txtForgetPassword);

        txtLogin.setOnClickListener(view -> {
            Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
            startActivity(intent);
        });

        txtForgetPassword.setOnClickListener(view -> {
            Intent intent = new Intent(SignupActivity.this, ForgetPasswordActivity.class);
            startActivity(intent);
        });

        imgButtonRegister.setOnClickListener(view -> {
            String name = edtName.getText().toString().trim();
            String email = edtEmail.getText().toString().trim();
            String password = edtPassword.getText().toString().trim();

            if (TextUtils.isEmpty(name)) {
                Toast.makeText(getApplicationContext(), "Nhập tên!", Toast.LENGTH_SHORT).show();
                return;
            }

            if (TextUtils.isEmpty(email)) {
                Toast.makeText(getApplicationContext(), "Nhập địa chỉ email!", Toast.LENGTH_SHORT).show();
                return;
            }

            if (TextUtils.isEmpty(password)) {
                Toast.makeText(getApplicationContext(), "Nhập mật khẩu!", Toast.LENGTH_SHORT).show();
                return;
            }

            if (password.length() < 6) {
                Toast.makeText(getApplicationContext(), "Mật khẩu quá ngắn, tối thiểu 6 ký tự", Toast.LENGTH_SHORT).show();
                return;
            }

            loadingDialog.show();

            auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(SignupActivity.this, task -> {
                        if (task.isSuccessful()) {
                            User user = new User(name, email, "new account", 0L);
                            dbRef.child(Objects.requireNonNull(auth.getCurrentUser()).getUid()).setValue(user).addOnCompleteListener(task1 -> {
                                if (task1.isSuccessful()) {
                                    loadingDialog.dismiss();
                                    Toast.makeText(this, "Đăng ký thành công!", Toast.LENGTH_LONG).show();
                                    startActivity(new Intent(SignupActivity.this, MainActivity.class));
                                    finish();
                                } else {
                                    loadingDialog.dismiss();
                                    Toast.makeText(SignupActivity.this, "Đăng ký thất bại!",
                                            Toast.LENGTH_SHORT).show();
                                }
                            });
                        } else {
                            loadingDialog.dismiss();
                            Toast.makeText(SignupActivity.this, "Đăng ký thất bại!",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
        });
    }
}
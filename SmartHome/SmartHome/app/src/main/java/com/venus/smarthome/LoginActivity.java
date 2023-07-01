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
import com.venus.smarthome.Utils.LoadingDialog;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private LoadingDialog loadingDialog;
    private EditText edtEmail, edtPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        auth = FirebaseAuth.getInstance();
        loadingDialog = new LoadingDialog(this);
        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        ImageView imgButtonLogin = findViewById(R.id.imgButtonLogin);
        TextView txtRegister = findViewById(R.id.txtRegister);
        TextView txtForgetPassword = findViewById(R.id.txtForgetPassword);

        txtRegister.setOnClickListener(view -> {
            startActivity(new Intent(LoginActivity.this, SignupActivity.class));
        });

        txtForgetPassword.setOnClickListener(view -> {
            startActivity(new Intent(LoginActivity.this, ForgetPasswordActivity.class));
        });

        imgButtonLogin.setOnClickListener(view -> {
            String email = edtEmail.getText().toString().trim();
            String password = edtPassword.getText().toString().trim();

            if (TextUtils.isEmpty(email)) {
                Toast.makeText(getApplicationContext(), "Nhập địa chỉ email!", Toast.LENGTH_SHORT).show();
                return;
            }

            if (TextUtils.isEmpty(password)) {
                Toast.makeText(getApplicationContext(), "Nhập mật khẩu!", Toast.LENGTH_SHORT).show();
                return;
            }

            loadingDialog.show();

            auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        loadingDialog.dismiss();
                        if (task.isSuccessful()) {
                            Toast.makeText(this, "Đăng nhập thành công!", Toast.LENGTH_LONG).show();
                            startActivity(new Intent(LoginActivity.this, MainActivity.class)
                                    .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK));
                            finish();
                        } else {
                            Toast.makeText(LoginActivity.this, "Tài khoản hoặc mật khẩu không chính xác!", Toast.LENGTH_SHORT).show();
                        }
                    });
        });
    }
}

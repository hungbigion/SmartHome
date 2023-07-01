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

public class ForgetPasswordActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private LoadingDialog loadingDialog;
    private EditText edtEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forget_password);

        auth = FirebaseAuth.getInstance();
        loadingDialog = new LoadingDialog(this);
        edtEmail = findViewById(R.id.edtEmail);
        ImageView imgButtonForgetPassword = findViewById(R.id.imgButtonForgetPassword);
        TextView txtLogin = findViewById(R.id.txtLogin);
        TextView txtRegister = findViewById(R.id.txtRegister);

        txtLogin.setOnClickListener(view -> {
            startActivity(new Intent(ForgetPasswordActivity.this, LoginActivity.class));
        });

        txtRegister.setOnClickListener(view -> {
            startActivity(new Intent(ForgetPasswordActivity.this, SignupActivity.class));
        });

        imgButtonForgetPassword.setOnClickListener(view -> {
            String email = edtEmail.getText().toString().trim();

            if (TextUtils.isEmpty(email)) {
                Toast.makeText(getApplicationContext(), "Nhập địa chỉ email!", Toast.LENGTH_SHORT).show();
                return;
            }

            loadingDialog.show();

            auth.sendPasswordResetEmail(email)
                    .addOnCompleteListener(task -> {
                        loadingDialog.dismiss();
                        if (task.isSuccessful()) {
                            Toast.makeText(this, "Mật khẩu đã được gửi vào email của bạn (Kiểm tra thư rác)!", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(ForgetPasswordActivity.this, "Thất bại email không tồn tại!", Toast.LENGTH_SHORT).show();
                        }
                    });
        });
    }
}

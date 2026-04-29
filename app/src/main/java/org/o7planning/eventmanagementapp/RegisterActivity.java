package org.o7planning.eventmanagementapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class RegisterActivity extends AppCompatActivity {

    private TextInputEditText etRegName, etRegPhone, etRegPass;
    private MaterialButton btnRegister;
    private TextView tvBackToLogin;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        initViews();
        sharedPreferences = getSharedPreferences("UserProfile", Context.MODE_PRIVATE);
        setupListeners();
    }

    private void initViews() {
        etRegName = findViewById(R.id.etRegName);
        etRegPhone = findViewById(R.id.etRegPhone);
        etRegPass = findViewById(R.id.etRegPass);
        btnRegister = findViewById(R.id.btnRegister);
        tvBackToLogin = findViewById(R.id.tvBackToLogin);
    }

    private void setupListeners() {
        btnRegister.setOnClickListener(v -> handleRegistration());
        tvBackToLogin.setOnClickListener(v -> finish());
    }

    private void handleRegistration() {
        String name = etRegName.getText().toString().trim();
        String phone = etRegPhone.getText().toString().trim();
        String pass = etRegPass.getText().toString().trim();

        if (name.isEmpty() || phone.isEmpty() || pass.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        sharedPreferences.edit()
                .putString("name", name)
                .putString("phone", phone)
                .putString("password", pass)
                .apply();

        // Gửi thông báo hệ thống khi đăng ký thành công
        NotificationHelper.sendNotification(this, "Chào mừng bạn!", 
                "Tài khoản " + name + " đã được đăng ký thành công.");

        Toast.makeText(this, "Đăng ký thành công!", Toast.LENGTH_SHORT).show();
        finish();
    }
}
